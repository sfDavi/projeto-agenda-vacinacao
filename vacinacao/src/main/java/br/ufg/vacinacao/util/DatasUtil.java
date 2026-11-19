package br.ufg.vacinacao.util;

import br.ufg.vacinacao.model.Periodicidade;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

/** Utilitários de data usados nas telas e na regra de geração das doses. */
public final class DatasUtil {

    /** Formato exibido nas telas. */
    public static final DateTimeFormatter BR = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private DatasUtil() {
    }

    /** {@code dd/MM/yyyy}, ou texto vazio se a data for nula. */
    public static String formatar(LocalDate data) {
        return data == null ? "" : data.format(BR);
    }

    /** Dia da semana abreviado em português ("seg.", "ter." ...). */
    public static String diaDaSemana(LocalDate data) {
        return data == null ? "" : data.getDayOfWeek().getDisplayName(TextStyle.SHORT, PT_BR);
    }

    /**
     * Soma {@code quantidade} unidades de {@code periodicidade} à data base.
     * <p>
     * Meses e anos usam {@code plusMonths}/{@code plusYears}, que mantêm o mesmo dia quando possível
     * e, se o dia não existir no mês de destino, usam o último dia do mês
     * (31/01 + 1 mês = 28/02; 29/02 + 1 ano = 28/02).
     */
    public static LocalDate somar(LocalDate base, Periodicidade periodicidade, int quantidade) {
        Objects.requireNonNull(base, "data base");
        Objects.requireNonNull(periodicidade, "periodicidade");
        return switch (periodicidade) {
            case DIAS -> base.plusDays(quantidade);
            case SEMANAS -> base.plusWeeks(quantidade);
            case MESES -> base.plusMonths(quantidade);
            case ANOS -> base.plusYears(quantidade);
        };
    }
}
