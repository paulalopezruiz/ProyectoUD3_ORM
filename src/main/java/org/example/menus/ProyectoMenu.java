package org.example.menus;

import org.example.dao.ProyectoDao;
import org.example.models.Proyecto;
import org.example.utils.AppLogger;

import java.util.List;
import java.util.Scanner;

public class ProyectoMenu {

    private final Scanner scanner = new Scanner(System.in);
    private final ProyectoDao proyectoDao = new ProyectoDao();

    public void mostrar() {

        int opcion = -1;

        do {
            System.out.println();
            System.out.println("--------------------------------");
            System.out.println("       GESTIÓN DE PROYECTOS");
            System.out.println("--------------------------------");
            System.out.println("1. Alta");
            System.out.println("2. Baja");
            System.out.println("3. Modificar");
            System.out.println("4. Buscar por código");
            System.out.println("5. Buscar por nombre");
            System.out.println("6. Buscar por ciudad");
            System.out.println("7. Buscar por proveedor");
            System.out.println("8. Buscar por pieza utilizada");
            System.out.println("9. Listar todos");
            System.out.println("0. Volver");
            System.out.println("--------------------------------");
            System.out.print("Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1:
                        altaProyecto();
                        break;
                    case 2:
                        bajaProyecto();
                        break;
                    case 3:
                        modificarProyecto();
                        break;
                    case 4:
                        buscarPorCodigo();
                        break;
                    case 5:
                        buscarPorNombre();
                        break;
                    case 6:
                        buscarPorCiudad();
                        break;
                    case 7:
                        buscarPorProveedor();
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

    private void altaProyecto() {

        System.out.println();
        System.out.println("| ALTA DE PROYECTO |");

        System.out.print("Código: ");
        String codigo = scanner.nextLine();

        if (proyectoDao.find(codigo) != null) {
            System.out.println("Ya existe un proyecto con ese código.");
            return;
        }

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();

        System.out.print("Ciudad: ");
        String ciudad = scanner.nextLine();

        Proyecto proyecto =
                new Proyecto(codigo, nombre, ciudad);

        try {
            proyectoDao.create(proyecto);

            System.out.println("Proyecto creado correctamente.");
            AppLogger.info("Proyecto creado: " + codigo);

        } catch (Exception e) {
            System.out.println("Error al crear el proyecto.");
            AppLogger.error("Error al crear proyecto " + codigo, e);
        }
    }

    private void bajaProyecto() {

        System.out.println();
        System.out.println("| BAJA DE PROYECTO |");

        System.out.print("Código del proyecto: ");
        String codigo = scanner.nextLine();

        Proyecto proyecto = proyectoDao.find(codigo);

        if (proyecto == null) {
            System.out.println("Proyecto no encontrado.");
            return;
        }

        try {
            proyectoDao.delete(proyecto);

            System.out.println("Proyecto eliminado correctamente.");
            AppLogger.info("Proyecto eliminado: " + codigo);

        } catch (Exception e) {
            System.out.println("No se puede eliminar el proyecto.");
            System.out.println("Puede tener suministros asociados.");

            AppLogger.error("Error al eliminar proyecto " + codigo, e);
        }
    }

    private void modificarProyecto() {

        System.out.println();
        System.out.println("| MODIFICAR PROYECTO |");

        System.out.print("Código del proyecto: ");
        String codigo = scanner.nextLine();

        Proyecto proyecto = proyectoDao.find(codigo);

        if (proyecto == null) {
            System.out.println("Proyecto no encontrado.");
            return;
        }

        System.out.print("Nuevo nombre: ");
        String nombre = scanner.nextLine();

        System.out.print("Nueva ciudad: ");
        String ciudad = scanner.nextLine();

        proyecto.setNombre(nombre);
        proyecto.setCiudad(ciudad);

        try {
            proyectoDao.update(proyecto);

            System.out.println("Proyecto modificado correctamente.");
            AppLogger.info("Proyecto modificado: " + codigo);

        } catch (Exception e) {
            System.out.println("Error al modificar el proyecto.");
            AppLogger.error("Error al modificar proyecto " + codigo, e);
        }
    }

    private void buscarPorCodigo() {

        System.out.println();
        System.out.println("| BUSCAR POR CÓDIGO |");

        System.out.print("Código: ");
        String codigo = scanner.nextLine();

        Proyecto proyecto = proyectoDao.find(codigo);

        if (proyecto == null) {
            System.out.println("Proyecto no encontrado.");
        } else {
            System.out.println(proyecto);
        }
    }

    private void buscarPorNombre() {

        System.out.println();
        System.out.println("| BUSCAR POR NOMBRE |");

        System.out.print("Nombre: ");
        String nombre = scanner.nextLine();

        mostrarProyectos(
                proyectoDao.buscarPorNombre(nombre)
        );
    }

    private void buscarPorCiudad() {

        System.out.println();
        System.out.println("| BUSCAR POR CIUDAD |");

        System.out.print("Ciudad: ");
        String ciudad = scanner.nextLine();

        mostrarProyectos(
                proyectoDao.buscarPorCiudad(ciudad)
        );
    }

    private void buscarPorProveedor() {

        System.out.println();
        System.out.println("| BUSCAR POR PROVEEDOR |");

        System.out.print("Código del proveedor: ");
        String codigo = scanner.nextLine();

        mostrarProyectos(
                proyectoDao.buscarPorProveedor(codigo)
        );
    }

    private void buscarPorPieza() {

        System.out.println();
        System.out.println("| BUSCAR POR PIEZA |");

        System.out.print("Código de la pieza: ");
        String codigo = scanner.nextLine();

        mostrarProyectos(
                proyectoDao.buscarPorPieza(codigo)
        );
    }

    private void listarTodos() {

        System.out.println();
        System.out.println("| LISTADO DE PROYECTOS |");

        mostrarProyectos(
                proyectoDao.listarTodos()
        );
    }

    private void mostrarProyectos(List<Proyecto> proyectos) {

        if (proyectos.isEmpty()) {
            System.out.println("No se han encontrado proyectos.");
            return;
        }

        for (Proyecto proyecto : proyectos) {
            System.out.println(proyecto);
        }
    }
}