package org.example;

import org.example.dao.EstadisticasDao;
import org.example.dao.GestionDao;
import org.example.dao.PiezaDao;
import org.example.dao.ProveedorDao;
import org.example.dao.ProyectoDao;
import org.example.models.Gestion;
import org.example.models.Pieza;
import org.example.models.Proveedor;
import org.example.models.Proyecto;
import org.example.utils.ContextoSeguridad;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class EstadisticasDaoTest {

    @BeforeEach
    void iniciarContextoSeguridad() {
        ContextoSeguridad.iniciar("TEST_JUNIT");
    }

    @AfterEach
    void cerrarContextoSeguridad() {
        ContextoSeguridad.cerrar();
    }

    @Test
    void comprobarResumenProyecto() {

        String sufijo =
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        String codigoProveedor =
                "TEST_PR_" + sufijo;

        String codigoPieza =
                "TEST_PI_" + sufijo;

        String codigoProyecto =
                "TEST_PY_" + sufijo;

        try (
                ProveedorDao proveedorDao =
                        new ProveedorDao();

                PiezaDao piezaDao =
                        new PiezaDao();

                ProyectoDao proyectoDao =
                        new ProyectoDao();

                GestionDao gestionDao =
                        new GestionDao();

                EstadisticasDao estadisticasDao =
                        new EstadisticasDao()
        ) {

            Proveedor proveedor =
                    new Proveedor(
                            codigoProveedor,
                            "Proveedor",
                            "Test",
                            "Dirección Test"
                    );

            Pieza pieza =
                    new Pieza(
                            codigoPieza,
                            "Pieza Estadistica",
                            10.0,
                            "Prueba"
                    );

            Proyecto proyecto =
                    new Proyecto(
                            codigoProyecto,
                            "Proyecto Estadistica",
                            "Vitoria"
                    );

            proveedorDao.create(
                    proveedor
            );

            piezaDao.create(
                    pieza
            );

            proyectoDao.create(
                    proyecto
            );

            Gestion gestion =
                    new Gestion(
                            LocalDate.now(),
                            5,
                            proveedor,
                            pieza,
                            proyecto
                    );

            gestionDao.create(
                    gestion
            );

            assertNotNull(
                    gestion.getId()
            );

            long proveedores =
                    estadisticasDao
                            .contarProveedoresProyecto(
                                    codigoProyecto
                            );

            long piezas =
                    estadisticasDao
                            .contarPiezasProyecto(
                                    codigoProyecto
                            );

            double coste =
                    estadisticasDao
                            .costeTotalProyecto(
                                    codigoProyecto
                            );

            assertEquals(
                    1,
                    proveedores
            );

            assertEquals(
                    1,
                    piezas
            );

            assertEquals(
                    50.0,
                    coste,
                    0.01
            );

            gestionDao.delete(
                    gestionDao.find(
                            gestion.getId()
                    )
            );

            proveedorDao.delete(
                    proveedorDao.find(
                            codigoProveedor
                    )
            );

            piezaDao.delete(
                    piezaDao.find(
                            codigoPieza
                    )
            );

            proyectoDao.delete(
                    proyectoDao.find(
                            codigoProyecto
                    )
            );
        }
    }
}