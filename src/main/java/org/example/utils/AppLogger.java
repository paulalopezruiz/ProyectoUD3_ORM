package org.example.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class AppLogger {

    static {
        new File("logs").mkdirs();
    }

    private static final Logger logger =
            LoggerFactory.getLogger("Aplicacion");

    public static void info(String mensaje) {
        logger.info(mensaje);
    }

    public static void warn(String mensaje) {
        logger.warn(mensaje);
    }

    public static void error(String mensaje) {
        logger.error(mensaje);
    }

    public static void error(String mensaje, Throwable excepcion) {
        logger.error(mensaje, excepcion);
    }
}