package br.ufg.vacinacao.servlet;

import br.ufg.vacinacao.dao.AgendaDAO;
import br.ufg.vacinacao.dao.UsuarioDAO;
import br.ufg.vacinacao.dao.VacinaDAO;
import br.ufg.vacinacao.model.Agenda;
import br.ufg.vacinacao.model.Situacao;
import br.ufg.vacinacao.model.Vacina;
import br.ufg.vacinacao.service.AgendamentoService;
import br.ufg.vacinacao.service.ContraindicacaoException;
import br.ufg.vacinacao.service.ContraindicacaoService;
import br.ufg.vacinacao.service.RegraNegocioException;
import br.ufg.vacinacao.util.DatasUtil;
import br.ufg.vacinacao.util.Mensagens;
import br.ufg.vacinacao.util.Parametros;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/** Agendas: listagem, agendamento (com geração automática das doses e prévia), detalhe e exclusão. */
@WebServlet({"/agendas", "/agendas/hoje", "/agendas/novo", "/agendas/previa", "/agendas/contraindicacoes",
        "/agendas/detalhe", "/agendas/baixa", "/agendas/excluir"})
public class AgendaServlet extends HttpServlet {

    private static final String HORA_PADRAO = "08:00";
    /** Destinos aceitos no parâmetro "voltar" (caminhos internos da aplicação). */
    private static final Pattern VOLTAR_VALIDO = Pattern.compile("^/[a-z/]+(\\?[\\w=&%.\\-+]*)?$");

    private final AgendaDAO dao = new AgendaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final VacinaDAO vacinaDAO = new VacinaDAO();
    private final AgendamentoService agendamentoService = new AgendamentoService();
    private final ContraindicacaoService contraindicacaoService = new ContraindicacaoService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("menuAtivo", "agendas");
        switch (req.getServletPath()) {
            case "/agendas" -> listar(req, resp);
            case "/agendas/hoje" -> listarHoje(req, resp);
            case "/agendas/novo" -> exibirFormulario(req, resp, Parametros.inteiro(req, "usuario"),
                    Parametros.inteiro(req, "vacina"), LocalDate.now().toString(), HORA_PADRAO, null, List.of());
            case "/agendas/previa" -> previa(req, resp);
            case "/agendas/contraindicacoes" -> contraindicacoes(req, resp);
            case "/agendas/detalhe" -> detalhar(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("menuAtivo", "agendas");
        switch (req.getServletPath()) {
            case "/agendas/novo" -> agendar(req, resp);
            case "/agendas/baixa" -> darBaixa(req, resp);
            case "/agendas/excluir" -> excluir(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    /** Listagem com filtros (Item 3): situação, período, usuário e vacina. */
    private void listar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Situacao situacao = lerSituacao(Parametros.texto(req, "situacao"));
        LocalDate de = lerData(Parametros.texto(req, "de"));
        LocalDate ate = lerData(Parametros.texto(req, "ate"));
        Integer usuarioId = Parametros.inteiro(req, "usuario");
        Integer vacinaId = Parametros.inteiro(req, "vacina");

        if (de != null && ate != null && de.isAfter(ate)) {
            req.setAttribute("erro", "Período inválido: a data inicial é posterior à final. O período foi ignorado.");
            de = null;
            ate = null;
        }

        boolean filtrando = situacao != null || de != null || ate != null || usuarioId != null || vacinaId != null;
        req.setAttribute("titulo", situacao == null ? "Agendas" : "Agendas - " + situacao.getRotulo());
        req.setAttribute("agendas", dao.filtrar(situacao, de, ate, usuarioId, vacinaId));
        req.setAttribute("filtroSituacao", situacao == null ? "" : situacao.name());
        req.setAttribute("filtroDe", de == null ? "" : de.toString());
        req.setAttribute("filtroAte", ate == null ? "" : ate.toString());
        req.setAttribute("filtroUsuario", usuarioId);
        req.setAttribute("filtroVacina", vacinaId);
        req.setAttribute("filtrando", filtrando);
        req.setAttribute("situacoes", Situacao.values());
        req.setAttribute("usuarios", usuarioDAO.listarTodos());
        req.setAttribute("vacinas", vacinaDAO.listarTodos());
        req.setAttribute("urlAtual", urlAtual(req));
        req.getRequestDispatcher("/WEB-INF/jsp/agendas/lista.jsp").forward(req, resp);
    }

    /** Agendas de hoje (Item 4), ordenadas Agendado, Realizado, Cancelado e por hora. */
    private void listarHoje(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate hoje = LocalDate.now();
        List<Agenda> agendas = dao.listarDoDia(hoje);
        Map<Situacao, Long> totais = new EnumMap<>(Situacao.class);
        for (Situacao s : Situacao.values()) {
            totais.put(s, agendas.stream().filter(a -> a.getSituacao() == s).count());
        }
        req.setAttribute("menuAtivo", "hoje");
        req.setAttribute("titulo", "Agendas de hoje");
        req.setAttribute("hoje", DatasUtil.formatar(hoje));
        req.setAttribute("diaSemana", DatasUtil.diaDaSemana(hoje));
        req.setAttribute("agendas", agendas);
        req.setAttribute("totais", totais);
        req.setAttribute("urlAtual", urlAtual(req));
        req.getRequestDispatcher("/WEB-INF/jsp/agendas/hoje.jsp").forward(req, resp);
    }

    /** POST /agendas/baixa (Item 5): id, acao (REALIZADO/CANCELADO), motivo, voltar. */
    private void darBaixa(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer id = Parametros.inteiro(req, "id");
        try {
            Agenda agenda = agendamentoService.darBaixa(id, Parametros.texto(req, "acao"),
                    Parametros.texto(req, "motivo"));
            Mensagens.sucesso(req, agenda.getVacina().getTitulo() + " (dose " + agenda.getDose() + ") de "
                    + agenda.getUsuario().getNome() + " marcada como "
                    + agenda.getSituacao().getRotuloFeminino() + " em " + agenda.getDataSituacaoFormatada() + ".");
        } catch (RegraNegocioException e) {
            Mensagens.erro(req, String.join(" ", e.getErros()));
        } catch (PersistenceException e) {
            Mensagens.erro(req, "Não foi possível registrar a baixa. Tente novamente.");
        }
        String voltar = req.getParameter("voltar");
        if (voltar == null || !VOLTAR_VALIDO.matcher(voltar).matches()) {
            voltar = id == null ? "/agendas" : "/agendas/detalhe?id=" + id;
        }
        resp.sendRedirect(req.getContextPath() + voltar);
    }

    private void detalhar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Agenda agenda = dao.buscarCompleta(Parametros.inteiro(req, "id"));
        if (agenda == null) {
            Mensagens.erro(req, "Agenda não encontrada.");
            resp.sendRedirect(req.getContextPath() + "/agendas");
            return;
        }
        List<Agenda> serie = dao.listarSerie(agenda.getUsuario().getId(), agenda.getVacina().getId());
        long dosesSeguintesAgendadas = serie.stream()
                .filter(a -> a.getSituacao() == Situacao.AGENDADO && a.getDose() > agenda.getDose())
                .count();
        req.setAttribute("titulo", "Agenda");
        req.setAttribute("agenda", agenda);
        req.setAttribute("serie", serie);
        req.setAttribute("dosesSeguintesAgendadas", dosesSeguintesAgendadas);
        req.setAttribute("urlAtual", urlAtual(req));
        req.getRequestDispatcher("/WEB-INF/jsp/agendas/detalhe.jsp").forward(req, resp);
    }

    private void exibirFormulario(HttpServletRequest req, HttpServletResponse resp, Integer usuarioId,
                                  Integer vacinaId, String data, String hora, String observacoes,
                                  List<String> erros) throws ServletException, IOException {
        req.setAttribute("titulo", "Novo agendamento");
        req.setAttribute("usuarios", usuarioDAO.listarTodos());
        req.setAttribute("vacinas", vacinaDAO.listarTodos());
        req.setAttribute("usuarioId", usuarioId);
        req.setAttribute("vacinaId", vacinaId);
        req.setAttribute("data", data);
        req.setAttribute("hora", hora);
        req.setAttribute("observacoes", observacoes);
        req.setAttribute("hojeIso", LocalDate.now().toString());
        req.setAttribute("erros", erros);
        req.getRequestDispatcher("/WEB-INF/jsp/agendas/form.jsp").forward(req, resp);
    }

    private void agendar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Integer usuarioId = Parametros.inteiro(req, "usuario");
        Integer vacinaId = Parametros.inteiro(req, "vacina");
        String dataTexto = Parametros.texto(req, "data");
        String hora = Parametros.texto(req, "hora");
        String observacoes = Parametros.texto(req, "observacoes");
        boolean ciente = "1".equals(req.getParameter("ciente"));
        req.setAttribute("ciente", ciente);

        LocalDate data = lerData(dataTexto);
        if (dataTexto != null && data == null) {
            exibirFormulario(req, resp, usuarioId, vacinaId, dataTexto, hora, observacoes,
                    List.of("Data da primeira dose inválida."));
            return;
        }

        List<Agenda> criadas;
        try {
            criadas = agendamentoService.agendar(usuarioId, vacinaId, data, hora, observacoes, ciente);
        } catch (ContraindicacaoException e) {
            // 7A: volta ao formulário com o alerta vermelho e a caixa "Estou ciente"
            req.setAttribute("conflitos", e.getAlergias());
            exibirFormulario(req, resp, usuarioId, vacinaId, dataTexto, hora, observacoes, List.of());
            return;
        } catch (RegraNegocioException e) {
            exibirFormulario(req, resp, usuarioId, vacinaId, dataTexto, hora, observacoes, e.getErros());
            return;
        } catch (PersistenceException e) {
            exibirFormulario(req, resp, usuarioId, vacinaId, dataTexto, hora, observacoes,
                    List.of("Não foi possível gravar o agendamento. Nenhuma dose foi salva; tente novamente."));
            return;
        }

        String datas = criadas.stream()
                .map(a -> "dose " + a.getDose() + " em " + a.getDataFormatada())
                .collect(Collectors.joining("; "));
        Mensagens.sucesso(req, criadas.size() + (criadas.size() == 1 ? " agenda criada" : " agendas criadas")
                + " às " + hora + ": " + datas + ".");
        resp.sendRedirect(req.getContextPath() + "/agendas/detalhe?id=" + criadas.get(0).getId());
    }

    /**
     * GET /agendas/previa?vacina=&data=&usuario= : datas que serão geradas, em JSON.
     * Não grava nada; usado pelo formulário via fetch.
     */
    private void previa(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Vacina vacina = vacinaDAO.buscarPorId(Parametros.inteiro(req, "vacina"));
        LocalDate data = lerData(Parametros.texto(req, "data"));

        StringBuilder json = new StringBuilder("{");
        if (vacina == null || data == null) {
            json.append("\"ok\":false,\"erro\":")
                    .append(texto(vacina == null ? "Selecione a vacina." : "Informe a data da primeira dose."));
        } else {
            json.append("\"ok\":true,\"vacina\":").append(texto(vacina.getTitulo()))
                    .append(",\"esquema\":").append(texto(vacina.getEsquema()))
                    .append(",\"doses\":[");
            List<LocalDate> datas = AgendamentoService.calcularDatas(vacina, data);
            for (int i = 0; i < datas.size(); i++) {
                if (i > 0) {
                    json.append(',');
                }
                json.append("{\"dose\":").append(i + 1)
                        .append(",\"data\":").append(texto(DatasUtil.formatar(datas.get(i))))
                        .append(",\"diaSemana\":").append(texto(DatasUtil.diaDaSemana(datas.get(i))))
                        .append('}');
            }
            json.append(']');

            if (data.isBefore(LocalDate.now())) {
                json.append(",\"aviso\":").append(texto("A data da primeira dose está no passado."));
            } else {
                Integer usuarioId = Parametros.inteiro(req, "usuario");
                if (usuarioId != null && dao.existeSerieAberta(usuarioId, vacina.getId())) {
                    json.append(",\"aviso\":").append(texto(
                            "Este usuário já tem doses desta vacina agendadas; o agendamento será recusado."));
                }
            }
        }
        json.append('}');
        resp.getWriter().write(json.toString());
    }

    /**
     * GET /agendas/contraindicacoes?usuario= (7A): vacinas contraindicadas ao usuário, em JSON.
     * Formato: {"ok":true,"vacinas":{"<idVacina>":["Alergia", ...], ...}}
     */
    private void contraindicacoes(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        Map<Integer, List<String>> mapa = contraindicacaoService.vacinasContraindicadas(
                Parametros.inteiro(req, "usuario"));
        StringBuilder json = new StringBuilder("{\"ok\":true,\"vacinas\":{");
        boolean primeira = true;
        for (Map.Entry<Integer, List<String>> item : mapa.entrySet()) {
            if (!primeira) {
                json.append(',');
            }
            primeira = false;
            json.append(texto(String.valueOf(item.getKey()))).append(":[")
                    .append(item.getValue().stream().map(AgendaServlet::texto).collect(Collectors.joining(",")))
                    .append(']');
        }
        json.append("}}");
        resp.getWriter().write(json.toString());
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Agenda agenda = dao.buscarCompleta(Parametros.inteiro(req, "id"));
        if (agenda == null) {
            Mensagens.erro(req, "Agenda não encontrada.");
            resp.sendRedirect(req.getContextPath() + "/agendas");
            return;
        }
        boolean seguintes = "seguintes".equals(req.getParameter("escopo"));
        String descricao = agenda.getVacina().getTitulo() + " (dose " + agenda.getDose() + ") de "
                + agenda.getUsuario().getNome();
        try {
            if (seguintes && agenda.getSituacao() == Situacao.AGENDADO) {
                int excluidas = dao.excluirDaDoseEmDiante(agenda);
                Mensagens.sucesso(req, excluidas + (excluidas == 1 ? " agenda excluída" : " agendas excluídas")
                        + ": " + descricao + " e as doses seguintes ainda agendadas.");
            } else {
                dao.excluir(agenda.getId());
                Mensagens.sucesso(req, "Agenda excluída: " + descricao + ", " + agenda.getDataFormatada() + ".");
            }
        } catch (PersistenceException e) {
            Mensagens.erro(req, "Não foi possível excluir a agenda.");
        }
        resp.sendRedirect(req.getContextPath() + "/agendas");
    }

    private static Situacao lerSituacao(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return Situacao.valueOf(texto.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** Caminho da requisição atual (sem o contexto), usado como destino após a baixa. */
    static String urlAtual(HttpServletRequest req) {
        String query = req.getQueryString();
        return req.getServletPath() + (query == null ? "" : "?" + query);
    }

    private static LocalDate lerData(String texto) {
        if (texto == null) {
            return null;
        }
        try {
            return LocalDate.parse(texto);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** String JSON com escape dos caracteres especiais. */
    private static String texto(String valor) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : valor.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '<' -> sb.append("\\u003c");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}
