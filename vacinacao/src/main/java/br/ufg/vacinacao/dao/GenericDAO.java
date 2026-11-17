package br.ufg.vacinacao.dao;

import br.ufg.vacinacao.util.JPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.function.Function;

/**
 * Operações básicas de persistência. Cada método abre o seu próprio {@link EntityManager}
 * e, quando altera dados, usa uma transação explícita (begin / commit / rollback).
 */
public abstract class GenericDAO<T> {

    protected final Class<T> classe;
    private final String ordenacao;

    /**
     * @param classe    entidade gerenciada
     * @param ordenacao trecho JPQL usado em {@link #listarTodos()} (ex.: {@code "e.nome"})
     */
    protected GenericDAO(Class<T> classe, String ordenacao) {
        this.classe = classe;
        this.ordenacao = ordenacao;
    }

    /** Inclui (id nulo) ou atualiza (id preenchido) a entidade. */
    public T salvar(T entidade) {
        return emTransacao(em -> {
            Object id = em.getEntityManagerFactory().getPersistenceUnitUtil().getIdentifier(entidade);
            if (id == null) {
                em.persist(entidade);
                return entidade;
            }
            return em.merge(entidade);
        });
    }

    public T buscarPorId(Integer id) {
        if (id == null) {
            return null;
        }
        return consultar(em -> em.find(classe, id));
    }

    public List<T> listarTodos() {
        String jpql = "SELECT e FROM " + classe.getSimpleName() + " e ORDER BY " + ordenacao;
        return consultar(em -> em.createQuery(jpql, classe).getResultList());
    }

    /** Remove a entidade; retorna {@code false} se ela não existir. */
    public boolean excluir(Integer id) {
        return emTransacao(em -> {
            T entidade = em.find(classe, id);
            if (entidade == null) {
                return false;
            }
            em.remove(entidade);
            em.flush(); // dispara já aqui eventual violação de chave estrangeira
            return true;
        });
    }

    /** Executa uma consulta (somente leitura) com um EntityManager novo. */
    protected <R> R consultar(Function<EntityManager, R> operacao) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            return operacao.apply(em);
        } finally {
            em.close();
        }
    }

    /** Executa a operação dentro de uma transação: tudo ou nada. */
    protected <R> R emTransacao(Function<EntityManager, R> operacao) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            R resultado = operacao.apply(em);
            tx.commit();
            return resultado;
        } catch (RuntimeException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /** Conta linhas de uma consulta nativa {@code SELECT COUNT(*) ... WHERE x = ?1}. */
    protected long contarNativo(String sql, Object parametro) {
        return consultar(em -> ((Number) em.createNativeQuery(sql)
                .setParameter(1, parametro)
                .getSingleResult()).longValue());
    }
}
