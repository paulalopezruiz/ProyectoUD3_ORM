package org.example;

import org.example.dao.ProveedorDao;
import org.example.models.Proveedor;
import org.example.utils.ContextoSeguridad;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProveedorDaoTest {

    @BeforeEach
    void iniciarContextoSeguridad() {
        ContextoSeguridad.iniciar("TEST_JUNIT");
    }

    @AfterEach
    void cerrarContextoSeguridad() {
        ContextoSeguridad.cerrar();
    }

    @Test
    void crearBuscarModificarYEliminarProveedor() {

        String codigo =
                "TEST_PR_"
                        + UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        try (
                ProveedorDao dao =
                        new ProveedorDao()
        ) {

            Proveedor proveedor =
                    new Proveedor(
                            codigo,
                            "Paula",
                            "Lopez",
                            "Calle Test 1"
                    );

            dao.create(proveedor);

            Proveedor encontrado =
                    dao.find(codigo);

            assertNotNull(encontrado);

            assertEquals(
                    "Paula",
                    encontrado.getNombre()
            );

            String estadoAnterior =
                    encontrado.toString();

            encontrado.setNombre(
                    "Laura"
            );

            dao.update(
                    encontrado,
                    estadoAnterior
            );

            Proveedor modificado =
                    dao.find(codigo);

            assertNotNull(modificado);

            assertEquals(
                    "Laura",
                    modificado.getNombre()
            );

            dao.delete(
                    modificado
            );

            assertNull(
                    dao.find(codigo)
            );
        }
    }
}