package org.example.menus;

import org.example.dao.PiezaDao;
import org.example.models.Pieza;
import org.example.utils.AppLogger;

import java.util.List;
import java.util.Scanner;

public class PiezaMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final PiezaDao piezaDao = new PiezaDao();

    public void mostrar() {

        int opcion = -1;

        do {
            System.out.println();
            System.out.println("--------------------------------");
            System.out.println("       GESTIÓN DE PIEZAS");
            System.out.println("--------------------------------");
            System.out.println("1. Alta");
            System.out.println("2. Baja");
            System.out.println("3. Modificar");
            System.out.println("4. Buscar por código");
            System.out.println("5. Buscar por nombre");
            System.out.println("6. Buscar por intervalo de precios");
            System.out.println("7. Buscar por proveedor");
            System.out.println("8. Buscar por proyecto");
            System.out.println("9. Listar todas");
            System.out.println("0. Volver");
            System.out.println("--------------------------------");
            System.out.print("Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1:
                        altaPieza();
                        break;
                    case 2:
                        bajaPieza();
                        break;
                    case 3:
                        modificarPieza();
                        break;
                    case 4:
                        buscarPorCodigo();
                        break;
                    case 5:
                        buscarPorNombre();
                        break;
                    case 6:
                        buscarPorPrecio();
                        break;
                    case 7:
                        buscarPorProveedor();
                        break;
                    case 8:
                        buscarPorProyecto();
                        break;
                    case 9:
                        listarTodas();
                        break;
                    case 0:
                        System.out.println("Volviendo al menú principal...");
                        break;
                    default:
                        System.out.println("Opción no válida.");
                }

            } catch (NumberFormatException e) {
                System.out.println("Debes introducir un número.");
            }

        } while (opcion != 0);
    }

    private void altaPieza() {

        System.out.println();
        System.out.println("| ALTA DE PIEZA |");

        System.out.print("Código: ");
        String codigo = scanner.nextLine();

        if (piezaDao.find(codigo) != null) {
            System.out.println("Ya existe una pieza con ese código.");
            return;
        }

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();

        double precio;

        try {
            System.out.print("Precio: ");
            precio = Double.parseDouble(scanner.nextLine());

            if (precio < 0) {
                System.out.println("El precio no puede ser negativo.");
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("El precio debe ser un número.");
            return;
        }

        System.out.print("Descripción: ");
        String descripcion = scanner.nextLine();

        Pieza pieza =
                new Pieza(codigo, nombre, precio, descripcion);

        try {
            piezaDao.create(pieza);

            System.out.println("Pieza creada correctamente.");
            AppLogger.info("Pieza creada: " + codigo);

        } catch (Exception e) {
            System.out.println("Error al crear la pieza.");
            AppLogger.error("Error al crear pieza " + codigo + ": " + e.getMessage());
        }
    }

    private void bajaPieza() {

        System.out.println();
        System.out.println("| BAJA DE PIEZA |");

        System.out.print("Código de la pieza: ");
        String codigo = scanner.nextLine();

        Pieza pieza = piezaDao.find(codigo);

        if (pieza == null) {
            System.out.println("Pieza no encontrada.");
            return;
        }

        try {
            piezaDao.delete(pieza);

            System.out.println("Pieza eliminada correctamente.");
            AppLogger.info("Pieza eliminada: " + codigo);

        } catch (Exception e) {
            System.out.println("No se puede eliminar la pieza.");
            System.out.println("Puede tener suministros asociados.");

            AppLogger.error("Error al eliminar pieza " + codigo + ": " + e.getMessage());
        }
    }

    private void modificarPieza() {

        System.out.println();
        System.out.println("| MODIFICAR PIEZA |");

        System.out.print("Código de la pieza: ");
        String codigo = scanner.nextLine();

        Pieza pieza = piezaDao.find(codigo);

        if (pieza == null) {
            System.out.println("Pieza no encontrada.");
            return;
        }

        System.out.print("Nuevo nombre: ");
        String nombre = scanner.nextLine();

        double precio;

        try {
            System.out.print("Nuevo precio: ");
            precio = Double.parseDouble(scanner.nextLine());

            if (precio < 0) {
                System.out.println("El precio no puede ser negativo.");
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("El precio debe ser un número.");
            return;
        }

        System.out.print("Nueva descripción: ");
        String descripcion = scanner.nextLine();

        pieza.setNombre(nombre);
        pieza.setPrecio(precio);
        pieza.setDescripcion(descripcion);

        try {
            piezaDao.update(pieza);

            System.out.println("Pieza modificada correctamente.");
            AppLogger.info("Pieza modificada: " + codigo);

        } catch (Exception e) {
            System.out.println("Error al modificar la pieza.");
            AppLogger.error("Error al modificar pieza " + codigo + ": " + e.getMessage());
        }
    }

    private void buscarPorCodigo() {

        System.out.println();
        System.out.println("| BUSCAR POR CÓDIGO |");

        System.out.print("Código: ");
        String codigo = scanner.nextLine();

        Pieza pieza = piezaDao.find(codigo);

        if (pieza == null) {
            System.out.println("Pieza no encontrada.");
        } else {
            System.out.println(pieza);
        }
    }

    private void buscarPorNombre() {

        System.out.println();
        System.out.println("| BUSCAR POR NOMBRE |");

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();

        mostrarPiezas(
                piezaDao.buscarPorNombre(nombre)
        );
    }

    private void buscarPorPrecio() {

        System.out.println();
        System.out.println("| BUSCAR POR PRECIO |");

        try {
            System.out.print("Precio mínimo: ");
            double minimo = Double.parseDouble(scanner.nextLine());

            System.out.print("Precio máximo: ");
            double maximo = Double.parseDouble(scanner.nextLine());

            if (minimo < 0 || maximo < minimo) {
                System.out.println("Intervalo de precios incorrecto.");
                return;
            }

            mostrarPiezas(
                    piezaDao.buscarPorRangoPrecio(minimo, maximo)
            );

        } catch (NumberFormatException e) {
            System.out.println("Los precios deben ser números.");
        }
    }

    private void buscarPorProveedor() {

        System.out.println();
        System.out.println("| BUSCAR POR PROVEEDOR |");

        System.out.print("Código del proveedor: ");
        String codigo = scanner.nextLine();

        mostrarPiezas(
                piezaDao.buscarPorProveedor(codigo)
        );
    }

    private void buscarPorProyecto() {

        System.out.println();
        System.out.println("| BUSCAR POR PROYECTO |");

        System.out.print("Código del proyecto: ");
        String codigo = scanner.nextLine();

        mostrarPiezas(
                piezaDao.buscarPorProyecto(codigo)
        );
    }

    private void listarTodas() {

        System.out.println();
        System.out.println("| LISTADO DE PIEZAS |");

        mostrarPiezas(
                piezaDao.listarTodos()
        );
    }

    private void mostrarPiezas(List<Pieza> piezas) {

        if (piezas.isEmpty()) {
            System.out.println("No se han encontrado piezas.");
            return;
        }

        for (Pieza pieza : piezas) {
            System.out.println(pieza);
        }
    }
}