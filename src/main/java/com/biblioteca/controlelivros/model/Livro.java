package com.biblioteca.controlelivros.model;

// Importa as anotações do JPA para integração com banco
import jakarta.persistence.*;

// Annotation Entity faz com que toda essa classe seja persistida na camada de repository que vai, por sua vez, se conectar com o banco de dados.
@Entity
// Nome da tabela.
@Table(name = "tabela_livros")
public class Livro {
    // Faz Id ser gerado automaticamente
    @Id

    // Estratégia para gerar o Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Atributos da tabela Livro
    // Colocar Interger ao invés de Int. Assim o valor inicial será null, não 0. Placeholder agora funciona.
    private String titulo;
    private String autor;
    private String genero;
    private Integer ano;
    private String isbn;



    // Construtor vazio
    public Livro() {
    }

    // Construtor com parâmetros
    public Livro(Long id, String titulo, String autor, String genero, Integer ano, String isbn) {
        this.id=id;
        this.titulo=titulo;
        this.autor=autor;
        this.genero=genero;
        this.ano=ano;
        this.isbn =isbn;
    }

    // Getter para retornar valores e Setter para alterar os valores
    public Long getId() {
        return id;
    }
    public void setId (Long Id) {
        this.id=Id;
    }

    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }
    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getGenero() {
        return genero;
    }
    public void setGenero(String genero) {
        this.genero = genero;
    }

    public Integer getAno() {
        return ano;
    }
    public void setAno(Integer ano) {
        this.ano = ano;
    }

    public String getIsbn() {
        return isbn;
    }
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
}
