package org.example.menus;

import org.example.dao.ProveedorDao;
import org.example.models.Proveedor;
import org.example.utils.AppLogger;
import org.example.utils.EntradaConsola;
import org.example.utils.Validaciones;

import java.util.List;
import java.util.Scanner;

public class ProveedorMenu {

    private final Scanner scanner = EntradaConsola.scanner();
    private final ProveedorDao proveedorDao = new ProveedorDao();

    public void mostrar() {

        int opcion = -1;

        do {
            System.out.println();
            System.out.println("--------------------------------");
            System.out.println("       GESTIÓN DE PROVEEDORES");
            System.out.println("--------------------------------");
            System.out.println("1. Alta");
            System.out.println("2. Baja");
            System.out.println("3. Modificar");
            System.out.println("4. Buscar por código");
            System.out.println("5. Buscar por nombre");
            System.out.println("6. Buscar por dirección");
            System.out.println("7. Buscar por apellidos");
            System.out.println("8. Buscar por pieza suministrada");
            System.out.println("9. Listar todos");
            System.out.println("0. Volver");
            System.out.println("--------------------------------");
            System.out.print("Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1:
                        altaProveedor();
                        break;
                    case 2:
                        bajaProveedor();
                        break;
                    case 3:
                        modificarProveedor();
                        break;
                    case 4:
                        buscarPorCodigo();
                        break;
                    case 5:
                        buscarPorNombre();
                        break;
                    case 6:
                        buscarPorDireccion();
                        break;
                    case 7:
                        buscarPorApellidos();
                        break;
                    case 8:
                        buscarPorPieza();
                        break;
                    case 9:
                        listarTodos();
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

    private void altaProveedor() {

        System.out.println();
        System.out.println("| ALTA DE PROVEEDOR |");

        String codigo =
                Validaciones.leerCodigoAlfanumerico(
                        scanner,
                        "Código: "
                );

        if (proveedorDao.find(codigo) != null) {
            System.out.println("Ya existe un proveedor con ese código.");
            return;
        }

        String nombre =
                Validaciones.leerObligatorio(
                        scanner,
                        "Nombre: "
                );

        String apellidos =
                Validaciones.leerObligatorio(
                        scanner,
                        "Apellidos: "
                );

        String direccion =
                Validaciones.leerObligatorio(
                        scanner,
                        "Dirección postal: "
                );

        Proveedor proveedor =
                new Proveedor(
                        codigo,
                        nombre,
                        apellidos,
                        direccion
                );

        try {
            proveedorDao.create(proveedor);

            System.out.println("Proveedor creado correctamente.");

            AppLogger.info("Proveedor creado: " + codigo);

        } catch (Exception e) {

            System.out.println("Error al crear el proveedor.");

            AppLogger.error("Error al crear proveedor " + codigo, e);
        }
    }

    private void bajaProveedor() {

        System.out.println();
        System.out.println("| BAJA DE PROVEEDOR |");

        String codigo =
                Validaciones.leerCodigoAlfanumerico(
                        scanner,
                        "Código del proveedor: "
                );

        Proveedor proveedor =
                proveedorDao.find(codigo);

        if (proveedor == null) {
            System.out.println(
                    "Proveedor no encontrado."
            );
            return;
        }

        if (!Validaciones.confirmar(
                scanner,
                "eliminar el proveedor " + codigo
        )) {

            System.out.println("Eliminación cancelada.");

            return;
        }

        try {
            proveedorDao.delete(proveedor);

            System.out.println("Proveedor eliminado correctamente.");

            AppLogger.info("Proveedor eliminado: " + codigo
            );

        } catch (Exception e) {

            System.out.println("No se puede eliminar el proveedor.");

            System.out.println("Puede tener suministros asociados.");

            AppLogger.error("Error al eliminar proveedor " + codigo, e
            );
        }
    }

    private void modificarProveedor() {

        System.out.println();
        System.out.println("| MODIFICAR PROVEEDOR |");

        String codigo =
                Validaciones.leerCodigoAlfanumerico(
                        scanner,
                        "Código del proveedor: ");

        Proveedor proveedor =
                proveedorDao.find(codigo);

        if (proveedor == null) {
            System.out.println("Proveedor no encontrado.");
            return;
        }

        String nombre =
                Validaciones.leerObligatorio(
                        scanner,
                        "Nuevo nombre: "
                );

        String apellidos =
                Validaciones.leerObligatorio(
                        scanner,
                        "Nuevos apellidos: "
                );

        String direccion =
                Validaciones.leerObligatorio(
                        scanner,
                        "Nueva dirección postal: "
                );

        String estadoAnterior =
                proveedor.toString();

        proveedor.setNombre(nombre);
        proveedor.setApellidos(apellidos);
        proveedor.setDireccionPostal(direccion);

        try {
            proveedorDao.update(
                    proveedor,
                    estadoAnterior
            );

            System.out.println("Proveedor modificado correctamente.");

            AppLogger.info("Proveedor modificado: " + codigo);

        } catch (Exception e) {

            System.out.println("Error al modificar el proveedor.");

            AppLogger.error("Error al modificar proveedor " + codigo, e);
        }
    }

    private void buscarPorCodigo() {

        System.out.println();
        System.out.println("| BUSCAR POR CÓDIGO |");

        String codigo =
                Validaciones.leerCodigoAlfanumerico(
                        scanner,
                        "Código: ");

        Proveedor proveedor =
                proveedorDao.find(codigo);

        if (proveedor == null) {

            System.out.println("Proveedor no encontrado.");

        } else {

            System.out.println(proveedor);
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

        List<Proveedor> proveedores =
                proveedorDao.buscarPorNombre(nombre);

        mostrarProveedores(proveedores);
    }

    private void buscarPorDireccion() {

        System.out.println();
        System.out.println("| BUSCAR POR DIRECCIÓN |");

        String direccion =
                Validaciones.leerObligatorio(
                        scanner,
                        "Dirección: "
                );

        List<Proveedor> proveedores =
                proveedorDao.buscarPorDireccion(
                        direccion
                );

        mostrarProveedores(proveedores);
    }

    private void buscarPorApellidos() {

        System.out.println();
        System.out.println("| BUSCAR POR APELLIDOS |");

        String apellidos =
                Validaciones.leerObligatorio(
                        scanner,
                        "Apellidos: "
                );

        mostrarProveedores(
                proveedorDao.buscarPorApellidos(
                        apellidos
                )
        );
    }

    private void buscarPorPieza() {

        System.out.println();
        System.out.println("| BUSCAR POR PIEZA SUMINISTRADA |");

        String codigo =
                Validaciones.leerCodigoAlfanumerico(
                        scanner,
                        "Código de la pieza: "
                );

        mostrarProveedores(
                proveedorDao.buscarPorPieza(codigo)
        );
    }

    private void listarTodos() {

        System.out.println();
        System.out.println("| LISTADO DE PROVEEDORES |");

        mostrarProveedores(
                proveedorDao.listarTodos()
        );
    }

    private void mostrarProveedores(List<Proveedor> proveedores) {

        if (proveedores.isEmpty()) {

            System.out.println("No se han encontrado proveedores.");

            return;
        }

        for (Proveedor proveedor : proveedores) {
            System.out.println(proveedor);
        }
    }
}