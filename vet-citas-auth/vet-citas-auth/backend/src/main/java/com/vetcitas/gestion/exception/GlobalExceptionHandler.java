package com.vetcitas.gestion.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * @RestControllerAdvice -> Es un interceptor global de excepciones para
 * TODOS los @RestController de la aplicación. En vez de poner un
 * try/catch repetido en cada método del AuthController, centralizamos
 * aquí el manejo de errores para que las respuestas HTTP sean siempre
 * consistentes (mismo formato de JSON, código de estado correcto, etc.).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @ExceptionHandler -> Le dice a Spring: "cuando cualquier controlador
     * lance esta excepción, ejecuta este método para construir la
     * respuesta". Devolvemos 409 CONFLICT porque el recurso (correo)
     * ya existe.
     */
    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarCorreoDuplicado(CorreoDuplicadoException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    /**
     * 401 UNAUTHORIZED -> credenciales incorrectas o cuenta inactiva.
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("mensaje", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Captura los errores generados automáticamente por las anotaciones
     * de Bean Validation (@NotBlank, @Email, @Size) que pusimos en los
     * DTOs. Devuelve un mapa "campo -> mensaje de error" para que el
     * frontend pueda, si quiere, mostrar el error junto al campo exacto.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> manejarValidacion(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errores.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errores);
    }
}
