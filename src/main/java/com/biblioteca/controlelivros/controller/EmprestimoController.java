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
                if ("pendentes".equals(filtro)) {
                    lista = emprestimoService.buscarPendentes(busca);
                } else if ("ativos".equals(filtro)) {
                    lista = emprestimoService.buscarAtivos(busca);
                } else if ("atrasados".equals(filtro)) {
                    lista = emprestimoService.buscarAtrasados(busca);
                } else {
                    lista = emprestimoService.buscar(busca);
                }
            } else if ("pendentes".equals(filtro)) {
                lista = emprestimoService.getPendentes();
            } else if ("prorrogacoes".equals(filtro)) {
                lista = emprestimoService.getProrrogacoesPendentes();
            } else if ("ativos".equals(filtro)) {
                lista = emprestimoService.getAtivos();
            } else if ("atrasados".equals(filtro)) {
                lista = emprestimoService.getAtrasados();
            } else {
                lista = emprestimoService.getTodosComPendentesNoTopo();
            }

            model.addAttribute("totalPendentes", emprestimoService.contarPendentes());
            model.addAttribute("totalAtivos", emprestimoService.contarAtivos());
            model.addAttribute("totalAtrasados", emprestimoService.contarAtrasados());
            model.addAttribute("totalProrrogacoes", emprestimoService.contarProrrogacoesPendentes());
        } else {
            lista = emprestimoService.doUsuarioComPendentesNoTopo(logado);

            List<Emprestimo> todos = lista;
            model.addAttribute("userTotalPendentes",  todos.stream().filter(e -> e.getStatus() == Emprestimo.Status.PENDENTE).count());
            model.addAttribute("userTotalAtivos",     todos.stream().filter(e -> (e.getStatus() == Emprestimo.Status.APROVADO || e.getStatus() == Emprestimo.Status.PRORROGACAO_SOLICITADA) && !e.isAtrasado()).count());
            model.addAttribute("userTotalAtrasados",  todos.stream().filter(e -> (e.getStatus() == Emprestimo.Status.APROVADO || e.getStatus() == Emprestimo.Status.PRORROGACAO_SOLICITADA) && e.isAtrasado()).count());
            model.addAttribute("userTotalDevolvidos", todos.stream().filter(e -> e.getStatus() == Emprestimo.Status.DEVOLVIDO).count());
            model.addAttribute("userTotalRecusados",  todos.stream().filter(e -> e.getStatus() == Emprestimo.Status.RECUSADO).count());
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
            return "redirect:/home";
        }

        try {
            emprestimoService.solicitarEmprestimo(livro, usuario);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Solicitação enviada! Aguarde a aprovação do bibliotecário.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }

        return "redirect:/home";
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

    // Usuário solicita prorrogação de prazo
    @PostMapping("/{id}/prorrogar")
    public String prorrogar(@PathVariable Long id,
                            @RequestParam(defaultValue = "7") int dias,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        Usuario usuario = usuarioLogado(authentication);

        try {
            emprestimoService.solicitarProrrogacao(id, dias, usuario);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Solicitação de prorrogação enviada! Aguarde a aprovação do bibliotecário.");
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/emprestimos";
    }

    // Bibliotecário aprova prorrogação
    @PostMapping("/{id}/prorrogar/aprovar")
    public String aprovarProrrogacao(@PathVariable Long id,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {

        if (!isBibliotecario(authentication)) return "redirect:/emprestimos";

        try {
            Emprestimo e = emprestimoService.aprovarProrrogacao(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso",
                    "Prorrogação aprovada! Novo prazo: " + e.getDataDevolucaoPrevistaFmt());
        } catch (IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("mensagemErro", ex.getMessage());
        }
        return "redirect:/emprestimos";
    }

    // Bibliotecário recusa prorrogação
    @PostMapping("/{id}/prorrogar/recusar")
    public String recusarProrrogacao(@PathVariable Long id,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {

        if (!isBibliotecario(authentication)) return "redirect:/emprestimos";

        try {
            emprestimoService.recusarProrrogacao(id);
            redirectAttributes.addFlashAttribute("mensagemSucesso", "Prorrogação recusada. Prazo original mantido.");
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
