package com.vetcitas.gestion.exception;

/**
 * Excepción de negocio personalizada (RuntimeException, no "checked").
 * Se lanza cuando alguien intenta registrarse con un correo que
 * ya existe en la base de datos (la columna "correo" tiene además
 * una restricción UNIQUE a nivel de base de datos como segunda capa
 * de seguridad).
 */
public class CorreoDuplicadoException extends RuntimeException {
    public CorreoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
