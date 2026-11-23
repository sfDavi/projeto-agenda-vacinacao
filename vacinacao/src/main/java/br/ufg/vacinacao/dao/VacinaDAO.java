package br.ufg.vacinacao.dao;

import br.ufg.vacinacao.model.Alergia;
import br.ufg.vacinacao.model.Vacina;

import java.util.Collection;
import java.util.List;

public class VacinaDAO extends GenericDAO<Vacina> {

    public VacinaDAO() {
        super(Vacina.class, "e.titulo");
    }

    /** Todas as vacinas com as contraindicações (7A) já carregadas, por título. */
    public List<Vacina> listarComContraindicacoes() {
        return consultar(em -> em.createQuery(
                        "SELECT DISTINCT v FROM Vacina v LEFT JOIN FETCH v.contraindicacoes ORDER BY v.titulo",
                        Vacina.class)
                .getResultList());
    }

    /** Vacina com as contraindicações carregadas, ou {@code null}. */
    public Vacina buscarComContraindicacoes(Integer id) {
        if (id == null) {
            return null;
        }
        return consultar(em -> em.createQuery(
                        "SELECT v FROM Vacina v LEFT JOIN FETCH v.contraindicacoes WHERE v.id = :id", Vacina.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    /**
     * Inclui a vacina vinculando as contraindicações informadas, numa única transação.
     *
     * @throws IllegalArgumentException se algum id de alergia não existir
     */
    public Vacina salvar(Vacina vacina, Collection<Integer> alergiaIds) {
        return emTransacao(em -> {
            vacina.getContraindicacoes().clear();
            for (Integer alergiaId : alergiaIds) {
                Alergia alergia = em.find(Alergia.class, alergiaId);
                if (alergia == null) {
                    throw new IllegalArgumentException("Alergia inexistente: " + alergiaId);
                }
                vacina.getContraindicacoes().add(alergia);
            }
            if (vacina.getId() == null) {
                em.persist(vacina);
                return vacina;
            }
            return em.merge(vacina);
        });
    }

    /** Quantas agendas usam esta vacina (impede a exclusão). */
    public long contarAgendas(Integer vacinaId) {
        return consultar(em -> em.createQuery(
                        "SELECT COUNT(a) FROM Agenda a WHERE a.vacina.id = :id", Long.class)
                .setParameter("id", vacinaId)
                .getSingleResult());
    }
}
