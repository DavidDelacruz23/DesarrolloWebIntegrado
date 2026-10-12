package com.vetcitas.gestion.controller;

import com.vetcitas.gestion.dto.*;
import com.vetcitas.gestion.security.UsuarioPrincipal;
import com.vetcitas.gestion.service.UsuarioService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * @RestController transforma las respuestas en JSON; SecurityConfig exige ADMIN salvo en /me.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
  private final UsuarioService service;

  public UsuarioController(UsuarioService service) {
    this.service = service;
  }

  // GET consulta sin modificar; /me obtiene exclusivamente la cuenta de la sesión.
  // Lista los registros que este controlador permite consultar.
  @GetMapping
  public List<UsuarioResponse> listar() {
    return service.listar();
  }

  // Obtiene exclusivamente el perfil identificado por la sesión.
  @GetMapping("/me")
  public UsuarioResponse perfil(@AuthenticationPrincipal UsuarioPrincipal actor) {
    return service.obtener(actor.id());
  }

  // Actualiza datos de contacto propios sin permitir cambiar rol ni estado.
  @PutMapping("/me")
  public UsuarioResponse perfil(
      @Valid @RequestBody PerfilRequest r, @AuthenticationPrincipal UsuarioPrincipal actor) {
    return service.actualizarPerfil(r, actor);
  }

  // Consulta un recurso por su identificador; si no existe, responde 404.
  @GetMapping("/{id}")
  public UsuarioResponse obtener(@PathVariable Long id) {
    return service.obtener(id);
  }

  // POST devuelve 201 Created y la URL donde consultar el nuevo recurso.
  // Crea un recurso nuevo y devuelve 201 Created.
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "201",
      description = "Recurso creado")
  @PostMapping
  public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioRequest r) {
    var creado = service.crear(r);
    return ResponseEntity.created(URI.create("/api/usuarios/" + creado.id())).body(creado);
  }

  // PUT actualiza un recurso existente. @Valid ejecuta las restricciones del DTO.
  // Recibe los datos completos editables y actualiza el recurso existente.
  @PutMapping("/{id}")
  public UsuarioResponse actualizar(
      @PathVariable Long id,
      @Valid @RequestBody UsuarioRequest r,
      @AuthenticationPrincipal UsuarioPrincipal actor) {
    return service.actualizar(id, r, actor);
  }

  // DELETE devuelve 204: la operación terminó y no necesita un cuerpo JSON.
  // Solicita un borrado; el servicio verifica permisos y relaciones antes de eliminar.
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "204",
      description = "Recurso eliminado")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> eliminar(
      @PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal actor) {
    service.eliminar(id, actor);
    return ResponseEntity.noContent().build();
  }
}
