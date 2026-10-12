package com.vetcitas.gestion.dto;

import com.vetcitas.gestion.entity.Rol;


public class AuthResponse {

    private Long id;
    private String nombre;
    private String correo;
    private Rol rol;
    private String mensaje;

    public AuthResponse() {
    }

    public AuthResponse(Long id, String nombre, String correo, Rol rol, String mensaje) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.mensaje = mensaje;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
