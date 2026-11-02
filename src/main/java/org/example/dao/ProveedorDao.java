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

    public List<Proveedor> buscarPorApellidos(String apellidos) {
        return em.createQuery(
                        "SELECT p FROM Proveedor p WHERE LOWER(p.apellidos) LIKE LOWER(:apellidos)",
                        Proveedor.class
                )
                .setParameter("apellidos", "%" + apellidos + "%")
                .getResultList();
    }

    public List<Proveedor> buscarPorPieza(String codigoPieza) {
        return em.createQuery(
                        "SELECT DISTINCT p FROM Proveedor p " +
                                "JOIN p.gestiones g " +
                                "WHERE g.pieza.codigo = :codigo",
                        Proveedor.class
                )
                .setParameter("codigo", codigoPieza)
                .getResultList();
    }
}