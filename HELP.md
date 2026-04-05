# Getting Started

## GoPoli (mapas y ubicaciones)

- **Clave de Google Maps:** guía en [`frontend/README_MAPS_ES.md`](frontend/README_MAPS_ES.md). Copias [`frontend/lib/config/google_maps_config.example.dart`](frontend/lib/config/google_maps_config.example.dart) a `google_maps_config.dart` (local, en `.gitignore`; Android lee la misma clave al compilar).
- **Coordenadas en la tabla `ubicacion`:** al arrancar el backend, `UbicacionCoordenadasSeeder` sincroniza `latitud`/`longitud` para cada nombre de estación / salida Poli (ver `backend/scripts/seed_ubicaciones_metro_poli.sql` si prefieres SQL a mano).

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/4.0.3/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/4.0.3/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/4.0.3/reference/web/servlet.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/4.0.3/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Spring Boot DevTools](https://docs.spring.io/spring-boot/4.0.3/reference/using/devtools.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

