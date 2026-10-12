package com.vetcitas.gestion.exception;

import java.util.*;
import org.springframework.dao.*;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Convierte errores Java en respuestas JSON comprensibles, sin revelar SQL ni contraseñas. */
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ReglaNegocioException.class)
  ResponseEntity<?> negocio(ReglaNegocioException e) {
    return error(e.getStatus(), e.getMessage());
  }

  // Todos los errores de validación se reúnen en un mapa campo -> explicación.
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<?> validacion(MethodArgumentNotValidException e) {
    Map<String, String> campos = new LinkedHashMap<>();
    e.getBindingResult()
        .getFieldErrors()
        .forEach(f -> campos.put(f.getField(), f.getDefaultMessage()));
    return ResponseEntity.badRequest()
        .body(Map.of("mensaje", "Revisa los datos del formulario", "errores", campos));
  }

  @ExceptionHandler({
    HttpMessageNotReadableException.class,
    MethodArgumentTypeMismatchException.class
  })
  ResponseEntity<?> formato(Exception e) {
    return error(HttpStatus.BAD_REQUEST, "Formato de datos, fecha o identificador inválido");
  }

  @ExceptionHandler(AuthenticationException.class)
  ResponseEntity<?> login(AuthenticationException e) {
    return error(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos");
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<?> permisos(AccessDeniedException e) {
    return error(HttpStatus.FORBIDDEN, "No tienes permiso para esta operación");
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  ResponseEntity<?> integridad(DataIntegrityViolationException e) {
    return error(
        HttpStatus.CONFLICT, "El correo ya existe o el registro está relacionado con otros datos");
  }

  @ExceptionHandler({
    OptimisticLockingFailureException.class,
    PessimisticLockingFailureException.class
  })
  ResponseEntity<?> concurrencia(Exception e) {
    return error(
        HttpStatus.CONFLICT, "Otro usuario modificó estos datos. Actualiza e inténtalo de nuevo");
  }

  // Respuesta uniforme para que el frontend siempre pueda leer la propiedad mensaje.
  private ResponseEntity<?> error(HttpStatus estado, String mensaje) {
    return ResponseEntity.status(estado).body(Map.of("mensaje", mensaje));
  }
}
