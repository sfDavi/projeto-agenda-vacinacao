package br.ufg.vacinacao.model;

/** Unidades federativas do Brasil. Gravada pela sigla (char(2)) via {@code @Enumerated(STRING)}. */
public enum UF {
    AC("Acre"),
    AL("Alagoas"),
    AP("Amapá"),
    AM("Amazonas"),
    BA("Bahia"),
    CE("Ceará"),
    DF("Distrito Federal"),
    ES("Espírito Santo"),
    GO("Goiás"),
    MA("Maranhão"),
    MT("Mato Grosso"),
    MS("Mato Grosso do Sul"),
    MG("Minas Gerais"),
    PA("Pará"),
    PB("Paraíba"),
    PR("Paraná"),
    PE("Pernambuco"),
    PI("Piauí"),
    RJ("Rio de Janeiro"),
    RN("Rio Grande do Norte"),
    RS("Rio Grande do Sul"),
    RO("Rondônia"),
    RR("Roraima"),
    SC("Santa Catarina"),
    SP("São Paulo"),
    SE("Sergipe"),
    TO("Tocantins");

    private final String nome;

    UF(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public String getSigla() {
        return name();
    }

    /** Sigla válida ou {@code null}. */
    public static UF deSigla(String sigla) {
        if (sigla == null || sigla.isBlank()) {
            return null;
        }
        try {
            return valueOf(sigla.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
