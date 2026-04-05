package com.proyect.gopoli.config;

import com.proyect.gopoli.model.Ubicacion;
import com.proyect.gopoli.repository.UbicacionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Asigna lat/long a cada fila de {@code ubicacion} según el nombre exacto de tu SQL
 * (metro Medellín + puntos alrededor del Politécnico Jaime Isaza Cadavid).
 * Sincroniza coordenadas para los nombres conocidos (corrige datos viejos si cambiaron las reglas).
 */
@Component
public class UbicacionCoordenadasSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(UbicacionCoordenadasSeeder.class);

    private final UbicacionRepository ubicacionRepository;

    public UbicacionCoordenadasSeeder(UbicacionRepository ubicacionRepository) {
        this.ubicacionRepository = ubicacionRepository;
    }

    /**
     * Coordenadas aproximadas (WGS84) — carpooling entre estaciones y campus Robledo del Poli.
     * Ajusta en BD si necesitas más precisión en un punto.
     */
    private static final Map<String, double[]> POR_NOMBRE = crearMapa();

    private static Map<String, double[]> crearMapa() {
        Map<String, double[]> m = new LinkedHashMap<>();
        // Politécnico Colombiano Jaime Isaza Cadavid (Robledo) — salidas del campus
        m.put("Salida Principal", new double[]{6.1960, -75.5863});
        m.put("Salida Parqueadero", new double[]{6.1945, -75.5872});
        // Línea A (norte → sur)
        m.put("Estación Niquía", new double[]{6.33764, -75.37808});
        m.put("Estación Bello", new double[]{6.32583, -75.55778});
        m.put("Estación Madera", new double[]{6.31361, -75.55750});
        m.put("Estación Acevedo", new double[]{6.30194, -75.55917});
        m.put("Estación Tricentenario", new double[]{6.29750, -75.55472});
        m.put("Estación Caribe", new double[]{6.28972, -75.55972});
        m.put("Estación Universidad", new double[]{6.26972, -75.56833});
        m.put("Estación Hospital", new double[]{6.26472, -75.56611});
        m.put("Estación Prado", new double[]{6.25778, -75.56444});
        m.put("Estación Parque Berrío", new double[]{6.25194, -75.56583});
        m.put("Estación San Antonio", new double[]{6.25306, -75.56528});
        m.put("Estación Alpujarra", new double[]{6.24667, -75.57222});
        m.put("Estación Exposiciones", new double[]{6.24056, -75.57583});
        m.put("Estación Industriales", new double[]{6.22972, -75.57528});
        m.put("Estación Poblado", new double[]{6.20806, -75.56694});
        m.put("Estación Aguacatala", new double[]{6.19472, -75.58028});
        m.put("Estación Ayurá", new double[]{6.18500, -75.59639});
        m.put("Estación Envigado", new double[]{6.16917, -75.59111});
        m.put("Estación Itagüí", new double[]{6.17167, -75.61028});
        m.put("Estación Sabaneta", new double[]{6.15056, -75.61639});
        m.put("Estación La Estrella", new double[]{6.15639, -75.64278});
        // Metrocable / líneas conexas
        m.put("Estación Cisneros", new double[]{6.28470, -75.55140});
        // Línea B
        m.put("Estación Suramericana", new double[]{6.24472, -75.59417});
        m.put("Estación Estadio", new double[]{6.25611, -75.59139});
        m.put("Estación Floresta", new double[]{6.26306, -75.59861});
        m.put("Estación Santa Lucía", new double[]{6.27000, -75.60389});
        m.put("Estación San Javier", new double[]{6.25583, -75.62222});
        return Map.copyOf(m);
    }

    private static double[] buscarCoordenadas(String nombreRaw) {
        if (nombreRaw == null) {
            return null;
        }
        String nombre = nombreRaw.trim();
        double[] c = POR_NOMBRE.get(nombre);
        if (c != null) {
            return c;
        }
        for (Map.Entry<String, double[]> e : POR_NOMBRE.entrySet()) {
            if (e.getKey().equalsIgnoreCase(nombre)) {
                return e.getValue();
            }
        }
        return null;
    }

    @Override
    public void run(ApplicationArguments args) {
        List<Ubicacion> todas = ubicacionRepository.findAll();
        int actualizadas = 0;
        int sinCoincidencia = 0;
        int yaOk = 0;
        for (Ubicacion u : todas) {
            if (u.getNombreUbicacion() == null || u.getNombreUbicacion().isBlank()) {
                continue;
            }
            double[] c = buscarCoordenadas(u.getNombreUbicacion());
            if (c == null) {
                sinCoincidencia++;
                log.warn("Sin coordenadas en mapa para nombre: '{}'", u.getNombreUbicacion());
                continue;
            }
            if (u.getLatitud() != null && u.getLongitud() != null
                    && Math.abs(u.getLatitud() - c[0]) < 1e-7
                    && Math.abs(u.getLongitud() - c[1]) < 1e-7) {
                yaOk++;
                continue;
            }
            u.setLatitud(c[0]);
            u.setLongitud(c[1]);
            ubicacionRepository.save(u);
            actualizadas++;
            log.info("Ubicación '{}' → {}, {}", u.getNombreUbicacion(), c[0], c[1]);
        }
        if (actualizadas > 0) {
            log.info("UbicacionCoordenadasSeeder: {} filas actualizadas.", actualizadas);
        }
        if (yaOk > 0) {
            log.info("UbicacionCoordenadasSeeder: {} filas ya coincidían con el mapa.", yaOk);
        }
        if (sinCoincidencia > 0) {
            log.warn("UbicacionCoordenadasSeeder: {} filas sin coincidencia de nombre (revisa el mapa en UbicacionCoordenadasSeeder).", sinCoincidencia);
        }
        if (actualizadas == 0 && sinCoincidencia == 0 && yaOk == 0) {
            log.info("UbicacionCoordenadasSeeder: no hay filas en ubicacion.");
        }
    }
}
