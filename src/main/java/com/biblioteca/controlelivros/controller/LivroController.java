package com.biblioteca.controlelivros.controller;

// Importa a anotação que define a classe como Controller MVC
import com.biblioteca.controlelivros.model.Livro;
import com.biblioteca.controlelivros.service.LivroService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
// Importa a anotação usada para mapear requisições GET
import org.springframework.web.bind.annotation.*;

//@RestController // Indica que é um controller

@Controller //RestController retorna JSON enquanto Controller retorna páginas HTML (usamos HTML + Thumeleaf no trabalho)
//@RequestMapping("/livros") // Mapeia as rotas no controller. Rota da API/backend para acessar os dados dos livros.
public class LivroController {

    // injeção de dependência. Controller sabe que o service existe.
    private final LivroService livroService;

    public LivroController(LivroService livroService) {
        this.livroService = livroService;
    }

    // rota pra pegar informações do servidor.
    @GetMapping("/") // getAll() Listas todos os livros que cadastrar aqui
    //@ResponseBody // indica que o valor de retorno de um método de controlador deve ser vinculado diretamente ao corpo da resposta HTTP (HTTP response body), e não mapeado para um nome de view (template HTML)
    public String listarLivros (Model model) {
        model.addAttribute("livros", livroService.getAll());

        model.addAttribute("totalAutores",
                livroService.contarAutores());

        model.addAttribute("totalGeneros",
                livroService.contarGeneros());

        return "index";
    }

    // ABRIR formulário
    @GetMapping("/form")
    public String abrirFormulario(Model model) {
        model.addAttribute("livro", new Livro());
        return "form";
    }

    @PostMapping // Requisição para postar/mandar para o servidor. Rota pra enviar informações.
    // O RequestBody pede pra você mandar no corpo da requisição os dados do Livro.
    public String salvarLivro(Livro livro) {
        livroService.save(livro);
        return "redirect:/";
    } // pega a informação enviada no corpo da requisição (Livro) e salva.
}
