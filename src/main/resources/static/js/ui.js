/**
 * KTX Management — UI Controller (Theme Switcher, Active Navigation & Mobile Drawer)
 * Presentation layer only; does not alter business logic or backend state.
 */
(function () {
    const STORAGE_KEY = 'ktx-theme';

    function getTheme() {
        return document.documentElement.getAttribute('data-theme') || localStorage.getItem(STORAGE_KEY) || 'dark';
    }

    function applyTheme(theme) {
        document.documentElement.setAttribute('data-theme', theme);
        try {
            localStorage.setItem(STORAGE_KEY, theme);
        } catch (e) {
            // Ignore storage restriction errors
        }
        updateThemeButtons(theme);
    }

    function updateThemeButtons(theme) {
        const buttons = document.querySelectorAll('[data-theme-toggle]');
        buttons.forEach(function (btn) {
            const isLight = theme === 'light';
            btn.innerHTML = isLight
                ? '<i class="bi bi-moon-stars"></i><span class="theme-label">Giao diện tối</span>'
                : '<i class="bi bi-sun"></i><span class="theme-label">Giao diện sáng</span>';
            btn.setAttribute('title', isLight ? 'Chuyển sang chế độ tối' : 'Chuyển sang chế độ sáng');
            btn.setAttribute('aria-label', isLight ? 'Chuyển sang chế độ tối' : 'Chuyển sang chế độ sáng');
        });
    }

    function toggleTheme() {
        const next = getTheme() === 'dark' ? 'light' : 'dark';
        applyTheme(next);
    }

    function initTopbarControls() {
        const topbar = document.querySelector('.topbar');
        const sidebar = document.querySelector('.sidebar');

        if (topbar && sidebar) {
            // 1. Mobile sidebar drawer toggle + backdrop
            let backdrop = document.querySelector('.sidebar-backdrop');
            if (!backdrop) {
                backdrop = document.createElement('div');
                backdrop.className = 'sidebar-backdrop';
                document.body.appendChild(backdrop);
            }

            const firstChild = topbar.firstElementChild;
            if (firstChild && !topbar.querySelector('.mobile-nav-toggle')) {
                const toggleBtn = document.createElement('button');
                toggleBtn.type = 'button';
                toggleBtn.className = 'mobile-nav-toggle';
                toggleBtn.setAttribute('aria-label', 'Mở menu điều hướng');
                toggleBtn.innerHTML = '<i class="bi bi-list"></i>';

                const leftWrap = document.createElement('div');
                leftWrap.className = 'topbar-left';
                topbar.insertBefore(leftWrap, firstChild);
                leftWrap.appendChild(toggleBtn);
                leftWrap.appendChild(firstChild);

                toggleBtn.addEventListener('click', function () {
                    sidebar.classList.toggle('open');
                    backdrop.classList.toggle('open');
                });

                backdrop.addEventListener('click', function () {
                    sidebar.classList.remove('open');
                    backdrop.classList.remove('open');
                });
            }

            // 2. Ensure Theme Switcher button in Topbar right side
            if (!topbar.querySelector('[data-theme-toggle]')) {
                let rightContainer = topbar.children[1];
                if (!rightContainer) {
                    rightContainer = document.createElement('div');
                    rightContainer.className = 'topbar-right';
                    topbar.appendChild(rightContainer);
                } else if (rightContainer.tagName === 'A' || rightContainer.tagName === 'BUTTON') {
                    const wrapper = document.createElement('div');
                    wrapper.className = 'topbar-right';
                    topbar.insertBefore(wrapper, rightContainer);
                    wrapper.appendChild(rightContainer);
                    rightContainer = wrapper;
                } else {
                    rightContainer.classList.add('topbar-right');
                }

                const themeBtn = document.createElement('button');
                themeBtn.type = 'button';
                themeBtn.className = 'theme-toggle-btn';
                themeBtn.setAttribute('data-theme-toggle', 'true');
                rightContainer.appendChild(themeBtn);
            }
        }

        // Bind click events to all theme toggle buttons (including Login page)
        document.querySelectorAll('[data-theme-toggle]').forEach(function (btn) {
            if (!btn.dataset.bound) {
                btn.dataset.bound = 'true';
                btn.addEventListener('click', toggleTheme);
            }
        });

        updateThemeButtons(getTheme());
    }

    function highlightActiveNav() {
        const currentPath = window.location.pathname;
        const navItems = document.querySelectorAll('.sidebar-nav .nav-item');
        let bestMatch = null;
        let bestLength = -1;

        navItems.forEach(function (link) {
            link.classList.remove('active');
            const href = link.getAttribute('href');
            if (!href) return;

            if (currentPath === href) {
                bestMatch = link;
                bestLength = href.length + 1000;
            } else if (href !== '/' && currentPath.startsWith(href + '/') && href.length > bestLength) {
                bestMatch = link;
                bestLength = href.length;
            }
        });

        if (bestMatch) {
            bestMatch.classList.add('active');
        }
    }

    document.addEventListener('DOMContentLoaded', function () {
        initTopbarControls();
        highlightActiveNav();
    });
})();

