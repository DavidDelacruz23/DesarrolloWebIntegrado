package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// Incluye nombres para mostrar en pantalla, IDs para editar y el precio histórico de la cita.
public record ReservaResponse(
    Long id,
    Long usuarioId,
    String cliente,
    Long veterinarioId,
    String veterinario,
    Long servicioId,
    String servicio,
    String mascota,
    String motivo,
    LocalDateTime fechaHora,
    LocalDateTime fin,
    BigDecimal precio,
    EstadoReserva estado) {}
