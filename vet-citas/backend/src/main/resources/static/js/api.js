/** Cliente HTTP común. Centralizar fetch evita repetir seguridad y manejo de errores en cada pantalla. */
async function api(ruta, opciones = {}) {
    const metodo = opciones.method || 'GET';
    const headers = { ...(opciones.headers || {}) };
    // El token CSRF viene del servidor. La cookie de sesión se envía solo al mismo origen.
    if (!['GET', 'HEAD', 'OPTIONS'].includes(metodo)) {
        const respuestaToken = await fetch('/api/auth/csrf', { credentials: 'same-origin' });
        if (!respuestaToken.ok) throw new Error('No se pudo preparar la solicitud. Recarga la página.');
        const csrf = await respuestaToken.json();
        headers[csrf.headerName] = csrf.token;
    }
    if (opciones.body !== undefined) headers['Content-Type'] = 'application/json';
    const respuesta = await fetch(ruta, { ...opciones, headers, credentials: 'same-origin' });
    // Un DELETE correcto devuelve 204 sin JSON: no debemos intentar convertir un cuerpo vacío.
    const datos = respuesta.status === 204 ? null : await respuesta.json().catch(() => ({}));
    if (!respuesta.ok) {
        const campos = datos.errores ? Object.entries(datos.errores).map(([campo, error]) => `${campo}: ${error}`).join('. ') : '';
        const error = new Error([datos.mensaje || 'No se pudo completar la operación', campos].filter(Boolean).join('. '));
        error.status = respuesta.status;
        throw error;
    }
    return datos;
}
