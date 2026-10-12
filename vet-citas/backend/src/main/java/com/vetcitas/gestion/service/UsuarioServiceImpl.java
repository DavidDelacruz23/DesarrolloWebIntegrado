package com.vetcitas.gestion.service;

import com.vetcitas.gestion.dto.*;
import com.vetcitas.gestion.entity.*;
import com.vetcitas.gestion.exception.ReglaNegocioException;
import com.vetcitas.gestion.repository.*;
import com.vetcitas.gestion.security.UsuarioPrincipal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Aquí se aplican reglas y transacciones; el controlador solo recibe y devuelve HTTP. */
@Service
@Transactional
public class UsuarioServiceImpl implements UsuarioService {
  private final UsuarioRepository usuarios;
  private final ReservaRepository reservas;
  private final PasswordEncoder encoder;

  // Inyección por constructor: Spring entrega las dependencias sin usar new en los controladores.
  public UsuarioServiceImpl(
      UsuarioRepository usuarios, ReservaRepository reservas, PasswordEncoder encoder) {
    this.usuarios = usuarios;
    this.reservas = reservas;
    this.encoder = encoder;
  }

  @Override
  public AuthResponse registrarCliente(RegistroRequest r) {
    // El registro público siempre crea CLIENTE, aunque alguien intente enviar un rol distinto.
    var u =
        crear(
            new UsuarioRequest(
                r.nombre(), r.correo(), r.telefono(), r.password(), Rol.CLIENTE, true));
    return new AuthResponse(
        u.id(), u.nombre(), u.correo(), u.rol(), "Cuenta creada. Ya puedes iniciar sesión");
  }

  @Override
  @Transactional(readOnly = true)
  public List<UsuarioResponse> listar() {
    return usuarios.findAll(org.springframework.data.domain.Sort.by("nombre")).stream()
        .map(this::dto)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public UsuarioResponse obtener(Long id) {
    return dto(buscar(id));
  }

  @Override
  public UsuarioResponse crear(UsuarioRequest r) {
    validarPassword(r.password(), true);
    var u = new Usuario();
    aplicar(u, r);
    u.setPassword(encoder.encode(r.password()));
    return dto(usuarios.save(u));
  }

  @Override
  public UsuarioResponse actualizar(Long id, UsuarioRequest r, UsuarioPrincipal actor) {
    var u = buscar(id);
    if (id.equals(actor.id()) && (r.rol() != Rol.ADMIN || !r.activo()))
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT,
          "No puedes quitarte el rol de administrador ni desactivar tu propia cuenta");
    if (u.getRol() != r.rol() && reservas.existsByUsuarioIdOrVeterinarioId(id, id))
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT, "No puedes cambiar el rol de un usuario con reservas asociadas");
    validarPassword(r.password(), false);
    aplicar(u, r);
    if (r.password() != null && !r.password().isEmpty())
      u.setPassword(encoder.encode(r.password()));
    return dto(usuarios.save(u));
  }

  @Override
  public UsuarioResponse actualizarPerfil(PerfilRequest r, UsuarioPrincipal actor) {
    var u = buscar(actor.id());
    validarCorreo(r.correo(), u.getId());
    // El DTO de perfil no admite rol ni activo, por lo que un cliente no puede ascender sus
    // permisos.
    u.setNombre(r.nombre().trim());
    u.setCorreo(normalizarCorreo(r.correo()));
    u.setTelefono(r.telefono().trim());
    return dto(usuarios.save(u));
  }

  @Override
  public void eliminar(Long id, UsuarioPrincipal actor) {
    var u = buscar(id);
    if (id.equals(actor.id()))
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT, "No puedes eliminar tu propia cuenta de administrador");
    if (reservas.existsByUsuarioIdOrVeterinarioId(id, id))
      throw new ReglaNegocioException(
          HttpStatus.CONFLICT,
          "Este usuario tiene reservas. Puedes desactivarlo para conservar el historial");
    usuarios.delete(u);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ProfesionalResponse> veterinarios() {
    // El catálogo público para clientes solo muestra id y nombre, nunca teléfonos ni correos del
    // personal.
    return usuarios.findByRolAndActivoTrueOrderByNombreAsc(Rol.VETERINARIO).stream()
        .map(u -> new ProfesionalResponse(u.getId(), u.getNombre()))
        .toList();
  }

  private Usuario buscar(Long id) {
    return usuarios
        .findById(id)
        .orElseThrow(
            () -> new ReglaNegocioException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
  }

  private void aplicar(Usuario u, UsuarioRequest r) {
    validarCorreo(r.correo(), u.getId() == null ? 0L : u.getId());
    u.setNombre(r.nombre().trim());
    u.setCorreo(normalizarCorreo(r.correo()));
    u.setTelefono(r.telefono().trim());
    u.setRol(r.rol());
    u.setActivo(r.activo());
  }

  private String normalizarCorreo(String correo) {
    return correo.trim().toLowerCase(Locale.ROOT);
  }

  private void validarCorreo(String correo, Long id) {
    if (usuarios.existsByCorreoIgnoreCaseAndIdNot(normalizarCorreo(correo), id))
      throw new ReglaNegocioException(HttpStatus.CONFLICT, "Ya existe una cuenta con este correo");
  }

  private void validarPassword(String password, boolean obligatorio) {
    if (!obligatorio && (password == null || password.isEmpty())) return;
    // BCrypt tiene un límite de 72 bytes: también se verifica para contraseñas con tildes o emojis.
    if (password == null
        || password.isBlank()
        || password.length() < 8
        || password.getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ReglaNegocioException(
          HttpStatus.BAD_REQUEST,
          "La contraseña debe tener al menos 8 caracteres y como máximo 72 bytes UTF-8");
  }

  private UsuarioResponse dto(Usuario u) {
    return new UsuarioResponse(
        u.getId(),
        u.getNombre(),
        u.getCorreo(),
        u.getTelefono(),
        u.getRol(),
        u.isActivo(),
        u.getFechaRegistro());
  }
}
