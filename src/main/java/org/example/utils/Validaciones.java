package org.example.utils;

import org.example.models.EstadoProyecto;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public final class Validaciones {

    private Validaciones() {
    }

    public static String leerObligatorio(Scanner scanner, String etiqueta) {

        while (true) {

            System.out.print(etiqueta);

            String valor = scanner.nextLine().trim();

            if (!valor.isEmpty()) {
                return valor;
            }

            System.out.println("Este campo es obligatorio.");
        }
    }

    public static String leerCodigoAlfanumerico(Scanner scanner, String etiqueta) {

        while (true) {

            System.out.print(etiqueta);

            String codigo = scanner.nextLine().trim();

            if (codigo.isEmpty()) {

                System.out.println("El código es obligatorio.");

                continue;
            }

            if (codigo.matches("[A-Za-z0-9]+")) {
                return codigo;
            }

            System.out.println("El código solo puede contener letras y números."
            );
        }
    }

    public static double leerPrecio(Scanner scanner, String etiqueta) {

        while (true) {

            System.out.print(etiqueta);

            try {

                double precio = Double.parseDouble(scanner.nextLine().trim());

                if (Double.isFinite(precio) && precio >= 0) {
                    return precio;
                }

            } catch (NumberFormatException ignored) {
            }
            System.out.println("Introduce un precio válido (cero o mayor).");
        }
    }

    public static int leerCantidad(Scanner scanner, String etiqueta) {

        while (true) {

            System.out.print(etiqueta);

            try {

                int cantidad = Integer.parseInt(scanner.nextLine().trim());

                if (cantidad > 0) {
                    return cantidad;
                }

            } catch (NumberFormatException ignored) {
            }
            System.out.println("Introduce una cantidad entera mayor que cero.");
        }
    }

    public static LocalDate leerFecha(Scanner scanner, String etiqueta) {

        while (true) {

            System.out.print(etiqueta);

            try {
                return LocalDate.parse(scanner.nextLine().trim());

            } catch (DateTimeParseException ignored) {

                System.out.println("Introduce una fecha válida con formato AAAA-MM-DD.");
            }
        }
    }

    public static EstadoProyecto leerEstadoProyecto(Scanner scanner) {

        while (true) {
            System.out.println(
                    "Estado del proyecto: "
                            + "1. Pendiente  "
                            + "2. En curso  "
                            + "3. Finalizado"
            );

            System.out.print("Selecciona una opción: ");

            switch (scanner.nextLine().trim()) {

                case "1":
                    return EstadoProyecto.PENDIENTE;

                case "2":
                    return EstadoProyecto.EN_CURSO;

                case "3":
                    return EstadoProyecto.FINALIZADO;

                default:
                    System.out.println("Selecciona 1, 2 o 3.");
            }
        }
    }

    public static boolean confirmar(Scanner scanner, String accion) {

        while (true) {

            System.out.print(
                    "¿Confirmas "
                            + accion
                            + "? (s/n): ");

            String respuesta = scanner.nextLine().trim();

            if ("s".equalsIgnoreCase(respuesta) || "si".equalsIgnoreCase(respuesta)) {

                return true;
            }

            if ("n".equalsIgnoreCase(respuesta) || "no".equalsIgnoreCase(respuesta)) {
                return false;
            }

            System.out.println("Responde s o n.");
        }
    }
}