import { configurarMenuPerfil } from './profile-menu.js';
import { toggleDarkMode } from './darkmode.js'


// Função que volta para a tela inicial ao clicar na logo
const goToHome = () => {
    /** @type {HTMLDivElement|null} */
    const element = document.querySelector("#header_logo")

    if (element) {
        element.style.cursor = 'pointer';
        element.addEventListener('click', (e) => {
            window.location.href = "/";
        })
    }
}


const main = () => {
    toggleDarkMode();
    goToHome();
    configurarMenuPerfil();
}


document.addEventListener("DOMContentLoaded", (e) => {
    main();
});