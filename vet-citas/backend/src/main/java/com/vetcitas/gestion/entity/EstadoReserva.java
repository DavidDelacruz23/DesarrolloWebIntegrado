package com.vetcitas.gestion.entity;

/** El enum limita los estados posibles tanto en Java como en las peticiones JSON. */
public enum EstadoReserva {
  PENDIENTE,
  CONFIRMADA,
  ATENDIDA,
  CANCELADA
}
