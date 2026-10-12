package com.vetcitas.gestion.exception;

import org.springframework.http.HttpStatus;

/**
 * Error esperado de la aplicación: por ejemplo, una cita que cruza otra o un registro inexistente.
 */
public class ReglaNegocioException extends RuntimeException {
  private final HttpStatus status;

  public ReglaNegocioException(HttpStatus status, String mensaje) {
    super(mensaje);
    this.status = status;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
