package br.ufg.vacinacao.servlet;

import br.ufg.vacinacao.dao.AlergiaDAO;
import br.ufg.vacinacao.dao.UsuarioDAO;
import br.ufg.vacinacao.model.Sexo;
import br.ufg.vacinacao.model.UF;
import br.ufg.vacinacao.model.Usuario;
import br.ufg.vacinacao.service.DoseService;
import br.ufg.vacinacao.service.SerieDoses;
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
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** CRUD de usuários: listagem (com busca por nome), inclusão, detalhe e exclusão. */
@WebServlet({"/usuarios", "/usuarios/novo", "/usuarios/editar", "/usuarios/detalhe", "/usuarios/agendas",
        "/usuarios/excluir"})
public class UsuarioServlet extends HttpServlet {

    private static final int TAMANHO_NOME = 60;
    private static final int TAMANHO_LOGRADOURO = 60;
    private static final int TAMANHO_SETOR = 40;
    private static final int TAMANHO_CIDADE = 40;
    private static final long NUMERO_MAXIMO = 9_999_999_999L; // NUMERIC(10)
    private static final LocalDate NASCIMENTO_MINIMO = LocalDate.of(1900, 1, 1);

    private final UsuarioDAO dao = new UsuarioDAO();
    private final AlergiaDAO alergiaDAO = new AlergiaDAO();
    private final DoseService doseService = new DoseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("menuAtivo", "usuarios");
        switch (req.getServletPath()) {
            case "/usuarios" -> listar(req, resp);
            case "/usuarios/novo" -> exibirFormulario(req, resp, new Usuario(), Set.of(), List.of());
            case "/usuarios/editar" -> {
                Usuario usuario = dao.buscarComAlergias(Parametros.inteiro(req, "id"));
                if (usuario == null) {
                    Mensagens.erro(req, "Usuário não encontrado.");
                    resp.sendRedirect(req.getContextPath() + "/usuarios");
                } else {
                    Set<Integer> selecionadas = new LinkedHashSet<>();
                    usuario.getAlergias().forEach(a -> selecionadas.add(a.getId()));
                    exibirFormulario(req, resp, usuario, selecionadas, List.of());
                }
            }
            case "/usuarios/detalhe" -> detalhar(req, resp);
            case "/usuarios/agendas" -> cartao(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("menuAtivo", "usuarios");
        switch (req.getServletPath()) {
            case "/usuarios/novo", "/usuarios/editar" -> salvar(req, resp);
            case "/usuarios/excluir" -> excluir(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String busca = Parametros.texto(req, "busca");
        req.setAttribute("titulo", "Usuários");
        req.setAttribute("busca", busca);
        req.setAttribute("usuarios", dao.listar(busca));
        req.setAttribute("agendasPorUsuario", dao.contarAgendasPorUsuario());
        req.getRequestDispatcher("/WEB-INF/jsp/usuarios/lista.jsp").forward(req, resp);
    }

    private void detalhar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario usuario = dao.buscarComAlergias(Parametros.inteiro(req, "id"));
        if (usuario == null) {
            Mensagens.erro(req, "Usuário não encontrado.");
            resp.sendRedirect(req.getContextPath() + "/usuarios");
            return;
        }
        req.setAttribute("titulo", usuario.getNome());
        req.setAttribute("usuario", usuario);
        req.setAttribute("totalAgendas", dao.contarAgendas(usuario.getId()));
        req.getRequestDispatcher("/WEB-INF/jsp/usuarios/detalhe.jsp").forward(req, resp);
    }

    /** Item 6 + 7B: cartão de vacinação - agendamentos do usuário agrupados por vacina, com progresso. */
    private void cartao(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Usuario usuario = dao.buscarComAlergias(Parametros.inteiro(req, "id"));
        if (usuario == null) {
            Mensagens.erro(req, "Usuário não encontrado.");
            resp.sendRedirect(req.getContextPath() + "/usuarios");
            return;
        }
        List<SerieDoses> series = doseService.montarCartao(usuario.getId());
        req.setAttribute("titulo", "Cartão de vacinação - " + usuario.getNome());
        req.setAttribute("usuario", usuario);
        req.setAttribute("series", series);
        req.setAttribute("totalAgendas", series.stream().mapToInt(s -> s.getDoses().size()).sum());
        req.setAttribute("hojeIso", LocalDate.now().toString());
        req.setAttribute("urlAtual", "/usuarios/agendas?id=" + usuario.getId());
        req.getRequestDispatcher("/WEB-INF/jsp/usuarios/agendas.jsp").forward(req, resp);
    }

    private void exibirFormulario(HttpServletRequest req, HttpServletResponse resp, Usuario usuario,
                                  Set<Integer> alergiasSelecionadas, List<String> erros)
            throws ServletException, IOException {
        req.setAttribute("titulo", usuario.getId() == null ? "Novo usuário" : "Editar usuário");
        req.setAttribute("usuario", usuario);
        req.setAttribute("alergias", alergiaDAO.listarTodos());
        req.setAttribute("alergiasSelecionadas", alergiasSelecionadas);
        req.setAttribute("sexos", Sexo.values());
        req.setAttribute("ufs", UF.values());
        req.setAttribute("hojeIso", LocalDate.now().toString());
        req.setAttribute("erros", erros);
        req.getRequestDispatcher("/WEB-INF/jsp/usuarios/form.jsp").forward(req, resp);
    }

    /** Inclusão (POST /usuarios/novo) ou alteração (POST /usuarios/editar com id). */
    private void salvar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        boolean edicao = "/usuarios/editar".equals(req.getServletPath());
        Integer id = null;
        if (edicao) {
            Usuario existente = dao.buscarPorId(Parametros.inteiro(req, "id"));
            if (existente == null) {
                Mensagens.erro(req, "Usuário não encontrado.");
                resp.sendRedirect(req.getContextPath() + "/usuarios");
                return;
            }
            id = existente.getId();
        }

        List<String> erros = new ArrayList<>();
        Usuario usuario = lerFormulario(req, erros);
        usuario.setId(id);
        Set<Integer> alergiaIds = lerAlergias(req, erros);

        if (!erros.isEmpty()) {
            exibirFormulario(req, resp, usuario, alergiaIds, erros);
            return;
        }
        try {
            dao.salvar(usuario, alergiaIds);
        } catch (IllegalArgumentException e) {
            exibirFormulario(req, resp, usuario, alergiaIds,
                    List.of("Uma das alergias selecionadas não existe mais. Recarregue a página e tente de novo."));
            return;
        } catch (PersistenceException e) {
            exibirFormulario(req, resp, usuario, alergiaIds,
                    List.of("Não foi possível salvar o usuário. Confira os dados informados."));
            return;
        }
        Mensagens.sucesso(req, "Usuário \"" + usuario.getNome() + "\" " + (edicao ? "alterado." : "cadastrado."));
        resp.sendRedirect(req.getContextPath() + "/usuarios/detalhe?id=" + usuario.getId());
    }

    /** Monta o usuário com os dados do formulário, acumulando as mensagens de validação em {@code erros}. */
    private Usuario lerFormulario(HttpServletRequest req, List<String> erros) {
        Usuario u = new Usuario();

        u.setNome(Parametros.texto(req, "nome"));
        if (u.getNome() == null) {
            erros.add("Informe o nome.");
        } else if (u.getNome().length() > TAMANHO_NOME) {
            erros.add("O nome deve ter no máximo " + TAMANHO_NOME + " caracteres.");
        }

        String nascimento = Parametros.texto(req, "dataNascimento");
        if (nascimento == null) {
            erros.add("Informe a data de nascimento.");
        } else {
            try {
                u.setDataNascimento(LocalDate.parse(nascimento));
                if (u.getDataNascimento().isAfter(LocalDate.now())) {
                    erros.add("A data de nascimento não pode ser no futuro.");
                } else if (u.getDataNascimento().isBefore(NASCIMENTO_MINIMO)) {
                    erros.add("A data de nascimento deve ser a partir de 01/01/1900.");
                }
            } catch (DateTimeParseException e) {
                erros.add("Data de nascimento inválida.");
            }
        }

        try {
            u.setSexo(Sexo.deCodigo(Parametros.texto(req, "sexo")));
        } catch (IllegalArgumentException e) {
            u.setSexo(null);
        }
        if (u.getSexo() == null) {
            erros.add("Selecione o sexo.");
        }

        u.setUf(UF.deSigla(Parametros.texto(req, "uf")));
        if (u.getUf() == null) {
            erros.add("Selecione a UF.");
        }

        u.setLogradouro(textoOpcional(req, "logradouro", "O logradouro", TAMANHO_LOGRADOURO, erros));
        u.setSetor(textoOpcional(req, "setor", "O setor", TAMANHO_SETOR, erros));
        u.setCidade(textoOpcional(req, "cidade", "A cidade", TAMANHO_CIDADE, erros));

        String numero = Parametros.texto(req, "numero");
        if (numero != null) {
            try {
                long valor = Long.parseLong(numero);
                if (valor < 0 || valor > NUMERO_MAXIMO) {
                    erros.add("O número deve ter no máximo 10 dígitos.");
                } else {
                    u.setNumero(valor);
                }
            } catch (NumberFormatException e) {
                erros.add("O número do endereço deve conter apenas dígitos.");
            }
        }
        return u;
    }

    private static String textoOpcional(HttpServletRequest req, String campo, String rotulo, int tamanho,
                                        List<String> erros) {
        String valor = Parametros.texto(req, campo);
        if (valor != null && valor.length() > tamanho) {
            erros.add(rotulo + " deve ter no máximo " + tamanho + " caracteres.");
        }
        return valor;
    }

    /** Ids das alergias marcadas (0..N). */
    private static Set<Integer> lerAlergias(HttpServletRequest req, List<String> erros) {
        Set<Integer> ids = new LinkedHashSet<>();
        String[] valores = req.getParameterValues("alergias");
        if (valores == null) {
            return ids;
        }
        for (String valor : valores) {
            try {
                ids.add(Integer.valueOf(valor.trim()));
            } catch (NumberFormatException e) {
                erros.add("Seleção de alergias inválida.");
                return ids;
            }
        }
        return ids;
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Usuario usuario = dao.buscarPorId(Parametros.inteiro(req, "id"));
        if (usuario == null) {
            Mensagens.erro(req, "Usuário não encontrado.");
            resp.sendRedirect(req.getContextPath() + "/usuarios");
            return;
        }
        long agendas = dao.contarAgendas(usuario.getId());
        try {
            dao.excluir(usuario.getId());
            Mensagens.sucesso(req, "Usuário \"" + usuario.getNome() + "\" excluído"
                    + (agendas == 0 ? "." : " junto com " + agendas + (agendas == 1 ? " agenda." : " agendas.")));
            resp.sendRedirect(req.getContextPath() + "/usuarios");
        } catch (PersistenceException e) {
            Mensagens.erro(req, "Não foi possível excluir o usuário \"" + usuario.getNome() + "\".");
            resp.sendRedirect(req.getContextPath() + "/usuarios/detalhe?id=" + usuario.getId());
        }
    }
}
