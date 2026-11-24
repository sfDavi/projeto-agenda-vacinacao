package br.ufg.vacinacao.dao;

import br.ufg.vacinacao.model.Agenda;
import br.ufg.vacinacao.model.Situacao;
import br.ufg.vacinacao.model.Usuario;
import br.ufg.vacinacao.model.Vacina;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * Listagem com filtros opcionais (Item 3). Parâmetros nulos são ignorados.
     *
     * @param situacao  situação exata
     * @param de        data mínima (inclusive)
     * @param ate       data máxima (inclusive)
     * @param usuarioId usuário
     * @param vacinaId  vacina
     */
    public List<Agenda> filtrar(Situacao situacao, LocalDate de, LocalDate ate, Integer usuarioId, Integer vacinaId) {
        List<String> condicoes = new ArrayList<>();
        if (situacao != null) {
            condicoes.add("a.situacao = :situacao");
        }
        if (de != null) {
            condicoes.add("a.data >= :de");
        }
        if (ate != null) {
            condicoes.add("a.data <= :ate");
        }
        if (usuarioId != null) {
            condicoes.add("a.usuario.id = :usuario");
        }
        if (vacinaId != null) {
            condicoes.add("a.vacina.id = :vacina");
        }
        String jpql = SELECT_COMPLETO
                + (condicoes.isEmpty() ? "" : "WHERE " + String.join(" AND ", condicoes) + " ")
                + "ORDER BY a.data, a.hora, a.usuario.nome";
        return consultar(em -> {
            TypedQuery<Agenda> query = em.createQuery(jpql, Agenda.class);
            if (situacao != null) {
                query.setParameter("situacao", situacao);
            }
            if (de != null) {
                query.setParameter("de", de);
            }
            if (ate != null) {
                query.setParameter("ate", ate);
            }
            if (usuarioId != null) {
                query.setParameter("usuario", usuarioId);
            }
            if (vacinaId != null) {
                query.setParameter("vacina", vacinaId);
            }
            return query.getResultList();
        });
    }

    /**
     * Agendas de um dia (Item 4), ordenadas por situação - Agendado, Realizado, Cancelado - e depois por hora.
     */
    public List<Agenda> listarDoDia(LocalDate dia) {
        return consultar(em -> em.createQuery(SELECT_COMPLETO + "WHERE a.data = :dia ORDER BY "
                        + "CASE a.situacao"
                        + " WHEN br.ufg.vacinacao.model.Situacao.AGENDADO THEN 0"
                        + " WHEN br.ufg.vacinacao.model.Situacao.REALIZADO THEN 1"
                        + " ELSE 2 END, a.hora, a.usuario.nome", Agenda.class)
                .setParameter("dia", dia)
                .getResultList());
    }

    /**
     * Grava a baixa (Item 5) se a agenda ainda estiver AGENDADO - a verificação é repetida dentro da
     * transação para que duas baixas simultâneas não se sobreponham.
     *
     * @return {@code false} se a agenda não existir ou já tiver baixa
     */
    public boolean registrarBaixa(Integer id, Situacao novaSituacao, LocalDate dataSituacao, String observacoes) {
        return emTransacao(em -> {
            Agenda agenda = em.find(Agenda.class, id, LockModeType.PESSIMISTIC_WRITE);
            if (agenda == null || agenda.getSituacao() != Situacao.AGENDADO) {
                return false;
            }
            agenda.setSituacao(novaSituacao);
            agenda.setDataSituacao(dataSituacao);
            agenda.setObservacoes(observacoes);
            return true;
        });
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

    /** Todas as agendas do usuário, por vacina e data (Item 6 - cartão de vacinação). */
    public List<Agenda> listarPorUsuario(Integer usuarioId) {
        return consultar(em -> em.createQuery(SELECT_COMPLETO
                        + "WHERE a.usuario.id = :u ORDER BY a.vacina.titulo, a.data, a.dose", Agenda.class)
                .setParameter("u", usuarioId)
                .getResultList());
    }

    /** Agendas ainda AGENDADO com data até {@code limite} - inclui as atrasadas. */
    public List<Agenda> listarAgendadasAte(LocalDate limite) {
        return consultar(em -> em.createQuery(SELECT_COMPLETO
                        + "WHERE a.situacao = :s AND a.data <= :limite ORDER BY a.data, a.hora, a.usuario.nome",
                        Agenda.class)
                .setParameter("s", Situacao.AGENDADO)
                .setParameter("limite", limite)
                .getResultList());
    }

    /**
     * Altera a data das agendas informadas (id -> nova data), numa única transação.
     * Só altera as que ainda estão AGENDADO.
     *
     * @return quantidade de agendas alteradas
     */
    public int reprogramar(Map<Integer, LocalDate> novasDatas) {
        return emTransacao(em -> {
            int alteradas = 0;
            for (Map.Entry<Integer, LocalDate> item : novasDatas.entrySet()) {
                Agenda agenda = em.find(Agenda.class, item.getKey(), LockModeType.PESSIMISTIC_WRITE);
                if (agenda != null && agenda.getSituacao() == Situacao.AGENDADO
                        && !agenda.getData().equals(item.getValue())) {
                    agenda.setData(item.getValue());
                    alteradas++;
                }
            }
            return alteradas;
        });
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

    /**
     * Grava todas as agendas de uma série numa única transação (tudo ou nada).
     * Atenção: usuário e vacina de cada agenda passam a ser referências (proxies) deste EntityManager.
     */
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
