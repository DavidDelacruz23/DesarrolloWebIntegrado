/** Swagger usa la misma cookie de sesión que el panel y obtiene un token antes de cada cambio. */
window.addEventListener('DOMContentLoaded', () => {
    SwaggerUIBundle({
        url: '/v3/api-docs',
        dom_id: '#swagger-ui',
        deepLinking: true,
        presets: [SwaggerUIBundle.presets.apis],
        // Swagger espera esta promesa antes de ejecutar la petición del botón Execute.
        requestInterceptor: async request => {
            if (!['GET','HEAD','OPTIONS'].includes((request.method || 'GET').toUpperCase())) {
                const response = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
                const token = await response.json();
                request.headers[token.headerName] = token.token;
            }
            request.credentials = 'same-origin';
            return request;
        }
    });
});
