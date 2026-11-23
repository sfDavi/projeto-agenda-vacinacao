package br.ufg.vacinacao.service;

import br.ufg.vacinacao.model.Agenda;
import br.ufg.vacinacao.model.Situacao;
import br.ufg.vacinacao.model.Usuario;
import br.ufg.vacinacao.model.Vacina;
import br.ufg.vacinacao.util.DatasUtil;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Item 7B - uma série de doses de uma vacina para um usuário, com o progresso e a próxima aplicação.
 * Tudo aqui é <strong>calculado</strong> a partir das agendas; nada é armazenado.
 */
public class SerieDoses {

    /** Situação calculada da série. */
    public enum Status {
        CONCLUIDA("Concluída", "text-bg-success"),
        EM_ANDAMENTO("Em andamento", "text-bg-primary"),
        ATRASADA("Atrasada", "text-bg-danger"),
        INTERROMPIDA("Sem doses agendadas", "text-bg-secondary");

        private final String rotulo;
        private final String classeCss;

        Status(String rotulo, String classeCss) {
            this.rotulo = rotulo;
            this.classeCss = classeCss;
        }

        public String getRotulo() {
            return rotulo;
        }

        public String getClasseCss() {
            return classeCss;
        }
    }

    private final Vacina vacina;
    private final Usuario usuario;
    private final List<Agenda> doses;
    private final int realizadas;
    private final Agenda proxima;
    private final Agenda ultimaRealizada;
    private final LocalDate dataMinimaProxima;
    private final Status status;
    private final Set<Integer> idsReagendaveis = new HashSet<>();

    /** @param doses agendas da série (mesmo usuário e vacina) */
    public SerieDoses(List<Agenda> doses) {
        this.doses = doses.stream()
                .sorted(Comparator.comparingInt(Agenda::getDose).thenComparing(Agenda::getData))
                .toList();
        Agenda primeira = this.doses.get(0);
        this.vacina = primeira.getVacina();
        this.usuario = primeira.getUsuario();

        this.realizadas = (int) this.doses.stream()
                .filter(a -> a.getSituacao() == Situacao.REALIZADO)
                .mapToInt(Agenda::getDose).distinct().count();

        this.proxima = this.doses.stream()
                .filter(a -> a.getSituacao() == Situacao.AGENDADO)
                .findFirst().orElse(null);

        this.ultimaRealizada = this.doses.stream()
                .filter(a -> a.getSituacao() == Situacao.REALIZADO && a.getDataSituacao() != null)
                .max(Comparator.comparingInt(Agenda::getDose))
                .orElse(null);

        this.dataMinimaProxima = proxima == null ? null : dataMinima(proxima.getDose());

        if (realizadas >= vacina.getDoses()) {
            this.status = Status.CONCLUIDA;
        } else if (proxima == null) {
            this.status = Status.INTERROMPIDA;
        } else if (proxima.isAtrasada()) {
            this.status = Status.ATRASADA;
        } else {
            this.status = Status.EM_ANDAMENTO;
        }

        // Dose cancelada pode ser reagendada se não houver outra agenda (agendada/realizada) da mesma dose
        for (Agenda a : this.doses) {
            if (a.getSituacao() == Situacao.CANCELADO && status != Status.CONCLUIDA) {
                boolean substituida = this.doses.stream().anyMatch(o -> o.getDose() == a.getDose()
                        && o.getSituacao() != Situacao.CANCELADO);
                if (!substituida) {
                    idsReagendaveis.add(a.getId());
                }
            }
        }
    }

    /**
     * Data mínima para aplicar a dose {@code numeroDose}: a última dose realizada antes dela, somada
     * dos intervalos correspondentes. {@code null} se não houver dose anterior realizada.
     */
    public LocalDate dataMinima(int numeroDose) {
        Agenda base = doses.stream()
                .filter(a -> a.getSituacao() == Situacao.REALIZADO && a.getDataSituacao() != null
                        && a.getDose() < numeroDose)
                .max(Comparator.comparingInt(Agenda::getDose))
                .orElse(null);
        if (base == null) {
            return null;
        }
        return vacina.somarIntervalos(base.getDataSituacao(), numeroDose - base.getDose());
    }

    /** A próxima dose está marcada antes da data mínima (dose anterior aplicada com atraso). */
    public boolean isPrecisaRecalcular() {
        return proxima != null && dataMinimaProxima != null && proxima.getData().isBefore(dataMinimaProxima);
    }

    public boolean isReagendavel(Agenda agenda) {
        return idsReagendaveis.contains(agenda.getId());
    }

    public Set<Integer> getIdsReagendaveis() {
        return idsReagendaveis;
    }

    /** Percentual de doses realizadas (0-100) para a barra de progresso. */
    public int getPercentual() {
        return Math.min(100, Math.round(100f * realizadas / vacina.getDoses()));
    }

    public String getDataMinimaProximaFormatada() {
        return DatasUtil.formatar(dataMinimaProxima);
    }

    public Vacina getVacina() {
        return vacina;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public List<Agenda> getDoses() {
        return doses;
    }

    public int getRealizadas() {
        return realizadas;
    }

    public int getTotal() {
        return vacina.getDoses();
    }

    public Agenda getProxima() {
        return proxima;
    }

    public Agenda getUltimaRealizada() {
        return ultimaRealizada;
    }

    public LocalDate getDataMinimaProxima() {
        return dataMinimaProxima;
    }

    public Status getStatus() {
        return status;
    }
}
