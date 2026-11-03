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
        String password = System.getenv("DB_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalStateException(
                    "Configura la variable de entorno DB_PASSWORD para conectar con MariaDB.");
        }

        Map<String, Object> propiedades = new HashMap<>();
        propiedades.put("hibernate.connection.url", valorEntorno("DB_URL",
                "jdbc:mariadb://localhost:33061/proyecto_ud3"));
        propiedades.put("hibernate.connection.username", valorEntorno("DB_USERNAME", "root"));
        propiedades.put("hibernate.connection.password", password);
        return Persistence.createEntityManagerFactory("default", propiedades);
    }

    private static String valorEntorno(String nombre, String valorPredeterminado) {
        String valor = System.getenv(nombre);
        return valor == null || valor.isBlank() ? valorPredeterminado : valor;
    }
}
