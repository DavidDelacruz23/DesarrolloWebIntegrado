package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// Solo recibe el estado solicitado. El servicio decide si la transición está permitida.
public record EstadoRequest(@NotNull EstadoReserva estado) {}
