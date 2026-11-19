package br.ufg.vacinacao.dao;

import br.ufg.vacinacao.model.Agenda;
import br.ufg.vacinacao.model.Situacao;
import br.ufg.vacinacao.model.Usuario;
import br.ufg.vacinacao.model.Vacina;

import java.util.List;

public class AgendaDAO extends GenericDAO<Agenda> {

    /** Base das consultas que a JSP percorre: usuário e vacina já carregados (evita LazyInitialization e N+1). */
    private static final String SELECT_COMPLETO =
            "SELECT a FROM Agenda a JOIN FETCH a.usuario JOIN FETCH a.vacina ";

    public AgendaDAO() {
        super(Agenda.class, "e.data, e.hora");
    }

    /** Todas as agendas, por data e hora. */
    public List<Agenda> listarTodas() {
        return consultar(em -> em.createQuery(
                        SELECT_COMPLETO + "ORDER BY a.data, a.hora, a.usuario.nome", Agenda.class)
                .getResultList());
    }

    /** Agenda com usuário e vacina carregados, ou {@code null}. */
    public Agenda buscarCompleta(Integer id) {
        if (id == null) {
            return null;
        }
        return consultar(em -> em.createQuery(SELECT_COMPLETO + "WHERE a.id = :id", Agenda.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    /** Todas as doses desta vacina para este usuário, em ordem de dose e data. */
    public List<Agenda> listarSerie(Integer usuarioId, Integer vacinaId) {
        return consultar(em -> em.createQuery(SELECT_COMPLETO
                        + "WHERE a.usuario.id = :u AND a.vacina.id = :v ORDER BY a.data, a.dose", Agenda.class)
                .setParameter("u", usuarioId)
                .setParameter("v", vacinaId)
                .getResultList());
    }

    /** {@code true} se o usuário já tem alguma dose AGENDADO desta vacina (série em aberto). */
    public boolean existeSerieAberta(Integer usuarioId, Integer vacinaId) {
        return consultar(em -> em.createQuery("SELECT COUNT(a) FROM Agenda a "
                        + "WHERE a.usuario.id = :u AND a.vacina.id = :v AND a.situacao = :s", Long.class)
                .setParameter("u", usuarioId)
                .setParameter("v", vacinaId)
                .setParameter("s", Situacao.AGENDADO)
                .getSingleResult() > 0);
    }

    /** Grava todas as agendas de uma série numa única transação (tudo ou nada). */
    public List<Agenda> salvarSerie(List<Agenda> agendas) {
        return emTransacao(em -> {
            for (Agenda agenda : agendas) {
                agenda.setUsuario(em.getReference(Usuario.class, agenda.getUsuario().getId()));
                agenda.setVacina(em.getReference(Vacina.class, agenda.getVacina().getId()));
                em.persist(agenda);
            }
            return agendas;
        });
    }

    /**
     * Exclui a agenda informada e as doses seguintes da mesma série que ainda estão AGENDADO.
     *
     * @return quantidade de agendas excluídas
     */
    public int excluirDaDoseEmDiante(Agenda base) {
        return emTransacao(em -> em.createQuery("DELETE FROM Agenda a "
                        + "WHERE a.usuario.id = :u AND a.vacina.id = :v AND a.situacao = :s AND a.dose >= :d")
                .setParameter("u", base.getUsuario().getId())
                .setParameter("v", base.getVacina().getId())
                .setParameter("s", Situacao.AGENDADO)
                .setParameter("d", base.getDose())
                .executeUpdate());
    }
}
