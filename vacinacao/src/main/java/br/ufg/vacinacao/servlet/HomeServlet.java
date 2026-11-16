package br.ufg.vacinacao.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Painel inicial. Nas próximas fases recebe as agendas de hoje (Item 4) e próximas aplicações (7B). */
@WebServlet("/home")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("titulo", "Início");
        req.setAttribute("menuAtivo", "home");
        req.setAttribute("hoje", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        req.setAttribute("erroJpa", getServletContext().getAttribute("erroJpa"));
        req.getRequestDispatcher("/WEB-INF/jsp/home.jsp").forward(req, resp);
    }
}
