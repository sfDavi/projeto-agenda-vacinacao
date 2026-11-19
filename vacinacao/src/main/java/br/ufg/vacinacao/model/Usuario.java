package br.ufg.vacinacao.model;

import br.ufg.vacinacao.util.DatasUtil;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nome", length = 60, nullable = false)
    private String nome;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Convert(converter = SexoConverter.class)
    @Column(name = "sexo", columnDefinition = "char(1)", nullable = false)
    private Sexo sexo;

    @Column(name = "logradouro", length = 60)
    private String logradouro;

    /** Coluna NUMERIC(10) no MER. */
    @Column(name = "numero", columnDefinition = "decimal(10,0)")
    private Long numero;

    @Column(name = "setor", length = 40)
    private String setor;

    @Column(name = "cidade", length = 40)
    private String cidade;

    @Enumerated(EnumType.STRING)
    @Column(name = "uf", columnDefinition = "char(2)", nullable = false)
    private UF uf;

    /** Relacionamento N:N do MER (tabela usuario_alergia). */
    @ManyToMany
    @JoinTable(name = "usuario_alergia",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "alergia_id"))
    @OrderBy("nome")
    private Set<Alergia> alergias = new LinkedHashSet<>();

    /** Relacionamento 1:N do MER. Excluir o usuário exclui também as suas agendas. */
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.REMOVE)
    @OrderBy("data, hora")
    private List<Agenda> agendas = new ArrayList<>();

    public Usuario() {
    }

    /** Idade em anos completos na data de hoje. */
    public Integer getIdade() {
        if (dataNascimento == null) {
            return null;
        }
        return Period.between(dataNascimento, LocalDate.now()).getYears();
    }

    public String getDataNascimentoFormatada() {
        return DatasUtil.formatar(dataNascimento);
    }

    /** "Goiânia/GO", "GO" ... para as listagens. */
    public String getCidadeUf() {
        if (cidade == null || cidade.isBlank()) {
            return uf == null ? "" : uf.name();
        }
        return uf == null ? cidade : cidade + "/" + uf.name();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(LocalDate dataNascimento) {
        this.dataNascimento = dataNascimento;
    }

    public Sexo getSexo() {
        return sexo;
    }

    public void setSexo(Sexo sexo) {
        this.sexo = sexo;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public Long getNumero() {
        return numero;
    }

    public void setNumero(Long numero) {
        this.numero = numero;
    }

    public String getSetor() {
        return setor;
    }

    public void setSetor(String setor) {
        this.setor = setor;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public UF getUf() {
        return uf;
    }

    public void setUf(UF uf) {
        this.uf = uf;
    }

    public Set<Alergia> getAlergias() {
        return alergias;
    }

    public void setAlergias(Set<Alergia> alergias) {
        this.alergias = alergias;
    }

    public List<Agenda> getAgendas() {
        return agendas;
    }

    public void setAgendas(List<Agenda> agendas) {
        this.agendas = agendas;
    }
}
