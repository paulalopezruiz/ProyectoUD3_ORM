package org.example.dao;

import org.example.models.Gestion;

import java.time.LocalDate;
import java.util.List;

public class EstadisticasDao extends Dao<Gestion, Integer> {

    @Override
    public Gestion find(Integer id) {
        return em.find(Gestion.class, id);
    }

    public List<Gestion> historicoProveedor(
            String codigoProveedor,
            LocalDate fechaInicio,
            LocalDate fechaFin) {

        return em.createQuery(
                        "SELECT g FROM Gestion g " +
                                "WHERE g.proveedor.codigo = :codigo " +
                                "AND g.fecha BETWEEN :inicio AND :fin " +
                                "ORDER BY g.fecha",
                        Gestion.class)
                .setParameter("codigo", codigoProveedor)
                .setParameter("inicio", fechaInicio)
                .setParameter("fin", fechaFin)
                .getResultList();
    }

    public long contarProveedoresProyecto(String codigoProyecto) {

        return em.createQuery(
                        "SELECT COUNT(DISTINCT g.proveedor.codigo) " +
                                "FROM Gestion g " +
                                "WHERE g.proyecto.codigo = :codigo",
                        Long.class)
                .setParameter("codigo", codigoProyecto)
                .getSingleResult();
    }

    public long contarPiezasProyecto(String codigoProyecto) {

        return em.createQuery(
                        "SELECT COUNT(DISTINCT g.pieza.codigo) " +
                                "FROM Gestion g " +
                                "WHERE g.proyecto.codigo = :codigo",
                        Long.class)
                .setParameter("codigo", codigoProyecto)
                .getSingleResult();
    }

    public double costeTotalProyecto(String codigoProyecto) {

        Object resultado = em.createQuery(
                        "SELECT SUM(g.cantidad * g.pieza.precio) " +
                                "FROM Gestion g " +
                                "WHERE g.proyecto.codigo = :codigo")
                .setParameter("codigo", codigoProyecto)
                .getSingleResult();

        if (resultado == null) {
            return 0;
        }

        return ((Number) resultado).doubleValue();
    }

    public List<Object[]> rankingProveedoresActivos() {
        return em.createQuery(
                        "SELECT g.proveedor.codigo, g.proveedor.nombre, SUM(g.cantidad) " +
                                "FROM Gestion g " +
                                "GROUP BY g.proveedor.codigo, g.proveedor.nombre " +
                                "ORDER BY SUM(g.cantidad) DESC, g.proveedor.codigo",
                        Object[].class)
                .getResultList();
    }

    public List<Object[]> rankingPiezasUtilizadas() {
        return em.createQuery(
                        "SELECT g.pieza.codigo, g.pieza.nombre, SUM(g.cantidad) " +
                                "FROM Gestion g " +
                                "GROUP BY g.pieza.codigo, g.pieza.nombre " +
                                "ORDER BY SUM(g.cantidad) DESC, g.pieza.codigo",
                        Object[].class)
                .getResultList();
    }

    public List<Gestion> gestionesProyecto(String codigoProyecto) {
        return em.createQuery(
                        "SELECT g FROM Gestion g " +
                                "WHERE g.proyecto.codigo = :codigo " +
                                "ORDER BY g.proveedor.codigo, g.pieza.codigo",
                        Gestion.class)
                .setParameter("codigo", codigoProyecto)
                .getResultList();
    }

    public long totalProveedores() {
        return em.createQuery(
                "SELECT COUNT(p) FROM Proveedor p",
                Long.class).getSingleResult();
    }

    public long totalPiezas() {
        return em.createQuery(
                "SELECT COUNT(p) FROM Pieza p",
                Long.class).getSingleResult();
    }

    public long totalProyectos() {
        return em.createQuery(
                "SELECT COUNT(p) FROM Proyecto p",
                Long.class).getSingleResult();
    }

    public long totalSuministros() {
        return em.createQuery(
                "SELECT COUNT(g) FROM Gestion g",
                Long.class).getSingleResult();
    }

    public long cantidadTotalSuministrada() {

        Long resultado = em.createQuery(
                "SELECT SUM(g.cantidad) FROM Gestion g",
                Long.class).getSingleResult();

        return resultado == null ? 0 : resultado;
    }
}