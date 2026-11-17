package br.ufg.vacinacao.model;

/**
 * Unidade de tempo entre as doses de uma vacina.
 * No banco é gravada como inteiro (1 dia, 2 semanas, 3 meses, 4 anos) - ver {@link PeriodicidadeConverter}.
 */
public enum Periodicidade {
    DIAS(1, "Dias", "dia", "dias"),
    SEMANAS(2, "Semanas", "semana", "semanas"),
    MESES(3, "Meses", "mês", "meses"),
    ANOS(4, "Anos", "ano", "anos");

    private final int codigo;
    private final String rotulo;
    private final String singular;
    private final String plural;

    Periodicidade(int codigo, String rotulo, String singular, String plural) {
        this.codigo = codigo;
        this.rotulo = rotulo;
        this.singular = singular;
        this.plural = plural;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getRotulo() {
        return rotulo;
    }

    /** "1 mês", "3 anos" etc. */
    public String descrever(int quantidade) {
        return quantidade + " " + (quantidade == 1 ? singular : plural);
    }

    public static Periodicidade deCodigo(Integer codigo) {
        if (codigo == null) {
            return null;
        }
        for (Periodicidade p : values()) {
            if (p.codigo == codigo) {
                return p;
            }
        }
        throw new IllegalArgumentException("Periodicidade inválida: " + codigo);
    }
}
