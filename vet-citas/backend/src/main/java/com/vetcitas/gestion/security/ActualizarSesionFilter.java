package com.vetcitas.gestion.security;

import com.vetcitas.gestion.repository.UsuarioRepository;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Revisa la cuenta en cada petición: un cambio de rol o una desactivación tiene efecto inmediato.
 */
public class ActualizarSesionFilter extends OncePerRequestFilter {
  private final UsuarioRepository usuarios;

  public ActualizarSesionFilter(UsuarioRepository usuarios) {
    this.usuarios = usuarios;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest req, HttpServletResponse res, FilterChain chain)
      throws ServletException, IOException {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.getPrincipal() instanceof UsuarioPrincipal anterior) {
      var actual = usuarios.findById(anterior.id()).orElse(null);
      if (actual == null
          || !actual.isActivo()
          || !actual.getPassword().equals(anterior.password())) {
        // Una cuenta borrada, inactiva o con otra contraseña ya no conserva su sesión anterior.
        SecurityContextHolder.clearContext();
        var session = req.getSession(false);
        if (session != null) session.invalidate();
      } else {
        var principal = UsuarioPrincipal.de(actual);
        var contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(
            UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(contexto);
      }
    }
    chain.doFilter(req, res);
  }
}
