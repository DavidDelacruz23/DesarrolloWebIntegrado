/** Formularios públicos. Validar aquí ayuda al usuario; la validación definitiva está en los DTOs Java. */
document.addEventListener('DOMContentLoaded', () => {
    const form = document.querySelector('form');
    if (!form) return;
    const esRegistro = form.id === 'formRegistro';
    // Usamos las restricciones HTML (required, minlength...) y las reglas adicionales del servidor.
    form.removeAttribute('novalidate');
    form.querySelectorAll('input').forEach(input => { input.required = true; });
    const limites = { nombre: 120, correo: 150, telefono: 20, password: 72 };
    Object.entries(limites).forEach(([id, max]) => { const input = document.getElementById(id); if (input) input.maxLength = max; });
    document.getElementById('password').minLength = 8;
    const mensaje = document.getElementById('mensajeGlobal');
    mensaje.setAttribute('role', 'status');
    form.addEventListener('submit', async evento => {
        evento.preventDefault();
        const boton = form.querySelector('button[type="submit"]');
        const textoOriginal = boton.textContent;
        boton.disabled = true; boton.textContent = 'Procesando…';
        mensaje.className = 'mensaje-global';
        // FormData lee los campos por su atributo name; el backend recibe un objeto JSON.
        const datos = Object.fromEntries(new FormData(form));
        try {
            const respuesta = await api(`/api/auth/${esRegistro ? 'registro' : 'login'}`, {
                method: 'POST', body: JSON.stringify(datos)
            });
            if (esRegistro) {
                mensaje.textContent = respuesta.mensaje; mensaje.className = 'mensaje-global visible exito';
                form.reset();
            } else {
                const destinos = { ADMIN: 'panel-admin.html', VETERINARIO: 'panel-veterinario.html', CLIENTE: 'panel-cliente.html' };
                window.location.assign(destinos[respuesta.rol]);
            }
        } catch (error) {
            mensaje.textContent = error.message; mensaje.className = 'mensaje-global visible error';
        } finally {
            boton.disabled = false; boton.textContent = textoOriginal;
        }
    });
});
