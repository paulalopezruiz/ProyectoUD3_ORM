package org.example.utils;

public final class ContextoSeguridad {

    private static String nombreUsuario;

    private ContextoSeguridad() {
    }

    public static void iniciar(String usuario) {
        nombreUsuario = usuario;
    }

    public static String usuarioActual() {
        if (nombreUsuario == null) {
            throw new IllegalStateException("No hay una sesión autenticada activa.");
        }
        return nombreUsuario;
    }

    public static void cerrar() {
        nombreUsuario = null;
    }
}
