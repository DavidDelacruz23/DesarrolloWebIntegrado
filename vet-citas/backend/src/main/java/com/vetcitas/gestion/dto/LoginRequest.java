package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// correo identifica la cuenta y password se utiliza únicamente para verificar credenciales.
public record LoginRequest(
    @NotBlank @Email @Size(max = 150) String correo, @NotBlank @Size(max = 72) String password) {}
