package org.example.utils;

import java.io.Console;
import java.util.Scanner;

public final class EntradaConsola {

    private static final Scanner SCANNER = new Scanner(System.in);

    private EntradaConsola() {
    }

    public static Scanner scanner() {
        return SCANNER;
    }

    public static char[] leerContrasena(String mensaje) {
        Console consola = System.console();
        if (consola != null) {
            char[] contrasena = consola.readPassword("%s", mensaje);
            return contrasena == null ? new char[0] : contrasena;
        }

        System.out.print(mensaje);
        return SCANNER.nextLine().toCharArray();
    }
}
