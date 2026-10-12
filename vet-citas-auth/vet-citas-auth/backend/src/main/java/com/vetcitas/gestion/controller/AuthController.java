package com.vetcitas.gestion.controller;

import com.vetcitas.gestion.dto.AuthResponse;
import com.vetcitas.gestion.dto.LoginRequest;
import com.vetcitas.gestion.dto.RegistroRequest;
import com.vetcitas.gestion.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @RestController -> Es la combinación de @Controller + @ResponseBody.
 * Le dice a Spring que esta clase maneja peticiones HTTP y que TODO lo
 * que devuelvan sus métodos se debe serializar automáticamente a JSON
 * (en vez de interpretarse como el nombre de una vista HTML, que es lo
 * que haría un @Controller normal en una app con Thymeleaf).
 *
 * @RequestMapping -> Define el prefijo de ruta común para todos los
 * endpoints de esta clase. Aquí, todos empiezan con /api/auth.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UsuarioService usuarioService;

    // Inyección por constructor: Spring crea el AuthController pasándole
    // automáticamente la instancia de UsuarioServiceImpl (porque
    // implementa la interfaz UsuarioService).
    public AuthController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * @PostMapping -> Atajo de @RequestMapping(method = RequestMethod.POST).
     * Este endpoint responde a: POST /api/auth/registro
     *
     * @RequestBody -> Le indica a Spring que debe tomar el cuerpo (body)
     * de la petición HTTP (en formato JSON) y convertirlo automáticamente
     * en un objeto Java (RegistroRequest). A esto se le llama
     * "deserialización".
     *
     * @Valid -> Activa las validaciones de Bean Validation que pusimos
     * en RegistroRequest (@NotBlank, @Email, @Size). Si algún campo no
     * cumple, Spring lanza automáticamente un MethodArgumentNotValidException
     * ANTES de que el código del método se ejecute, y nuestro
     * GlobalExceptionHandler la convierte en una respuesta 400 con el
     * detalle de los errores por campo.
     */
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        AuthResponse respuesta = usuarioService.registrarCliente(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

   
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> iniciarSesion(@Valid @RequestBody LoginRequest request, HttpSession session) {
        AuthResponse respuesta = usuarioService.iniciarSesion(request);

   
        session.setAttribute("usuarioId", respuesta.getId());
        session.setAttribute("usuarioRol", respuesta.getRol().name());
        session.setAttribute("usuarioNombre", respuesta.getNombre());

        return ResponseEntity.ok(respuesta); 
    }

  
    @GetMapping("/sesion")
    public ResponseEntity<?> obtenerSesionActual(HttpSession session) {
        Object usuarioId = session.getAttribute("usuarioId");

        if (usuarioId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "No hay una sesión activa"));
        }

        return ResponseEntity.ok(Map.of(
                "id", usuarioId,
                "rol", session.getAttribute("usuarioRol"),
                "nombre", session.getAttribute("usuarioNombre")
        ));
    }

   
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> cerrarSesion(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada correctamente"));
    }
}
