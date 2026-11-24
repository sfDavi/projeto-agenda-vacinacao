package br.ufg.vacinacao.model;

import br.ufg.vacinacao.util.DatasUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "agendas")
public class Agenda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    /** "HH:mm" - o MER permite time ou char(5); char(5) evita problemas de fuso. */
    @Column(name = "hora", columnDefinition = "char(5)", nullable = false)
    private String hora;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", columnDefinition = "char(10)", nullable = false)
    private Situacao situacao = Situacao.AGENDADO;

    /** Data em que a agenda foi dada como realizada ou cancelada (nula enquanto AGENDADO). */
    @Column(name = "data_situacao")
    private LocalDate dataSituacao;

    @Column(name = "observacoes", length = 200)
    private String observacoes;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "vacina_id", nullable = false)
    private Vacina vacina;

    /** Número da dose dentro da série (1..vacina.doses) - adequação ao MER. */
    @Column(name = "dose", nullable = false)
    private int dose;

    public Agenda() {
    }

    public String getDataFormatada() {
        return DatasUtil.formatar(data);
    }

    public String getDataSituacaoFormatada() {
        return DatasUtil.formatar(dataSituacao);
    }

    /** Dias de hoje até a data da agenda (negativo = já passou). Calculado, não armazenado. */
    public long getDiasAte() {
        return ChronoUnit.DAYS.between(LocalDate.now(), data);
    }

    /** {@code true} se ainda está AGENDADO e a data já passou sem baixa. */
    public boolean isAtrasada() {
        return situacao == Situacao.AGENDADO && data.isBefore(LocalDate.now());
    }

    /** "hoje", "amanhã", "em 5 dias", "atrasada há 3 dias". */
    public String getPrazo() {
        long dias = getDiasAte();
        if (dias == 0) {
            return "hoje";
        }
        if (dias == 1) {
            return "amanhã";
        }
        if (dias > 1) {
            return "em " + dias + " dias";
        }
        return "atrasada há " + (-dias) + (dias == -1 ? " dia" : " dias");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public Situacao getSituacao() {
        return situacao;
    }

    public void setSituacao(Situacao situacao) {
        this.situacao = situacao;
    }

    public LocalDate getDataSituacao() {
        return dataSituacao;
    }

    public void setDataSituacao(LocalDate dataSituacao) {
        this.dataSituacao = dataSituacao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Vacina getVacina() {
        return vacina;
    }

    public void setVacina(Vacina vacina) {
        this.vacina = vacina;
    }

    public int getDose() {
        return dose;
    }

    public void setDose(int dose) {
        this.dose = dose;
    }
}
