package br.ufg.vacinacao.util;

import br.ufg.vacinacao.model.Periodicidade;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatasUtilTest {

    @Test
    void somaDias() {
        assertEquals(LocalDate.of(2026, 12, 2), DatasUtil.somar(LocalDate.of(2026, 11, 29), Periodicidade.DIAS, 3));
    }

    @Test
    void somaSemanas() {
        assertEquals(LocalDate.of(2026, 12, 17), DatasUtil.somar(LocalDate.of(2026, 11, 19), Periodicidade.SEMANAS, 4));
    }

    @Test
    void somaMesesMantemODiaQuandoPossivel() {
        assertEquals(LocalDate.of(2026, 3, 31), DatasUtil.somar(LocalDate.of(2026, 1, 31), Periodicidade.MESES, 2));
    }

    @Test
    void somaMesesUsaUltimoDiaQuandoODiaNaoExiste() {
        assertEquals(LocalDate.of(2026, 2, 28), DatasUtil.somar(LocalDate.of(2026, 1, 31), Periodicidade.MESES, 1));
        assertEquals(LocalDate.of(2028, 2, 29), DatasUtil.somar(LocalDate.of(2028, 1, 31), Periodicidade.MESES, 1));
    }

    @Test
    void somaAnosAPartirDe29DeFevereiro() {
        assertEquals(LocalDate.of(2029, 2, 28), DatasUtil.somar(LocalDate.of(2028, 2, 29), Periodicidade.ANOS, 1));
        assertEquals(LocalDate.of(2032, 2, 29), DatasUtil.somar(LocalDate.of(2028, 2, 29), Periodicidade.ANOS, 4));
    }

    @Test
    void quantidadeZeroDevolveAMesmaData() {
        LocalDate base = LocalDate.of(2026, 11, 19);
        for (Periodicidade p : Periodicidade.values()) {
            assertEquals(base, DatasUtil.somar(base, p, 0));
        }
    }

    @Test
    void formataNoPadraoBrasileiro() {
        assertEquals("05/03/2026", DatasUtil.formatar(LocalDate.of(2026, 3, 5)));
        assertEquals("", DatasUtil.formatar(null));
    }

    @Test
    void periodicidadeObrigatoria() {
        assertThrows(NullPointerException.class, () -> DatasUtil.somar(LocalDate.now(), null, 1));
    }
}
