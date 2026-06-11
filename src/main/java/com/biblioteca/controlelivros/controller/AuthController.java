package com.biblioteca.controlelivros.controller;

import com.biblioteca.controlelivros.model.Usuario;
import com.biblioteca.controlelivros.service.UsuarioService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpServletRequest;

import com.biblioteca.controlelivros.model.ValidationGroups;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;

    public AuthController(UsuarioService usuarioService, AuthenticationManager authenticationManager) {
        this.usuarioService = usuarioService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
            Authentication authentication,
            Model model) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/";
        }

        if (error != null) {
            model.addAttribute("loginError", "E-mail ou senha inválidos. Tente novamente.");
        }

        model.addAttribute("pageTitle", "Login");

        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("pageTitle", "Registrar");
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@Valid Usuario usuario,
            BindingResult result,
            Model model,
            HttpSession session) {
        if (usuarioService.existsByEmail(usuario.getEmail())) {
            result.rejectValue("email", null, "Este e-mail já está em uso.");
        }

        if (!usuario.getSenha().equals(usuario.getConfirmSenha())) {
            result.rejectValue("confirmSenha", null, "As senhas não coincidem.");
        }

        if (result.hasErrors()) {
            return "register";
        }

        String rawSenha = usuario.getSenha();
        usuarioService.save(usuario);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(usuario.getEmail(), rawSenha));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Persistir a autenticação na sessão HTTP
        SecurityContext securityContext = SecurityContextHolder.getContext();
        session.setAttribute("SPRING_SECURITY_CONTEXT", securityContext);

        return "redirect:/";
    }

    @GetMapping("/profile")
    public String profilePage(Authentication authentication, Model model) {
        String email = authentication.getName();
        Usuario usuario = usuarioService.findByEmail(email).orElse(null);
        model.addAttribute("usuario", usuario);
        model.addAttribute("pageTitle", "Profile");
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Usuario usuario,
            BindingResult result,
            Authentication authentication,
            Model model,
            HttpServletRequest request) {
        String emailAtual = authentication.getName();
        Usuario usuarioAtual = usuarioService.findByEmail(emailAtual).orElse(null);

        if (usuarioAtual == null) {
            return "redirect:/login";
        }

        // Validar nome
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            result.rejectValue("nome", null, "Nome é obrigatório");
        }

        // Validar email
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            result.rejectValue("email", null, "E-mail é obrigatório");
        } else if (!usuario.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            result.rejectValue("email", null, "E-mail inválido");
        } else if (!usuario.getEmail().equals(emailAtual) && usuarioService.existsByEmail(usuario.getEmail())) {
            result.rejectValue("email", null, "Este e-mail já está em uso.");
        }

        // Validar senha se for alterada
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            if (!usuario.getSenha().equals(usuario.getConfirmSenha())) {
                result.rejectValue("confirmSenha", null, "As senhas não coincidem.");
            }
            if (usuario.getSenha().length() < 6) {
                result.rejectValue("senha", null, "A senha deve ter ao menos 6 caracteres.");
            }
        }

        if (result.hasErrors()) {
            model.addAttribute("usuario", usuario);
            return "profile";
        }

        // Atualizar dados
        usuarioAtual.setNome(usuario.getNome());
        usuarioAtual.setEmail(usuario.getEmail());
        usuarioAtual.setRole(usuario.getRole());

        // Atualizar senha se foi fornecida
        if (usuario.getSenha() != null && !usuario.getSenha().isBlank()) {
            usuarioAtual.setSenha(usuario.getSenha());
            usuarioService.save(usuarioAtual);
        } else {
            usuarioService.update(usuarioAtual);
        }

        request.getSession().invalidate();

        SecurityContextHolder.clearContext();

        return "redirect:/login";
    }
}
