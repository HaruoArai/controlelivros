package com.biblioteca.controlelivros.service;

import com.biblioteca.controlelivros.model.Emprestimo;
import com.biblioteca.controlelivros.model.Emprestimo.Status;
import com.biblioteca.controlelivros.model.Livro;
import com.biblioteca.controlelivros.model.Usuario;
import com.biblioteca.controlelivros.repository.EmprestimoRepository;
import com.biblioteca.controlelivros.repository.LivroRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmprestimoService {

    private final EmprestimoRepository emprestimoRepository;
    private final LivroRepository livroRepository;

    public EmprestimoService(EmprestimoRepository emprestimoRepository,
                             LivroRepository livroRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.livroRepository = livroRepository;
    }

    public boolean usuarioJaPossuiSolicitacao(Usuario usuario, Long livroId) {
        return emprestimoRepository.existsEmprestimoAtivoByUsuarioAndLivro(usuario, livroId);
    }

    // Usuário solicita empréstimo
    @Transactional
    public Emprestimo solicitarEmprestimo(Livro livro, Usuario usuario) {
        if (!livro.isDisponivel()) {
            throw new IllegalStateException("Livro sem estoque disponível.");
        }
        if (emprestimoRepository.existsEmprestimoAtivoByUsuarioAndLivro(usuario, livro.getId())) {
            throw new IllegalStateException("Você já possui um empréstimo ativo ou solicitação pendente deste livro.");
        }

        Emprestimo e = new Emprestimo();
        e.setLivro(livro);
        e.setUsuario(usuario);
        e.setStatus(Status.PENDENTE);
        e.setDataSolicitacao(LocalDate.now());
        return emprestimoRepository.save(e);
    }

    // Bibliotecário aprova
    @Transactional
    public Emprestimo aprovar(Long emprestimoId, int diasPrazo) {
        Emprestimo e = getById(emprestimoId);
        if (e.getStatus() != Status.PENDENTE) {
            throw new IllegalStateException("Apenas empréstimos pendentes podem ser aprovados.");
        }
        if (!e.getLivro().isDisponivel()) {
            throw new IllegalStateException("Livro sem estoque disponível.");
        }

        e.setStatus(Status.APROVADO);
        e.setDataRetirada(LocalDate.now());
        e.setDataDevolucaoPrevista(LocalDate.now().plusDays(diasPrazo));

        Livro livro = e.getLivro();
        livro.setQuantidadeEmprestada(livro.getQuantidadeEmprestada() + 1);
        livroRepository.save(livro);

        return emprestimoRepository.save(e);
    }

    // Bibliotecário recusa
    @Transactional
    public Emprestimo recusar(Long emprestimoId) {
        Emprestimo e = getById(emprestimoId);
        if (e.getStatus() != Status.PENDENTE) {
            throw new IllegalStateException("Apenas empréstimos pendentes podem ser recusados.");
        }
        e.setStatus(Status.RECUSADO);
        return emprestimoRepository.save(e);
    }

    // Bibliotecário registra devolução
    @Transactional
    public Emprestimo registrarDevolucao(Long emprestimoId) {
        Emprestimo e = getById(emprestimoId);
        if (e.getStatus() != Status.APROVADO && e.getStatus() != Status.PRORROGACAO_SOLICITADA) {
            throw new IllegalStateException("Apenas empréstimos ativos podem ser devolvidos.");
        }

        e.setStatus(Status.DEVOLVIDO);
        e.setDataDevolucaoReal(LocalDate.now());

        Livro livro = e.getLivro();
        livro.setQuantidadeEmprestada(Math.max(0, livro.getQuantidadeEmprestada() - 1));
        livroRepository.save(livro);

        return emprestimoRepository.save(e);
    }

    // Usuário solicita prorrogação de prazo
    @Transactional
    public Emprestimo solicitarProrrogacao(Long emprestimoId, int diasExtras, Usuario usuario) {
        Emprestimo e = getById(emprestimoId);

        if (!e.getUsuario().getId().equals(usuario.getId())) {
            throw new IllegalStateException("Você não tem permissão para prorrogar este empréstimo.");
        }
        if (e.getStatus() != Status.APROVADO) {
            throw new IllegalStateException("Apenas empréstimos ativos podem ter prazo prorrogado.");
        }
        if (diasExtras < 1 || diasExtras > 30) {
            throw new IllegalStateException("O número de dias de prorrogação deve ser entre 1 e 30.");
        }

        e.setStatus(Status.PRORROGACAO_SOLICITADA);
        e.setDiasProrrogacaoSolicitados(diasExtras);
        e.setDataSolicitacaoProrrogacao(LocalDate.now());
        return emprestimoRepository.save(e);
    }

    // Bibliotecário aprova prorrogação
    @Transactional
    public Emprestimo aprovarProrrogacao(Long emprestimoId) {
        Emprestimo e = getById(emprestimoId);
        if (e.getStatus() != Status.PRORROGACAO_SOLICITADA) {
            throw new IllegalStateException("Este empréstimo não tem prorrogação pendente.");
        }

        LocalDate novaData = e.getDataDevolucaoPrevista().plusDays(e.getDiasProrrogacaoSolicitados());
        e.setDataDevolucaoPrevista(novaData);
        e.setStatus(Status.APROVADO);
        e.setDiasProrrogacaoSolicitados(null);
        e.setDataSolicitacaoProrrogacao(null);
        return emprestimoRepository.save(e);
    }

    // Bibliotecário recusa prorrogação
    @Transactional
    public Emprestimo recusarProrrogacao(Long emprestimoId) {
        Emprestimo e = getById(emprestimoId);
        if (e.getStatus() != Status.PRORROGACAO_SOLICITADA) {
            throw new IllegalStateException("Este empréstimo não tem prorrogação pendente.");
        }

        e.setStatus(Status.APROVADO);
        e.setDiasProrrogacaoSolicitados(null);
        e.setDataSolicitacaoProrrogacao(null);
        return emprestimoRepository.save(e);
    }

    // Listagens
    public List<Emprestimo> getTodos() {
        return emprestimoRepository.findAllByOrderByDataSolicitacaoDesc();
    }

    public List<Emprestimo> getTodosComPendentesNoTopo() {
        List<Emprestimo> todos = emprestimoRepository.findAllByOrderByDataSolicitacaoDesc();
        List<Emprestimo> urgentes = todos.stream()
                .filter(e -> e.getStatus() == Status.PENDENTE || e.getStatus() == Status.PRORROGACAO_SOLICITADA)
                .toList();
        List<Emprestimo> demais = todos.stream()
                .filter(e -> e.getStatus() != Status.PENDENTE && e.getStatus() != Status.PRORROGACAO_SOLICITADA)
                .toList();
        List<Emprestimo> result = new java.util.ArrayList<>();
        result.addAll(urgentes);
        result.addAll(demais);
        return result;
    }

    public List<Emprestimo> getPendentes() {
        return emprestimoRepository.findByStatusOrderByDataSolicitacaoDesc(Status.PENDENTE);
    }

    public List<Emprestimo> getProrrogacoesPendentes() {
        return emprestimoRepository.findByStatusOrderByDataSolicitacaoDesc(Status.PRORROGACAO_SOLICITADA);
    }

    public List<Emprestimo> getAtivos() {
        return emprestimoRepository.findByStatusInOrderByDataSolicitacaoDesc(
                List.of(Status.APROVADO, Status.PRORROGACAO_SOLICITADA));
    }

    public List<Emprestimo> doUsuario(Usuario usuario) {
        return emprestimoRepository.findByUsuarioOrderByDataSolicitacaoDesc(usuario);
    }

    public List<Emprestimo> doUsuarioComPendentesNoTopo(Usuario usuario) {
        List<Emprestimo> todos = emprestimoRepository.findByUsuarioOrderByDataSolicitacaoDesc(usuario);
        List<Emprestimo> urgentes = todos.stream()
                .filter(e -> e.getStatus() == Status.PENDENTE || e.getStatus() == Status.PRORROGACAO_SOLICITADA)
                .toList();
        List<Emprestimo> demais = todos.stream()
                .filter(e -> e.getStatus() != Status.PENDENTE && e.getStatus() != Status.PRORROGACAO_SOLICITADA)
                .toList();
        List<Emprestimo> result = new java.util.ArrayList<>();
        result.addAll(urgentes);
        result.addAll(demais);
        return result;
    }

    public List<Emprestimo> buscar(String busca) {
        return emprestimoRepository.buscar(busca);
    }

    public Emprestimo getById(Long id) {
        return emprestimoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Empréstimo não encontrado."));
    }

    public long contarPendentes() {
        return emprestimoRepository.findByStatusOrderByDataSolicitacaoDesc(Status.PENDENTE).size();
    }

    public long contarProrrogacoesPendentes() {
        return emprestimoRepository.findByStatusOrderByDataSolicitacaoDesc(Status.PRORROGACAO_SOLICITADA).size();
    }

    public long contarAtivos() {
        return getAtivos().size();
    }

    public long contarAtrasados() {
        return getAtivos().stream().filter(Emprestimo::isAtrasado).count();
    }

    public List<Emprestimo> getAtrasados() {
        return getAtivos().stream()
                .filter(Emprestimo::isAtrasado)
                .sorted(java.util.Comparator.comparing(Emprestimo::getDataSolicitacao).reversed())
                .toList();
    }

    public List<Emprestimo> buscarAtrasados(String busca) {
        return buscarAtivos(busca).stream()
                .filter(Emprestimo::isAtrasado)
                .toList();
    }

    public List<Emprestimo> buscarAtivos(String busca) {
        return emprestimoRepository.buscarAtivos(busca);
    }

    public List<Emprestimo> buscarPendentes(String busca) {
        return emprestimoRepository.buscarPendentes(busca);
    }
}
