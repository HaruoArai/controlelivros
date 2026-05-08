package com.biblioteca.controlelivros.repository;

// Importa a classe Livro que será manipulada no banco
import com.biblioteca.controlelivros.model.Livro;

// Importa a interface JpaRepository do Spring Data JPA
import org.springframework.data.jpa.repository.JpaRepository;

// Interface responsável pela comunicação com o banco de dados
public interface LivroRepository extends JpaRepository<Livro, Long> {

    // JpaRepository já fornece métodos prontos como:
    // save() -> salvar livro
    // findAll() -> listar livros
    // findById() -> buscar por ID
    // deleteById() -> excluir livro

}
