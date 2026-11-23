package br.ufg.vacinacao.service;

import br.ufg.vacinacao.model.Agenda;
import br.ufg.vacinacao.model.Periodicidade;
import br.ufg.vacinacao.model.Situacao;
import br.ufg.vacinacao.model.Usuario;
import br.ufg.vacinacao.model.Vacina;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Item 7B: progresso, próxima dose, data mínima e separação de séries (sem banco). */
class SerieDosesTest {

    private static final LocalDate HOJE = LocalDate.now();
    private static int proximoId = 1;

    private static Vacina vacina(int doses, Periodicidade p, Integer intervalo) {
        Vacina v = new Vacina();
        v.setId(1);
        v.setTitulo("Teste");
        v.setDoses(doses);
        v.setPeriodicidade(p);
        v.setIntervalo(intervalo);
        return v;
    }

    private static Agenda agenda(Vacina v, int dose, LocalDate data, Situacao s, LocalDate dataSituacao) {
        Agenda a = new Agenda();
        a.setId(proximoId++);
        a.setUsuario(new Usuario());
        a.setVacina(v);
        a.setDose(dose);
        a.setData(data);
        a.setHora("10:00");
        a.setSituacao(s);
        a.setDataSituacao(dataSituacao);
        return a;
    }

    @Test
    void progressoEProximaDose() {
        Vacina v = vacina(3, Periodicidade.MESES, 1);
        SerieDoses serie = new SerieDoses(List.of(
                agenda(v, 1, HOJE.minusMonths(1), Situacao.REALIZADO, HOJE.minusMonths(1)),
                agenda(v, 2, HOJE.plusDays(2), Situacao.AGENDADO, null),
                agenda(v, 3, HOJE.plusMonths(1).plusDays(2), Situacao.AGENDADO, null)));
        assertEquals(1, serie.getRealizadas());
        assertEquals(33, serie.getPercentual());
        assertEquals(2, serie.getProxima().getDose());
        assertEquals("em 2 dias", serie.getProxima().getPrazo());
        assertEquals(SerieDoses.Status.EM_ANDAMENTO, serie.getStatus());
        assertFalse(serie.isPrecisaRecalcular());
    }

    @Test
    void doseAtrasadaEAplicacaoTardiaPedemRecalculo() {
        // Antirrábica: dose 1 marcada há 10 dias, aplicada há 4; dose 2 marcada há 7 dias (antes da mínima)
        Vacina v = vacina(4, Periodicidade.DIAS, 3);
        SerieDoses serie = new SerieDoses(List.of(
                agenda(v, 1, HOJE.minusDays(10), Situacao.REALIZADO, HOJE.minusDays(4)),
                agenda(v, 2, HOJE.minusDays(7), Situacao.AGENDADO, null),
                agenda(v, 3, HOJE.minusDays(4), Situacao.AGENDADO, null)));
        assertEquals(SerieDoses.Status.ATRASADA, serie.getStatus());
        assertEquals("atrasada há 7 dias", serie.getProxima().getPrazo());
        assertEquals(HOJE.minusDays(1), serie.getDataMinimaProxima());
        assertTrue(serie.isPrecisaRecalcular());
        assertEquals(HOJE.plusDays(2), serie.dataMinima(3));
    }

    @Test
    void doseCanceladaPodeSerReagendadaUmaVez() {
        Vacina v = vacina(2, Periodicidade.MESES, 6);
        Agenda cancelada = agenda(v, 1, HOJE, Situacao.CANCELADO, HOJE);
        Agenda dose2 = agenda(v, 2, HOJE.plusMonths(6), Situacao.AGENDADO, null);
        assertTrue(new SerieDoses(List.of(cancelada, dose2)).isReagendavel(cancelada));
        assertNull(new SerieDoses(List.of(cancelada, dose2)).dataMinima(1));

        Agenda reagendada = agenda(v, 1, HOJE.plusDays(2), Situacao.AGENDADO, null);
        assertFalse(new SerieDoses(List.of(cancelada, reagendada, dose2)).isReagendavel(cancelada));
    }

    @Test
    void serieConcluida() {
        Vacina v = vacina(2, Periodicidade.SEMANAS, 4);
        SerieDoses serie = new SerieDoses(List.of(
                agenda(v, 1, HOJE.minusWeeks(8), Situacao.REALIZADO, HOJE.minusWeeks(8)),
                agenda(v, 2, HOJE.minusWeeks(4), Situacao.REALIZADO, HOJE.minusWeeks(4))));
        assertEquals(SerieDoses.Status.CONCLUIDA, serie.getStatus());
        assertEquals(100, serie.getPercentual());
        assertNull(serie.getProxima());
    }

    @Test
    void novaDose1ComecaNovaSerie() {
        Vacina v = vacina(1, null, null);
        List<Agenda> agendas = new ArrayList<>(List.of(
                agenda(v, 1, HOJE.minusYears(2), Situacao.REALIZADO, HOJE.minusYears(2)),
                agenda(v, 1, HOJE.plusDays(1), Situacao.AGENDADO, null)));
        assertEquals(2, DoseService.separarSeries(agendas).size());
    }

    @Test
    void reagendamentoDaDose1CanceladaContinuaNaMesmaSerie() {
        Vacina v = vacina(2, Periodicidade.MESES, 1);
        List<Agenda> agendas = List.of(
                agenda(v, 1, HOJE.minusDays(5), Situacao.CANCELADO, HOJE.minusDays(5)),
                agenda(v, 1, HOJE.plusDays(1), Situacao.AGENDADO, null),
                agenda(v, 2, HOJE.plusMonths(1), Situacao.AGENDADO, null));
        assertEquals(1, DoseService.separarSeries(agendas).size());
    }
}
