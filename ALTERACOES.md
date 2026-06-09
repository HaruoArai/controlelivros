# Alterações realizadas

## Resumo

Foram realizadas melhorias na interface do sistema, com foco na adição de suporte ao modo escuro, reaproveitamento de estrutura HTML com Thymeleaf Layout e ajustes em telas de cadastro, edição, login e perfil.

## Principais alterações

### Dark mode

* Adicionada funcionalidade de alternância entre tema claro e tema escuro.
* Criado script JavaScript para controlar o modo escuro.
* Adicionadas classes e estilos CSS específicos para adaptação visual da interface.
* Ajustados elementos visuais para manter contraste e legibilidade no modo escuro.

### Thymeleaf Layout

* Adicionado uso de Thymeleaf Layout para reaproveitamento de estrutura HTML.
* Criados arquivos de layout base para evitar repetição de código entre páginas.
* Criados fragments reutilizáveis para partes comuns da interface.
* Atualizadas páginas existentes para utilizar a nova estrutura de layout.

### Telas ajustadas

* Ajustada a tela de criação e edição de livros.
* Atualizada a tela de login.
* Atualizada a tela de cadastro de usuário.
* Atualizada a tela de perfil.
* Substituída a estrutura antiga da página inicial por uma nova tela baseada no layout reutilizável.

### Arquivos estáticos

* Adicionados novos arquivos CSS para organização dos estilos.
* Adicionados novos arquivos JavaScript para controle do tema e componentes da interface.
* Separados scripts e estilos por responsabilidade para melhorar a manutenção do projeto.

## Observações

* Os arquivos de banco local não devem ser incluídos no commit.
* Arquivos de teste local, como dados de login usados apenas durante o desenvolvimento, também não devem ser enviados ao repositório.
* As alterações foram feitas na branch `feature/dark-mode-thymeleaf-layout`.
