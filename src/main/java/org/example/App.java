package org.example;

import org.example.menus.MainMenu;
import org.example.utils.AppLogger;
import org.example.utils.ContextoSeguridad;
import org.example.utils.EntradaConsola;
import org.example.utils.ServicioAutenticacion;
import org.example.utils.SesionAutenticada;

public class App {

    public static void main(String[] args) {
        AppLogger.info("Aplicación iniciada.");
        ServicioAutenticacion servicioAutenticacion =
                new ServicioAutenticacion(EntradaConsola.scanner());
        SesionAutenticada sesion = null;
        try {
            sesion = servicioAutenticacion.autenticar();
            if (sesion == null) {
                System.out.println("Acceso cancelado o bloqueado.");
                return;
            }

            ContextoSeguridad.iniciar(sesion.nombreUsuario());
            MainMenu menu = new MainMenu();
            menu.mostrar();
        } catch (RuntimeException e) {
            AppLogger.error("Error no controlado durante la ejecución de la aplicación.", e);
            throw e;
        } finally {
            if (sesion != null) {
                try {
                    servicioAutenticacion.cerrarSesion(sesion.idRegistroAcceso());
                    AppLogger.info("Sesión cerrada para el usuario: " + sesion.nombreUsuario());
                } catch (RuntimeException e) {
                    AppLogger.error("No se pudo guardar el cierre de sesión.", e);
                }
            }
            ContextoSeguridad.cerrar();
            AppLogger.info("Aplicación finalizada.");
        }
    }
}