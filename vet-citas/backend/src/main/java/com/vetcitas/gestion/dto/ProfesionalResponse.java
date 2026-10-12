package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.*;
import jakarta.validation.constraints.*;

/**
 * DTO: transporta solo los campos permitidos. Un record genera constructor y métodos de lectura.
 */
// Solo id y nombre: el selector de citas no necesita datos privados del profesional.
public record ProfesionalResponse(Long id, String nombre) {}
