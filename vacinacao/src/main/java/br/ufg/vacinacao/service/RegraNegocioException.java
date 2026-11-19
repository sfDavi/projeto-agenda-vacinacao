package br.ufg.vacinacao.service;

import java.util.List;

/** Violação de regra de negócio, com mensagens prontas para exibir ao usuário. */
public class RegraNegocioException extends Exception {

    private static final long serialVersionUID = 1L;

    private final List<String> erros;

    public RegraNegocioException(List<String> erros) {
        super(String.join(" ", erros));
        this.erros = List.copyOf(erros);
    }

    public RegraNegocioException(String erro) {
        this(List.of(erro));
    }

    public List<String> getErros() {
        return erros;
    }
}
