package org.example.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros_auditoria")
public class RegistroAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombreUsuario;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 20)
    private String operacion;

    @Column(nullable = false, length = 100)
    private String tipoEntidad;

    @Column(length = 255)
    private String idEntidad;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String detalles;

    protected RegistroAuditoria() {
    }

    public RegistroAuditoria(String nombreUsuario, LocalDateTime fechaHora,
                             String operacion, String tipoEntidad, String idEntidad,
                             String detalles) {
        this.nombreUsuario = nombreUsuario;
        this.fechaHora = fechaHora;
        this.operacion = operacion;
        this.tipoEntidad = tipoEntidad;
        this.idEntidad = idEntidad;
        this.detalles = detalles;
    }
}
