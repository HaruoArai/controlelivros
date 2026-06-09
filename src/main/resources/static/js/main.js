// main.js — Biblioteca Sistema de Livros

// Modal de exclusão
function confirmarExclusao(id) {
  const modal = document.getElementById('modalExclusao');
  modal.classList.add('active');

  const btnConfirmar = modal.querySelector('.btn-confirm-delete');
  btnConfirmar.onclick = () => {
    alert(`Livro ${id} excluído! (Simulação — em Spring Boot seria uma rota POST/DELETE)`);
    modal.classList.remove('active');
  };
}

function fecharModal() {
  document.getElementById('modalExclusao').classList.remove('active');
}


// Função que volta para a tela inicial ao clicar na logo
const logoHome = () => {
  /** @type {HTMLDivElement|null} */
  const element = document.querySelector("#header_logo")

  if (element) {
    element.style.cursor = 'pointer';
    element.addEventListener('click', (e) => {
      window.location.href = "/";
    })
  }
}



document.addEventListener('DOMContentLoaded', () => {
  logoHome();

  // Fechar modal ao clicar fora
  const modal = document.getElementById('modalExclusao');
  if (modal) {
    modal.addEventListener('click', (e) => {
      if (e.target === modal) fecharModal();
    });
  }

  // Animação nas linhas da tabela
  const rows = document.querySelectorAll('.table-row');
  rows.forEach((row, i) => {
    row.style.animationDelay = `${i * 0.06}s`;
  });



  // Highlight campo de busca
  const searchInput = document.querySelector('.search-input');
  if (searchInput) {
    const params = new URLSearchParams(window.location.search);
    const titulo = params.get('titulo');
    if (titulo) searchInput.value = titulo;
  }

  // Definir ano máximo automaticamente
  const campoAno = document.getElementById('ano');
  if (campoAno) {
    const anoAtual = new Date().getFullYear();
    campoAno.max = anoAtual;

    campoAno.addEventListener('input', () => {
      const valor = parseInt(campoAno.value);
      if (valor > anoAtual) {
        campoAno.setCustomValidity(
          `O ano não pode ser maior que ${anoAtual}.`
        );

        campoAno.reportValidity();

      } else {
        campoAno.setCustomValidity('');
      }
    });
  }

});
