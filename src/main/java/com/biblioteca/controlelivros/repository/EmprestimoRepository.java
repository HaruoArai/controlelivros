package com.biblioteca.controlelivros.repository;

import com.biblioteca.controlelivros.model.Emprestimo;
import com.biblioteca.controlelivros.model.Emprestimo.Status;
import com.biblioteca.controlelivros.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EmprestimoRepository extends JpaRepository<Emprestimo, Long> {

    // Todos os empréstimos de um usuário
    List<Emprestimo> findByUsuarioOrderByDataSolicitacaoDesc(Usuario usuario);

    // Por status
    List<Emprestimo> findByStatusOrderByDataSolicitacaoDesc(Status status);

    // Pendentes (para o bibliotecário aceitar/recusar)
    List<Emprestimo> findByStatusInOrderByDataSolicitacaoDesc(List<Status> statuses);

    // Verificar se o usuário já tem empréstimo ativo deste livro
    @Query("SELECT COUNT(e) > 0 FROM Emprestimo e WHERE e.usuario = :usuario AND e.livro.id = :livroId AND e.status IN ('PENDENTE','APROVADO')")
    boolean existsEmprestimoAtivoByUsuarioAndLivro(@Param("usuario") Usuario usuario, @Param("livroId") Long livroId);

    // Todos ordenados por data (para dashboard do bibliotecário)
    List<Emprestimo> findAllByOrderByDataSolicitacaoDesc();

    // Busca por nome do usuário ou título do livro
    @Query("""
        SELECT e FROM Emprestimo e
        WHERE LOWER(e.usuario.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
           OR LOWER(e.livro.titulo) LIKE LOWER(CONCAT('%', :busca, '%'))
        ORDER BY e.dataSolicitacao DESC
    """)
    List<Emprestimo> buscar(@Param("busca") String busca);

    @Query("""
        SELECT e FROM Emprestimo e
        WHERE e.status = 'APROVADO'
          AND (
                LOWER(e.usuario.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
             OR LOWER(e.livro.titulo) LIKE LOWER(CONCAT('%', :busca, '%'))
          )
        ORDER BY e.dataSolicitacao DESC
    """)
    List<Emprestimo> buscarAtivos(@Param("busca") String busca);

    @Query("""
        SELECT e FROM Emprestimo e
        WHERE e.status = 'PENDENTE'
          AND (
                LOWER(e.usuario.nome) LIKE LOWER(CONCAT('%', :busca, '%'))
             OR LOWER(e.livro.titulo) LIKE LOWER(CONCAT('%', :busca, '%'))
          )
        ORDER BY e.dataSolicitacao DESC
    """)
    List<Emprestimo> buscarPendentes(@Param("busca") String busca);
}
