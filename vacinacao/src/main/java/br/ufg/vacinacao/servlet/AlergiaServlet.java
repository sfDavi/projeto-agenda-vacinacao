package br.ufg.vacinacao.servlet;

import br.ufg.vacinacao.dao.AlergiaDAO;
import br.ufg.vacinacao.model.Alergia;
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
import java.util.List;

/** CRUD de alergias: listagem, inclusão e exclusão. */
@WebServlet({"/alergias", "/alergias/novo", "/alergias/excluir"})
public class AlergiaServlet extends HttpServlet {

    private static final int TAMANHO_NOME = 40;

    private final AlergiaDAO dao = new AlergiaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("menuAtivo", "alergias");
        switch (req.getServletPath()) {
            case "/alergias/novo" -> exibirFormulario(req, resp, new Alergia(), List.of());
            case "/alergias" -> listar(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("menuAtivo", "alergias");
        switch (req.getServletPath()) {
            case "/alergias/novo" -> incluir(req, resp);
            case "/alergias/excluir" -> excluir(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    private void listar(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("titulo", "Alergias");
        req.setAttribute("alergias", dao.listarTodos());
        req.getRequestDispatcher("/WEB-INF/jsp/alergias/lista.jsp").forward(req, resp);
    }

    private void exibirFormulario(HttpServletRequest req, HttpServletResponse resp, Alergia alergia, List<String> erros)
            throws ServletException, IOException {
        req.setAttribute("titulo", "Nova alergia");
        req.setAttribute("alergia", alergia);
        req.setAttribute("erros", erros);
        req.getRequestDispatcher("/WEB-INF/jsp/alergias/form.jsp").forward(req, resp);
    }

    private void incluir(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Alergia alergia = new Alergia(Parametros.texto(req, "nome"));

        List<String> erros = new ArrayList<>();
        if (alergia.getNome() == null) {
            erros.add("Informe o nome da alergia.");
        } else if (alergia.getNome().length() > TAMANHO_NOME) {
            erros.add("O nome deve ter no máximo " + TAMANHO_NOME + " caracteres.");
        } else if (dao.existeNome(alergia.getNome())) {
            erros.add("Já existe uma alergia chamada \"" + alergia.getNome() + "\".");
        }
        if (!erros.isEmpty()) {
            exibirFormulario(req, resp, alergia, erros);
            return;
        }

        try {
            dao.salvar(alergia);
        } catch (PersistenceException e) {
            // ex.: nome igual com acentuação diferente (a collation do banco não diferencia)
            exibirFormulario(req, resp, alergia, List.of("Não foi possível salvar: já existe uma alergia equivalente."));
            return;
        }
        Mensagens.sucesso(req, "Alergia \"" + alergia.getNome() + "\" cadastrada.");
        resp.sendRedirect(req.getContextPath() + "/alergias");
    }

    private void excluir(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Alergia alergia = dao.buscarPorId(Parametros.inteiro(req, "id"));
        if (alergia == null) {
            Mensagens.erro(req, "Alergia não encontrada.");
        } else {
            long usuarios = dao.contarUsuarios(alergia.getId());
            long vacinas = dao.contarVacinas(alergia.getId());
            if (usuarios > 0 || vacinas > 0) {
                List<String> usos = new ArrayList<>();
                if (usuarios > 0) {
                    usos.add("está registrada em " + usuarios + (usuarios == 1 ? " usuário" : " usuários"));
                }
                if (vacinas > 0) {
                    usos.add("é contraindicação de " + vacinas + (vacinas == 1 ? " vacina" : " vacinas"));
                }
                Mensagens.erro(req, "Não é possível excluir \"" + alergia.getNome() + "\": ela "
                        + String.join(" e ", usos) + ".");
            } else {
                try {
                    dao.excluir(alergia.getId());
                    Mensagens.sucesso(req, "Alergia \"" + alergia.getNome() + "\" excluída.");
                } catch (PersistenceException e) {
                    Mensagens.erro(req, "Não é possível excluir \"" + alergia.getNome()
                            + "\": ela está em uso em outros cadastros.");
                }
            }
        }
        resp.sendRedirect(req.getContextPath() + "/alergias");
    }
}
