package com.vetcitas.gestion.service;

import com.vetcitas.gestion.dto.AuthResponse;
import com.vetcitas.gestion.dto.LoginRequest;
import com.vetcitas.gestion.dto.RegistroRequest;


public interface UsuarioService {

  
    AuthResponse registrarCliente(RegistroRequest request);

    AuthResponse iniciarSesion(LoginRequest request);
}
