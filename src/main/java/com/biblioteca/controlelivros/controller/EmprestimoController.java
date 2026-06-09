package com.biblioteca.controlelivros.controller;

import com.biblioteca.controlelivros.model.Emprestimo;
import com.biblioteca.controlelivros.model.Livro;
import com.biblioteca.controlelivros.model.Role;
import com.biblioteca.controlelivros.model.Usuario;
import com.biblioteca.controlelivros.service.EmprestimoService;
import com.biblioteca.controlelivros.service.LivroService;
import com.biblioteca.controlelivros.service.UsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/emprestimos")
public class EmprestimoController {

    private final EmprestimoService emprestimoService;
    private final LivroService livroService;
    private final UsuarioService usuarioService;

    public EmprestimoController(EmprestimoService emprestimoService,
                                LivroService livroService,
                                UsuarioService usuarioService) {
        this.emprestimoService = emprestimoService;
        this.livroService = livroService;
        this.usuarioService = usuarioService;
    }

    // Painel do bibliotecário — lista todos os empréstimos
    @GetMapping
    public String painel(@RequestParam(required = false) String busca,
                         @RequestParam(required = false) String filtro,
                         Authentication authentication,
                         Model model) {

        Usuario logado = usuarioLogado(authentication);
        boolean isBibliotecario = logado.getRole() == Role.BIBLIOTECARIO;
        model.addAttribute("isBibliotecario", isBibliotecario);
        model.addAttribute("usuarioLogado", logado);

        List<Emprestimo> lista;

        if (isBibliotecario) {
            if (busca != null && !busca.isBlank()) {
                lista = emprestimoService.buscar(busca);
            } else if ("pendentes".equals(filtro)) {
                lista = emprestimoService.getPendentes();
            } else if ("ativos".equals(filtro)) {
                lista = emprestimoService.getAtivos();
            } else {
                lista = emprestimoService.getTodos();
            }

            model.addAttribute("totalPendentes", emprestimoService.contarPendentes());
            model.addAttribute("totalAtivos", emprestimoService.contarAtivos());
            model.addAttribute("totalAtrasados", emprestimoService.contarAtrasados());
        } else {
            // Usuário comum vê apenas seus próprios empréstimos
            lista = emprestimoService.doUsuario(logado);
        }

        model.addAttribute("emprestimos", lista);
        model.addAttribute("busca", busca);
        model.addAttribute("filtro", filtro);
        return "emprestimos";
    }

    // Usuário solicita empréstimo de um livro
    @PostMapping("/solicitar/{livroId}")
    public String solicitar(@PathVariable Long livroId,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioLogado(authentication);
        Livro livro = livroService.getById(livroId);

        if (livro == null) {
            redirectAttributes.addFlashAttribute("mensagemErro", "Livro não encontrado.");
            return "redirect:/";
        }

        try {
            emprestimoService.solicitarEmprestimo(livro, usuario);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Solicitação enviada! Aguarde a aprovação do bibliotecário.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }

        return "redirect:/";
    }

    // Bibliotecário aprova
    @PostMapping("/{id}/aprovar")
    public String aprovar(@PathVariable Long id,
                          @RequestParam(defaultValue = "14") int dias,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {

        if (!isBibliotecario(authentication)) return "redirect:/emprestimos";

        try {
            emprestimoService.aprovar(id, dias);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Empréstimo aprovado com sucesso!");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/emprestimos";
    }

    // Bibliotecário recusa
    @PostMapping("/{id}/recusar")
    public String recusar(@PathVariable Long id,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {

        if (!isBibliotecario(authentication)) return "redirect:/emprestimos";

        try {
            emprestimoService.recusar(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Solicitação recusada.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/emprestimos";
    }

    // Bibliotecário registra devolução
    @PostMapping("/{id}/devolver")
    public String devolver(@PathVariable Long id,
                           Authentication authentication,
                           RedirectAttributes redirectAttributes) {

        if (!isBibliotecario(authentication)) return "redirect:/emprestimos";

        try {
            emprestimoService.registrarDevolucao(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Devolução registrada com sucesso!");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/emprestimos";
    }

    private Usuario usuarioLogado(Authentication authentication) {
        return usuarioService.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalStateException("Usuário não encontrado."));
    }

    private boolean isBibliotecario(Authentication authentication) {
        return usuarioLogado(authentication).getRole() == Role.BIBLIOTECARIO;
    }
}
