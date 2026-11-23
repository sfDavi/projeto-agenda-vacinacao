package br.ufg.vacinacao.service;

import java.util.List;

/**
 * Item 7A: o usuário tem alergia que contraindica a vacina e o operador ainda não confirmou
 * "Estou ciente - prosseguir mesmo assim".
 */
public class ContraindicacaoException extends RegraNegocioException {

    private static final long serialVersionUID = 1L;

    private final List<String> alergias;

    public ContraindicacaoException(List<String> alergias) {
        super("Contraindicação: o usuário tem alergia a " + String.join(", ", alergias)
                + ". Para prosseguir, marque \"Estou ciente\".");
        this.alergias = List.copyOf(alergias);
    }

    public List<String> getAlergias() {
        return alergias;
    }
}
