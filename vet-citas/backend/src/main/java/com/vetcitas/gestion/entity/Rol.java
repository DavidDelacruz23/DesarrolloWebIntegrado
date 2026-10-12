package com.vetcitas.gestion.entity;

/**
 * Enum que representa los roles disponibles en el sistema. Usar un enum (en lugar de un String
 * libre) nos da seguridad de tipos: el compilador impide asignar un rol que no exista (evita
 * errores como escribir "CLIENT" en vez de "CLIENTE").
 */
public enum Rol {
  CLIENTE,
  VETERINARIO,
  ADMIN
}
