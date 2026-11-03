package org.example;

import org.example.dao.PiezaDao;
import org.example.models.Pieza;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PiezaDaoTest {

    @Test
    void crearYBuscarPiezaPorNombre() {

        PiezaDao dao = new PiezaDao();

        String codigo = "TEST_PI_01";

        Pieza pieza = new Pieza(
                codigo,
                "PiezaTestJUnit",
                25.50,
                "Pieza para prueba");

        dao.create(pieza);

        List<Pieza> resultado = dao.buscarPorNombre("PiezaTestJUnit");

        assertFalse(resultado.isEmpty());

        boolean encontrada = resultado.stream().anyMatch(p -> p.getCodigo().equals(codigo));

        assertTrue(encontrada);

        dao.delete(dao.find(codigo));
    }
}