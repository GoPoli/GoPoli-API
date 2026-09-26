package com.gopoli.api;

import java.time.ZoneId;
import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GoPoliApplication {

    static final String DEFAULT_TIMEZONE = "America/Bogota";

    public static void main(String[] args) {
        // La sesión JDBC hereda la zona de la JVM; debe coincidir con el reloj de la aplicación.
        TimeZone.setDefault(TimeZone.getTimeZone(applicationZone(System.getenv("APP_TIMEZONE"))));
        SpringApplication.run(GoPoliApplication.class, args);
    }

    static ZoneId applicationZone(String configured) {
        return ZoneId.of(configured == null || configured.isBlank() ? DEFAULT_TIMEZONE : configured.trim());
    }
}
