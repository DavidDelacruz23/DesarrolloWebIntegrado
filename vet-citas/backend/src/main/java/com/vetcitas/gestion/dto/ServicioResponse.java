package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// Datos del catálogo que el navegador puede presentar sin acceder a la entidad.
public record ServicioResponse(
    Long id,
    String nombre,
    String descripcion,
    BigDecimal precio,
    Integer duracionMinutos,
    boolean activo) {}
