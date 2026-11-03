package org.example.utils;

public final class ContextoSeguridad {

    private static final ThreadLocal<String> NOMBRE_USUARIO = new ThreadLocal<>();

    private ContextoSeguridad() {
    }

    public static void iniciar(String usuario) {
        NOMBRE_USUARIO.set(usuario);
    }

    public static String usuarioActual() {
        String nombreUsuario = NOMBRE_USUARIO.get();
        if (nombreUsuario == null) {
            throw new IllegalStateException("No hay una sesión autenticada activa.");
        }
        return nombreUsuario;
    }

    public static void cerrar() {
        NOMBRE_USUARIO.remove();
    }
}
