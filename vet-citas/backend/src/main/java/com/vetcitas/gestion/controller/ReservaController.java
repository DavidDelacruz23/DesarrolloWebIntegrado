package com.vetcitas.gestion.controller;

import com.vetcitas.gestion.dto.*;
import com.vetcitas.gestion.entity.EstadoReserva;
import com.vetcitas.gestion.security.UsuarioPrincipal;
import com.vetcitas.gestion.service.ReservaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** El actor se toma de la sesión verificada, nunca de un rol enviado por el navegador. */
@RestController
@RequestMapping("/api/reservas")
public class ReservaController {
  private final ReservaService service;

  public ReservaController(ReservaService service) {
    this.service = service;
  }

  // Lista los registros que este controlador permite consultar.
  @GetMapping
  public List<ReservaResponse> listar(
      @RequestParam(required = false) EstadoReserva estado,
      @AuthenticationPrincipal UsuarioPrincipal actor) {
    return service.listar(actor, estado);
  }

  // Consulta un recurso por su identificador; si no existe, responde 404.
  @GetMapping("/{id}")
  public ReservaResponse obtener(
      @PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal actor) {
    return service.obtener(id, actor);
  }

  // Crea un recurso nuevo y devuelve 201 Created.
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "201",
      description = "Recurso creado")
  @PostMapping
  public ResponseEntity<ReservaResponse> crear(
      @Valid @RequestBody ReservaRequest r, @AuthenticationPrincipal UsuarioPrincipal actor) {
    var cita = service.crear(r, actor);
    return ResponseEntity.created(URI.create("/api/reservas/" + cita.id())).body(cita);
  }

  // Recibe los datos completos editables y actualiza el recurso existente.
  @PutMapping("/{id}")
  public ReservaResponse actualizar(
      @PathVariable Long id,
      @Valid @RequestBody ReservaRequest r,
      @AuthenticationPrincipal UsuarioPrincipal actor) {
    return service.actualizar(id, r, actor);
  }

  // PATCH modifica solo el estado; el servicio comprueba qué transiciones permite cada rol.
  @PatchMapping("/{id}/estado")
  public ReservaResponse estado(
      @PathVariable Long id,
      @Valid @RequestBody EstadoRequest r,
      @AuthenticationPrincipal UsuarioPrincipal actor) {
    return service.cambiarEstado(id, r.estado(), actor);
  }

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
