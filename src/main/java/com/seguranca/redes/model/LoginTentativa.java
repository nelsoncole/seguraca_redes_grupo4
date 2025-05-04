package com.seguranca.redes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "login_tentativas")
public class LoginTentativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "tentativa_em", nullable = false)
    private LocalDateTime tentativaEm = LocalDateTime.now(); // Valor padrão para CURRENT_TIMESTAMP

    @Column(name = "session_id", length = 255)
    private String sessionId;

    @Column(nullable = false)
    private Boolean sucesso = false; // Padrão como falso

    // Getters e Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getTentativaEm() {
        return tentativaEm;
    }

    public void setTentativaEm(LocalDateTime tentativaEm) {
        this.tentativaEm = tentativaEm;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Boolean getSucesso() {
        return sucesso;
    }

    public void setSucesso(Boolean sucesso) {
        this.sucesso = sucesso;
    }
}