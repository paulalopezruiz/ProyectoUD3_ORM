package org.example.dao;

import org.example.models.Proyecto;

import java.util.List;

public class ProyectoDao extends Dao<Proyecto, String> {

    @Override
    public Proyecto find(String codigo) {
        return em.find(Proyecto.class, codigo);
    }

    public List<Proyecto> buscarPorNombre(String nombre) {
        return em.createQuery(
                        "SELECT p FROM Proyecto p WHERE LOWER(p.nombre) LIKE LOWER(:nombre)",
                        Proyecto.class)
                .setParameter("nombre", "%" + nombre + "%")
                .getResultList();
    }

    public List<Proyecto> buscarPorCiudad(String ciudad) {
        return em.createQuery(
                        "SELECT p FROM Proyecto p WHERE LOWER(p.ciudad) LIKE LOWER(:ciudad)",
                        Proyecto.class)
                .setParameter("ciudad", "%" + ciudad + "%")
                .getResultList();
    }

    public List<Proyecto> listarTodos() {
        return em.createQuery(
                "SELECT p FROM Proyecto p",
                Proyecto.class).getResultList();
    }

    public List<Proyecto> buscarPorProveedor(String codigoProveedor) {
        return em.createQuery(
                        "SELECT DISTINCT p FROM Proyecto p " +
                                "JOIN p.gestiones g " +
                                "WHERE g.proveedor.codigo = :codigo",
                        Proyecto.class
                )
                .setParameter("codigo", codigoProveedor)
                .getResultList();
    }

    public List<Proyecto> buscarPorPieza(String codigoPieza) {
        return em.createQuery(
                        "SELECT DISTINCT p FROM Proyecto p " +
                                "JOIN p.gestiones g " +
                                "WHERE g.pieza.codigo = :codigo",
                        Proyecto.class
                )
                .setParameter("codigo", codigoPieza)
                .getResultList();
    }
}