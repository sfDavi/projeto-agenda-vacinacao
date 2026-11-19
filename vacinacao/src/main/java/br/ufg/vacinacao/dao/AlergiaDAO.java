package br.ufg.vacinacao.dao;

import br.ufg.vacinacao.model.Alergia;

public class AlergiaDAO extends GenericDAO<Alergia> {

    public AlergiaDAO() {
        super(Alergia.class, "e.nome");
    }

    /** Verifica se já existe alergia com o mesmo nome (sem diferenciar maiúsculas). */
    public boolean existeNome(String nome) {
        return consultar(em -> em.createQuery(
                        "SELECT COUNT(a) FROM Alergia a WHERE LOWER(a.nome) = LOWER(:nome)", Long.class)
                .setParameter("nome", nome)
                .getSingleResult() > 0);
    }

    /** Quantos usuários têm esta alergia (relacionamento N:N Usuario.alergias). */
    public long contarUsuarios(Integer alergiaId) {
        return consultar(em -> em.createQuery(
                        "SELECT COUNT(u) FROM Usuario u JOIN u.alergias a WHERE a.id = :id", Long.class)
                .setParameter("id", alergiaId)
                .getSingleResult());
    }
}
