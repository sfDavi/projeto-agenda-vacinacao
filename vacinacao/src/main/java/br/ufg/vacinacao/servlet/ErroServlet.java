package br.ufg.vacinacao.servlet;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Página de erro amigável (registrada em web.xml para 400/404/405/500 e qualquer exceção).
 * A exceção completa vai apenas para o log do servidor; o usuário vê uma mensagem em português.
 */
@WebServlet("/erro")
public class ErroServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(ErroServlet.class.getName());

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Throwable erro = (Throwable) req.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Integer status = (Integer) req.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String uri = (String) req.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        if (status == null) {
            status = erro == null ? HttpServletResponse.SC_NOT_FOUND : HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
        }

        String titulo;
        String mensagem;
        String icone;
        if (erro != null && ehErroDeBanco(erro)) {
            titulo = "Banco de dados indisponível";
            mensagem = "Não foi possível acessar o banco de dados. Verifique se o MySQL está em execução e "
                    + "se o banco \"vacinacao\" foi criado com os scripts da pasta sql/. Depois, tente novamente.";
            icone = "bi-database-x";
        } else if (status == HttpServletResponse.SC_NOT_FOUND) {
            titulo = "Página não encontrada";
            mensagem = "O endereço acessado não existe. Use o menu acima para navegar.";
            icone = "bi-signpost-split";
        } else if (status == HttpServletResponse.SC_METHOD_NOT_ALLOWED) {
            titulo = "Ação não permitida";
            mensagem = "Esta ação só pode ser feita pelos botões e formulários do sistema.";
            icone = "bi-slash-circle";
        } else if (status == HttpServletResponse.SC_BAD_REQUEST) {
            titulo = "Requisição inválida";
            mensagem = "Os dados enviados não puderam ser entendidos. Volte e tente novamente.";
            icone = "bi-exclamation-diamond";
        } else {
            titulo = "Erro inesperado";
            mensagem = "Ocorreu um erro ao processar a solicitação. Nada foi alterado além do que já havia sido "
                    + "confirmado. Volte e tente novamente.";
            icone = "bi-bug";
        }

        if (erro != null) {
            LOG.log(Level.SEVERE, "Erro ao processar " + uri, erro);
        }

        resp.setStatus(status);
        req.setAttribute("titulo", titulo);
        req.setAttribute("tituloErro", titulo);
        req.setAttribute("mensagemErro", mensagem);
        req.setAttribute("iconeErro", icone);
        req.setAttribute("codigoErro", status);
        req.getRequestDispatcher("/WEB-INF/jsp/erro.jsp").forward(req, resp);
    }

    private static boolean ehErroDeBanco(Throwable erro) {
        for (Throwable t = erro; t != null; t = t.getCause()) {
            if (t instanceof PersistenceException || t instanceof SQLException) {
                return true;
            }
            if (t.getCause() == t) {
                break;
            }
        }
        return false;
    }
}
