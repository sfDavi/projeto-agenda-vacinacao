package br.ufg.vacinacao.dao;

import br.ufg.vacinacao.model.Alergia;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlergiaDAO extends GenericDAO<Alergia> {

    public AlergiaDAO() {
        super(Alergia.class, "e.nome");
    }

    /** Verifica se já existe alergia com o mesmo nome (sem diferenciar maiúsculas). */
    public boolean existeNome(String nome) {
        return existeNome(nome, null);
    }

    /** Como {@link #existeNome(String)}, ignorando a própria alergia ({@code ignorarId}) na edição. */
    public boolean existeNome(String nome, Integer ignorarId) {
        return consultar(em -> em.createQuery(
                        "SELECT COUNT(a) FROM Alergia a WHERE LOWER(a.nome) = LOWER(:nome)"
                                + " AND (:ignorar IS NULL OR a.id <> :ignorar)", Long.class)
                .setParameter("nome", nome)
                .setParameter("ignorar", ignorarId)
                .getSingleResult() > 0);
    }

    /** Quantos usuários têm esta alergia (relacionamento N:N Usuario.alergias). */
    public long contarUsuarios(Integer alergiaId) {
        return consultar(em -> em.createQuery(
                        "SELECT COUNT(u) FROM Usuario u JOIN u.alergias a WHERE a.id = :id", Long.class)
                .setParameter("id", alergiaId)
                .getSingleResult());
    }

    /** 7A: alergias do usuário que são contraindicação da vacina (interseção), por nome. */
    public List<Alergia> listarConflitos(Integer usuarioId, Integer vacinaId) {
        return consultar(em -> em.createQuery(
                        "SELECT a FROM Vacina v JOIN v.contraindicacoes a "
                                + "WHERE v.id = :vacina AND a IN "
                                + "(SELECT ua FROM Usuario u JOIN u.alergias ua WHERE u.id = :usuario) "
                                + "ORDER BY a.nome", Alergia.class)
                .setParameter("vacina", vacinaId)
                .setParameter("usuario", usuarioId)
                .getResultList());
    }

    /** 7A: para cada vacina contraindicada ao usuário, os nomes das alergias em conflito. */
    public Map<Integer, List<String>> listarConflitosPorVacina(Integer usuarioId) {
        return consultar(em -> {
            Map<Integer, List<String>> mapa = new LinkedHashMap<>();
            List<Object[]> linhas = em.createQuery(
                            "SELECT v.id, a.nome FROM Vacina v JOIN v.contraindicacoes a "
                                    + "WHERE a IN (SELECT ua FROM Usuario u JOIN u.alergias ua WHERE u.id = :usuario) "
                                    + "ORDER BY v.id, a.nome", Object[].class)
                    .setParameter("usuario", usuarioId)
                    .getResultList();
            for (Object[] linha : linhas) {
                mapa.computeIfAbsent((Integer) linha[0], k -> new ArrayList<>()).add((String) linha[1]);
            }
            return mapa;
        });
    }

    /** Quantas vacinas têm esta alergia como contraindicação (7A). */
    public long contarVacinas(Integer alergiaId) {
        return consultar(em -> em.createQuery(
                        "SELECT COUNT(v) FROM Vacina v JOIN v.contraindicacoes a WHERE a.id = :id", Long.class)
                .setParameter("id", alergiaId)
                .getSingleResult());
    }
}
