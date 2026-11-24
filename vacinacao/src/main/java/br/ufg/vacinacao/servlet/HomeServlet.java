package br.ufg.vacinacao.servlet;

import br.ufg.vacinacao.dao.AgendaDAO;
import br.ufg.vacinacao.model.Agenda;
import br.ufg.vacinacao.service.DoseService;
import br.ufg.vacinacao.util.DatasUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

/** Painel inicial: agendas de hoje (Item 4) com baixa rápida, e resumo das próximas aplicações. */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    private static final int LIMITE_PROXIMAS = 6;

    private final AgendaDAO agendaDAO = new AgendaDAO();
    private final DoseService doseService = new DoseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate hoje = LocalDate.now();
        Object erroJpa = null;

        req.setAttribute("titulo", "Início");
        req.setAttribute("menuAtivo", "home");
        req.setAttribute("hoje", DatasUtil.formatar(hoje));
        req.setAttribute("urlAtual", "/home");

        // Sempre tenta consultar: se o banco voltou depois de uma falha no startup, o painel se recupera sozinho
        try {
            req.setAttribute("agendasHoje", agendaDAO.listarDoDia(hoje));
            // Atrasadas + próximas (sem as de hoje, que já estão no outro card)
            List<Agenda> pendentes = doseService.proximasEAtrasadas().stream()
                    .filter(a -> !a.getData().equals(hoje))
                    .toList();
            req.setAttribute("totalAtrasadas", pendentes.stream().filter(Agenda::isAtrasada).count());
            req.setAttribute("proximasHome", pendentes.stream().limit(LIMITE_PROXIMAS).toList());
            getServletContext().removeAttribute("erroJpa");
        } catch (RuntimeException e) {
            // PersistenceException ou falha ao criar o EntityManagerFactory (MySQL fora do ar / esquema ausente)
            Object erroStartup = getServletContext().getAttribute("erroJpa");
            erroJpa = erroStartup != null ? erroStartup : e.getMessage();
        }
        if (erroJpa != null) {
            req.setAttribute("agendasHoje", List.of());
            req.setAttribute("proximasHome", List.of());
        }
        req.setAttribute("erroJpa", erroJpa);
        req.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(req, resp);
    }
}
