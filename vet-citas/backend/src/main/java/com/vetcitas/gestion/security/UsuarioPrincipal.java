package com.vetcitas.gestion.security;

import com.vetcitas.gestion.entity.*;
import java.util.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/** Datos internos de la sesión de Spring Security. Nunca se devuelven como JSON. */
public record UsuarioPrincipal(Long id, String correo, String password, Rol rol, boolean activo)
    implements UserDetails {
  public static UsuarioPrincipal de(Usuario u) {
    return new UsuarioPrincipal(
        u.getId(), u.getCorreo(), u.getPassword(), u.getRol(), u.isActivo());
  }

  @Override
  public String getUsername() {
    return correo;
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
  }

  @Override
  public boolean isEnabled() {
    return activo;
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }
}
