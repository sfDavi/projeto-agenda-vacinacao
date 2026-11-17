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
