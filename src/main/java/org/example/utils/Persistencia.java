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
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Configura DB_PASSWORD o la propiedad JVM -Ddb.password para conectar con MariaDB.");
        }

        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("hibernate.connection.url", valorConfiguracion("DB_URL", "db.url",
                "jdbc:mariadb://localhost:33061/proyecto_ud3"));
        propiedades.put("hibernate.connection.username", valorConfiguracion("DB_USERNAME", "db.username",
                "root"));
        propiedades.put("hibernate.connection.password", password);
        return Persistence.createEntityManagerFactory("default", propiedades);
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
