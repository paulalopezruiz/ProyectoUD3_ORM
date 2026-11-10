package org.example.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "usuarios_app")
public class Usuario {

    @Id
    @Column(length = 100, nullable = false)
    private String nombreUsuario;

    @Column(nullable = false, length = 512)
    private String hashContrasena;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    protected Usuario() {
    }
 
    public Usuario(String nombreUsuario, String hashContrasena, LocalDateTime creadoEn) {
        this.nombreUsuario = nombreUsuario;
        this.hashContrasena = hashContrasena;
        this.creadoEn = creadoEn;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public String getHashContrasena() {
        return hashContrasena;
    }
}
