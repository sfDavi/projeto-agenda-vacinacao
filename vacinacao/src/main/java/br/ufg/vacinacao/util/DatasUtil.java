package br.ufg.vacinacao.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Utilitários de data. A soma de periodicidades (regra das doses) entra na Fase 3. */
public final class DatasUtil {

    /** Formato exibido nas telas. */
    public static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private DatasUtil() {
    }

    /** {@code dd/MM/yyyy}, ou texto vazio se a data for nula. */
    public static String formatar(LocalDate data) {
        return data == null ? "" : data.format(BR);
    }
}
