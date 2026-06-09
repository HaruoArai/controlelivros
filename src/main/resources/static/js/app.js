import { toggleDarkMode } from './modules/DarkMode.js'
import { HeaderUtils } from './modules/Header.js';


const main = () => {
    toggleDarkMode(document.getElementById('darkModeToggle'));

    const headerUtils = new HeaderUtils({
        logo: document.getElementById('headerLogo'),
        toggle: document.getElementById('headerToggle'),
        nav: document.getElementById('headerNav'),
        profileMenu: document.getElementById('profileMenu'),
        profileButton: document.getElementById('profileMenuButton'),
        profileDropdown: document.getElementById('profileDropdown')
    });

    headerUtils.init();

}


document.addEventListener("DOMContentLoaded", (e) => {
    main();
});