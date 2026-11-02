package org.example.menus;

import org.example.dao.ProveedorDao;
import org.example.models.Proveedor;

import java.util.List;
import java.util.Scanner;

public class ProveedorMenu {

    private final Scanner scanner = new Scanner(System.in);
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

        System.out.print("Código: ");
        String codigo = scanner.nextLine();

        if (proveedorDao.find(codigo) != null) {
            System.out.println("Ya existe un proveedor con ese código.");
            return;
        }

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();

        System.out.print("Apellidos: ");
        String apellidos = scanner.nextLine();

        System.out.print("Dirección postal: ");
        String direccion = scanner.nextLine();

        Proveedor proveedor =
                new Proveedor(codigo, nombre, apellidos, direccion);

        try {
            proveedorDao.create(proveedor);
            System.out.println("Proveedor creado correctamente.");

        } catch (Exception e) {
            System.out.println("Error al crear el proveedor.");
        }
    }

    private void bajaProveedor() {

        System.out.println();
        System.out.println("| BAJA DE PROVEEDOR |");

        System.out.print("Código del proveedor: ");
        String codigo = scanner.nextLine();

        Proveedor proveedor = proveedorDao.find(codigo);

        if (proveedor == null) {
            System.out.println("Proveedor no encontrado.");
            return;
        }

        try {
            proveedorDao.delete(proveedor);
            System.out.println("Proveedor eliminado correctamente.");

        } catch (Exception e) {
            System.out.println("No se puede eliminar el proveedor.");
            System.out.println("Puede tener suministros asociados.");
        }
    }

    private void modificarProveedor() {

        System.out.println();
        System.out.println("| MODIFICAR PROVEEDOR |");

        System.out.print("Código del proveedor: ");
        String codigo = scanner.nextLine();

        Proveedor proveedor = proveedorDao.find(codigo);

        if (proveedor == null) {
            System.out.println("Proveedor no encontrado.");
            return;
        }

        System.out.print("Nuevo nombre: ");
        String nombre = scanner.nextLine();

        System.out.print("Nuevos apellidos: ");
        String apellidos = scanner.nextLine();

        System.out.print("Nueva dirección postal: ");
        String direccion = scanner.nextLine();

        proveedor.setNombre(nombre);
        proveedor.setApellidos(apellidos);
        proveedor.setDireccionPostal(direccion);

        try {
            proveedorDao.update(proveedor);
            System.out.println("Proveedor modificado correctamente.");

        } catch (Exception e) {
            System.out.println("Error al modificar el proveedor.");
        }
    }

    private void buscarPorCodigo() {

        System.out.println();
        System.out.println("| BUSCAR POR CÓDIGO |");

        System.out.print("Código: ");
        String codigo = scanner.nextLine();

        Proveedor proveedor = proveedorDao.find(codigo);

        if (proveedor == null) {
            System.out.println("Proveedor no encontrado.");
        } else {
            System.out.println(proveedor);
        }
    }

    private void buscarPorNombre() {

        System.out.println();
        System.out.println("| BUSCAR POR NOMBRE |");

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();

        List<Proveedor> proveedores =
                proveedorDao.buscarPorNombre(nombre);

        if (proveedores.isEmpty()) {
            System.out.println("No se han encontrado proveedores.");
        } else {
            for (Proveedor proveedor : proveedores) {
                System.out.println(proveedor);
            }
        }
    }

    private void buscarPorDireccion() {

        System.out.println();
        System.out.println("| BUSCAR POR DIRECCIÓN |");

        System.out.print("Dirección: ");
        String direccion = scanner.nextLine();

        List<Proveedor> proveedores =
                proveedorDao.buscarPorDireccion(direccion);

        if (proveedores.isEmpty()) {
            System.out.println("No se han encontrado proveedores.");
        } else {
            for (Proveedor proveedor : proveedores) {
                System.out.println(proveedor);
            }
        }
    }

    private void listarTodos() {

        System.out.println();
        System.out.println("| LISTADO DE PROVEEDORES |");

        List<Proveedor> proveedores =
                proveedorDao.listarTodos();

        if (proveedores.isEmpty()) {
            System.out.println("No hay proveedores.");
        } else {
            for (Proveedor proveedor : proveedores) {
                System.out.println(proveedor);
            }
        }
    }

    private void buscarPorApellidos() {

        System.out.println();
        System.out.println("| BUSCAR POR APELLIDOS |");

        System.out.print("Apellidos: ");
        String apellidos = scanner.nextLine();

        List<Proveedor> proveedores =
                proveedorDao.buscarPorApellidos(apellidos);

        if (proveedores.isEmpty()) {
            System.out.println("No se han encontrado proveedores.");
        } else {
            for (Proveedor proveedor : proveedores) {
                System.out.println(proveedor);
            }
        }
    }

    private void buscarPorPieza() {

        System.out.println();
        System.out.println("| BUSCAR POR PIEZA SUMINISTRADA |");

        System.out.print("Código de la pieza: ");
        String codigo = scanner.nextLine();

        List<Proveedor> proveedores =
                proveedorDao.buscarPorPieza(codigo);

        if (proveedores.isEmpty()) {
            System.out.println("No se han encontrado proveedores.");
        } else {
            for (Proveedor proveedor : proveedores) {
                System.out.println(proveedor);
            }
        }
    }
}