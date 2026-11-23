package br.ufg.vacinacao.servlet;

import br.ufg.vacinacao.dao.AlergiaDAO;
import br.ufg.vacinacao.dao.VacinaDAO;
import br.ufg.vacinacao.model.Periodicidade;
import br.ufg.vacinacao.model.Vacina;
import br.ufg.vacinacao.util.Mensagens;
import br.ufg.vacinacao.util.Parametros;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** CRUD de vacinas: listagem, inclusão, alteração, detalhe e exclusão. */
@WebServlet({"/vacinas", "/vacinas/novo", "/vacinas/editar", "/vacinas/detalhe", "/vacinas/excluir"})
public class VacinaServlet extends HttpServlet {

    private static final int TAMANHO_TITULO = 60;
    private static final int TAMANHO_DESCRICAO = 200;

    private final VacinaDAO dao = new VacinaDAO();
    private final AlergiaDAO alergiaDAO = new AlergiaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("menuAtivo", "vacinas");
        switch (req.getServletPath()) {
            case "/vacinas" -> listar(req, resp);
            case "/vacinas/novo" -> exibirFormulario(req, resp, new Vacina(), Set.of(), false, List.of());
            case "/vacinas/editar" -> editar(req, resp);
            case "/vacinas/detalhe" -> detalhar(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("menuAtivo", "vacinas");
        switch (req.getServletPath()) {
            case "/vacinas/novo", "/vacinas/editar" -> salvar(req, resp);
            case "/vacinas/excluir" -> excluir(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("titulo", "Vacinas");
        req.setAttribute("vacinas", dao.listarComContraindicacoes());
        req.getRequestDispatcher("/WEB-INF/jsp/vacinas/lista.jsp").forward(req, resp);
    }

    private void detalhar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Vacina vacina = dao.buscarComContraindicacoes(Parametros.inteiro(req, "id"));
        if (vacina == null) {
            Mensagens.erro(req, "Vacina não encontrada.");
            resp.sendRedirect(req.getContextPath() + "/vacinas");
            return;
        }
        req.setAttribute("titulo", vacina.getTitulo());
        req.setAttribute("vacina", vacina);
        req.setAttribute("totalAgendas", dao.contarAgendas(vacina.getId()));
        req.getRequestDispatcher("/WEB-INF/jsp/vacinas/detalhe.jsp").forward(req, resp);
    }

    private void editar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Vacina vacina = dao.buscarComContraindicacoes(Parametros.inteiro(req, "id"));
        if (vacina == null) {
            Mensagens.erro(req, "Vacina não encontrada.");
            resp.sendRedirect(req.getContextPath() + "/vacinas");
            return;
        }
        Set<Integer> selecionadas = new LinkedHashSet<>();
        vacina.getContraindicacoes().forEach(a -> selecionadas.add(a.getId()));
        exibirFormulario(req, resp, vacina, selecionadas, dao.contarAgendas(vacina.getId()) > 0, List.of());
    }

    /**
     * @param esquemaBloqueado doses, periodicidade e intervalo não podem mudar quando já há agendas
     *                         (as séries existentes foram geradas com o esquema atual)
     */
    private void exibirFormulario(HttpServletRequest req, HttpServletResponse resp, Vacina vacina,
                                  Set<Integer> contraindicacoesSelecionadas, boolean esquemaBloqueado,
                                  List<String> erros) throws ServletException, IOException {
        req.setAttribute("titulo", vacina.getId() == null ? "Nova vacina" : "Editar vacina");
        req.setAttribute("vacina", vacina);
        req.setAttribute("esquemaBloqueado", esquemaBloqueado);
        req.setAttribute("periodicidades", Periodicidade.values());
        req.setAttribute("alergias", alergiaDAO.listarTodos());
        req.setAttribute("contraindicacoesSelecionadas", contraindicacoesSelecionadas);
        req.setAttribute("erros", erros);
        req.getRequestDispatcher("/WEB-INF/jsp/vacinas/form.jsp").forward(req, resp);
    }

    /** Inclusão (POST /vacinas/novo) ou alteração (POST /vacinas/editar com id). */
    private void salvar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean edicao = "/vacinas/editar".equals(req.getServletPath());
        Vacina existente = null;
        boolean esquemaBloqueado = false;
        if (edicao) {
            existente = dao.buscarPorId(Parametros.inteiro(req, "id"));
            if (existente == null) {
                Mensagens.erro(req, "Vacina não encontrada.");
                resp.sendRedirect(req.getContextPath() + "/vacinas");
                return;
            }
            esquemaBloqueado = dao.contarAgendas(existente.getId()) > 0;
        }

        List<String> erros = new ArrayList<>();
        Vacina vacina = lerFormulario(req, erros, esquemaBloqueado ? existente : null);
        if (existente != null) {
            vacina.setId(existente.getId());
        }
        Set<Integer> contraindicacoes = new LinkedHashSet<>();
        String[] valores = req.getParameterValues("contraindicacoes");
        if (valores != null) {
            for (String valor : valores) {
                try {
                    contraindicacoes.add(Integer.valueOf(valor.trim()));
                } catch (NumberFormatException e) {
                    erros.add("Seleção de contraindicações inválida.");
                    break;
                }
            }
        }
        if (!erros.isEmpty()) {
            exibirFormulario(req, resp, vacina, contraindicacoes, esquemaBloqueado, erros);
            return;
        }
        try {
            dao.salvar(vacina, contraindicacoes);
        } catch (IllegalArgumentException e) {
            exibirFormulario(req, resp, vacina, contraindicacoes, esquemaBloqueado,
                    List.of("Uma das alergias selecionadas não existe mais. Recarregue a página e tente de novo."));
            return;
        } catch (PersistenceException e) {
            exibirFormulario(req, resp, vacina, contraindicacoes, esquemaBloqueado,
                    List.of("Não foi possível salvar a vacina. Confira os dados informados."));
            return;
        }
        Mensagens.sucesso(req, "Vacina \"" + vacina.getTitulo() + "\" " + (edicao ? "alterada." : "cadastrada."));
        resp.sendRedirect(req.getContextPath() + "/vacinas/detalhe?id=" + vacina.getId());
    }

    /**
     * Monta a vacina com os dados do formulário, acumulando as mensagens de validação em {@code erros}.
     *
     * @param esquemaAtual se informado, doses/periodicidade/intervalo são copiados dela e os campos do
     *                     formulário são ignorados (vacina com agendas)
     */
    private Vacina lerFormulario(HttpServletRequest req, List<String> erros, Vacina esquemaAtual) {
        Vacina vacina = new Vacina();
        vacina.setTitulo(Parametros.texto(req, "titulo"));
        vacina.setDescricao(Parametros.texto(req, "descricao"));

        if (vacina.getTitulo() == null) {
            erros.add("Informe o título da vacina.");
        } else if (vacina.getTitulo().length() > TAMANHO_TITULO) {
            erros.add("O título deve ter no máximo " + TAMANHO_TITULO + " caracteres.");
        }
        if (vacina.getDescricao() != null && vacina.getDescricao().length() > TAMANHO_DESCRICAO) {
            erros.add("A descrição deve ter no máximo " + TAMANHO_DESCRICAO + " caracteres.");
        }

        if (esquemaAtual != null) {
            vacina.setDoses(esquemaAtual.getDoses());
            vacina.setPeriodicidade(esquemaAtual.getPeriodicidade());
            vacina.setIntervalo(esquemaAtual.getIntervalo());
            return vacina;
        }

        Integer doses = Parametros.inteiro(req, "doses");
        if (doses == null || doses < 1) {
            erros.add("Informe a quantidade de doses (número inteiro maior ou igual a 1).");
            return vacina;
        }
        vacina.setDoses(doses);

        if (doses == 1) {
            // Dose única: periodicidade e intervalo não se aplicam
            vacina.setPeriodicidade(null);
            vacina.setIntervalo(null);
            return vacina;
        }

        Integer codigoPeriodicidade = Parametros.inteiro(req, "periodicidade");
        try {
            vacina.setPeriodicidade(Periodicidade.deCodigo(codigoPeriodicidade));
        } catch (IllegalArgumentException e) {
            vacina.setPeriodicidade(null);
        }
        if (vacina.getPeriodicidade() == null) {
            erros.add("Para mais de uma dose, escolha a periodicidade (dias, semanas, meses ou anos).");
        }

        Integer intervalo = Parametros.inteiro(req, "intervalo");
        vacina.setIntervalo(intervalo);
        if (intervalo == null || intervalo < 1) {
            erros.add("Para mais de uma dose, informe o intervalo entre as doses (número inteiro maior ou igual a 1).");
        }
        return vacina;
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Vacina vacina = dao.buscarPorId(Parametros.inteiro(req, "id"));
        if (vacina == null) {
            Mensagens.erro(req, "Vacina não encontrada.");
            resp.sendRedirect(req.getContextPath() + "/vacinas");
            return;
        }

        long agendas = dao.contarAgendas(vacina.getId());
        if (agendas > 0) {
            Mensagens.erro(req, "Não é possível excluir \"" + vacina.getTitulo() + "\": ela está em "
                    + agendas + (agendas == 1 ? " agenda." : " agendas."));
            resp.sendRedirect(req.getContextPath() + "/vacinas/detalhe?id=" + vacina.getId());
            return;
        }

        try {
            dao.excluir(vacina.getId());
            Mensagens.sucesso(req, "Vacina \"" + vacina.getTitulo() + "\" excluída.");
        } catch (PersistenceException e) {
            Mensagens.erro(req, "Não é possível excluir \"" + vacina.getTitulo()
                    + "\": ela está em uso em outros cadastros.");
        }
        resp.sendRedirect(req.getContextPath() + "/vacinas");
    }
}
