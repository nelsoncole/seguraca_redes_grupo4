package com.seguranca.redes.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CodigoAutenticacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String destino;

    private String codigo;

    private LocalDateTime dataGeracao;

    public void setCodigo(String codigo){
        this.codigo = codigo;
    }

    public void setDestino(String destino){
        this.destino = destino;
    }

    public void setDataGeracao(LocalDateTime dataGeracao){
        this.dataGeracao = dataGeracao;
    }

    public String getCodigo() {
        return codigo;
    }

    public String getDestino() {
        return destino;
    }

    public LocalDateTime getDataGeracao() {
        return dataGeracao;
    }

    // Método para verificar se o código ainda é válido (5 minutos)
    public boolean isValido() {
        LocalDateTime agora = LocalDateTime.now();
        return dataGeracao.plusMinutes(5).isAfter(agora);
    }
}
