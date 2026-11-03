package org.example.utils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.example.models.RegistroAcceso;
import org.example.models.Usuario;

import java.time.LocalDateTime;
import java.util.Scanner;

public class ServicioAutenticacion {

    private static final int MAX_INTENTOS = 3;
    private final Scanner scanner;

    public ServicioAutenticacion(Scanner scanner) {
        this.scanner = scanner;
    }

    public SesionAutenticada autenticar() {
        EntityManager em = Persistencia.crearEntityManager();
        try {
            long numeroUsuarios = em.createQuery(
                    "SELECT COUNT(u) FROM Usuario u", Long.class).getSingleResult();
            if (numeroUsuarios == 0) {
                Usuario usuario = registrarPrimerUsuario(em);
                if (usuario == null) {
                    return null;
                }
                return crearSesion(em, usuario.getNombreUsuario());
            }

            for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
                System.out.print("Usuario: ");
                String nombreUsuario = scanner.nextLine().trim();
                char[] contrasena = EntradaConsola.leerContrasena("Contraseña: ");

                Usuario usuario = em.find(Usuario.class, nombreUsuario);
                boolean valido;
                try {
                    valido = usuario != null
                            && PasswordHasher.verify(contrasena, usuario.getHashContrasena());
                } finally {
                    java.util.Arrays.fill(contrasena, '\0');
                }

                if (valido) {
                    return crearSesion(em, usuario.getNombreUsuario());
                }

                AppLogger.warn("Intento de acceso fallido para el usuario: " + nombreUsuario);
                System.out.println("Credenciales incorrectas. Intentos restantes: "
                        + (MAX_INTENTOS - intento));
            }
            return null;
        } finally {
            em.close();
        }
    }

    public void cerrarSesion(Long idRegistroAcceso) {
        EntityManager em = Persistencia.crearEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            RegistroAcceso registro = em.find(RegistroAcceso.class, idRegistroAcceso);
            if (registro == null) {
                throw new IllegalStateException("No se encontró el registro de acceso.");
            }
            registro.setFechaCierre(LocalDateTime.now());
            transaction.commit();
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private Usuario registrarPrimerUsuario(EntityManager em) {
        System.out.println("No hay usuarios registrados. Crea la cuenta inicial.");
        System.out.print("Nuevo usuario (deja vacío para salir): ");
        String nombreUsuario = scanner.nextLine().trim();
        if (nombreUsuario.isBlank()) {
            return null;
        }
        if (nombreUsuario.length() > 100) {
            System.out.println("El nombre de usuario no puede superar 100 caracteres.");
            return null;
        }

        char[] contrasena;
        while (true) {
            contrasena = EntradaConsola.leerContrasena(
                    "Nueva contraseña (mínimo 8 caracteres, vacío para cancelar): ");
            if (contrasena.length == 0) {
                return null;
            }
            if (contrasena.length < 8) {
                java.util.Arrays.fill(contrasena, '\0');
                System.out.println("La contraseña debe tener al menos 8 caracteres.");
                continue;
            }

            char[] confirmacion = EntradaConsola.leerContrasena("Repite la contraseña: ");
            boolean coincide = coinciden(contrasena, confirmacion);
            java.util.Arrays.fill(confirmacion, '\0');
            if (coincide) {
                break;
            }

            java.util.Arrays.fill(contrasena, '\0');
            System.out.println("Las contraseñas no coinciden.");
        }

        String hash;
        try {
            hash = PasswordHasher.hash(contrasena);
        } finally {
            java.util.Arrays.fill(contrasena, '\0');
        }
        Usuario usuario = new Usuario(nombreUsuario, hash, LocalDateTime.now());
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(usuario);
            transaction.commit();
            AppLogger.info("Cuenta de aplicación creada: " + nombreUsuario);
            return usuario;
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private SesionAutenticada crearSesion(EntityManager em, String nombreUsuario) {
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            RegistroAcceso acceso = new RegistroAcceso(nombreUsuario, LocalDateTime.now());
            em.persist(acceso);
            transaction.commit();
            AppLogger.info("Acceso correcto para el usuario: " + nombreUsuario);
            return new SesionAutenticada(nombreUsuario, acceso.getId());
        } catch (RuntimeException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw e;
        }
    }

    private boolean coinciden(char[] primera, char[] segunda) {
        int diferencia = primera.length ^ segunda.length;
        int longitud = Math.max(primera.length, segunda.length);
        for (int i = 0; i < longitud; i++) {
            char caracterPrimero = i < primera.length ? primera[i] : 0;
            char caracterSegundo = i < segunda.length ? segunda[i] : 0;
            diferencia |= caracterPrimero ^ caracterSegundo;
        }
        return diferencia == 0;
    }
}
