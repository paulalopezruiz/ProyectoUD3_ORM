package org.example.menus;

import org.example.dao.GestionDao;
import org.example.dao.PiezaDao;
import org.example.dao.ProveedorDao;
import org.example.dao.ProyectoDao;
import org.example.export.Exportador;
import org.example.utils.EntradaConsola;

import java.util.Scanner;

public class ExportMenu {

    private final Scanner scanner = EntradaConsola.scanner();

    private final ProveedorDao proveedorDao = new ProveedorDao();
    private final PiezaDao piezaDao = new PiezaDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final GestionDao gestionDao = new GestionDao();

    private final Exportador exportador = new Exportador();

    public void mostrar() {

        int opcion = -1;

        do {
            System.out.println();
            System.out.println("--------------------------------");
            System.out.println("       EXPORTACIONES");
            System.out.println("--------------------------------");
            System.out.println("1. Exportar JSON");
            System.out.println("2. Exportar XML");
            System.out.println("0. Volver");
            System.out.println("--------------------------------");
            System.out.print("Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {

                    case 1:
                        exportador.exportarJson(
                                proveedorDao.listarTodos(),
                                piezaDao.listarTodos(),
                                proyectoDao.listarTodos(),
                                gestionDao.listarTodos()
                        );
                        break;

                    case 2:
                        exportador.exportarXml(
                                proveedorDao.listarTodos(),
                                piezaDao.listarTodos(),
                                proyectoDao.listarTodos(),
                                gestionDao.listarTodos()
                        );
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
}