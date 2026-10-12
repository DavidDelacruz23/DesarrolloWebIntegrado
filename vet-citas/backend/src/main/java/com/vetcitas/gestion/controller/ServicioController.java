package com.vetcitas.gestion.controller;

import com.vetcitas.gestion.dto.*;
import com.vetcitas.gestion.entity.Rol;
import com.vetcitas.gestion.security.UsuarioPrincipal;
import com.vetcitas.gestion.service.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Todos los usuarios autenticados consultan el catálogo; solo ADMIN puede modificarlo. */
@RestController
@RequestMapping("/api")
public class ServicioController {
  private final ServicioService service;
  private final UsuarioService usuarios;

  public ServicioController(ServicioService service, UsuarioService usuarios) {
    this.service = service;
    this.usuarios = usuarios;
  }

  // El parámetro opcional nombre demuestra la búsqueda JPQL solicitada en el informe.
  // Busca servicios por nombre mediante la consulta JPQL del repositorio.
  @GetMapping("/servicios")
  public List<ServicioResponse> listar(
      @RequestParam(defaultValue = "") String nombre,
      @AuthenticationPrincipal UsuarioPrincipal actor) {
    return service.listar(nombre, actor.rol() == Rol.ADMIN);
  }

  // Consulta un servicio; los inactivos solo son visibles para ADMIN.
  @GetMapping("/servicios/{id}")
  public ServicioResponse obtener(
      @PathVariable Long id, @AuthenticationPrincipal UsuarioPrincipal actor) {
    return service.obtener(id, actor.rol() == Rol.ADMIN);
  }

  // Crea un servicio; la cadena de seguridad exige ADMIN.
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "201",
      description = "Recurso creado")
  @PostMapping("/servicios")
  public ResponseEntity<ServicioResponse> crear(@Valid @RequestBody ServicioRequest r) {
    var s = service.crear(r);
    return ResponseEntity.created(URI.create("/api/servicios/" + s.id())).body(s);
  }

  // Actualiza el catálogo; las citas guardan su propio precio histórico.
  @PutMapping("/servicios/{id}")
  public ServicioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ServicioRequest r) {
    return service.actualizar(id, r);
  }

  // Borra un servicio sin reservas; si tiene historial, el servicio devuelve 409.
  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "204",
      description = "Recurso eliminado")
  @DeleteMapping("/servicios/{id}")
  public ResponseEntity<Void> eliminar(@PathVariable Long id) {
    service.eliminar(id);
    return ResponseEntity.noContent().build();
  }

  // Lista mínima para el selector de profesionales al agendar una cita.
  @GetMapping("/veterinarios")
  public List<ProfesionalResponse> veterinarios() {
    return usuarios.veterinarios();
  }
}
