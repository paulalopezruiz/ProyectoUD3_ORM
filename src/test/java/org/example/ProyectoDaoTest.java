package org.example;

import org.example.dao.ProyectoDao;
import org.example.models.Proyecto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProyectoDaoTest {

    @Test
    void crearYBuscarProyectoPorCiudad() {

        ProyectoDao dao = new ProyectoDao();

        String codigo = "TEST_PY_01";

        Proyecto proyecto = new Proyecto(
                codigo,
                "Proyecto Test",
                "VitoriaTest");

        dao.create(proyecto);

        List<Proyecto> resultado = dao.buscarPorCiudad("VitoriaTest");

        assertFalse(resultado.isEmpty());

        boolean encontrado = resultado.stream()
                .anyMatch(p -> p.getCodigo().equals(codigo));

        assertTrue(encontrado);

        dao.delete(dao.find(codigo));
    }
}