package org.example.utils;

import org.example.models.EstadoProyecto;
import org.example.models.Proyecto;
import org.junit.jupiter.api.Test;

import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionesTest {

    @Test
    void leeTextoObligatorioIgnorandoEntradasVacias() {
        assertEquals("Proyecto A",
                Validaciones.leerObligatorio(new Scanner("\n Proyecto A \n"), "Nombre: "));
    }

    @Test
    void rechazaPrecioInvalidoHastaRecibirUnoValido() {
        assertEquals(12.5,
                Validaciones.leerPrecio(new Scanner("-1\nNaN\n12.5\n"), "Precio: "));
    }

    @Test
    void soloAceptaCantidadPositiva() {
        assertEquals(4,
                Validaciones.leerCantidad(new Scanner("0\nabc\n4\n"), "Cantidad: "));
    }

    @Test
    void permiteElegirLosTresEstadosDelProyecto() {
        assertEquals(EstadoProyecto.FINALIZADO,
                Validaciones.leerEstadoProyecto(new Scanner("9\n3\n")));
    }

    @Test
    void proyectoNuevoEmpiezaPendiente() {
        assertEquals(EstadoProyecto.PENDIENTE, new Proyecto().getEstado());
    }

    @Test
    void confirmacionSoloApruebaRespuestaAfirmativa() {
        assertFalse(Validaciones.confirmar(new Scanner("tal vez\nn\n"), "borrar"));
        assertTrue(Validaciones.confirmar(new Scanner("si\n"), "borrar"));
    }
}
