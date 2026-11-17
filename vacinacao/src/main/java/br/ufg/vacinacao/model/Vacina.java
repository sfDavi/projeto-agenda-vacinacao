package br.ufg.vacinacao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "vacinas")
public class Vacina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "titulo", length = 60, nullable = false)
    private String titulo;

    @Column(name = "descricao", length = 200)
    private String descricao;

    @Column(name = "doses", nullable = false)
    private int doses = 1;

    /** Nulo quando a vacina tem dose única. */
    @Convert(converter = PeriodicidadeConverter.class)
    @Column(name = "periodicidade")
    private Periodicidade periodicidade;

    /** Quantidade de unidades de {@link #periodicidade} entre as doses. Nulo quando dose única. */
    @Column(name = "intervalo")
    private Integer intervalo;

    public Vacina() {
    }

    public boolean isDoseUnica() {
        return doses <= 1;
    }

    /** Resumo do esquema para as telas: "Dose única" ou "3 doses, a cada 1 mês". */
    public String getEsquema() {
        if (isDoseUnica() || periodicidade == null || intervalo == null) {
            return "Dose única";
        }
        return doses + " doses, a cada " + periodicidade.descrever(intervalo);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getDoses() {
        return doses;
    }

    public void setDoses(int doses) {
        this.doses = doses;
    }

    public Periodicidade getPeriodicidade() {
        return periodicidade;
    }

    public void setPeriodicidade(Periodicidade periodicidade) {
        this.periodicidade = periodicidade;
    }

    public Integer getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(Integer intervalo) {
        this.intervalo = intervalo;
    }
}
