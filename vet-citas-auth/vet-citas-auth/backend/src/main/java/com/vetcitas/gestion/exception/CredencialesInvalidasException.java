package com.vetcitas.gestion.exception;

/**
 * Se lanza cuando el correo no existe, la contraseña no coincide,
 * o la cuenta está inactiva (activo = false).
 *
 * Nota de seguridad: intencionalmente usamos un mensaje GENÉRICO
 * ("Correo o contraseña incorrectos") tanto si el correo no existe
 * como si la contraseña está mal. Esto evita que un atacante pueda
 * usar el sistema para "adivinar" qué correos están registrados
 * (ataque de enumeración de usuarios).
 */
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException(String mensaje) {
        super(mensaje);
    }
}
