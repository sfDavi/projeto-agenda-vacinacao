package br.ufg.vacinacao.service;

import br.ufg.vacinacao.dao.AgendaDAO;
import br.ufg.vacinacao.dao.UsuarioDAO;
import br.ufg.vacinacao.dao.VacinaDAO;
import br.ufg.vacinacao.model.Agenda;
import br.ufg.vacinacao.model.Situacao;
import br.ufg.vacinacao.model.Usuario;
import br.ufg.vacinacao.model.Vacina;
import br.ufg.vacinacao.util.DatasUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Regra de geração automática de agendas (seção 5 do plano).
 * <p>
 * Ao agendar a vacina V para o usuário U na data D e hora H, são criadas V.doses agendas:
 * a dose i (0-based) cai em {@code D + i × V.intervalo} unidades de {@code V.periodicidade}.
 * As datas são sempre calculadas a partir de D (e não somando em cadeia), para manter o
 * "mesmo dia/mês, se possível" do enunciado.
 */
public class AgendamentoService {

    private static final Pattern HORA = Pattern.compile("^([01]\\d|2[0-3]):[0-5]\\d$");
    private static final int TAMANHO_OBSERVACOES = 200;

    private final AgendaDAO agendaDAO = new AgendaDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final VacinaDAO vacinaDAO = new VacinaDAO();

    /** Datas de todas as doses de uma série que começa em {@code inicio} (não acessa o banco). */
    public static List<LocalDate> calcularDatas(Vacina vacina, LocalDate inicio) {
        List<LocalDate> datas = new ArrayList<>();
        datas.add(inicio);
        if (vacina.isDoseUnica() || vacina.getPeriodicidade() == null || vacina.getIntervalo() == null) {
            return datas;
        }
        for (int i = 1; i < vacina.getDoses(); i++) {
            datas.add(DatasUtil.somar(inicio, vacina.getPeriodicidade(), i * vacina.getIntervalo()));
        }
        return datas;
    }

    /** {@code true} se a hora estiver no formato HH:mm (00:00 a 23:59). */
    public static boolean horaValida(String hora) {
        return hora != null && HORA.matcher(hora).matches();
    }

    /**
     * Valida e grava, numa única transação, todas as agendas da série.
     *
     * @return as agendas criadas, em ordem de dose
     * @throws RegraNegocioException com as mensagens de validação
     */
    public List<Agenda> agendar(Integer usuarioId, Integer vacinaId, LocalDate data, String hora,
                                String observacoes) throws RegraNegocioException {
        List<String> erros = new ArrayList<>();

        Usuario usuario = usuarioDAO.buscarPorId(usuarioId);
        if (usuario == null) {
            erros.add("Selecione o usuário.");
        }
        Vacina vacina = vacinaDAO.buscarPorId(vacinaId);
        if (vacina == null) {
            erros.add("Selecione a vacina.");
        }
        if (data == null) {
            erros.add("Informe a data da primeira dose.");
        } else if (data.isBefore(LocalDate.now())) {
            erros.add("A data da primeira dose não pode estar no passado.");
        }
        if (!horaValida(hora)) {
            erros.add("Informe a hora no formato HH:mm (00:00 a 23:59).");
        }
        if (observacoes != null && observacoes.length() > TAMANHO_OBSERVACOES) {
            erros.add("As observações devem ter no máximo " + TAMANHO_OBSERVACOES + " caracteres.");
        }
        if (usuario != null && vacina != null && agendaDAO.existeSerieAberta(usuario.getId(), vacina.getId())) {
            erros.add(usuario.getNome() + " já tem doses de \"" + vacina.getTitulo()
                    + "\" agendadas. Dê baixa ou exclua essas doses antes de iniciar uma nova série.");
        }
        if (!erros.isEmpty()) {
            throw new RegraNegocioException(erros);
        }

        List<LocalDate> datas = calcularDatas(vacina, data);
        List<Agenda> agendas = new ArrayList<>();
        for (int i = 0; i < datas.size(); i++) {
            Agenda agenda = new Agenda();
            agenda.setUsuario(usuario);
            agenda.setVacina(vacina);
            agenda.setData(datas.get(i));
            agenda.setHora(hora);
            agenda.setSituacao(Situacao.AGENDADO);
            agenda.setDose(i + 1);
            agenda.setObservacoes(observacoes);
            agendas.add(agenda);
        }
        return agendaDAO.salvarSerie(agendas);
    }

    /**
     * "Dar baixa" (Item 5): marca a agenda como REALIZADO ou CANCELADO com {@code data_situacao = hoje}.
     * Só agendas AGENDADO podem receber baixa. Ao cancelar, o motivo (opcional) é acrescentado às observações.
     *
     * @param acao   {@code "REALIZADO"} ou {@code "CANCELADO"}
     * @param motivo motivo do cancelamento (ignorado ao realizar)
     * @return a agenda com a baixa registrada
     */
    public Agenda darBaixa(Integer agendaId, String acao, String motivo) throws RegraNegocioException {
        Situacao nova = "REALIZADO".equals(acao) ? Situacao.REALIZADO
                : "CANCELADO".equals(acao) ? Situacao.CANCELADO : null;
        if (nova == null) {
            throw new RegraNegocioException("Ação inválida: escolha Realizada ou Cancelar.");
        }
        Agenda agenda = agendaDAO.buscarCompleta(agendaId);
        if (agenda == null) {
            throw new RegraNegocioException("Agenda não encontrada.");
        }
        if (agenda.getSituacao() != Situacao.AGENDADO) {
            throw new RegraNegocioException("Esta agenda já foi " + agenda.getSituacao().getRotuloFeminino()
                    + " em " + agenda.getDataSituacaoFormatada() + "; não é possível dar baixa novamente.");
        }

        LocalDate hoje = LocalDate.now();
        String observacoes = agenda.getObservacoes();
        if (nova == Situacao.CANCELADO && motivo != null && !motivo.isBlank()) {
            String registro = "Cancelada em " + DatasUtil.formatar(hoje) + ": " + motivo.trim();
            observacoes = (observacoes == null || observacoes.isBlank()) ? registro : observacoes + " | " + registro;
            if (observacoes.length() > TAMANHO_OBSERVACOES) {
                int disponivel = Math.max(0, motivo.trim().length() - (observacoes.length() - TAMANHO_OBSERVACOES));
                throw new RegraNegocioException("O motivo é longo demais para caber nas observações desta agenda "
                        + "(use no máximo " + disponivel + " caracteres).");
            }
        }

        if (!agendaDAO.registrarBaixa(agenda.getId(), nova, hoje, observacoes)) {
            throw new RegraNegocioException("Esta agenda acabou de receber baixa por outra pessoa.");
        }
        agenda.setSituacao(nova);
        agenda.setDataSituacao(hoje);
        agenda.setObservacoes(observacoes);
        return agenda;
    }
}
