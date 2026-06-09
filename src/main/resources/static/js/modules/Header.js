export class HeaderUtils {

    /**
     * Classe de utilitários para o Header e Nav
     * @param {Object} elements
     * @param {HTMLDivElement|null} elements.logo
     * @param {HTMLButtonElement|null} elements.toggle
     * @param {HTMLElement|null} elements.nav
     * @param {HTMLElement|null} elements.profileMenu
     * @param {HTMLButtonElement|null} elements.profileButton
     * @param {HTMLElement|null} elements.profileDropdown
     */
    constructor({
        logo,
        toggle,
        nav,
        profileMenu,
        profileButton,
        profileDropdown
    }) {
        this.logo = logo;
        this.toggle = toggle;
        this.nav = nav;

        this.profileMenu = profileMenu;
        this.profileButton = profileButton;
        this.profileDropdown = profileDropdown;
    }

    goToHome() {
        if (!this.logo) return;

        this.logo.style.cursor = 'pointer';

        this.logo.addEventListener('click', () => {
            window.location.href = "/";
        });
    }

    setupMobileMenu() {
        if (!this.toggle || !this.nav) return;

        this.toggle.addEventListener('click', () => {
            this.nav.classList.toggle('is-open');

            const isOpen = this.nav.classList.contains('is-open');

            this.toggle.setAttribute('aria-expanded', String(isOpen));
            this.toggle.textContent = isOpen ? '✕' : '☰';
        });
    }

    setupProfileMenu() {
        if (!this.profileMenu || !this.profileButton || !this.profileDropdown) {
            return;
        }

        this.profileButton.addEventListener('click', (event) => {
            event.stopPropagation();
            this.profileDropdown.classList.toggle('show');
        });

        document.addEventListener('click', (event) => {
            if (!this.profileMenu.contains(event.target)) {
                this.profileDropdown.classList.remove('show');
            }
        });

        document.addEventListener('keydown', (event) => {
            if (event.key === 'Escape') {
                this.profileDropdown.classList.remove('show');
            }
        });
    }

    init() {
        this.goToHome();
        this.setupMobileMenu();
        this.setupProfileMenu();
    }
}