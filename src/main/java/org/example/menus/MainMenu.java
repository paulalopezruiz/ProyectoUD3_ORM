package org.example.menus;

import java.util.Scanner;

public class MainMenu {

    private final Scanner scanner = new Scanner(System.in);

    public void mostrar() {

        int opcion = -1;

        do {
            System.out.println();
            System.out.println("--------------------------------");
            System.out.println("       GESTIÓN DE PROYECTOS");
            System.out.println("--------------------------------");
            System.out.println("1. Gestión de proveedores");
            System.out.println("2. Gestión de piezas");
            System.out.println("3. Gestión de proyectos");
            System.out.println("4. Gestión de suministros");
            System.out.println("5. Estadísticas");
            System.out.println("6. Exportaciones");
            System.out.println("7. Ayuda");
            System.out.println("0. Salir");
            System.out.println("--------------------------------");
            System.out.print("Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {

                    case 1:
                        new ProveedorMenu().mostrar();
                        break;

                    case 2:
                        new PiezaMenu().mostrar();
                        break;

                    case 3:
                        new ProyectoMenu().mostrar();
                        break;

                    case 4:
                        new GestionMenu().mostrar();
                        break;

                    case 5:
                        new EstadisticasMenu().mostrar();
                        break;

                    case 6:
                        new ExportMenu().mostrar();
                        break;

                    case 7:
                        mostrarAyuda();
                        break;

                    case 0:
                        System.out.println("Cerrando aplicación...");
                        break;

                    default:
                        System.out.println("Opción no válida.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Debes introducir un número.");
            }

        } while (opcion != 0);
    }

    private void mostrarAyuda() {

        System.out.println();
        System.out.println("| AYUDA |");
        System.out.println("1. Proveedores: altas, bajas, modificaciones y búsquedas.");
        System.out.println("2. Piezas: altas, bajas, modificaciones y búsquedas.");
        System.out.println("3. Proyectos: altas, bajas, modificaciones y búsquedas.");
        System.out.println("4. Suministros: relaciona proveedores, piezas y proyectos.");
        System.out.println("5. Estadísticas: históricos y análisis.");
        System.out.println("6. Exportaciones: genera archivos JSON y XML.");
        System.out.println("0. Salir de la aplicación.");
    }
}