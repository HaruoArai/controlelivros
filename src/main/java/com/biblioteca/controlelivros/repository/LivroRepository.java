package com.biblioteca.controlelivros.repository;

// Importa a classe Livro que será manipulada no banco
import com.biblioteca.controlelivros.model.Livro;

// Importa a interface JpaRepository do Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import org.springframework.data.repository.query.Param;

// Interface responsável pela comunicação com o banco de dados que estende ao Jpa. Livro é a classe que estamos tratando. Long é o identificador, no caso, tipo do Id que é um Long.
public interface LivroRepository extends JpaRepository<Livro, Long> {

    // JpaRepository já fornece métodos prontos como:
    // save() -> salvar livro
    // findAll() -> listar livros
    // findById() -> buscar por ID
    // deleteById() -> excluir livro

    @Query("""
        SELECT l
        FROM Livro l
        WHERE LOWER(l.titulo) LIKE LOWER(CONCAT('%', :busca, '%'))
           OR LOWER(l.autor) LIKE LOWER(CONCAT('%', :busca, '%'))
           OR LOWER(l.genero) LIKE LOWER(CONCAT('%', :busca, '%'))
    """)
    List<Livro> buscar(@Param("busca") String busca);

}


