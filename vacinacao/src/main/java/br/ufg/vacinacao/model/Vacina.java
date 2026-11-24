package br.ufg.vacinacao.model;

import br.ufg.vacinacao.util.DatasUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

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

    /** Alergias que contraindicam esta vacina (tabela vacina_alergia, criada em complementos.sql). */
    @ManyToMany
    @JoinTable(name = "vacina_alergia",
            joinColumns = @JoinColumn(name = "vacina_id"),
            inverseJoinColumns = @JoinColumn(name = "alergia_id"))
    @OrderBy("nome")
    private Set<Alergia> contraindicacoes = new LinkedHashSet<>();

    public Vacina() {
    }

    /**
     * Soma {@code quantidade} intervalos à data (usado pelo controle de doses).
     * Para dose única devolve a própria data.
     */
    public LocalDate somarIntervalos(LocalDate data, int quantidade) {
        if (isDoseUnica() || periodicidade == null || intervalo == null) {
            return data;
        }
        return DatasUtil.somar(data, periodicidade, quantidade * intervalo);
    }

    public Set<Alergia> getContraindicacoes() {
        return contraindicacoes;
    }

    public void setContraindicacoes(Set<Alergia> contraindicacoes) {
        this.contraindicacoes = contraindicacoes;
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
