# Implementación correspondiente al avance de la semana 10

El modelo Word sirve como guía de requisitos técnicos para el backend. El dominio se adapta a VetCitas: los servicios son prestaciones veterinarias y las reservas son citas. Se conserva el rol CLIENTE del proyecto original como equivalente al usuario final del modelo, junto con VETERINARIO y ADMIN.

## Correspondencia con la rúbrica

| Requisito del modelo | Implementación verificable |
|---|---|
| Arquitectura por capas | Paquetes controller, service, repository, entity, dto, security, config y exception |
| API REST y CRUD | Usuarios, servicios y reservas con GET, POST, PUT y DELETE; PATCH para estado |
| Inyección de dependencias | Constructores de controladores y servicios, beans de configuración |
| DTOs | Requests validados y responses sin contraseñas ni entidades recursivas |
| JPA/Hibernate con MySQL | Usuario, Servicio y Reserva; IDs automáticos y repositorios JPA |
| Relaciones | Usuario→Reservas, Veterinario→Reservas y Servicio→Reservas |
| Consultas | Búsqueda JPQL por nombre, listado de reservas por actor/estado y comprobación de cruces |
| Spring Security | Sesión persistida, BCrypt, roles, CSRF, protección de API y páginas |
| Evidencias y documentación | Postman, Swagger, SQL, guía de ejecución, pruebas y capturas verificadas |

## Decisiones del avance

Se añadió `/api` a las rutas para separar REST de HTML. Usuarios es un CRUD administrativo; cada usuario final consulta y edita su perfil en `/api/usuarios/me`, evitando exponer el listado completo de clientes a cualquier cuenta. Las reservas se limitan al propietario o al profesional asignado, salvo el administrador.

Los errores del modelo de informe se consideran ejemplos de redacción, no problemas necesariamente encontrados en este proyecto. La aplicación se sirve en el mismo origen, así que no requiere CORS global. La contraseña se protege mediante hash BCrypt, que no permite recuperar el texto original.

El borrado físico está implementado, pero se devuelve 409 si un usuario o servicio conserva reservas. La desactivación sirve para mantener historial. El nombre de la mascota pertenece a cada reserva en este alcance; no se añadió ficha clínica ni módulo de mascotas porque no se describe en el modelo.

## Material para completar el informe académico

La carpeta `docs/evidencias` contiene capturas y resultados reales de esta implementación. `database/consultas.sql` permite repetir consultas en Workbench. `docs/VERIFICACION.md` enumera qué se comprobó.

Los nombres de institución, docente, integrantes y el enlace de un repositorio GitHub no se inventaron. No se creó ni publicó un repositorio remoto. Para el informe final, completa esos datos personales y, si subes el proyecto, incluye el enlace y tus commits reales. El documento Word de referencia se conserva sin modificaciones.
