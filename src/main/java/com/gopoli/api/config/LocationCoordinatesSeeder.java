package com.gopoli.api.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.gopoli.api.model.Location;
import com.gopoli.api.repository.LocationRepository;

// Sincroniza al arrancar las coordenadas (WGS84) de las estaciones del metro y las salidas del campus.
@Component
public class LocationCoordinatesSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(LocationCoordinatesSeeder.class);
    private static final double TOLERANCE = 1e-7;

    private static final Map<String, double[]> COORDINATES_BY_NAME = buildCoordinates();

    private final LocationRepository locationRepository;

    public LocationCoordinatesSeeder(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    private static Map<String, double[]> buildCoordinates() {
        Map<String, double[]> m = new LinkedHashMap<>();
        m.put("salida principal", new double[]{6.1960, -75.5863});
        m.put("salida parqueadero", new double[]{6.1945, -75.5872});
        m.put("estación niquía", new double[]{6.33764, -75.37808});
        m.put("estación bello", new double[]{6.32583, -75.55778});
        m.put("estación madera", new double[]{6.31361, -75.55750});
        m.put("estación acevedo", new double[]{6.30194, -75.55917});
        m.put("estación tricentenario", new double[]{6.29750, -75.55472});
        m.put("estación caribe", new double[]{6.28972, -75.55972});
        m.put("estación universidad", new double[]{6.26972, -75.56833});
        m.put("estación hospital", new double[]{6.26472, -75.56611});
        m.put("estación prado", new double[]{6.25778, -75.56444});
        m.put("estación parque berrío", new double[]{6.25194, -75.56583});
        m.put("estación san antonio", new double[]{6.25306, -75.56528});
        m.put("estación alpujarra", new double[]{6.24667, -75.57222});
        m.put("estación exposiciones", new double[]{6.24056, -75.57583});
        m.put("estación industriales", new double[]{6.22972, -75.57528});
        m.put("estación poblado", new double[]{6.20806, -75.56694});
        m.put("estación aguacatala", new double[]{6.19472, -75.58028});
        m.put("estación ayurá", new double[]{6.18500, -75.59639});
        m.put("estación envigado", new double[]{6.16917, -75.59111});
        m.put("estación itagüí", new double[]{6.17167, -75.61028});
        m.put("estación sabaneta", new double[]{6.15056, -75.61639});
        m.put("estación la estrella", new double[]{6.15639, -75.64278});
        m.put("estación cisneros", new double[]{6.28470, -75.55140});
        m.put("estación suramericana", new double[]{6.24472, -75.59417});
        m.put("estación estadio", new double[]{6.25611, -75.59139});
        m.put("estación floresta", new double[]{6.26306, -75.59861});
        m.put("estación santa lucía", new double[]{6.27000, -75.60389});
        m.put("estación san javier", new double[]{6.25583, -75.62222});
        return Map.copyOf(m);
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Location> locations = locationRepository.findAll();
        int updated = 0;
        int unmatched = 0;
        int unchanged = 0;
        for (Location location : locations) {
            if (location.getName() == null || location.getName().isBlank()) {
                continue;
            }
            double[] coordinates = COORDINATES_BY_NAME.get(location.getName().trim().toLowerCase(Locale.ROOT));
            if (coordinates == null) {
                unmatched++;
                log.warn("Sin coordenadas conocidas para la ubicación '{}'", location.getName());
                continue;
            }
            if (location.getLatitude() != null && location.getLongitude() != null
                    && Math.abs(location.getLatitude() - coordinates[0]) < TOLERANCE
                    && Math.abs(location.getLongitude() - coordinates[1]) < TOLERANCE) {
                unchanged++;
                continue;
            }
            location.setLatitude(coordinates[0]);
            location.setLongitude(coordinates[1]);
            locationRepository.save(location);
            updated++;
        }
        log.info("Coordenadas de ubicaciones: {} actualizadas, {} sin cambios, {} sin coincidencia",
                updated, unchanged, unmatched);
    }
}
