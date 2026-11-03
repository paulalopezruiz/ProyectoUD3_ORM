package org.example.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros_acceso")
public class RegistroAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombreUsuario;

    @Column(nullable = false)
    private LocalDateTime fechaAcceso;

    private LocalDateTime fechaCierre;

    protected RegistroAcceso() {
    }

    public RegistroAcceso(String nombreUsuario, LocalDateTime fechaAcceso) {
        this.nombreUsuario = nombreUsuario;
        this.fechaAcceso = fechaAcceso;
    }

    public Long getId() {
        return id;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }
}
