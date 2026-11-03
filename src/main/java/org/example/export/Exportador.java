package org.example.export;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.example.models.Gestion;
import org.example.models.Pieza;
import org.example.models.Proveedor;
import org.example.models.Proyecto;
import org.example.utils.AppLogger;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Exportador {

    public void exportarJson(List<Proveedor> proveedores,
                             List<Pieza> piezas,
                             List<Proyecto> proyectos,
                             List<Gestion> gestiones) {

        try {
            new File("exports").mkdirs();

            ObjectMapper mapper = new ObjectMapper();

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(
                            new File("exports/datos.json"),
                            prepararDatos(proveedores, piezas, proyectos, gestiones)
                    );

            System.out.println("JSON exportado correctamente.");
            System.out.println("Archivo: exports/datos.json");
            AppLogger.info("Exportación JSON completada: exports/datos.json");

        } catch (Exception e) {
            System.out.println("Error al exportar JSON.");
            AppLogger.error("Error al exportar datos a JSON.", e);
        }
    }

    public void exportarXml(List<Proveedor> proveedores,
                            List<Pieza> piezas,
                            List<Proyecto> proyectos,
                            List<Gestion> gestiones) {

        try {
            new File("exports").mkdirs();

            XmlMapper mapper = new XmlMapper();

            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(
                            new File("exports/datos.xml"),
                            prepararDatos(proveedores, piezas, proyectos, gestiones)
                    );

            System.out.println("XML exportado correctamente.");
            System.out.println("Archivo: exports/datos.xml");
            AppLogger.info("Exportación XML completada: exports/datos.xml");

        } catch (Exception e) {
            System.out.println("Error al exportar XML.");
            AppLogger.error("Error al exportar datos a XML.", e);
        }
    }

    private Map<String, Object> prepararDatos(
            List<Proveedor> proveedores,
            List<Pieza> piezas,
            List<Proyecto> proyectos,
            List<Gestion> gestiones) {

        Map<String, Object> datos = new LinkedHashMap<>();

        datos.put("proveedores", proveedores);
        datos.put("piezas", piezas);
        datos.put("proyectos", proyectos);

        List<Map<String, Object>> suministros = new ArrayList<>();

        for (Gestion g : gestiones) {

            Map<String, Object> suministro = new LinkedHashMap<>();

            suministro.put("id", g.getId());
            suministro.put("fecha", g.getFecha().toString());
            suministro.put("cantidad", g.getCantidad());
            suministro.put("proveedor", g.getProveedor().getCodigo());
            suministro.put("pieza", g.getPieza().getCodigo());
            suministro.put("proyecto", g.getProyecto().getCodigo());

            suministros.add(suministro);
        }

        datos.put("suministros", suministros);

        return datos;
    }
}