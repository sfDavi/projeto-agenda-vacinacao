package br.ufg.vacinacao.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Mantém um único {@link EntityManagerFactory} para toda a aplicação.
 * <p>
 * Cada operação de serviço deve abrir o seu próprio {@link EntityManager}
 * com {@link #getEntityManager()} e fechá-lo ao final.
 * <p>
 * A conexão pode ser configurada por variáveis de ambiente (ou propriedades de sistema
 * de mesmo nome), com padrão {@code root} / senha vazia:
 * <ul>
 *   <li>{@code VACINACAO_DB_URL}</li>
 *   <li>{@code VACINACAO_DB_USER}</li>
 *   <li>{@code VACINACAO_DB_PASSWORD}</li>
 * </ul>
 */
public final class JPAUtil {

    private static final String PERSISTENCE_UNIT = "vacinacaoPU";

    private static volatile EntityManagerFactory emf;

    private JPAUtil() {
    }

    /** Cria o factory (se ainda não existir). Chamado no startup pelo JpaListener. */
    public static EntityManagerFactory getEntityManagerFactory() {
        EntityManagerFactory local = emf;
        if (local == null) {
            synchronized (JPAUtil.class) {
                local = emf;
                if (local == null) {
                    local = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, sobrescritas());
                    emf = local;
                }
            }
        }
        return local;
    }

    public static EntityManager getEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /** Fecha o factory. Chamado no undeploy pelo JpaListener. */
    public static synchronized void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = null;
    }

    private static Map<String, Object> sobrescritas() {
        Map<String, Object> props = new HashMap<>();
        colocarSeDefinida(props, "jakarta.persistence.jdbc.url", "VACINACAO_DB_URL");
        colocarSeDefinida(props, "jakarta.persistence.jdbc.user", "VACINACAO_DB_USER");
        colocarSeDefinida(props, "jakarta.persistence.jdbc.password", "VACINACAO_DB_PASSWORD");
        return props;
    }

    private static void colocarSeDefinida(Map<String, Object> props, String chave, String variavel) {
        String valor = System.getProperty(variavel);
        if (valor == null) {
            valor = System.getenv(variavel);
        }
        if (valor != null) {
            props.put(chave, valor);
        }
    }
}
