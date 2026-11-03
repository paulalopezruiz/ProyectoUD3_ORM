package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.example.models.RegistroAuditoria;
import org.example.utils.ContextoSeguridad;
import org.example.utils.Persistencia;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

public abstract class Dao<T, K> implements AutoCloseable {

    private static final EntityManagerFactory emf = Persistencia.factory();

    protected EntityManager em = emf.createEntityManager();

    public EntityManager getEntityManager() {
        return em;
    }

    @Override
    public void close() {
        if (em.isOpen()) {
            em.close();
        }
    }

    public abstract T find(K id);

    public T create(T objeto) {
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.persist(objeto);
            registrarAuditoria("CREATE", objeto, null, objeto.toString());
            transaction.commit();
            return objeto;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public T update(T objeto) {
        return update(objeto, objeto.toString());
    }

    public T update(T objeto, String estadoAnterior) {
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            T actualizado = em.merge(objeto);
            registrarAuditoria("UPDATE", actualizado, estadoAnterior, actualizado.toString());
            transaction.commit();
            return actualizado;

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    public void delete(T objeto) {
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            String estadoAnterior = objeto.toString();

            if (!em.contains(objeto)) {
                objeto = em.merge(objeto);
            }

            em.remove(objeto);
            registrarAuditoria("DELETE", objeto, estadoAnterior, null);
            transaction.commit();

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private void registrarAuditoria(String operacion, T objeto,
                                    String estadoAnterior, String estadoNuevo) {
        String detalles = "antes=" + (estadoAnterior == null ? "<inexistente>" : estadoAnterior)
                + "; despues=" + (estadoNuevo == null ? "<eliminado>" : estadoNuevo);
        em.persist(new RegistroAuditoria(
                ContextoSeguridad.usuarioActual(),
                LocalDateTime.now(),
                operacion,
                objeto.getClass().getSimpleName(),
                obtenerId(objeto),
                detalles
        ));
    }

    private String obtenerId(T objeto) {
        for (String nombreMetodo : new String[]{"getCodigo", "getId"}) {
            try {
                Method metodo = objeto.getClass().getMethod(nombreMetodo);
                Object id = metodo.invoke(objeto);
                return id == null ? null : id.toString();
            } catch (NoSuchMethodException ignored) {
                // Try the next supported identifier getter.
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException("No se pudo obtener el identificador de la entidad.", e);
            }
        }
        throw new IllegalStateException("La entidad no expone un identificador compatible.");
    }
}