package org.example.dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public abstract class Dao<T, K> {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("default");

    protected EntityManager em = emf.createEntityManager();

    public EntityManager getEntityManager() {
        return em;
    }

    public abstract T find(K id);

    public T create(T objeto) {
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.persist(objeto);
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
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            T actualizado = em.merge(objeto);
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

            if (!em.contains(objeto)) {
                objeto = em.merge(objeto);
            }

            em.remove(objeto);
            transaction.commit();

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }
}