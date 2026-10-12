package com.vetcitas.gestion.controller;

import com.vetcitas.gestion.dto.*;
import com.vetcitas.gestion.security.UsuarioPrincipal;
import com.vetcitas.gestion.service.UsuarioService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.session.ChangeSessionIdAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.*;
import org.springframework.web.bind.annotation.*;

/** Autenticación por cookie de sesión: el navegador guarda JSESSIONID automáticamente. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final UsuarioService usuarios;
  private final AuthenticationManager manager;
  private final SecurityContextRepository contexts;
  private final CsrfTokenRepository csrf;

  public AuthController(
      UsuarioService usuarios,
      AuthenticationManager manager,
      SecurityContextRepository contexts,
      CsrfTokenRepository csrf) {
    this.usuarios = usuarios;
    this.manager = manager;
    this.contexts = contexts;
    this.csrf = csrf;
  }

  // El frontend pide este token antes de enviar formularios, incluso antes del login.
  @GetMapping("/csrf")
  public Map<String, String> token(CsrfToken token) {
    return Map.of("token", token.getToken(), "headerName", token.getHeaderName());
  }

  @io.swagger.v3.oas.annotations.responses.ApiResponse(
      responseCode = "201",
      description = "Recurso creado")
  @PostMapping("/registro")
  public ResponseEntity<AuthResponse> registro(@Valid @RequestBody RegistroRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(usuarios.registrarCliente(r));
  }

  @PostMapping("/login")
  public AuthResponse login(
      @Valid @RequestBody LoginRequest r, HttpServletRequest req, HttpServletResponse res) {
    var auth =
        manager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(
                r.correo().trim().toLowerCase(Locale.ROOT), r.password()));
    // Cambiar el id de sesión al entrar evita reutilizar un identificador previo al login.
    new ChangeSessionIdAuthenticationStrategy().onAuthentication(auth, req, res);
    var context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(auth);
    SecurityContextHolder.setContext(context);
    contexts.saveContext(context, req, res);
    csrf.saveToken(null, req, res); // El cliente solicitará un token nuevo después del login.
    var u = usuarios.obtener(((UsuarioPrincipal) auth.getPrincipal()).id());
    return new AuthResponse(u.id(), u.nombre(), u.correo(), u.rol(), "Sesión iniciada");
  }

  // Devuelve la cuenta actual sin exponer su contraseña ni hash.
  @GetMapping("/sesion")
  public UsuarioResponse sesion(@AuthenticationPrincipal UsuarioPrincipal actor) {
    return usuarios.obtener(actor.id());
  }

  @PostMapping("/logout")
  public Map<String, String> logout(HttpServletRequest req, HttpServletResponse res) {
    var session = req.getSession(false);
    if (session != null) session.invalidate();
    SecurityContextHolder.clearContext();
    csrf.saveToken(null, req, res);
    return Map.of("mensaje", "Sesión cerrada");
  }
}
