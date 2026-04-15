package model;

import java.time.Duration;
import java.time.LocalDateTime;

public class Ponto {

    private Long id;
    private Long motoristaId;
    private LocalDateTime registro;
    private String tipo;
    private Duration duracaoEfetiva;
    private LocalDateTime dataCriacao;
    private String mensagemConformidade;

    public Ponto(){}

    public Ponto(Long id, Long motoristaId, LocalDateTime registro, String tipo, Duration duracaoEfetiva, LocalDateTime dataCriacao, String mensagemConformidade) {
        this.id = id;
        this.motoristaId = motoristaId;
        this.registro = registro;
        this.tipo = tipo;
        this.duracaoEfetiva = duracaoEfetiva;
        this.dataCriacao = dataCriacao;
        this.mensagemConformidade = mensagemConformidade;
    }

    public Ponto(LocalDateTime registro, String tipo) {
        this.registro = registro;
        this.tipo = tipo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMotoristaId() {
        return motoristaId;
    }

    public void setMotoristaId(Long motoristaId) {
        this.motoristaId = motoristaId;
    }

    public LocalDateTime getRegistro() {
        return registro;
    }

    public void setRegistro(LocalDateTime registro) {
        this.registro = registro;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Duration getDuracaoEfetiva() {
        return duracaoEfetiva;
    }

    public void setDuracaoEfetiva(Duration duracaoEfetiva) {
        this.duracaoEfetiva = duracaoEfetiva;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public String getMensagemConformidade() {
        return mensagemConformidade;
    }

    public void setMensagemConformidade(String mensagemConformidade) {
        this.mensagemConformidade = mensagemConformidade;
    }
}
