package org.example.menus;

import org.example.dao.EstadisticasDao;
import org.example.dao.ProveedorDao;
import org.example.dao.ProyectoDao;
import org.example.models.Gestion;
import org.example.models.Proveedor;
import org.example.models.Proyecto;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class EstadisticasMenu {

    private final Scanner scanner = new Scanner(System.in);

    private final EstadisticasDao estadisticasDao =
            new EstadisticasDao();

    private final ProveedorDao proveedorDao =
            new ProveedorDao();

    private final ProyectoDao proyectoDao =
            new ProyectoDao();

    public void mostrar() {

        int opcion = -1;

        do {
            System.out.println();
            System.out.println("--------------------------------");
            System.out.println("       ESTADÍSTICAS");
            System.out.println("--------------------------------");
            System.out.println("1. Histórico de proveedor");
            System.out.println("2. Resumen de proyecto");
            System.out.println("3. Proveedor más activo");
            System.out.println("4. Pieza más utilizada");
            System.out.println("5. Estadísticas globales");
            System.out.println("0. Volver");
            System.out.println("--------------------------------");
            System.out.print("Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1:
                        historicoProveedor();
                        break;

                    case 2:
                        resumenProyecto();
                        break;

                    case 3:
                        proveedorMasActivo();
                        break;

                    case 4:
                        piezaMasUtilizada();
                        break;

                    case 5:
                        estadisticasGlobales();
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

    private void historicoProveedor() {

        System.out.println();
        System.out.println("| HISTÓRICO DE PROVEEDOR |");

        System.out.print("Código del proveedor: ");
        String codigo = scanner.nextLine();

        Proveedor proveedor = proveedorDao.find(codigo);

        if (proveedor == null) {
            System.out.println("Proveedor no encontrado.");
            return;
        }

        try {
            System.out.print("Fecha inicial (AAAA-MM-DD): ");
            LocalDate inicio = LocalDate.parse(scanner.nextLine());

            System.out.print("Fecha final (AAAA-MM-DD): ");
            LocalDate fin = LocalDate.parse(scanner.nextLine());

            if (inicio.isAfter(fin)) {
                System.out.println("La fecha inicial no puede ser posterior a la final.");
                return;
            }

            List<Gestion> gestiones =
                    estadisticasDao.historicoProveedor(
                            codigo,
                            inicio,
                            fin
                    );

            if (gestiones.isEmpty()) {
                System.out.println("No hay suministros en ese periodo.");
                return;
            }

            System.out.println();
            System.out.println("Proveedor: " +
                    proveedor.getNombre() + " " +
                    proveedor.getApellidos());

            for (Gestion gestion : gestiones) {

                System.out.println(
                        "Fecha: " + gestion.getFecha() +
                                " | Pieza: " + gestion.getPieza().getNombre() +
                                " | Proyecto: " + gestion.getProyecto().getNombre() +
                                " | Cantidad: " + gestion.getCantidad()
                );
            }

        } catch (DateTimeParseException e) {
            System.out.println("Formato de fecha incorrecto.");
        }
    }

    private void resumenProyecto() {

        System.out.println();
        System.out.println("| RESUMEN DE PROYECTO |");

        System.out.print("Código del proyecto: ");
        String codigo = scanner.nextLine();

        Proyecto proyecto = proyectoDao.find(codigo);

        if (proyecto == null) {
            System.out.println("Proyecto no encontrado.");
            return;
        }

        long proveedores =
                estadisticasDao.contarProveedoresProyecto(codigo);

        long piezas =
                estadisticasDao.contarPiezasProyecto(codigo);

        double coste =
                estadisticasDao.costeTotalProyecto(codigo);

        System.out.println();
        System.out.println("Proyecto: " + proyecto.getNombre());
        System.out.println("Ciudad: " + proyecto.getCiudad());
        System.out.println("Proveedores participantes: " + proveedores);
        System.out.println("Piezas diferentes utilizadas: " + piezas);
        System.out.printf("Coste total estimado: %.2f €%n", coste);
    }

    private void proveedorMasActivo() {

        System.out.println();
        System.out.println("| PROVEEDOR MÁS ACTIVO |");

        Object[] resultado =
                estadisticasDao.proveedorMasActivo();

        if (resultado == null) {
            System.out.println("No hay suministros registrados.");
            return;
        }

        System.out.println("Código: " + resultado[0]);
        System.out.println("Nombre: " + resultado[1]);
        System.out.println(
                "Cantidad total suministrada: " +
                        ((Number) resultado[2]).longValue()
        );
    }

    private void piezaMasUtilizada() {

        System.out.println();
        System.out.println("| PIEZA MÁS UTILIZADA |");

        Object[] resultado =
                estadisticasDao.piezaMasUtilizada();

        if (resultado == null) {
            System.out.println("No hay suministros registrados.");
            return;
        }

        System.out.println("Código: " + resultado[0]);
        System.out.println("Nombre: " + resultado[1]);
        System.out.println(
                "Cantidad total utilizada: " +
                        ((Number) resultado[2]).longValue()
        );
    }

    private void estadisticasGlobales() {

        System.out.println();
        System.out.println("| ESTADÍSTICAS GLOBALES |");

        System.out.println(
                "Número de proveedores: " + estadisticasDao.totalProveedores());

        System.out.println(
                "Número de piezas: " + estadisticasDao.totalPiezas());

        System.out.println(
                "Número de proyectos: " + estadisticasDao.totalProyectos());

        System.out.println(
                "Número de suministros: " + estadisticasDao.totalSuministros());

        System.out.println(
                "Cantidad total suministrada: " + estadisticasDao.cantidadTotalSuministrada());
    }
}