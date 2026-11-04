package org.example;

import org.example.dao.PiezaDao;
import org.example.models.Pieza;
import org.example.utils.ContextoSeguridad;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PiezaDaoTest {

    @BeforeEach
    void iniciarContextoSeguridad() {
        ContextoSeguridad.iniciar("TEST_JUNIT");
    }

    @AfterEach
    void cerrarContextoSeguridad() {
        ContextoSeguridad.cerrar();
    }

    @Test
    void crearYBuscarPiezaPorNombre() {

        String codigo =
                "TEST_PI_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        try (
                PiezaDao dao =
                        new PiezaDao()
        ) {

            Pieza pieza =
                    new Pieza(
                            codigo,
                            "PiezaTestJUnit",
                            25.50,
                            "Pieza para prueba"
                    );

            dao.create(pieza);

            List<Pieza> resultado =
                    dao.buscarPorNombre(
                            "PiezaTestJUnit"
                    );

            assertFalse(
                    resultado.isEmpty()
            );

            boolean encontrada =
                    resultado
                            .stream()
                            .anyMatch(
                                    p ->
                                            p.getCodigo()
                                                    .equals(codigo)
                            );

            assertTrue(
                    encontrada
            );

            dao.delete(
                    dao.find(codigo)
            );
        }
    }
}