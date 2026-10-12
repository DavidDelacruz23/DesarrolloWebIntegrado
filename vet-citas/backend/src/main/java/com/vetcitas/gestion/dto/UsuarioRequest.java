package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// rol y activo son editables solo por ADMIN. password vacío conserva la clave al editar.
public record UsuarioRequest(
    @NotBlank @Size(max = 120) String nombre,
    @NotBlank @Email @Size(max = 150) String correo,
    @NotBlank @Size(max = 20) String telefono,
    @Size(max = 72) String password,
    @NotNull Rol rol,
    @NotNull Boolean activo) {}
