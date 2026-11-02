package org.example.dao;

import org.example.models.Proveedor;

import java.util.List;

public class ProveedorDao extends Dao<Proveedor, String> {

    @Override
    public Proveedor find(String codigo) {
        return em.find(Proveedor.class, codigo);
    }

    public List<Proveedor> buscarPorNombre(String nombre) {
        return em.createQuery(
                        "SELECT p FROM Proveedor p WHERE LOWER(p.nombre) LIKE LOWER(:nombre)",
                        Proveedor.class
                )
                .setParameter("nombre", "%" + nombre + "%")
                .getResultList();
    }

    public List<Proveedor> buscarPorDireccion(String direccion) {
        return em.createQuery(
                        "SELECT p FROM Proveedor p WHERE LOWER(p.direccionPostal) LIKE LOWER(:direccion)",
                        Proveedor.class
                )
                .setParameter("direccion", "%" + direccion + "%")
                .getResultList();
    }

    public List<Proveedor> listarTodos() {
        return em.createQuery(
                "SELECT p FROM Proveedor p",
                Proveedor.class
        ).getResultList();
    }
}