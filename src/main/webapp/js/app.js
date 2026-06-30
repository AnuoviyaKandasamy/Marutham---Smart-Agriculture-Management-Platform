

const App = {
    CONFIG: {
        TOAST_DURATION: 4000,
        CONTEXT_PATH: window.location.pathname.split('/')[1] === 'Marutham' ? '/Marutham/' : '/'
    },

    UI: {
        showToast(message, type = 'success') {
            let container = document.getElementById('toast-container');
            if (!container) {
                container = document.createElement('div');
                container.id = 'toast-container';
                container.style.cssText = 'position: fixed; top: 24px; right: 24px; z-index: 9999; display: flex; flex-direction: column; gap: 12px;';
                document.body.appendChild(container);
            }

            const toast = document.createElement('div');
            const colors = { success: '#2dcc70', error: '#f56565', info: '#3498db', warning: '#f39c12' };
            const icons = { success: '✅', error: '❌', info: 'ℹ️', warning: '⚠️' };

            toast.style.cssText = `
                background: white; border-left: 5px solid ${colors[type]}; color: #2d3748;
                padding: 16px 24px; border-radius: 12px; box-shadow: 0 10px 25px rgba(0,0,0,0.1);
                font-family: 'Inter', sans-serif; font-weight: 600; display: flex; align-items: center; gap: 12px;
                animation: slideInLeft 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275) forwards;
            `;

            toast.innerHTML = `<span style="font-size:1.2rem">${icons[type]}</span> <span>${message}</span>`;
            container.appendChild(toast);

            setTimeout(() => {
                toast.style.opacity = '0';
                toast.style.transform = 'translateX(50px)';
                setTimeout(() => toast.remove(), 400);
            }, App.CONFIG.TOAST_DURATION);
        },

        setLoading(btnId, isLoading) {
            const btn = document.getElementById(btnId);
            if (!btn) return;
            if (isLoading) {
                btn.disabled = true;
                btn.dataset.oldContent = btn.innerHTML;
                btn.innerHTML = `<span class="spinner"></span> Working...`;
            } else {
                btn.disabled = false;
                btn.innerHTML = btn.dataset.oldContent || btn.innerHTML;
            }
        },

        initAnimations() {
            const observer = new IntersectionObserver((entries) => {
                entries.forEach(entry => {
                    if (entry.isIntersecting) entry.target.classList.add('active');
                });
            }, { threshold: 0.1 });
            document.querySelectorAll('.reveal').forEach(el => observer.observe(el));
        }
    },

    Forms: {
        serialize(form) { return new URLSearchParams(new FormData(form)); },
        validate(formId) {
            const form = document.getElementById(formId);
            let valid = true;
            form.querySelectorAll('[required]').forEach(i => {
                if (!i.value.trim()) { i.style.borderColor = 'var(--danger)'; valid = false; }
                else { i.style.borderColor = '#e2e8f0'; }
            });
            return valid;
        }
    },

    Api: {
        csrfToken: null,

        async request(endpoint, options = {}) {
            let url = endpoint.startsWith('http') ? endpoint : App.CONFIG.CONTEXT_PATH + endpoint.replace(/^\//, '');

            options.headers = options.headers || {};
            const method = (options.method || 'GET').toUpperCase();
            if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method) && this.csrfToken) {
                options.headers['X-CSRF-Token'] = this.csrfToken;
            }

            try {
                const response = await fetch(url, options);

                const newToken = response.headers.get('X-CSRF-Token');
                if (newToken) this.csrfToken = newToken;

                if (response.status === 401) {
                    if (!window.location.pathname.includes('login.html')) {
                        App.UI.showToast('Session expired.', 'warning');
                        setTimeout(() => window.location.href = 'login.html', 1000);
                    }
                    return null;
                }

                const text = await response.text();
                let data;
                try { data = JSON.parse(text); } catch (e) { data = { success: response.ok, message: text }; }

                if (!response.ok) throw new Error(data.message || 'Server Error');
                return data;
            } catch (error) {
                console.error('API Error:', error);
                // Don't show toast for the initial status/login ping
                if (method !== 'GET' && endpoint !== 'login' && endpoint !== 'status') {
                    App.UI.showToast(error.message, 'error');
                }
                return null;
            }
        },
        async get(url) { return this.request(url, { method: 'GET' }); },
        async post(url, data) {
            return this.request(url, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
                body: data
            });
        }
    }
};

if ('serviceWorker' in navigator) {
    window.addEventListener('load', () => {
        navigator.serviceWorker.register('service-worker.js')
            .then(reg => console.log('Marutham Service Worker registered'))
            .catch(err => console.log('Service Worker registration failed', err));
    });
}


const style = document.createElement('style');
style.innerHTML = `
    .spinner { width: 18px; height: 18px; border: 2px solid rgba(255,255,255,0.3); border-radius: 50%; border-top-color: #fff; animation: spin 0.8s linear infinite; display: inline-block; margin-right: 8px; vertical-align: middle; }
    @keyframes spin { to { transform: rotate(360deg); } }
    @keyframes slideInLeft { from { opacity: 0; transform: translateX(30px); } to { opacity: 1; transform: translateX(0); } }
`;
document.head.appendChild(style);

document.addEventListener('DOMContentLoaded', () => {
    App.UI.initAnimations();
    const nav = document.querySelector('nav');
    if (nav) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 50) nav.classList.add('scrolled');
            else nav.classList.remove('scrolled');
        });
    }

    App.Api.get('login').catch(() => {});
});

const Toast = App.UI;
const Api = App.Api;