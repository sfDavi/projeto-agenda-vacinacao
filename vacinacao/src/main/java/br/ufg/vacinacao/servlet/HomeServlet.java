package br.ufg.vacinacao.servlet;

import br.ufg.vacinacao.dao.AgendaDAO;
import br.ufg.vacinacao.util.DatasUtil;
import jakarta.persistence.PersistenceException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/** Painel inicial: agendas de hoje (Item 4) com baixa rápida. Próximas aplicações (7B) entram na Fase 5. */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private final AgendaDAO agendaDAO = new AgendaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate hoje = LocalDate.now();
        Object erroJpa = getServletContext().getAttribute("erroJpa");

        req.setAttribute("titulo", "Início");
        req.setAttribute("menuAtivo", "home");
        req.setAttribute("hoje", DatasUtil.formatar(hoje));
        req.setAttribute("urlAtual", "/home");

        if (erroJpa == null) {
            try {
                req.setAttribute("agendasHoje", agendaDAO.listarDoDia(hoje));
            } catch (PersistenceException e) {
                erroJpa = e.getMessage();
            }
        }
        if (erroJpa != null) {
            req.setAttribute("agendasHoje", List.of());
        }
        req.setAttribute("erroJpa", erroJpa);
        req.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(req, resp);
    }
}
