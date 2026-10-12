package com.vetcitas.gestion.service;

import com.vetcitas.gestion.dto.*;
import com.vetcitas.gestion.security.UsuarioPrincipal;
import java.util.List;

/** Contrato del servicio: el controlador conoce operaciones, no consultas SQL. */
public interface UsuarioService {
  AuthResponse registrarCliente(RegistroRequest request);

  List<UsuarioResponse> listar();

  UsuarioResponse obtener(Long id);

  UsuarioResponse crear(UsuarioRequest request);

  UsuarioResponse actualizar(Long id, UsuarioRequest request, UsuarioPrincipal actor);

  UsuarioResponse actualizarPerfil(PerfilRequest request, UsuarioPrincipal actor);

  void eliminar(Long id, UsuarioPrincipal actor);

  List<ProfesionalResponse> veterinarios();
}
