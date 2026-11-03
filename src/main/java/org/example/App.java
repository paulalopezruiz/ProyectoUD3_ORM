package org.example;

import org.example.menus.MainMenu;
import org.example.utils.AppLogger;

public class App {

    public static void main(String[] args) {
        AppLogger.info("Aplicación iniciada.");
        try {
            MainMenu menu = new MainMenu();
            menu.mostrar();
        } catch (RuntimeException e) {
            AppLogger.error("Error no controlado durante la ejecución de la aplicación.", e);
            throw e;
        } finally {
            AppLogger.info("Aplicación finalizada.");
        }
    }
}