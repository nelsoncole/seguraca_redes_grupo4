package com.seguranca.redes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Arquivos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_original", length = 255)
    private String nomeOriginal; // Nome que o usuário enviou
    @Column(name = "nome_armazenado", length = 255)
    private String nomeArmazenado; // Nome salvo no servidor
    @Column(name = "url", length = 255)
    private String url; // Caminho para download
    @Column(name = "finalidade", length = 255)
    private String finalidade; // finalidade do arquivo (por exemplo: "documento pessoal", "relatório", "foto de perfil", etc.)
     @Column(name = "dataEnvio", nullable = false)
    private LocalDateTime dataEnvio = LocalDateTime.now(); // Valor padrão para CURRENT_TIMESTAMP

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    public Arquivos() {
        this.dataEnvio = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() {
        return id;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getNomeArmazenado() {
        return nomeArmazenado;
    }

    public void setNomeArmazenado(String nomeArmazenado) {
        this.nomeArmazenado = nomeArmazenado;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFinalidade() {
        return finalidade;
    }

    public void setFinalidade(String finalidade) {
        this.finalidade = finalidade;
    }

    public LocalDateTime getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(LocalDateTime dataEnvio) {
        this.dataEnvio = dataEnvio;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
