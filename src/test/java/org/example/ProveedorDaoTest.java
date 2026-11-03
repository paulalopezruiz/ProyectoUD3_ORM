package org.example;

import org.example.dao.ProveedorDao;
import org.example.models.Proveedor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProveedorDaoTest {

    @Test
    void crearBuscarModificarYEliminarProveedor() {

        ProveedorDao dao = new ProveedorDao();

        String codigo = "TEST_PR_01";

        Proveedor proveedor = new Proveedor(
                codigo,
                "Paula",
                "Lopez",
                "Calle Test 1");

        dao.create(proveedor);

        Proveedor encontrado = dao.find(codigo);

        assertNotNull(encontrado);
        assertEquals("Paula", encontrado.getNombre());

        encontrado.setNombre("Laura");
        dao.update(encontrado);

        Proveedor modificado = dao.find(codigo);

        assertEquals("Laura", modificado.getNombre());

        dao.delete(modificado);

        assertNull(dao.find(codigo));
    }
}