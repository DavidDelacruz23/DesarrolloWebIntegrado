# VetCitas — Módulo de Autenticación y Usuarios

Primer entregable del sistema de gestión de citas veterinarias.
Incluye registro de clientes, login con identificación de rol
(CLIENTE / VETERINARIO / ADMIN) y protección básica de paneles
mediante sesión HTTP.

## Estructura del proyecto

```
vet-citas-auth/
├── database/
│   └── schema.sql                 -> Script de creación de BD + datos de prueba
├── backend/                       -> Proyecto Spring Boot (Maven)
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/vetcitas/gestion/
│       │   ├── GestionCitasVeterinariasApplication.java
│       │   ├── config/SecurityConfig.java
│       │   ├── entity/(Usuario.java, Rol.java)
│       │   ├── repository/UsuarioRepository.java
│       │   ├── service/(UsuarioService.java, UsuarioServiceImpl.java)
│       │   ├── controller/AuthController.java
│       │   ├── dto/(RegistroRequest, LoginRequest, AuthResponse)
│       │   └── exception/(CorreoDuplicadoException, CredencialesInvalidasException, GlobalExceptionHandler)
│       └── resources/
│           ├── application.properties
│           └── static/
│               ├── login.html
│               ├── registro.html
│               ├── panel-cliente.html
│               ├── panel-veterinario.html
│               ├── panel-admin.html
│               ├── css/styles.css
│               └── js/auth.js
```

## Cómo ejecutarlo

1. **Base de datos**: abre MySQL (Workbench, DBeaver o consola) y ejecuta
   `database/schema.sql`. Esto crea la base `vet_citas_db`, la tabla
   `usuarios` y 3 usuarios de prueba.
   > El hash de contraseña incluido en el script es solo un ejemplo de
   > formato. Para que las contraseñas de prueba funcionen realmente
   > con el `PasswordEncoder` del backend, sigue la nota al final del
   > propio archivo `schema.sql` (registra un usuario real desde el
   > formulario y luego actualiza su rol con un `UPDATE`).

2. **Backend**:
   - Abre la carpeta `backend/` en tu IDE (IntelliJ, Eclipse o VS Code)
     como proyecto Maven, o pégala sobre un proyecto ya generado desde
     [start.spring.io](https://start.spring.io) con las dependencias
     indicadas en el propio `pom.xml`.
   - Edita `src/main/resources/application.properties` con tu usuario
     y contraseña reales de MySQL.
   - Ejecuta la clase `GestionCitasVeterinariasApplication`.
   - El servidor levanta en `http://localhost:8080`.

3. **Frontend**: como los archivos HTML/CSS/JS están dentro de
   `src/main/resources/static`, Spring Boot los sirve automáticamente.
   Abre en el navegador:
   - `http://localhost:8080/registro.html` para crear una cuenta cliente.
   - `http://localhost:8080/login.html` para iniciar sesión.

## Endpoints del backend

| Método | Ruta                  | Descripción                                      |
|--------|-----------------------|---------------------------------------------------|
| POST   | `/api/auth/registro`  | Registra un cliente nuevo (rol CLIENTE por defecto)|
| POST   | `/api/auth/login`     | Valida credenciales y abre sesión                  |
| GET    | `/api/auth/sesion`    | Devuelve id/nombre/rol de la sesión activa (o 401) |
| POST   | `/api/auth/logout`    | Cierra la sesión activa                            |

## Decisiones de diseño para explicar en la presentación

- **BCrypt vía Spring Security**: se agregó `spring-boot-starter-security`
  únicamente para usar el `PasswordEncoder` (BCrypt). No se configuró
  una cadena de filtros de seguridad completa en este entregable para
  mantener el flujo simple; el control de acceso a los paneles se hace
  con sesión HTTP (`HttpSession`) y se valida en el frontend (`auth.js`)
  llamando a `/api/auth/sesion`.
- **DTOs en vez de exponer la entidad `Usuario` directamente**: evita que
  el cliente pueda enviar campos sensibles como `rol` o `activo` desde
  el formulario público de registro.
- **Rol como `ENUM` en la tabla `usuarios`**: suficiente para 3 roles
  fijos; se documenta en `schema.sql` cómo migrar a una tabla `roles`
  aparte si en el futuro se necesitan roles dinámicos.
- **Mensajes de error genéricos en login** ("Correo o contraseña
  incorrectos"): evita ataques de enumeración de usuarios.
