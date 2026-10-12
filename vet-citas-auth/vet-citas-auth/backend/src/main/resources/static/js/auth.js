/**
 * auth.js
 * Contiene:
 *   1) Validaciones en tiempo real para los formularios de login y registro.
 *   2) El envío de esos formularios al backend de Spring Boot usando fetch().
 *
 * Nota didáctica: estas validaciones mejoran la EXPERIENCIA del usuario
 * (feedback inmediato, sin recargar la página), pero NO reemplazan las
 * validaciones del backend (@NotBlank, @Email, @Size en los DTOs). El
 * backend es la única fuente de verdad porque el frontend siempre se
 * puede manipular o desactivar.
 */

// Expresión regular simple para validar formato de correo.
const REGEX_CORREO = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const LONGITUD_MINIMA_PASSWORD = 8;

const URL_BASE_API = '/api/auth';

/* ============================================================
   UTILIDADES GENERALES
   ============================================================ */

function mostrarMensajeGlobal(texto, tipo) {
    const contenedor = document.getElementById('mensajeGlobal');
    if (!contenedor) return;
    contenedor.textContent = texto;
    contenedor.className = `mensaje-global visible ${tipo}`; // tipo: 'error' | 'exito'
}

function limpiarMensajeGlobal() {
    const contenedor = document.getElementById('mensajeGlobal');
    if (!contenedor) return;
    contenedor.className = 'mensaje-global';
    contenedor.textContent = '';
}

function marcarCampoInvalido(inputId, mensajeErrorId, mensaje) {
    const input = document.getElementById(inputId);
    const spanError = document.getElementById(mensajeErrorId);
    input.classList.add('campo-invalido');
    spanError.textContent = mensaje;
}

function limpiarErrorCampo(inputId, mensajeErrorId) {
    const input = document.getElementById(inputId);
    const spanError = document.getElementById(mensajeErrorId);
    input.classList.remove('campo-invalido');
    spanError.textContent = '';
}

/* ============================================================
   VALIDACIONES INDIVIDUALES (reutilizables entre login y registro)
   ============================================================ */

function validarObligatorio(valor) {
    return valor.trim().length > 0;
}

function validarFormatoCorreo(valor) {
    return REGEX_CORREO.test(valor.trim());
}

function validarLongitudPassword(valor) {
    return valor.length >= LONGITUD_MINIMA_PASSWORD;
}

/* ============================================================
   FORMULARIO DE LOGIN
   ============================================================ */

function inicializarFormularioLogin() {
    const form = document.getElementById('formLogin');
    if (!form) return; // no estamos en login.html, no hacemos nada

    const inputCorreo = document.getElementById('correo');
    const inputPassword = document.getElementById('password');
    const btnLogin = document.getElementById('btnLogin');

    // Validación EN TIEMPO REAL mientras el usuario escribe (evento "input")
    inputCorreo.addEventListener('input', () => validarCampoCorreoLogin());
    inputPassword.addEventListener('input', () => validarCampoPasswordLogin());

    function validarCampoCorreoLogin() {
        const valor = inputCorreo.value;
        if (!validarObligatorio(valor)) {
            marcarCampoInvalido('correo', 'errorCorreo', 'El correo es obligatorio');
            return false;
        }
        if (!validarFormatoCorreo(valor)) {
            marcarCampoInvalido('correo', 'errorCorreo', 'Ingresa un correo con formato válido');
            return false;
        }
        limpiarErrorCampo('correo', 'errorCorreo');
        return true;
    }

    function validarCampoPasswordLogin() {
        const valor = inputPassword.value;
        if (!validarObligatorio(valor)) {
            marcarCampoInvalido('password', 'errorPassword', 'La contraseña es obligatoria');
            return false;
        }
        if (!validarLongitudPassword(valor)) {
            marcarCampoInvalido('password', 'errorPassword', `Debe tener mínimo ${LONGITUD_MINIMA_PASSWORD} caracteres`);
            return false;
        }
        limpiarErrorCampo('password', 'errorPassword');
        return true;
    }

    form.addEventListener('submit', async (evento) => {
        evento.preventDefault(); // evitamos el envío tradicional (recarga de página)
        limpiarMensajeGlobal();

        const correoValido = validarCampoCorreoLogin();
        const passwordValido = validarCampoPasswordLogin();

        // Si alguna validación de frontend falla, ni siquiera llamamos al backend.
        if (!correoValido || !passwordValido) {
            mostrarMensajeGlobal('Por favor corrige los campos marcados en rojo.', 'error');
            return;
        }

        const datos = {
            correo: inputCorreo.value.trim(),
            password: inputPassword.value
        };

        btnLogin.disabled = true;
        btnLogin.textContent = 'Ingresando...';

        try {
            const respuesta = await fetch(`${URL_BASE_API}/login`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(datos)
            });

            const cuerpo = await respuesta.json();

            if (!respuesta.ok) {
                // El backend respondió 401 (credenciales inválidas) o 400 (validación)
                mostrarMensajeGlobal(cuerpo.mensaje || 'No se pudo iniciar sesión.', 'error');
                return;
            }

            mostrarMensajeGlobal(`¡Bienvenido, ${cuerpo.nombre}!`, 'exito');

            // Regla de negocio: el login identifica el rol y redirige
            // al panel correspondiente para cada tipo de actor.
            redirigirSegunRol(cuerpo.rol);

        } catch (error) {
            console.error('Error de red al iniciar sesión:', error);
            mostrarMensajeGlobal('No se pudo conectar con el servidor. Intenta nuevamente.', 'error');
        } finally {
            btnLogin.disabled = false;
            btnLogin.textContent = 'Iniciar sesión';
        }
    });
}

function redirigirSegunRol(rol) {
    const destinos = {
        CLIENTE: 'panel-cliente.html',
        VETERINARIO: 'panel-veterinario.html',
        ADMIN: 'panel-admin.html'
    };
    const destino = destinos[rol] || 'login.html';
    setTimeout(() => {
        window.location.href = destino;
    }, 800); // pequeña espera para que el usuario alcance a leer el mensaje de éxito
}

/* ============================================================
   FORMULARIO DE REGISTRO
   ============================================================ */

function inicializarFormularioRegistro() {
    const form = document.getElementById('formRegistro');
    if (!form) return; // no estamos en registro.html

    const inputNombre = document.getElementById('nombre');
    const inputCorreo = document.getElementById('correo');
    const inputTelefono = document.getElementById('telefono');
    const inputPassword = document.getElementById('password');
    const btnRegistro = document.getElementById('btnRegistro');

    inputNombre.addEventListener('input', () => validarCampoNombre());
    inputCorreo.addEventListener('input', () => validarCampoCorreoRegistro());
    inputTelefono.addEventListener('input', () => validarCampoTelefono());
    inputPassword.addEventListener('input', () => validarCampoPasswordRegistro());

    function validarCampoNombre() {
        if (!validarObligatorio(inputNombre.value)) {
            marcarCampoInvalido('nombre', 'errorNombre', 'El nombre es obligatorio');
            return false;
        }
        limpiarErrorCampo('nombre', 'errorNombre');
        return true;
    }

    function validarCampoCorreoRegistro() {
        const valor = inputCorreo.value;
        if (!validarObligatorio(valor)) {
            marcarCampoInvalido('correo', 'errorCorreo', 'El correo es obligatorio');
            return false;
        }
        if (!validarFormatoCorreo(valor)) {
            marcarCampoInvalido('correo', 'errorCorreo', 'Ingresa un correo con formato válido');
            return false;
        }
        limpiarErrorCampo('correo', 'errorCorreo');
        return true;
    }

    function validarCampoTelefono() {
        if (!validarObligatorio(inputTelefono.value)) {
            marcarCampoInvalido('telefono', 'errorTelefono', 'El teléfono es obligatorio');
            return false;
        }
        limpiarErrorCampo('telefono', 'errorTelefono');
        return true;
    }

    function validarCampoPasswordRegistro() {
        const valor = inputPassword.value;
        if (!validarObligatorio(valor)) {
            marcarCampoInvalido('password', 'errorPassword', 'La contraseña es obligatoria');
            return false;
        }
        if (!validarLongitudPassword(valor)) {
            marcarCampoInvalido('password', 'errorPassword', `Debe tener mínimo ${LONGITUD_MINIMA_PASSWORD} caracteres`);
            return false;
        }
        limpiarErrorCampo('password', 'errorPassword');
        return true;
    }

    form.addEventListener('submit', async (evento) => {
        evento.preventDefault();
        limpiarMensajeGlobal();

        // Regla: "todos los campos son obligatorios" -> se valida cada uno
        // y solo se continúa si TODOS pasan.
        const nombreValido = validarCampoNombre();
        const correoValido = validarCampoCorreoRegistro();
        const telefonoValido = validarCampoTelefono();
        const passwordValido = validarCampoPasswordRegistro();

        if (!nombreValido || !correoValido || !telefonoValido || !passwordValido) {
            mostrarMensajeGlobal('Por favor corrige los campos marcados en rojo.', 'error');
            return;
        }

        const datos = {
            nombre: inputNombre.value.trim(),
            correo: inputCorreo.value.trim(),
            telefono: inputTelefono.value.trim(),
            password: inputPassword.value
        };

        btnRegistro.disabled = true;
        btnRegistro.textContent = 'Creando cuenta...';

        try {
            const respuesta = await fetch(`${URL_BASE_API}/registro`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(datos)
            });

            const cuerpo = await respuesta.json();

            if (!respuesta.ok) {
                // 409: correo duplicado | 400: fallo alguna validación de backend
                if (respuesta.status === 400) {
                    aplicarErroresDeBackend(cuerpo);
                }
                mostrarMensajeGlobal(cuerpo.mensaje || 'No se pudo completar el registro.', 'error');
                return;
            }

            mostrarMensajeGlobal('Cuenta creada exitosamente. Redirigiendo al login...', 'exito');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 1200);

        } catch (error) {
            console.error('Error de red al registrar:', error);
            mostrarMensajeGlobal('No se pudo conectar con el servidor. Intenta nuevamente.', 'error');
        } finally {
            btnRegistro.disabled = false;
            btnRegistro.textContent = 'Crear cuenta';
        }
    });

    // Si el backend devuelve errores de validación por campo (mapa "campo -> mensaje"),
    // los mostramos también junto al input correspondiente.
    function aplicarErroresDeBackend(erroresPorCampo) {
        const mapaSpans = {
            nombre: 'errorNombre',
            correo: 'errorCorreo',
            telefono: 'errorTelefono',
            password: 'errorPassword'
        };
        Object.entries(erroresPorCampo).forEach(([campo, mensaje]) => {
            if (mapaSpans[campo]) {
                marcarCampoInvalido(campo, mapaSpans[campo], mensaje);
            }
        });
    }
}

/* ============================================================
   PROTECCION DE PANELES (autenticación estricta)
   Se usa en panel-cliente.html, panel-veterinario.html y panel-admin.html
   ============================================================ */

async function verificarSesionActiva(rolEsperado) {
    try {
        const respuesta = await fetch(`${URL_BASE_API}/sesion`);

        if (!respuesta.ok) {
            // No hay sesión activa: regla de negocio -> no puede acceder al panel.
            window.location.href = 'login.html';
            return;
        }

        const sesion = await respuesta.json();

        // Si el rol de la sesión no coincide con el panel que se intenta ver
        // (ej. un CLIENTE tratando de abrir panel-admin.html), lo devolvemos
        // a su propio panel.
        if (rolEsperado && sesion.rol !== rolEsperado) {
            window.location.href = 'login.html';
            return;
        }

        const elementoNombre = document.getElementById('nombreUsuarioSesion');
        if (elementoNombre) {
            elementoNombre.textContent = sesion.nombre;
        }

    } catch (error) {
        console.error('Error al verificar la sesión:', error);
        window.location.href = 'login.html';
    }
}

async function cerrarSesion() {
    try {
        await fetch(`${URL_BASE_API}/logout`, { method: 'POST' });
    } finally {
        window.location.href = 'login.html';
    }
}

/* ============================================================
   PUNTO DE ENTRADA
   ============================================================ */

document.addEventListener('DOMContentLoaded', () => {
    inicializarFormularioLogin();
    inicializarFormularioRegistro();
});
