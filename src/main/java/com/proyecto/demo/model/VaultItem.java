package com.proyecto.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;

@Entity
@Table(name = "vault_items")
public class VaultItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "El título es obligatorio")
    @Column(nullable = false, length = 150)
    private String titulo;

    // contenido cifrado en Base64
    @Column(nullable = false, length = 4000)
    private String contentEnc;

    @Column(nullable = false, length = 255)
    private String nonce;

    @Column(nullable = false, length = 255)
    private String kdfSalt;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    // Campo solo para el formulario (texto plano)
    @Transient
    private String contenidoPlano;

    // getters y setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContentEnc() { return contentEnc; }
    public void setContentEnc(String contentEnc) { this.contentEnc = contentEnc; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public String getKdfSalt() { return kdfSalt; }
    public void setKdfSalt(String kdfSalt) { this.kdfSalt = kdfSalt; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public String getContenidoPlano() { return contenidoPlano; }
    public void setContenidoPlano(String contenidoPlano) { this.contenidoPlano = contenidoPlano; }
}

