package com.vetcitas.gestion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @SpringBootApplication -> Es una anotación "combo" que agrupa tres: 1) @Configuration -> esta
 * clase puede definir beans. 2) @EnableAutoConfiguration -> Spring Boot configura automáticamente
 * todo lo necesario según las dependencias del pom.xml (por ejemplo, al detectar
 * spring-boot-starter-data-jpa + el driver de MySQL, configura solo el DataSource y el
 * EntityManager). 3) @ComponentScan -> Spring escanea este paquete (com.vetcitas.gestion) y todos
 * sus subpaquetes buscando clases anotadas
 * con @Component, @Service, @Repository, @Controller/@RestController, etc., para registrarlas
 * automáticamente en su contenedor de IoC.
 *
 * <p>Por eso es MUY importante que esta clase esté en el paquete raíz (com.vetcitas.gestion) y no
 * dentro de un subpaquete: si estuviera, por ejemplo, dentro de "controller", Spring nunca
 * encontraría las clases de "service" o "repository".
 */
@SpringBootApplication
public class GestionCitasVeterinariasApplication {

  public static void main(String[] args) {
    SpringApplication.run(GestionCitasVeterinariasApplication.class, args);
  }
}
