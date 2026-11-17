package br.ufg.vacinacao.dao;

import br.ufg.vacinacao.model.Vacina;

public class VacinaDAO extends GenericDAO<Vacina> {

    public VacinaDAO() {
        super(Vacina.class, "e.titulo");
    }

    /** Quantas agendas usam esta vacina (impede a exclusão). */
    public long contarAgendas(Integer vacinaId) {
        return contarNativo("SELECT COUNT(*) FROM agendas WHERE vacina_id = ?1", vacinaId);
    }
}
