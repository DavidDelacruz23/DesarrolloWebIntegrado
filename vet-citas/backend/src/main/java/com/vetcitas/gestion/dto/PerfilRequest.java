package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// Incluye solo datos de contacto; no admite rol, estado ni contraseñas.
public record PerfilRequest(
    @NotBlank @Size(max = 120) String nombre,
    @NotBlank @Email @Size(max = 150) String correo,
    @NotBlank @Size(max = 20) String telefono) {}
