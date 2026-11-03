package org.example.menus;

import org.example.dao.EstadisticasDao;
import org.example.dao.ProveedorDao;
import org.example.dao.ProyectoDao;
import org.example.export.Exportador;
import org.example.models.Gestion;
import org.example.models.Proveedor;
import org.example.models.Proyecto;
import org.example.utils.EntradaConsola;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class EstadisticasMenu {

    private final Scanner scanner = EntradaConsola.scanner();

    private final EstadisticasDao estadisticasDao = new EstadisticasDao();
    private final ProveedorDao proveedorDao = new ProveedorDao();
    private final ProyectoDao proyectoDao = new ProyectoDao();
    private final Exportador exportador = new Exportador();

    public void mostrar() {
        int opcion = -1;

        do {
            System.out.println();
            System.out.println("--------------------------------");
            System.out.println("       ESTADÍSTICAS");
            System.out.println("--------------------------------");
            System.out.println("1. Histórico de proveedor");
            System.out.println("2. Resumen de proyecto");
            System.out.println("3. Ranking de proveedores activos");
            System.out.println("4. Ranking de piezas utilizadas");
            System.out.println("5. Estadísticas globales");
            System.out.println("0. Volver");
            System.out.println("--------------------------------");
            System.out.print("Selecciona una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine());

                switch (opcion) {
                    case 1 -> historicoProveedor();
                    case 2 -> resumenProyecto();
                    case 3 -> rankingProveedores();
                    case 4 -> rankingPiezas();
                    case 5 -> estadisticasGlobales();
                    case 0 -> System.out.println("Volviendo al menú principal...");
                    default -> System.out.println("Opción no válida.");
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

            List<Gestion> gestiones = estadisticasDao.historicoProveedor(codigo, inicio, fin);
            Map<String, Object> informe = new LinkedHashMap<>();
            informe.put("proveedor", datosProveedor(proveedor));
            informe.put("fechaInicio", inicio.toString());
            informe.put("fechaFin", fin.toString());
            System.out.println("Proveedor: " + proveedor.getNombre() + " " + proveedor.getApellidos());

            List<Map<String, Object>> suministros = new ArrayList<>();
            for (Gestion gestion : gestiones) {
                Map<String, Object> suministro = new LinkedHashMap<>();
                suministro.put("fecha", gestion.getFecha().toString());
                suministro.put("pieza", datosPieza(gestion));
                suministro.put("proyecto", datosProyecto(gestion));
                suministro.put("cantidad", gestion.getCantidad());
                suministros.add(suministro);

                System.out.println(
                        "Fecha: " + gestion.getFecha() +
                                " | Pieza: " + gestion.getPieza().getNombre() +
                                " | Proyecto: " + gestion.getProyecto().getNombre() +
                                " | Cantidad: " + gestion.getCantidad()
                );
            }

            if (gestiones.isEmpty()) {
                System.out.println("No hay suministros en ese periodo.");
            }
            informe.put("suministros", suministros);
            ofrecerExportacion("historico-proveedor", informe);
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

        List<Gestion> gestiones = estadisticasDao.gestionesProyecto(codigo);
        Map<String, Map<String, Object>> proveedores = new LinkedHashMap<>();
        Map<String, Map<String, Object>> piezas = new LinkedHashMap<>();
        for (Gestion gestion : gestiones) {
            proveedores.putIfAbsent(
                    gestion.getProveedor().getCodigo(), datosProveedor(gestion.getProveedor()));
            piezas.putIfAbsent(gestion.getPieza().getCodigo(), datosPieza(gestion));
        }

        double coste = estadisticasDao.costeTotalProyecto(codigo);
        long numeroProveedores = estadisticasDao.contarProveedoresProyecto(codigo);
        long numeroPiezas = estadisticasDao.contarPiezasProyecto(codigo);
        System.out.println("Proyecto: " + proyecto.getNombre());
        System.out.println("Ciudad: " + proyecto.getCiudad());
        System.out.println("Proveedores participantes: " + numeroProveedores);
        proveedores.values().forEach(p -> System.out.println("  - " + p.get("nombreCompleto") + " (" + p.get("codigo") + ")"));
        System.out.println("Piezas diferentes utilizadas: " + numeroPiezas);
        piezas.values().forEach(p -> System.out.println("  - " + p.get("nombre") + " (" + p.get("codigo") + ")"));
        System.out.printf("Coste total estimado: %.2f €%n", coste);

        Map<String, Object> informe = new LinkedHashMap<>();
        informe.put("proyecto", datosProyecto(proyecto));
        informe.put("numeroProveedores", numeroProveedores);
        informe.put("proveedores", new ArrayList<>(proveedores.values()));
        informe.put("numeroPiezas", numeroPiezas);
        informe.put("piezas", new ArrayList<>(piezas.values()));
        informe.put("costeTotalEstimado", coste);
        ofrecerExportacion("resumen-proyecto", informe);
    }

    private void rankingProveedores() {
        List<Object[]> resultados = estadisticasDao.rankingProveedoresActivos();
        System.out.println();
        System.out.println("| RANKING DE PROVEEDORES ACTIVOS |");
        List<Map<String, Object>> ranking = new ArrayList<>();

        for (int i = 0; i < resultados.size(); i++) {
            Object[] resultado = resultados.get(i);
            Map<String, Object> proveedor = new LinkedHashMap<>();
            proveedor.put("posicion", i + 1);
            proveedor.put("codigo", resultado[0]);
            proveedor.put("nombre", resultado[1]);
            proveedor.put("cantidadTotal", ((Number) resultado[2]).longValue());
            ranking.add(proveedor);
            System.out.println((i + 1) + ". " + resultado[1] + " (" + resultado[0] + "): " + resultado[2]);
        }

        if (resultados.isEmpty()) {
            System.out.println("No hay suministros registrados.");
        }
        Map<String, Object> informe = new LinkedHashMap<>();
        informe.put("proveedoresActivos", ranking);
        ofrecerExportacion("ranking-proveedores", informe);
    }

    private void rankingPiezas() {
        List<Object[]> resultados = estadisticasDao.rankingPiezasUtilizadas();
        System.out.println();
        System.out.println("| RANKING DE PIEZAS UTILIZADAS |");
        List<Map<String, Object>> ranking = new ArrayList<>();

        for (int i = 0; i < resultados.size(); i++) {
            Object[] resultado = resultados.get(i);
            Map<String, Object> pieza = new LinkedHashMap<>();
            pieza.put("posicion", i + 1);
            pieza.put("codigo", resultado[0]);
            pieza.put("nombre", resultado[1]);
            pieza.put("cantidadTotal", ((Number) resultado[2]).longValue());
            ranking.add(pieza);
            System.out.println((i + 1) + ". " + resultado[1] + " (" + resultado[0] + "): " + resultado[2]);
        }

        if (resultados.isEmpty()) {
            System.out.println("No hay suministros registrados.");
        }
        Map<String, Object> informe = new LinkedHashMap<>();
        informe.put("piezasUtilizadas", ranking);
        ofrecerExportacion("ranking-piezas", informe);
    }

    private void estadisticasGlobales() {
        List<Object[]> proveedores = estadisticasDao.rankingProveedoresActivos();
        List<Object[]> piezas = estadisticasDao.rankingPiezasUtilizadas();
        Map<String, Object> informe = new LinkedHashMap<>();
        informe.put("numeroProveedores", estadisticasDao.totalProveedores());
        informe.put("numeroPiezas", estadisticasDao.totalPiezas());
        informe.put("numeroProyectos", estadisticasDao.totalProyectos());
        informe.put("numeroSuministros", estadisticasDao.totalSuministros());
        informe.put("cantidadTotalSuministrada", estadisticasDao.cantidadTotalSuministrada());
        informe.put("rankingProveedoresActivos", convertirRanking(proveedores));
        informe.put("rankingPiezasUtilizadas", convertirRanking(piezas));

        System.out.println();
        System.out.println("| ESTADÍSTICAS GLOBALES |");
        System.out.println("Número de proveedores: " + informe.get("numeroProveedores"));
        System.out.println("Número de piezas: " + informe.get("numeroPiezas"));
        System.out.println("Número de proyectos: " + informe.get("numeroProyectos"));
        System.out.println("Número de suministros: " + informe.get("numeroSuministros"));
        System.out.println("Cantidad total suministrada: " + informe.get("cantidadTotalSuministrada"));
        imprimirRanking("Proveedores más activos", proveedores);
        imprimirRanking("Piezas más utilizadas", piezas);
        ofrecerExportacion("estadisticas-globales", informe);
    }

    private List<Map<String, Object>> convertirRanking(List<Object[]> resultados) {
        List<Map<String, Object>> ranking = new ArrayList<>();
        for (int i = 0; i < resultados.size(); i++) {
            Object[] resultado = resultados.get(i);
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("posicion", i + 1);
            fila.put("codigo", resultado[0]);
            fila.put("nombre", resultado[1]);
            fila.put("cantidadTotal", ((Number) resultado[2]).longValue());
            ranking.add(fila);
        }
        return ranking;
    }

    private void imprimirRanking(String titulo, List<Object[]> resultados) {
        System.out.println(titulo + ":");
        if (resultados.isEmpty()) {
            System.out.println("  No hay suministros registrados.");
            return;
        }
        for (int i = 0; i < resultados.size(); i++) {
            Object[] resultado = resultados.get(i);
            System.out.println("  " + (i + 1) + ". " + resultado[1] + " (" + resultado[0] + "): " + resultado[2]);
        }
    }

    private Map<String, Object> datosProveedor(Proveedor proveedor) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("codigo", proveedor.getCodigo());
        datos.put("nombre", proveedor.getNombre());
        datos.put("apellidos", proveedor.getApellidos());
        datos.put("nombreCompleto", proveedor.getNombre() + " " + proveedor.getApellidos());
        return datos;
    }

    private Map<String, Object> datosPieza(Gestion gestion) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("codigo", gestion.getPieza().getCodigo());
        datos.put("nombre", gestion.getPieza().getNombre());
        datos.put("precioUnitario", gestion.getPieza().getPrecio());
        return datos;
    }

    private Map<String, Object> datosProyecto(Gestion gestion) {
        return datosProyecto(gestion.getProyecto());
    }

    private Map<String, Object> datosProyecto(Proyecto proyecto) {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("codigo", proyecto.getCodigo());
        datos.put("nombre", proyecto.getNombre());
        datos.put("ciudad", proyecto.getCiudad());
        return datos;
    }

    private void ofrecerExportacion(String nombreArchivo, Map<String, Object> informe) {
        System.out.println("Exportar este informe: 1. JSON  2. XML  0. No");
        System.out.print("Selecciona una opción: ");
        try {
            int opcion = Integer.parseInt(scanner.nextLine());
            if (opcion == 1) {
                exportador.exportarInforme(nombreArchivo, informe, true);
            } else if (opcion == 2) {
                exportador.exportarInforme(nombreArchivo, informe, false);
            } else if (opcion != 0) {
                System.out.println("Opción de exportación no válida.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Debes introducir un número.");
        }
    }
}
