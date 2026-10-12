package com.vetcitas.gestion.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/** Representa una fila de la tabla servicios. JPA convierte estos objetos en registros SQL. */
@Entity
@Table(name = "servicios")
public class Servicio {
  // Identificador del servicio.
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Por ejemplo, Consulta general o Vacunación.
  @Column(nullable = false, length = 100)
  private String nombre;

  // Explica qué incluye el servicio.
  @Column(nullable = false, length = 500)
  private String descripcion;

  // BigDecimal evita errores de redondeo al representar soles.
  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal precio;

  // Duración usada para calcular la hora de finalización.
  @Column(nullable = false)
  private Integer duracionMinutos;

  // Solo se pueden reservar servicios activos.
  @Column(nullable = false)
  private boolean activo = true;

  // Relación inversa: un servicio aparece en muchas reservas.
  @OneToMany(mappedBy = "servicio")
  private List<Reserva> reservas = new ArrayList<>();

  // JPA necesita un constructor sin argumentos para reconstruir objetos desde MySQL.
  public Servicio() {}

  /** Permite leer id sin acceder directamente al atributo privado. */
  public Long getId() {
    return id;
  }

  /** Asigna id; las reglas de negocio se validan en el servicio. */
  public void setId(Long id) {
    this.id = id;
  }

  /** Permite leer nombre sin acceder directamente al atributo privado. */
  public String getNombre() {
    return nombre;
  }

  /** Asigna nombre; las reglas de negocio se validan en el servicio. */
  public void setNombre(String nombre) {
    this.nombre = nombre;
  }

  /** Permite leer descripcion sin acceder directamente al atributo privado. */
  public String getDescripcion() {
    return descripcion;
  }

  /** Asigna descripcion; las reglas de negocio se validan en el servicio. */
  public void setDescripcion(String descripcion) {
    this.descripcion = descripcion;
  }

  /** Permite leer precio sin acceder directamente al atributo privado. */
  public BigDecimal getPrecio() {
    return precio;
  }

  /** Asigna precio; las reglas de negocio se validan en el servicio. */
  public void setPrecio(BigDecimal precio) {
    this.precio = precio;
  }

  /** Permite leer duracionMinutos sin acceder directamente al atributo privado. */
  public Integer getDuracionMinutos() {
    return duracionMinutos;
  }

  /** Asigna duracionMinutos; las reglas de negocio se validan en el servicio. */
  public void setDuracionMinutos(Integer duracionMinutos) {
    this.duracionMinutos = duracionMinutos;
  }

  /** Permite leer activo sin acceder directamente al atributo privado. */
  public boolean isActivo() {
    return activo;
  }

  /** Asigna activo; las reglas de negocio se validan en el servicio. */
  public void setActivo(boolean activo) {
    this.activo = activo;
  }

  /** Permite leer reservas sin acceder directamente al atributo privado. */
  public List<Reserva> getReservas() {
    return reservas;
  }

  /** Asigna reservas; las reglas de negocio se validan en el servicio. */
  public void setReservas(List<Reserva> reservas) {
    this.reservas = reservas;
  }
}
