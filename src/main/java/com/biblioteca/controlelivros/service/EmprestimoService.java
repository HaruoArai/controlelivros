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

    //Usuário solicita empréstimo 
    @Transactional
    public Emprestimo solicitarEmprestimo(Livro livro, Usuario usuario) {
        // Verificar estoque disponível
        if (!livro.isDisponivel()) {
            throw new IllegalStateException("Livro sem estoque disponível.");
        }
        // Verificar se já tem empréstimo ativo
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

        // Dar baixa no estoque
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
        if (e.getStatus() != Status.APROVADO) {
            throw new IllegalStateException("Apenas empréstimos aprovados podem ser devolvidos.");
        }

        e.setStatus(Status.DEVOLVIDO);
        e.setDataDevolucaoReal(LocalDate.now());

        // Devolver ao estoque
        Livro livro = e.getLivro();
        livro.setQuantidadeEmprestada(Math.max(0, livro.getQuantidadeEmprestada() - 1));
        livroRepository.save(livro);

        return emprestimoRepository.save(e);
    }

    // Listagens 
    public List<Emprestimo> getTodos() {
        return emprestimoRepository.findAllByOrderByDataSolicitacaoDesc();
    }

    public List<Emprestimo> getPendentes() {
        return emprestimoRepository.findByStatusOrderByDataSolicitacaoDesc(Status.PENDENTE);
    }

    public List<Emprestimo> getAtivos() {
        return emprestimoRepository.findByStatusOrderByDataSolicitacaoDesc(Status.APROVADO);
    }

    public List<Emprestimo> doUsuario(Usuario usuario) {
        return emprestimoRepository.findByUsuarioOrderByDataSolicitacaoDesc(usuario);
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

    public long contarAtivos() {
        return emprestimoRepository.findByStatusOrderByDataSolicitacaoDesc(Status.APROVADO).size();
    }

    public long contarAtrasados() {
        return getAtivos().stream().filter(Emprestimo::isAtrasado).count();
    }
}
