// static/js/profile-menu.js

export function configurarMenuPerfil() {
    const profileMenu = document.getElementById('profileMenu');
    const profileButton = document.getElementById('profileMenuButton');
    const profileDropdown = document.getElementById('profileDropdown');

    if (!profileMenu || !profileButton || !profileDropdown) {
        return;
    }

    profileButton.addEventListener('click', function (event) {
        event.stopPropagation();
        profileDropdown.classList.toggle('show');
    });

    document.addEventListener('click', function (event) {
        if (!profileMenu.contains(event.target)) {
            profileDropdown.classList.remove('show');
        }
    });

    document.addEventListener('keydown', function (event) {
        if (event.key === 'Escape') {
            profileDropdown.classList.remove('show');
        }
    });
}