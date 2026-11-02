package org.example.dao;

import org.example.models.Pieza;

import java.util.List;

public class PiezaDao extends Dao<Pieza, String> {

    @Override
    public Pieza find(String codigo) {
        return em.find(Pieza.class, codigo);
    }

    public List<Pieza> buscarPorNombre(String nombre) {
        return em.createQuery(
                        "SELECT p FROM Pieza p WHERE LOWER(p.nombre) LIKE LOWER(:nombre)",
                        Pieza.class)
                .setParameter("nombre", "%" + nombre + "%")
                .getResultList();
    }

    public List<Pieza> listarTodos() {
        return em.createQuery(
                "SELECT p FROM Pieza p",
                Pieza.class).getResultList();
    }

    public List<Pieza> buscarPorRangoPrecio(double minimo, double maximo) {
        return em.createQuery(
                        "SELECT p FROM Pieza p WHERE p.precio BETWEEN :minimo AND :maximo",
                        Pieza.class
                )
                .setParameter("minimo", minimo)
                .setParameter("maximo", maximo)
                .getResultList();
    }

    public List<Pieza> buscarPorProveedor(String codigoProveedor) {
        return em.createQuery(
                        "SELECT DISTINCT p FROM Pieza p " +
                                "JOIN p.gestiones g " +
                                "WHERE g.proveedor.codigo = :codigo",
                        Pieza.class
                )
                .setParameter("codigo", codigoProveedor)
                .getResultList();
    }

    public List<Pieza> buscarPorProyecto(String codigoProyecto) {
        return em.createQuery(
                        "SELECT DISTINCT p FROM Pieza p " +
                                "JOIN p.gestiones g " +
                                "WHERE g.proyecto.codigo = :codigo",
                        Pieza.class
                )
                .setParameter("codigo", codigoProyecto)
                .getResultList();
    }
}