package com.biblioteca.controlelivros.controller;

// Importa a anotação que define a classe como Controller MVC
import org.springframework.stereotype.Controller;

// Importa a anotação usada para mapear requisições GET
import org.springframework.web.bind.annotation.GetMapping;

// Define esta classe como um Controller do Spring Boot
@Controller
public class LivroController {

    // Define a rota principal do sistema
    // Quando acessar localhost:8080
    @GetMapping("/")

    // Método responsável por abrir a página inicial 
    public String home() {

        // Retorna o arquivo index.html da pasta templates
        return "index";
    }
}
