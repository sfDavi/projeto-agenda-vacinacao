package br.ufg.vacinacao.model;

/** Situação de uma agenda. Gravada pelo nome (AGENDADO/REALIZADO/CANCELADO) em {@code agendas.situacao}. */
public enum Situacao {
    AGENDADO("Agendado", "badge-agendado", 0),
    REALIZADO("Realizado", "badge-realizado", 1),
    CANCELADO("Cancelado", "badge-cancelado", 2);

    private final String rotulo;
    private final String classeCss;
    private final int ordemExibicao;

    Situacao(String rotulo, String classeCss, int ordemExibicao) {
        this.rotulo = rotulo;
        this.classeCss = classeCss;
        this.ordemExibicao = ordemExibicao;
    }

    public String getRotulo() {
        return rotulo;
    }

    /** Classe do badge colorido (ver assets/app.css). */
    public String getClasseCss() {
        return classeCss;
    }

    /** Ordem usada na agenda do dia (Item 4): Agendado, Realizado, Cancelado. */
    public int ordemExibicao() {
        return ordemExibicao;
    }

    public String getNome() {
        return name();
    }
}
