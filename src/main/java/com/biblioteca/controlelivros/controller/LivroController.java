package com.biblioteca.controlelivros.controller;

// Importa a anotação que define a classe como Controller MVC
import com.biblioteca.controlelivros.model.Livro;
import com.biblioteca.controlelivros.model.Role;
import com.biblioteca.controlelivros.model.Usuario;
import com.biblioteca.controlelivros.service.LivroService;
import com.biblioteca.controlelivros.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
// Importa a anotação usada para mapear requisições GET
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.security.core.Authentication;
import com.biblioteca.controlelivros.service.EmprestimoService;
import com.biblioteca.controlelivros.model.Emprestimo;

import java.util.List;
//@RestController // Indica que é um controller

@Controller // RestController retorna JSON enquanto Controller retorna páginas HTML (usamos
            // HTML + Thumeleaf no trabalho)
// @RequestMapping("/livros") // Mapeia as rotas no controller. Rota da
// API/backend para acessar os dados dos livros.
public class LivroController {

    // injeção de dependência. Controller sabe que o service existe.
    private final LivroService livroService;
    private final UsuarioService usuarioService;
    private final EmprestimoService emprestimoService;

    public LivroController(LivroService livroService, UsuarioService usuarioService, EmprestimoService emprestimoService) {
        this.livroService = livroService;
        this.usuarioService = usuarioService;
        this.emprestimoService = emprestimoService;
    }

    // rota pra pegar informações do servidor.
    @GetMapping("/") // getAll() Listas todos os livros que cadastrar aqui
    //@ResponseBody // indica que o valor de retorno de um método de controlador deve ser vinculado diretamente ao corpo da resposta HTTP (HTTP response body), e não mapeado para um nome de view (template HTML)
    public String listarLivros (
            @RequestParam(required = false) String busca,
            Model model) {

        // Criei a váriavel livro para simplificar o filtro Buscar
        List<Livro> livros;

        if (busca != null && !busca.isBlank()) {
            livros = livroService.buscar(busca);
        } else {
            livros = livroService.getAll();
        }

        model.addAttribute("livros", livros);

        model.addAttribute("totalAutores",
                livros.stream()
                        .map(Livro::getAutor)
                        .distinct()
                        .count());

        model.addAttribute("totalGeneros",
                livros.stream()
                        .map(Livro::getGenero)
                        .distinct()
                        .count());

        model.addAttribute("totalEstoque",
                livros.stream()
                        .mapToInt(Livro::getQuantidadeDisponivel)
                        .sum());

        model.addAttribute("emprestimosAtivos",
                emprestimoService.contarAtivos());

        model.addAttribute("emprestimosPendentes",
                emprestimoService.contarPendentes());

        return "index";
    }

    // PÁGINA USUÁRIO
    @GetMapping("/home")
    public String home(
            @RequestParam(required = false) String busca,
            Authentication authentication,
            Model model) {

        List<Livro> livros;

        if (busca != null && !busca.isBlank()) {
            livros = livroService.buscar(busca);
        } else {
            livros = livroService.getAll();
        }

        Usuario usuario = usuarioService
                .findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado."));

        List<Emprestimo> emprestimosUsuario = emprestimoService.doUsuario(usuario);

        List<Long> livrosPendentes = emprestimosUsuario.stream()
                .filter(e -> e.getStatus() == Emprestimo.Status.PENDENTE)
                .map(e -> e.getLivro().getId())
                .toList();

        List<Long> livrosAprovados = emprestimosUsuario.stream()
                .filter(e -> e.getStatus() == Emprestimo.Status.APROVADO)
                .map(e -> e.getLivro().getId())
                .toList();

        List<Long> livrosSolicitados = new java.util.ArrayList<>();
        livrosSolicitados.addAll(livrosPendentes);
        livrosSolicitados.addAll(livrosAprovados);

        model.addAttribute("livrosSolicitados", livrosSolicitados);
        model.addAttribute("livrosPendentes", livrosPendentes);
        model.addAttribute("livrosAprovados", livrosAprovados);
        model.addAttribute("totalAutores",
                livros.stream()
                        .map(Livro::getAutor)
                        .distinct()
                        .count());
        model.addAttribute("totalGeneros",
                livros.stream()
                        .map(Livro::getGenero)
                        .distinct()
                        .count());
        model.addAttribute("livros", livros);
        model.addAttribute("pageTitle", "Catálogo");

        return "home";
    }

    // ABRIR formulário
    @GetMapping("/form")
    public String abrirFormulario(Model model) {
        model.addAttribute("livro", new Livro());
        model.addAttribute("pageTitle", "Criar Livro");

        return "form";
    }

    @PostMapping("/livros") // Requisição para postar/mandar para o servidor. Rota pra enviar informações.
    public String salvarLivro(
            Livro livro,
            RedirectAttributes redirectAttributes) {

        boolean edicao = livro.getId() != null;

        livroService.save(livro);

        if (edicao) {
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Livro editado com sucesso!");
        } else {
            redirectAttributes.addFlashAttribute(
                    "mensagemSucesso",
                    "Livro cadastrado com sucesso!");
        }

        return "redirect:/";
    } // pega a informação enviada no corpo da requisição (Livro) e salva.

    // EXCLUIR
    @GetMapping("/livros/delete/{id}")
    public String excluirLivro(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        livroService.delete(id);

        redirectAttributes.addFlashAttribute(
                "mensagemSucesso",
                "Livro excluído com sucesso!");

        return "redirect:/";
        }

    // EDITAR
    @GetMapping("/livros/editar/{id}")
    public String editarLivro(@PathVariable Long id, Model model) {

        Livro livro = livroService.getById(id);

        model.addAttribute("livro", livro);
        model.addAttribute("pageTitle", "Editar Livro");

        return "form";
    }

    // Método para redirecionar bibliocário e user para seus respectivos menus
    @GetMapping("/acervo")
    public String acervo(Authentication authentication) {

        boolean bibliotecario = authentication.getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_BIBLIOTECARIO"));

        if (bibliotecario) {
            return "redirect:/";
        }

        return "redirect:/home";
    }

}
