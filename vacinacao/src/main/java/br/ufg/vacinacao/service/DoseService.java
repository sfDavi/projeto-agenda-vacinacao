package br.ufg.vacinacao.service;

import br.ufg.vacinacao.dao.AgendaDAO;
import br.ufg.vacinacao.model.Agenda;
import br.ufg.vacinacao.model.Situacao;
import br.ufg.vacinacao.util.DatasUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Item 6 (agendamentos do usuário) e Item 7B (controle de doses e próximas aplicações).
 * <p>
 * Progresso, próxima dose, atraso e data mínima são <strong>calculados</strong> a partir das agendas.
 */
public class DoseService {

    /** Janela da página "Próximas aplicações". */
    public static final int DIAS_PROXIMAS = 30;

    private final AgendaDAO agendaDAO = new AgendaDAO();

    /** Cartão de vacinação: as séries do usuário, agrupadas por vacina (ordem alfabética). */
    public List<SerieDoses> montarCartao(Integer usuarioId) {
        Map<Integer, List<Agenda>> porVacina = new LinkedHashMap<>();
        for (Agenda a : agendaDAO.listarPorUsuario(usuarioId)) {
            porVacina.computeIfAbsent(a.getVacina().getId(), k -> new ArrayList<>()).add(a);
        }
        List<SerieDoses> series = new ArrayList<>();
        for (List<Agenda> agendas : porVacina.values()) {
            for (List<Agenda> serie : separarSeries(agendas)) {
                series.add(new SerieDoses(serie));
            }
        }
        return series;
    }

    /**
     * Separa as agendas de um usuário para uma mesma vacina em séries. Uma nova série começa quando
     * aparece outra dose 1 e a série atual já tem uma dose 1 não cancelada (ex.: reforço anos depois).
     *
     * @param agendas ordenadas por data
     */
    static List<List<Agenda>> separarSeries(List<Agenda> agendas) {
        List<List<Agenda>> series = new ArrayList<>();
        List<Agenda> atual = new ArrayList<>();
        for (Agenda a : agendas) {
            boolean jaTemDose1 = atual.stream()
                    .anyMatch(o -> o.getDose() == 1 && o.getSituacao() != Situacao.CANCELADO);
            if (a.getDose() == 1 && jaTemDose1) {
                series.add(atual);
                atual = new ArrayList<>();
            }
            atual.add(a);
        }
        if (!atual.isEmpty()) {
            series.add(atual);
        }
        return series;
    }

    /** Série mais recente do usuário para a vacina, ou {@code null}. */
    public SerieDoses serieAtual(Integer usuarioId, Integer vacinaId) {
        List<List<Agenda>> series = separarSeries(agendaDAO.listarSerie(usuarioId, vacinaId));
        return series.isEmpty() ? null : new SerieDoses(series.get(series.size() - 1));
    }

    /** Doses AGENDADO atrasadas e dos próximos {@link #DIAS_PROXIMAS} dias, por data. */
    public List<Agenda> proximasEAtrasadas() {
        return agendaDAO.listarAgendadasAte(LocalDate.now().plusDays(DIAS_PROXIMAS));
    }

    /**
     * "Recalcular próximas doses": reprograma as doses AGENDADO restantes da série a partir da data real
     * de aplicação da última dose realizada (mesma regra da seção 5: data base + k × intervalo).
     * Se a data calculada para a próxima dose já tiver passado, a série recomeça em hoje.
     *
     * @return quantidade de agendas reprogramadas
     */
    public int recalcular(Integer usuarioId, Integer vacinaId) throws RegraNegocioException {
        SerieDoses serie = serieAtual(usuarioId, vacinaId);
        if (serie == null || serie.getProxima() == null) {
            throw new RegraNegocioException("Não há doses agendadas para recalcular nesta série.");
        }
        if (serie.getUltimaRealizada() == null) {
            throw new RegraNegocioException("Nenhuma dose desta série foi realizada ainda; não há data de "
                    + "aplicação para recalcular. Se a dose estiver atrasada, cancele-a e reagende.");
        }
        Agenda proxima = serie.getProxima();
        LocalDate inicio = serie.getDataMinimaProxima();
        LocalDate hoje = LocalDate.now();
        if (inicio.isBefore(hoje)) {
            inicio = hoje;
        }

        Map<Integer, LocalDate> novasDatas = new LinkedHashMap<>();
        for (Agenda a : serie.getDoses()) {
            if (a.getSituacao() == Situacao.AGENDADO && a.getDose() >= proxima.getDose()) {
                novasDatas.put(a.getId(), serie.getVacina().somarIntervalos(inicio, a.getDose() - proxima.getDose()));
            }
        }
        return agendaDAO.reprogramar(novasDatas);
    }

    /**
     * Reagenda uma dose cancelada: cria nova agenda com o mesmo número de dose, numa data que respeite
     * a data mínima (última dose realizada + intervalo) e não esteja no passado.
     */
    public Agenda reagendar(Integer agendaCanceladaId, LocalDate data, String hora) throws RegraNegocioException {
        Agenda cancelada = agendaDAO.buscarCompleta(agendaCanceladaId);
        if (cancelada == null) {
            throw new RegraNegocioException("Agenda não encontrada.");
        }
        if (cancelada.getSituacao() != Situacao.CANCELADO) {
            throw new RegraNegocioException("Só é possível reagendar uma dose cancelada.");
        }
        SerieDoses serie = separarSeries(agendaDAO.listarSerie(cancelada.getUsuario().getId(),
                cancelada.getVacina().getId())).stream()
                .filter(s -> s.stream().anyMatch(a -> a.getId().equals(cancelada.getId())))
                .map(SerieDoses::new)
                .findFirst()
                .orElseThrow(() -> new RegraNegocioException("Série da agenda não encontrada."));
        if (!serie.isReagendavel(cancelada)) {
            throw new RegraNegocioException("A dose " + cancelada.getDose() + " já foi reagendada ou realizada.");
        }

        List<String> erros = new ArrayList<>();
        LocalDate minima = serie.dataMinima(cancelada.getDose());
        if (data == null) {
            erros.add("Informe a nova data.");
        } else if (data.isBefore(LocalDate.now())) {
            erros.add("A nova data não pode estar no passado.");
        } else if (minima != null && data.isBefore(minima)) {
            erros.add("A dose " + cancelada.getDose() + " só pode ser aplicada a partir de "
                    + DatasUtil.formatar(minima) + " (dose anterior + intervalo).");
        }
        if (!AgendamentoService.horaValida(hora)) {
            erros.add("Informe a hora no formato HH:mm (00:00 a 23:59).");
        }
        if (!erros.isEmpty()) {
            throw new RegraNegocioException(erros);
        }

        Agenda nova = new Agenda();
        nova.setUsuario(cancelada.getUsuario());
        nova.setVacina(cancelada.getVacina());
        nova.setDose(cancelada.getDose());
        nova.setData(data);
        nova.setHora(hora);
        nova.setSituacao(Situacao.AGENDADO);
        nova.setObservacoes("Reagendamento da dose " + cancelada.getDose() + " cancelada em "
                + cancelada.getDataSituacaoFormatada() + ".");
        agendaDAO.salvarSerie(List.of(nova));
        // salvarSerie troca usuário/vacina por referências do EntityManager (já fechado): volta os objetos carregados
        nova.setUsuario(cancelada.getUsuario());
        nova.setVacina(cancelada.getVacina());
        return nova;
    }
}
