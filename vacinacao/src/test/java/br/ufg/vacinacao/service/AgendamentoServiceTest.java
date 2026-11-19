package br.ufg.vacinacao.service;

import br.ufg.vacinacao.model.Periodicidade;
import br.ufg.vacinacao.model.Vacina;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Regra de geração das datas das doses (seção 5 / plano de testes da seção 9). */
class AgendamentoServiceTest {

    private static Vacina vacina(int doses, Periodicidade periodicidade, Integer intervalo) {
        Vacina v = new Vacina();
        v.setTitulo("Teste");
        v.setDoses(doses);
        v.setPeriodicidade(periodicidade);
        v.setIntervalo(intervalo);
        return v;
    }

    @Test
    void doseUnicaGeraUmaAgendaNaDataEscolhida() {
        LocalDate inicio = LocalDate.of(2026, 11, 20);
        assertEquals(List.of(inicio), AgendamentoService.calcularDatas(vacina(1, null, null), inicio));
    }

    @Test
    void doseUnicaIgnoraPeriodicidadeEIntervalo() {
        LocalDate inicio = LocalDate.of(2026, 11, 20);
        assertEquals(List.of(inicio), AgendamentoService.calcularDatas(vacina(1, Periodicidade.MESES, 6), inicio));
    }

    @Test
    void tresDosesMensaisIntervaloDoisAPartirDe31DeJaneiro() {
        assertEquals(
                List.of(LocalDate.of(2027, 1, 31), LocalDate.of(2027, 3, 31), LocalDate.of(2027, 5, 31)),
                AgendamentoService.calcularDatas(vacina(3, Periodicidade.MESES, 2), LocalDate.of(2027, 1, 31)));
    }

    @Test
    void duasDosesMensaisAPartirDe31DeJaneiroCaemNoFimDeFevereiro() {
        assertEquals(
                List.of(LocalDate.of(2027, 1, 31), LocalDate.of(2027, 2, 28)),
                AgendamentoService.calcularDatas(vacina(2, Periodicidade.MESES, 1), LocalDate.of(2027, 1, 31)));
        assertEquals(
                List.of(LocalDate.of(2028, 1, 31), LocalDate.of(2028, 2, 29)),
                AgendamentoService.calcularDatas(vacina(2, Periodicidade.MESES, 1), LocalDate.of(2028, 1, 31)));
    }

    @Test
    void calculaSempreAPartirDaDataBaseENaoEmCadeia() {
        // 31/01 -> 28/02 -> 31/03 (somar em cadeia daria 28/03)
        List<LocalDate> datas = AgendamentoService.calcularDatas(vacina(3, Periodicidade.MESES, 1), LocalDate.of(2027, 1, 31));
        assertEquals(LocalDate.of(2027, 3, 31), datas.get(2));
    }

    @Test
    void exemploDoEnunciadoCincoDosesAnuaisIntervaloTres() {
        LocalDate inicio = LocalDate.of(2026, 11, 20);
        assertEquals(
                List.of(inicio, inicio.plusYears(3), inicio.plusYears(6), inicio.plusYears(9), inicio.plusYears(12)),
                AgendamentoService.calcularDatas(vacina(5, Periodicidade.ANOS, 3), inicio));
    }

    @Test
    void anualAPartirDe29DeFevereiro() {
        assertEquals(
                List.of(LocalDate.of(2028, 2, 29), LocalDate.of(2029, 2, 28), LocalDate.of(2030, 2, 28),
                        LocalDate.of(2031, 2, 28), LocalDate.of(2032, 2, 29)),
                AgendamentoService.calcularDatas(vacina(5, Periodicidade.ANOS, 1), LocalDate.of(2028, 2, 29)));
    }

    @Test
    void diaria() {
        assertEquals(
                List.of(LocalDate.of(2026, 12, 30), LocalDate.of(2027, 1, 2), LocalDate.of(2027, 1, 5),
                        LocalDate.of(2027, 1, 8)),
                AgendamentoService.calcularDatas(vacina(4, Periodicidade.DIAS, 3), LocalDate.of(2026, 12, 30)));
    }

    @Test
    void semanal() {
        assertEquals(
                List.of(LocalDate.of(2026, 11, 20), LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 18)),
                AgendamentoService.calcularDatas(vacina(3, Periodicidade.SEMANAS, 2), LocalDate.of(2026, 11, 20)));
    }

    @Test
    void validaFormatoDaHora() {
        assertTrue(AgendamentoService.horaValida("00:00"));
        assertTrue(AgendamentoService.horaValida("09:30"));
        assertTrue(AgendamentoService.horaValida("23:59"));
        assertFalse(AgendamentoService.horaValida("24:00"));
        assertFalse(AgendamentoService.horaValida("9:30"));
        assertFalse(AgendamentoService.horaValida("12:60"));
        assertFalse(AgendamentoService.horaValida(null));
    }
}
