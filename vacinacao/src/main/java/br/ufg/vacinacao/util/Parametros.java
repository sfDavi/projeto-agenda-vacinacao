package br.ufg.vacinacao.util;

import jakarta.servlet.http.HttpServletRequest;

/** Leitura segura de parâmetros de formulário. */
public final class Parametros {

    private Parametros() {
    }

    /** Texto sem espaços nas pontas; {@code null} se ausente ou em branco. */
    public static String texto(HttpServletRequest req, String nome) {
        String valor = req.getParameter(nome);
        if (valor == null) {
            return null;
        }
        valor = valor.trim();
        return valor.isEmpty() ? null : valor;
    }

    /**
     * Destino interno informado no parâmetro "voltar" (ex.: "/usuarios/agendas?id=3"), aceito apenas se
     * for um caminho da própria aplicação; caso contrário devolve {@code padrao}.
     */
    public static String voltar(HttpServletRequest req, String padrao) {
        String valor = req.getParameter("voltar");
        if (valor != null && VOLTAR_VALIDO.matcher(valor).matches()) {
            return valor;
        }
        return padrao;
    }

    private static final java.util.regex.Pattern VOLTAR_VALIDO =
            java.util.regex.Pattern.compile("^/[a-z/]+(\\?[\\w=&%.\\-+]*)?$");

    /** Inteiro; {@code null} se ausente, em branco ou inválido. */
    public static Integer inteiro(HttpServletRequest req, String nome) {
        String valor = texto(req, nome);
        if (valor == null) {
            return null;
        }
        try {
            return Integer.valueOf(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
