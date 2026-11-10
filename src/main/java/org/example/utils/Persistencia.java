package org.example.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

public final class Persistencia {

    private static final EntityManagerFactory FACTORY = crearFactory();

    private Persistencia() {
    }

    public static EntityManagerFactory factory() {
        return FACTORY;
    }

    public static EntityManager crearEntityManager() {
        return FACTORY.createEntityManager();
    }

    private static EntityManagerFactory crearFactory() {
        String password = valorConfiguracion("DB_PASSWORD", "db.password", null);
        Map<String, Object> propiedades = new HashMap<>();

        if (password == null || password.isBlank()) {
            if (!h2Disponible()) {
                throw new IllegalStateException(
                        "Configura DB_PASSWORD o la propiedad JVM -Ddb.password para conectar con MariaDB.");
            }

            propiedades.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
            propiedades.put("jakarta.persistence.jdbc.url",
                    "jdbc:h2:mem:proyecto_test;MODE=MariaDB;DB_CLOSE_DELAY=-1");
            propiedades.put("jakarta.persistence.jdbc.user", "sa");
            propiedades.put("jakarta.persistence.jdbc.password", "");
            propiedades.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
            propiedades.put("hibernate.hbm2ddl.auto", "create-drop");
        } else {
            propiedades.put("hibernate.connection.url", valorConfiguracion("DB_URL", "db.url",
                    "jdbc:mariadb://localhost:33061/proyecto_ud3"));
            propiedades.put("hibernate.connection.username", valorConfiguracion("DB_USERNAME", "db.username",
                    "root"));
            propiedades.put("hibernate.connection.password", password);
        }
        return Persistence.createEntityManagerFactory("default", propiedades);
    }

    private static boolean h2Disponible() {
        try {
            Class.forName("org.h2.Driver");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static String valorConfiguracion(String variableEntorno, String propiedadJvm,
                                             String valorPredeterminado) {
        String valor = System.getenv(variableEntorno);
        if (valor == null || valor.isBlank()) {
            valor = System.getProperty(propiedadJvm);
        }
        return valor == null || valor.isBlank() ? valorPredeterminado : valor;
    }
}
