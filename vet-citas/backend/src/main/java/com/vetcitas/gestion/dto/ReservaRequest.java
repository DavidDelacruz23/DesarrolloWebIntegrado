package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// IDs enlazan las tres entidades. fechaHora utiliza hora de Lima; el estado no se envía aquí.
public record ReservaRequest(
    @Positive Long usuarioId,
    @NotNull @Positive Long veterinarioId,
    @NotNull @Positive Long servicioId,
    @NotBlank @Size(max = 100) String mascota,
    @NotBlank @Size(max = 500) String motivo,
    @NotNull LocalDateTime fechaHora) {}
