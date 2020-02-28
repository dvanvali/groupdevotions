var currentCacheName = 'v@@version';
var files = [
    '/',
    '/index.html',
    '/favicon-3.png',
    '/favicon-3-192.png',
    '/images/buttonSlice.png',
    '/images/Error.png',
    '/images/groupdevotions.png',
    '/images/load.svg',
    '/images/OK.png',
    '/images/Warning.png',
    '/css/normalize.css?v=@@version',
    '/css/main.css?v=@@version',
    '/css/bootstrap.css?v=@@version',
    '/css/app.css?v=@@version',
    '/scripts/libraries.min.js?v=@@version',
    '/scripts/templates.js?v=@@version',
    '/scripts/app.js?v=@@version',
    '/manifest.json?v=@@version'
];

self.addEventListener('install', function(e) {
    e.waitUntil(
        caches.open(currentCacheName).then(function(cache) {
            return cache.addAll(files);
        }).catch(function (error) {
            console.log('Install failure', error);
        })
    );
});


self.addEventListener('fetch', function(event) {
    console.log('Fetch event for ', event.request.url);
    event.respondWith(
    caches.match(event.request)
        .then(function (response) {
            if (response) {
                return response;
            }
            return fetch(event.request)
        }).catch(function (error) {
            console.log('Network request for ', event.request.url, error);
        })
    );
});

self.addEventListener('activate', function(event) {
    var cacheWhitelist = [currentCacheName];
    event.waitUntil(
        caches.keys().then (function (cacheNames)  {
            return Promise.all(
                cacheNames.map( function (cacheName) {
                    if (cacheWhitelist.indexOf(cacheName) === -1) {
                        return caches.delete(cacheName);
                    } else {
                        //console.log('NOT Deleting ' + cacheName);
                    }
                })
            )
        })
    );
});
//*/