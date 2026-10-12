package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// Precio en soles y duración entre 15 y 240 minutos; activo habilita nuevas reservas.
public record ServicioRequest(
    @NotBlank @Size(max = 100) String nombre,
    @NotBlank @Size(max = 500) String descripcion,
    @NotNull @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal precio,
    @NotNull @Min(15) @Max(240) Integer duracionMinutos,
    @NotNull Boolean activo) {}
