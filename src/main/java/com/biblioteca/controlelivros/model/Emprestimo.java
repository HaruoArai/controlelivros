package com.biblioteca.controlelivros.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "emprestimos")
public class Emprestimo {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public enum Status {
        PENDENTE, APROVADO, DEVOLVIDO, RECUSADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "livro_id")
    private Livro livro;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDENTE;

    private LocalDate dataSolicitacao;
    private LocalDate dataRetirada;
    private LocalDate dataDevolucaoPrevista;
    private LocalDate dataDevolucaoReal;

    public Emprestimo() {
        this.dataSolicitacao = LocalDate.now();
    }


    public boolean isAtrasado() {
        return status == Status.APROVADO
                && dataDevolucaoPrevista != null
                && LocalDate.now().isAfter(dataDevolucaoPrevista);
    }

    public String getDataSolicitacaoFmt() {
        return dataSolicitacao != null ? dataSolicitacao.format(FMT) : "—";
    }
    public String getDataRetiradaFmt() {
        return dataRetirada != null ? dataRetirada.format(FMT) : "—";
    }
    public String getDataDevolucaoPrevistaFmt() {
        return dataDevolucaoPrevista != null ? dataDevolucaoPrevista.format(FMT) : "—";
    }
    public String getDataDevolucaoRealFmt() {
        return dataDevolucaoReal != null ? dataDevolucaoReal.format(FMT) : "—";
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Livro getLivro() { return livro; }
    public void setLivro(Livro livro) { this.livro = livro; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public LocalDate getDataSolicitacao() { return dataSolicitacao; }
    public void setDataSolicitacao(LocalDate dataSolicitacao) { this.dataSolicitacao = dataSolicitacao; }

    public LocalDate getDataRetirada() { return dataRetirada; }
    public void setDataRetirada(LocalDate dataRetirada) { this.dataRetirada = dataRetirada; }

    public LocalDate getDataDevolucaoPrevista() { return dataDevolucaoPrevista; }
    public void setDataDevolucaoPrevista(LocalDate dataDevolucaoPrevista) { this.dataDevolucaoPrevista = dataDevolucaoPrevista; }

    public LocalDate getDataDevolucaoReal() { return dataDevolucaoReal; }
    public void setDataDevolucaoReal(LocalDate dataDevolucaoReal) { this.dataDevolucaoReal = dataDevolucaoReal; }
}
