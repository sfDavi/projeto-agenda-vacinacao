package br.ufg.vacinacao.model;

/** Sexo do usuário. No banco é gravado como {@code M}/{@code F} (char(1)) - ver {@link SexoConverter}. */
public enum Sexo {
    M("Masculino"),
    F("Feminino");

    private final String rotulo;

    Sexo(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    /** Código gravado no banco ({@code "M"} ou {@code "F"}). */
    public String getCodigo() {
        return name();
    }

    public static Sexo deCodigo(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return null;
        }
        return valueOf(codigo.trim().toUpperCase());
    }
}
