package org.example.dao;

import org.example.models.Gestion;

import java.util.List;

public class GestionDao extends Dao<Gestion, Integer> {

    @Override
    public Gestion find(Integer id) {
        return em.find(Gestion.class, id);
    }

    public List<Gestion> listarTodos() {
        return em.createQuery(
                "SELECT g FROM Gestion g",
                Gestion.class
        ).getResultList();
    }

    public List<Gestion> buscarPorProveedor(String codigoProveedor) {
        return em.createQuery(
                        "SELECT g FROM Gestion g WHERE g.proveedor.codigo = :codigo",
                        Gestion.class
                )
                .setParameter("codigo", codigoProveedor)
                .getResultList();
    }

    public List<Gestion> buscarPorPieza(String codigoPieza) {
        return em.createQuery(
                        "SELECT g FROM Gestion g WHERE g.pieza.codigo = :codigo",
                        Gestion.class
                )
                .setParameter("codigo", codigoPieza)
                .getResultList();
    }

    public List<Gestion> buscarPorProyecto(String codigoProyecto) {
        return em.createQuery(
                        "SELECT g FROM Gestion g WHERE g.proyecto.codigo = :codigo",
                        Gestion.class
                )
                .setParameter("codigo", codigoProyecto)
                .getResultList();
    }
}