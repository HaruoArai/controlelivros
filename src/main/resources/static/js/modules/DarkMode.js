/** @param {HTMLInputElement|null} toggle */
export const toggleDarkMode = (toggle) => {
    if (toggle) {
        const isDark = document.documentElement.classList.contains("dark");
        toggle.checked = isDark;

        toggle.addEventListener("change", () => {
            document.documentElement.classList.toggle("dark", toggle.checked);
            localStorage.setItem("theme", toggle.checked ? "dark" : "light");
        });
    }
}