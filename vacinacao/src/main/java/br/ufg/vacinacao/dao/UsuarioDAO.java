package br.ufg.vacinacao.dao;

import br.ufg.vacinacao.model.Alergia;
import br.ufg.vacinacao.model.Usuario;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsuarioDAO extends GenericDAO<Usuario> {

    public UsuarioDAO() {
        super(Usuario.class, "e.nome");
    }

    /**
     * Lista os usuários (com as alergias já carregadas, para a JSP), filtrando por parte do nome.
     *
     * @param busca trecho do nome; {@code null} lista todos
     */
    public List<Usuario> listar(String busca) {
        return consultar(em -> {
            String jpql = "SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.alergias"
                    + (busca == null ? "" : " WHERE LOWER(u.nome) LIKE :busca ESCAPE '!'")
                    + " ORDER BY u.nome";
            var query = em.createQuery(jpql, Usuario.class);
            if (busca != null) {
                query.setParameter("busca", "%" + escaparLike(busca.toLowerCase()) + "%");
            }
            return query.getResultList();
        });
    }

    /** Usuário com as alergias carregadas, ou {@code null}. */
    public Usuario buscarComAlergias(Integer id) {
        if (id == null) {
            return null;
        }
        return consultar(em -> em.createQuery(
                        "SELECT u FROM Usuario u LEFT JOIN FETCH u.alergias WHERE u.id = :id", Usuario.class)
                .setParameter("id", id)
                .getResultStream()
                .findFirst()
                .orElse(null));
    }

    /**
     * Inclui o usuário vinculando as alergias informadas, numa única transação.
     *
     * @throws IllegalArgumentException se algum id de alergia não existir
     */
    public Usuario salvar(Usuario usuario, Collection<Integer> alergiaIds) {
        return emTransacao(em -> {
            usuario.getAlergias().clear();
            for (Integer alergiaId : alergiaIds) {
                Alergia alergia = em.find(Alergia.class, alergiaId);
                if (alergia == null) {
                    throw new IllegalArgumentException("Alergia inexistente: " + alergiaId);
                }
                usuario.getAlergias().add(alergia);
            }
            if (usuario.getId() == null) {
                em.persist(usuario);
                return usuario;
            }
            return em.merge(usuario);
        });
    }

    /** Quantas agendas o usuário tem (são removidas junto com ele - cascade REMOVE). */
    public long contarAgendas(Integer usuarioId) {
        return consultar(em -> em.createQuery(
                        "SELECT COUNT(a) FROM Agenda a WHERE a.usuario.id = :id", Long.class)
                .setParameter("id", usuarioId)
                .getSingleResult());
    }

    /** Mapa id do usuário -> quantidade de agendas (só usuários que têm agendas). */
    public Map<Integer, Long> contarAgendasPorUsuario() {
        return consultar(em -> {
            Map<Integer, Long> mapa = new HashMap<>();
            List<Object[]> linhas = em.createQuery(
                    "SELECT a.usuario.id, COUNT(a) FROM Agenda a GROUP BY a.usuario.id", Object[].class)
                    .getResultList();
            for (Object[] linha : linhas) {
                mapa.put((Integer) linha[0], (Long) linha[1]);
            }
            return mapa;
        });
    }

    private static String escaparLike(String texto) {
        return texto.replace("!", "!!").replace("%", "!%").replace("_", "!_");
    }
}
