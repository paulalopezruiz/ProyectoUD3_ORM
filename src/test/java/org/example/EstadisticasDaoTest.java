package org.example;

import org.example.dao.*;
import org.example.models.*;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class EstadisticasDaoTest {

    @Test
    void comprobarResumenProyecto() {

        ProveedorDao proveedorDao = new ProveedorDao();
        PiezaDao piezaDao = new PiezaDao();
        ProyectoDao proyectoDao = new ProyectoDao();
        GestionDao gestionDao = new GestionDao();
        EstadisticasDao estadisticasDao = new EstadisticasDao();

        Proveedor proveedor = new Proveedor(
                "TEST_PR_EST",
                "Proveedor",
                "Test",
                "Dirección Test");

        Pieza pieza = new Pieza(
                "TEST_PI_EST",
                "Pieza Estadistica",
                10.0,
                "Prueba");

        Proyecto proyecto = new Proyecto(
                "TEST_PY_EST",
                "Proyecto Estadistica",
                "Vitoria");

        proveedorDao.create(proveedor);
        piezaDao.create(pieza);
        proyectoDao.create(proyecto);

        Gestion gestion = new Gestion(
                LocalDate.now(),
                5,
                proveedor,
                pieza,
                proyecto);

        gestionDao.create(gestion);

        long proveedores = estadisticasDao.contarProveedoresProyecto(
                        "TEST_PY_EST");

        long piezas = estadisticasDao.contarPiezasProyecto(
                        "TEST_PY_EST");

        double coste = estadisticasDao.costeTotalProyecto(
                        "TEST_PY_EST");

        assertEquals(1, proveedores);
        assertEquals(1, piezas);
        assertEquals(50.0, coste, 0.01);

        gestionDao.delete(gestionDao.find(gestion.getId()));

        proveedorDao.delete(proveedorDao.find("TEST_PR_EST"));

        piezaDao.delete(piezaDao.find("TEST_PI_EST"));

        proyectoDao.delete(proyectoDao.find("TEST_PY_EST"));
    }
}