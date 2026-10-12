package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// Datos públicos del perfil. Se omiten siempre password y relaciones JPA.
public record UsuarioResponse(
    Long id,
    String nombre,
    String correo,
    String telefono,
    Rol rol,
    boolean activo,
    LocalDateTime fechaRegistro) {}
