package br.ufg.vacinacao.web;

import br.ufg.vacinacao.util.JPAUtil;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Cria o EntityManagerFactory no deploy (o Hibernate valida o esquema já no startup,
 * por causa de hbm2ddl.auto=validate) e o fecha no undeploy.
 */
@WebListener
public class JpaListener implements ServletContextListener {

    private static final Logger LOG = Logger.getLogger(JpaListener.class.getName());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            JPAUtil.getEntityManagerFactory();
            LOG.info("EntityManagerFactory criado; esquema validado pelo Hibernate.");
        } catch (RuntimeException e) {
            // Não derruba o deploy: a página mostrará o erro na primeira consulta.
            LOG.log(Level.SEVERE, "Falha ao iniciar o JPA/Hibernate. Verifique o MySQL e o schema.", e);
            sce.getServletContext().setAttribute("erroJpa", causaRaiz(e).getMessage());
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        JPAUtil.close();
        LOG.info("EntityManagerFactory fechado.");
    }

    private static Throwable causaRaiz(Throwable t) {
        Throwable atual = t;
        while (atual.getCause() != null && atual.getCause() != atual) {
            atual = atual.getCause();
        }
        return atual;
    }
}
