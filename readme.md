# Controle de Livros

Sistema web para controle de livros, desenvolvido como trabalho da disciplina de Programação para Web.

## Sobre o projeto

O projeto tem como objetivo permitir o gerenciamento de livros em uma biblioteca, incluindo cadastro, edição, listagem e controle de usuários.

Nesta versão, foram adicionadas melhorias na interface e na organização das páginas, incluindo suporte ao modo escuro e reaproveitamento de layout com Thymeleaf Layout.

## Funcionalidades

* Cadastro de usuários
* Login e autenticação
* Listagem de livros
* Cadastro de livros
* Edição de livros
* Tela de perfil do usuário
* Suporte a modo escuro
* Layout reutilizável com Thymeleaf Layout

## Tecnologias utilizadas

* Java
* Spring Boot
* Spring Security
* Thymeleaf
* Thymeleaf Layout Dialect
* Maven
* HTML
* CSS
* JavaScript
* Banco de dados H2

## Estrutura das alterações

As principais melhorias desta versão envolvem:

* Criação de layout base reutilizável
* Criação de fragments Thymeleaf
* Separação de arquivos CSS e JavaScript
* Implementação de alternância entre tema claro e tema escuro
* Ajustes nas telas de login, cadastro, perfil e formulário de livros

## Como executar o projeto

Clone o repositório:

```bash
git clone https://github.com/evertonmarianogomes/controlelivros.git
```

Acesse a pasta do projeto:

```bash
cd controlelivros
```

Execute com Maven:

```bash
./mvnw spring-boot:run
```

No Windows, caso esteja usando PowerShell ou CMD:

```bash
mvnw.cmd spring-boot:run
```

Depois acesse no navegador:

```text
http://localhost:8080
```

## Observações

Arquivos de banco local, como arquivos `.db` e `.trace.db`, não devem ser enviados para o repositório.

Arquivos de teste local contendo dados de login ou informações temporárias também não devem ser versionados.

## Branch da alteração

```text
feature/dark-mode-thymeleaf-layout
```

## Principais mudanças

Para detalhes das mudanças realizadas, consulte o arquivo:

```text
ALTERACOES.md
```
