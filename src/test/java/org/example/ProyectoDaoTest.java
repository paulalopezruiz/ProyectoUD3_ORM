package org.example;

import org.example.dao.ProyectoDao;
import org.example.models.Proyecto;
import org.example.utils.ContextoSeguridad;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProyectoDaoTest {

    @BeforeEach
    void iniciarContextoSeguridad() {
        ContextoSeguridad.iniciar("TEST_JUNIT");
    }

    @AfterEach
    void cerrarContextoSeguridad() {
        ContextoSeguridad.cerrar();
    }

    @Test
    void crearYBuscarProyectoPorCiudad() {

        String codigo =
                "TEST_PY_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        try (
                ProyectoDao dao =
                        new ProyectoDao()
        ) {

            Proyecto proyecto =
                    new Proyecto(
                            codigo,
                            "Proyecto Test",
                            "VitoriaTest"
                    );

            dao.create(proyecto);

            List<Proyecto> resultado =
                    dao.buscarPorCiudad(
                            "VitoriaTest"
                    );

            assertFalse(
                    resultado.isEmpty()
            );

            boolean encontrado =
                    resultado
                            .stream()
                            .anyMatch(
                                    p ->
                                            p.getCodigo()
                                                    .equals(codigo)
                            );

            assertTrue(
                    encontrado
            );

            dao.delete(
                    dao.find(codigo)
            );
        }
    }
}