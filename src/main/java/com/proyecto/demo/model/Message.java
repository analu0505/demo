package com.proyecto.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Entity @Table(name="messages")
public class Message {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message="El título es obligatorio")
    @Column(nullable=false, length=150)
    private String titulo;

    @NotEmpty(message="El contenido no puede estar vacío")
    @Column(nullable=false, length=2000)
    private String contenido;

    @Column(nullable=false)
    private LocalDateTime fechaCreacion;

    @Column(nullable=false)
    private LocalDateTime fechaActualizacion;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="usuario_id", nullable=false)
    private User usuario;

    @PrePersist
    public void prePersist(){
        this.fechaCreacion = LocalDateTime.now();
        this.fechaActualizacion = this.fechaCreacion;
    }
    @PreUpdate
    public void preUpdate(){ this.fechaActualizacion = LocalDateTime.now(); }

    // Getters/Setters
    public Long getId(){ return id; } public void setId(Long id){ this.id=id; }
    public String getTitulo(){ return titulo; } public void setTitulo(String t){ this.titulo=t; }
    public String getContenido(){ return contenido; } public void setContenido(String c){ this.contenido=c; }
    public LocalDateTime getFechaCreacion(){ return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime f){ this.fechaCreacion=f; }
    public LocalDateTime getFechaActualizacion(){ return fechaActualizacion; }
    public void setFechaActualizacion(LocalDateTime f){ this.fechaActualizacion=f; }
    public User getUsuario(){ return usuario; } public void setUsuario(User u){ this.usuario=u; }
}
