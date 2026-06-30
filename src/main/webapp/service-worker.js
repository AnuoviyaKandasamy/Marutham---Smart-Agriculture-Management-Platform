const CACHE_NAME = 'marutham-cache-v2';
const ASSETS = [
    'index.html',
    'dashboard.html',
    'css/style.css',
    'js/app.js',
    'js/i18n.js'
];

// Install Event
self.addEventListener('install', event => {
    self.skipWaiting();
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(ASSETS))
    );
});

// Activate Event
self.addEventListener('activate', event => {
    event.waitUntil(
        caches.keys().then(keys => {
            return Promise.all(keys
                .filter(key => key !== CACHE_NAME)
                .map(key => caches.delete(key))
            );
        }).then(() => self.clients.claim())
    );
});

// Fetch Event
self.addEventListener('fetch', event => {
    // Only cache GET requests and non-API calls
    if (event.request.method !== 'GET' || event.request.url.includes('/login') || event.request.url.includes('/weather') ||
        event.request.url.includes('/notifications') || event.request.url.includes('/equipment-requests') ||
        event.request.url.includes('/status')) {
        return;
    }

    event.respondWith(
        fetch(event.request).catch(() => {
            return caches.match(event.request);
        })
    );
});