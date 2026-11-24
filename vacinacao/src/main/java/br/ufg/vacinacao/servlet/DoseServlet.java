package br.ufg.vacinacao.servlet;

import br.ufg.vacinacao.model.Agenda;
import br.ufg.vacinacao.service.DoseService;
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
import java.util.List;

/** Controle de doses: próximas aplicações, recalcular doses e reagendar dose cancelada. */
@WebServlet({"/doses/proximas", "/doses/recalcular", "/doses/reagendar"})
public class DoseServlet extends HttpServlet {

    private final DoseService doseService = new DoseService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"/doses/proximas".equals(req.getServletPath())) {
            resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        List<Agenda> agendas = doseService.proximasEAtrasadas();
        LocalDate hoje = LocalDate.now();
        req.setAttribute("menuAtivo", "doses");
        req.setAttribute("titulo", "Próximas aplicações");
        req.setAttribute("atrasadas", agendas.stream().filter(a -> a.getData().isBefore(hoje)).toList());
        req.setAttribute("proximas", agendas.stream().filter(a -> !a.getData().isBefore(hoje)).toList());
        req.setAttribute("dias", DoseService.DIAS_PROXIMAS);
        req.setAttribute("ate", DatasUtil.formatar(hoje.plusDays(DoseService.DIAS_PROXIMAS)));
        req.setAttribute("urlAtual", "/doses/proximas");
        req.getRequestDispatcher("/WEB-INF/jsp/doses/proximas.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        switch (req.getServletPath()) {
            case "/doses/recalcular" -> recalcular(req, resp);
            case "/doses/reagendar" -> reagendar(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        }
    }

    /** POST usuario, vacina, voltar. */
    private void recalcular(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Integer usuarioId = Parametros.inteiro(req, "usuario");
        Integer vacinaId = Parametros.inteiro(req, "vacina");
        try {
            int alteradas = doseService.recalcular(usuarioId, vacinaId);
            if (alteradas == 0) {
                Mensagens.info(req, "As datas das próximas doses já estavam corretas; nada foi alterado.");
            } else {
                Mensagens.sucesso(req, alteradas + (alteradas == 1 ? " dose reprogramada" : " doses reprogramadas")
                        + " a partir da data real da última aplicação.");
            }
        } catch (RegraNegocioException e) {
            Mensagens.erro(req, String.join(" ", e.getErros()));
        } catch (PersistenceException e) {
            Mensagens.erro(req, "Não foi possível recalcular as doses. Tente novamente.");
        }
        resp.sendRedirect(req.getContextPath()
                + Parametros.voltar(req, usuarioId == null ? "/usuarios" : "/usuarios/agendas?id=" + usuarioId));
    }

    /** POST id (agenda cancelada), data, hora, voltar. */
    private void reagendar(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LocalDate data = null;
        String dataTexto = Parametros.texto(req, "data");
        if (dataTexto != null) {
            try {
                data = LocalDate.parse(dataTexto);
            } catch (DateTimeParseException e) {
                data = null;
            }
        }
        String destino = Parametros.voltar(req, "/agendas");
        try {
            Agenda nova = doseService.reagendar(Parametros.inteiro(req, "id"), data, Parametros.texto(req, "hora"));
            Mensagens.sucesso(req, "Dose " + nova.getDose() + " de " + nova.getVacina().getTitulo()
                    + " reagendada para " + nova.getDataFormatada() + " às " + nova.getHora() + ".");
        } catch (RegraNegocioException e) {
            Mensagens.erro(req, String.join(" ", e.getErros()));
        } catch (PersistenceException e) {
            Mensagens.erro(req, "Não foi possível reagendar a dose. Tente novamente.");
        }
        resp.sendRedirect(req.getContextPath() + destino);
    }
}
