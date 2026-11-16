package br.ufg.vacinacao.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Mensagens "flash": guardadas na sessão antes de um redirect (Post/Redirect/Get)
 * e exibidas uma única vez por {@code layout/alertas.jspf}.
 */
public final class Mensagens {

    /** Nome do atributo de sessão lido pela JSP de alertas. */
    public static final String ATRIBUTO = "flash";

    private Mensagens() {
    }

    public static void sucesso(HttpServletRequest req, String texto) {
        adicionar(req, "success", texto);
    }

    public static void erro(HttpServletRequest req, String texto) {
        adicionar(req, "danger", texto);
    }

    public static void aviso(HttpServletRequest req, String texto) {
        adicionar(req, "warning", texto);
    }

    public static void info(HttpServletRequest req, String texto) {
        adicionar(req, "info", texto);
    }

    private static void adicionar(HttpServletRequest req, String tipo, String texto) {
        HttpSession sessao = req.getSession();
        sessao.setAttribute(ATRIBUTO, new Mensagem(tipo, texto));
    }

    /** Tipo segue as classes de alerta do Bootstrap (success, danger, warning, info). */
    public static final class Mensagem implements java.io.Serializable {
        private static final long serialVersionUID = 1L;

        private final String tipo;
        private final String texto;

        public Mensagem(String tipo, String texto) {
            this.tipo = tipo;
            this.texto = texto;
        }

        public String getTipo() {
            return tipo;
        }

        public String getTexto() {
            return texto;
        }
    }
}
