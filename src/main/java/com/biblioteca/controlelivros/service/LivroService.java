package com.biblioteca.controlelivros.service;

import com.biblioteca.controlelivros.model.Livro;
import com.biblioteca.controlelivros.repository.LivroRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service // Indica que é um sevice
public class LivroService {

    private final LivroRepository livroRepository;

    // Injeta dependência. É o mesmo que fazer com a annotation @Autowired
    public LivroService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    // método público que vai listar todos os livros
    // findAll vai listar tudo que passar no livroRepository
    // método LISTAR
    public List<Livro> getAll () {return livroRepository.findAll();}

    // CONTAR AUTORES ÚNICOS
    public long contarAutores() {
        return livroRepository.findAll()
                .stream()
                .map(Livro::getAutor)
                .distinct()
                .count();
    }

    // CONTAR GÊNEROS ÚNICOS
    public long contarGeneros() {
        return livroRepository.findAll()
                .stream()
                .map(Livro::getGenero)
                .distinct()
                .count();
    }

    // BUSCAR POR ID
    public Livro getById(Long id) {return livroRepository.findById(id).orElse(null);}

    // método  SALVAR
    public Livro save(Livro livro) {return livroRepository.save(livro);}

    // método DELETAR
    public void delete(Long id){livroRepository.deleteById(id);}

    public List<Livro> buscar(String busca) {
        return livroRepository.buscar(busca);
    }
}
