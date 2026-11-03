package org.example.models;

public enum EstadoProyecto {
    PENDIENTE("Pendiente"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado");

    private final String etiqueta;

    EstadoProyecto(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
