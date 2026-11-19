package br.ufg.vacinacao.dao;

import br.ufg.vacinacao.model.Vacina;

public class VacinaDAO extends GenericDAO<Vacina> {

    public VacinaDAO() {
        super(Vacina.class, "e.titulo");
    }

    /** Quantas agendas usam esta vacina (impede a exclusão). */
    public long contarAgendas(Integer vacinaId) {
        return consultar(em -> em.createQuery(
                        "SELECT COUNT(a) FROM Agenda a WHERE a.vacina.id = :id", Long.class)
                .setParameter("id", vacinaId)
                .getSingleResult());
    }
}
