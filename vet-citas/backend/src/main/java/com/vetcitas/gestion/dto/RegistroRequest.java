package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// nombre, correo y teléfono identifican al cliente; password se recibe solo para generar su hash.
public record RegistroRequest(
    @NotBlank @Size(max = 120) String nombre,
    @NotBlank @Email @Size(max = 150) String correo,
    @NotBlank @Size(max = 20) String telefono,
    @NotBlank @Size(min = 8, max = 72) String password) {}
