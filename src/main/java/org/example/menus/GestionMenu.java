package org.example.menus;

import org.example.dao.GestionDao;
import org.example.dao.PiezaDao;
import org.example.dao.ProveedorDao;
import org.example.dao.ProyectoDao;
import org.example.models.Gestion;
import org.example.models.EstadoProyecto;
import org.example.models.Pieza;
import org.example.models.Proveedor;
import org.example.models.Proyecto;
import org.example.utils.AppLogger;
import org.example.utils.EntradaConsola;
import org.example.utils.Validaciones;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class GestionMenu {

    private final Scanner scanner = EntradaConsola.scanner();

    private final GestionDao gestionDao = new GestionDao();
    private final ProveedorDao proveedorDao = new ProveedorDao();
    private final PiezaDao piezaDao = new PiezaDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();

    public void mostrar() {

        int opcion = -1;

        do {
            System.out.println();
            System.out.println("--------------------------------");
            System.out.println("       GESTIÓN DE SUMINISTROS");
            System.out.println("--------------------------------");
            System.out.println("1. Alta");
            System.out.println("2. Baja");
            System.out.println("3. Modificar");
            System.out.println("4. Buscar por ID");
            System.out.println("5. Buscar por proveedor");
            System.out.println("6. Buscar por pieza");
            System.out.println("7. Buscar por proyecto");
            System.out.println("8. Listar todos");
            System.out.println("0. Volver");
            System.out.println("--------------------------------");
            System.out.print("Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1:
                        altaGestion();
                        break;
                    case 2:
                        bajaGestion();
                        break;
                    case 3:
                        modificarGestion();
                        break;
                    case 4:
                        buscarPorId();
                        break;
                    case 5:
                        buscarPorProveedor();
                        break;
                    case 6:
                        buscarPorPieza();
                        break;
                    case 7:
                        buscarPorProyecto();
                        break;
                    case 8:
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

    private void altaGestion() {

        System.out.println();
        System.out.println("| ALTA DE SUMINISTRO |");

        String codigoProveedor = Validaciones.leerObligatorio(scanner, "Código del proveedor: ");

        Proveedor proveedor =
                proveedorDao.find(codigoProveedor);

        if (proveedor == null) {
            System.out.println("El proveedor no existe.");
            return;
        }

        String codigoPieza = Validaciones.leerObligatorio(scanner, "Código de la pieza: ");

        Pieza pieza =
                piezaDao.find(codigoPieza);

        if (pieza == null) {
            System.out.println("La pieza no existe.");
            return;
        }

        String codigoProyecto = Validaciones.leerObligatorio(scanner, "Código del proyecto: ");

        Proyecto proyecto =
                proyectoDao.find(codigoProyecto);

        if (proyecto == null) {
            System.out.println("El proyecto no existe.");
            return;
        }

        if (proyecto.getEstado() == EstadoProyecto.FINALIZADO) {
            System.out.println("No se pueden añadir suministros a un proyecto finalizado.");
            return;
        }

        LocalDate fecha = Validaciones.leerFecha(scanner, "Fecha (AAAA-MM-DD): ");
        int cantidad = Validaciones.leerCantidad(scanner, "Cantidad: ");

        Gestion gestion =
                new Gestion(
                        fecha,
                        cantidad,
                        proveedor,
                        pieza,
                        proyecto
                );

        try {
            gestionDao.create(gestion);

            System.out.println("Suministro creado correctamente.");
            System.out.println("ID asignado: " + gestion.getId());

            AppLogger.info(
                    "Suministro creado. ID: " +
                            gestion.getId()
            );

        } catch (Exception e) {
            System.out.println("Error al crear el suministro.");

            AppLogger.error(
                    "Error al crear suministro",
                    e
            );
        }
    }

    private void bajaGestion() {

        System.out.println();
        System.out.println("| BAJA DE SUMINISTRO |");

        Integer id = leerId();

        if (id == null) {
            return;
        }

        Gestion gestion =
                gestionDao.find(id);

        if (gestion == null) {
            System.out.println("Suministro no encontrado.");
            return;
        }

        if (!Validaciones.confirmar(scanner, "eliminar el suministro " + id)) {
            System.out.println("Eliminación cancelada.");
            return;
        }

        try {
            gestionDao.delete(gestion);

            System.out.println("Suministro eliminado correctamente.");
            AppLogger.info("Suministro eliminado. ID: " + id);

        } catch (Exception e) {
            System.out.println("Error al eliminar el suministro.");

            AppLogger.error(
                    "Error al eliminar suministro " + id,
                    e
            );
        }
    }

    private void modificarGestion() {

        System.out.println();
        System.out.println("| MODIFICAR SUMINISTRO |");

        Integer id = leerId();

        if (id == null) {
            return;
        }

        Gestion gestion =
                gestionDao.find(id);

        if (gestion == null) {
            System.out.println("Suministro no encontrado.");
            return;
        }

        String codigoProveedor = Validaciones.leerObligatorio(scanner, "Código del proveedor: ");

        Proveedor proveedor =
                proveedorDao.find(codigoProveedor);

        if (proveedor == null) {
            System.out.println("El proveedor no existe.");
            return;
        }

        String codigoPieza = Validaciones.leerObligatorio(scanner, "Código de la pieza: ");

        Pieza pieza =
                piezaDao.find(codigoPieza);

        if (pieza == null) {
            System.out.println("La pieza no existe.");
            return;
        }

        String codigoProyecto = Validaciones.leerObligatorio(scanner, "Código del proyecto: ");

        Proyecto proyecto =
                proyectoDao.find(codigoProyecto);

        if (proyecto == null) {
            System.out.println("El proyecto no existe.");
            return;
        }

        if (proyecto.getEstado() == EstadoProyecto.FINALIZADO) {
            System.out.println("No se pueden asignar suministros a un proyecto finalizado.");
            return;
        }

        LocalDate fecha = Validaciones.leerFecha(scanner, "Nueva fecha (AAAA-MM-DD): ");
        int cantidad = Validaciones.leerCantidad(scanner, "Nueva cantidad: ");

        String estadoAnterior = gestion.toString();
        gestion.setProveedor(proveedor);
        gestion.setPieza(pieza);
        gestion.setProyecto(proyecto);
        gestion.setFecha(fecha);
        gestion.setCantidad(cantidad);

        try {
            gestionDao.update(gestion, estadoAnterior);

            System.out.println("Suministro modificado correctamente.");
            AppLogger.info("Suministro modificado. ID: " + id);

        } catch (Exception e) {
            System.out.println("Error al modificar el suministro.");

            AppLogger.error(
                    "Error al modificar suministro " + id,
                    e
            );
        }
    }

    private void buscarPorId() {

        System.out.println();
        System.out.println("| BUSCAR POR ID |");

        Integer id = leerId();

        if (id == null) {
            return;
        }

        Gestion gestion =
                gestionDao.find(id);

        if (gestion == null) {
            System.out.println("Suministro no encontrado.");
        } else {
            System.out.println(gestion);
        }
    }

    private void buscarPorProveedor() {

        System.out.println();
        System.out.println("| BUSCAR POR PROVEEDOR |");

        System.out.print("Código del proveedor: ");
        String codigo = scanner.nextLine();

        mostrarLista(
                gestionDao.buscarPorProveedor(codigo)
        );
    }

    private void buscarPorPieza() {

        System.out.println();
        System.out.println("| BUSCAR POR PIEZA |");

        System.out.print("Código de la pieza: ");
        String codigo = scanner.nextLine();

        mostrarLista(
                gestionDao.buscarPorPieza(codigo)
        );
    }

    private void buscarPorProyecto() {

        System.out.println();
        System.out.println("| BUSCAR POR PROYECTO |");

        System.out.print("Código del proyecto: ");
        String codigo = scanner.nextLine();

        mostrarLista(
                gestionDao.buscarPorProyecto(codigo)
        );
    }

    private void listarTodas() {

        System.out.println();
        System.out.println("| LISTADO DE SUMINISTROS |");

        mostrarLista(
                gestionDao.listarTodos()
        );
    }

    private Integer leerId() {

        try {
            System.out.print("ID del suministro: ");
            return Integer.parseInt(scanner.nextLine());

        } catch (NumberFormatException e) {
            System.out.println("El ID debe ser un número.");
            return null;
        }
    }

    private void mostrarLista(List<Gestion> gestiones) {

        if (gestiones.isEmpty()) {
            System.out.println("No se han encontrado suministros.");
            return;
        }

        for (Gestion gestion : gestiones) {
            System.out.println(gestion);
        }
    }
}