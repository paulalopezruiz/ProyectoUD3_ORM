package org.example.menus;

import org.example.dao.PiezaDao;
import org.example.models.Pieza;
import org.example.utils.AppLogger;
import org.example.utils.EntradaConsola;
import org.example.utils.Validaciones;

import java.util.List;
import java.util.Scanner;

public class PiezaMenu {

    private final Scanner scanner =
            EntradaConsola.scanner();

    private final PiezaDao piezaDao =
            new PiezaDao();

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

                opcion =
                        Integer.parseInt(
                                scanner.nextLine()
                        );

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

        String codigo =
                Validaciones.leerCodigoAlfanumerico(
                        scanner,
                        "Código: ");

        if (piezaDao.find(codigo) != null) {

            System.out.println("Ya existe una pieza con ese código.");

            return;
        }

        String nombre =
                Validaciones.leerObligatorio(
                        scanner,
                        "Nombre: ");

        double precio =
                Validaciones.leerPrecio(
                        scanner,
                        "Precio: ");

        System.out.print("Descripción: ");

        String descripcion = scanner.nextLine().trim();

        Pieza pieza =
                new Pieza(
                        codigo,
                        nombre,
                        precio,
                        descripcion);

        try {

            piezaDao.create(pieza);

            System.out.println("Pieza creada correctamente.");

            AppLogger.info("Pieza creada: " + codigo);

        } catch (Exception e) {

            System.out.println("Error al crear la pieza.");

            AppLogger.error("Error al crear pieza " + codigo, e);
        }
    }

    private void bajaPieza() {

        System.out.println();
        System.out.println("| BAJA DE PIEZA |");

        String codigo =
                Validaciones.leerCodigoAlfanumerico(
                        scanner,
                        "Código de la pieza: ");

        Pieza pieza = piezaDao.find(codigo);

        if (pieza == null) {

            System.out.println("Pieza no encontrada.");
            return;
        }

        if (!Validaciones.confirmar(scanner, "eliminar la pieza " + codigo)) {

            System.out.println("Eliminación cancelada.");
            return;
        }

        try {

            piezaDao.delete(pieza);

            System.out.println("Pieza eliminada correctamente.");

            AppLogger.info("Pieza eliminada: " + codigo);

        } catch (Exception e) {

            System.out.println("No se puede eliminar la pieza.");

            System.out.println("Puede tener suministros asociados.");

            AppLogger.error("Error al eliminar pieza " + codigo, e
            );
        }
    }

    private void modificarPieza() {

        System.out.println();
        System.out.println("| MODIFICAR PIEZA |");

        String codigo =
                Validaciones.leerCodigoAlfanumerico(
                        scanner,
                        "Código de la pieza: ");

        Pieza pieza = piezaDao.find(codigo);

        if (pieza == null) {

            System.out.println(
                    "Pieza no encontrada.");
            return;
        }

        String nombre =
                Validaciones.leerObligatorio(
                        scanner,
                        "Nuevo nombre: ");

        double precio =
                Validaciones.leerPrecio(
                        scanner,
                        "Nuevo precio: ");

        System.out.print("Nueva descripción: ");

        String descripcion = scanner.nextLine().trim();

        String estadoAnterior = pieza.toString();

        pieza.setNombre(nombre);
        pieza.setPrecio(precio);
        pieza.setDescripcion(descripcion);

        try {

            piezaDao.update(
                    pieza,
                    estadoAnterior
            );

            System.out.println("Pieza modificada correctamente.");

            AppLogger.info("Pieza modificada: " + codigo);

        } catch (Exception e) {

            System.out.println("Error al modificar la pieza.");

            AppLogger.error("Error al modificar pieza " + codigo, e);
        }
    }

    private void buscarPorCodigo() {

        System.out.println();
        System.out.println("| BUSCAR POR CÓDIGO |");

        String codigo =
                Validaciones.leerCodigoAlfanumerico(
                        scanner,
                        "Código: ");

        Pieza pieza =
                piezaDao.find(codigo);

        if (pieza == null) {
            System.out.println("Pieza no encontrada.");

        } else {
            System.out.println(pieza);
        }
    }

    private void buscarPorNombre() {

        System.out.println();
        System.out.println("| BUSCAR POR NOMBRE |");

        String nombre =
                Validaciones.leerObligatorio(
                        scanner,
                        "Nombre: "
                );

        mostrarPiezas(piezaDao.buscarPorNombre(nombre));
    }

    private void buscarPorPrecio() {

        System.out.println();
        System.out.println("| BUSCAR POR PRECIO |");

        double minimo = Validaciones.leerPrecio(scanner, "Precio mínimo: ");

        double maximo;

        do {
            maximo = Validaciones.leerPrecio(scanner, "Precio máximo: ");
            if (maximo < minimo) {
                System.out.println("El precio máximo debe ser igual o mayor que el mínimo.");
            }

        } while (maximo < minimo);

        mostrarPiezas(
                piezaDao.buscarPorRangoPrecio(
                        minimo,
                        maximo)
        );
    }

    private void buscarPorProveedor() {

        System.out.println();
        System.out.println("| BUSCAR POR PROVEEDOR |");

        String codigo = Validaciones.leerCodigoAlfanumerico(scanner, "Código del proveedor: ");

        mostrarPiezas(piezaDao.buscarPorProveedor(codigo));
    }

    private void buscarPorProyecto() {

        System.out.println();
        System.out.println("| BUSCAR POR PROYECTO |");

        String codigo = Validaciones.leerCodigoAlfanumerico(scanner, "Código del proyecto: ");

        mostrarPiezas(piezaDao.buscarPorProyecto(codigo));
    }

    private void listarTodas() {

        System.out.println();
        System.out.println("| LISTADO DE PIEZAS |");

        mostrarPiezas(piezaDao.listarTodos());
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