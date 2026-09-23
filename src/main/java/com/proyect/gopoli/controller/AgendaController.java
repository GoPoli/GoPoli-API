package com.proyect.gopoli.controller;

import com.proyect.gopoli.model.GoPoliConstants;
import com.proyect.gopoli.model.RutaHabitual;
import com.proyect.gopoli.model.Ubicacion;
import com.proyect.gopoli.repository.RutaHabitualRepository;
import com.proyect.gopoli.repository.UbicacionRepository;
import com.proyect.gopoli.repository.UsuarioRepository;
import com.proyect.gopoli.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "*")
public class AgendaController {

    @Autowired
    RutaHabitualRepository rutaRepo;
    @Autowired
    UbicacionRepository ubicacionRepo;
    @Autowired
    UsuarioRepository usuarioRepo;
    @Autowired
    JwtService jwtService;

    private ResponseEntity<String> exigirDueno(String auth, Integer idUsuario) {
        Integer actor = jwtService.parseUserId(auth);
        if (actor == null) {
            return ResponseEntity.status(401).body("Token inválido o ausente");
        }
        if (idUsuario != null && !actor.equals(idUsuario)) {
            return ResponseEntity.status(403).body("No puedes consultar la agenda de otra persona");
        }
        return null;
    }

    @GetMapping("/agenda/rutas/usuario/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idUsuario) {
        ResponseEntity<String> acceso = exigirDueno(auth, idUsuario);
        if (acceso != null) {
            return acceso;
        }
        try {
            if (usuarioRepo.findById(idUsuario).isEmpty()) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }
            List<Map<String, Object>> lista = rutaRepo.findByIdUsuarioOrderByIdRutaAsc(idUsuario)
                    .stream()
                    .map(this::enriquecer)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(lista);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al listar rutas");
        }
    }

    @PostMapping("/agenda/rutas")
    public ResponseEntity<?> crear(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody RutaHabitual ruta) {
        try {
            Integer actor = jwtService.parseUserId(auth);
            if (actor == null) {
                return ResponseEntity.status(401).body("Token inválido o ausente");
            }
            ruta.setIdUsuario(actor);
            String error = validar(ruta, true);
            if (error != null) {
                return ResponseEntity.status(400).body(error);
            }
            if (usuarioRepo.findById(ruta.getIdUsuario()).isEmpty()) {
                return ResponseEntity.status(404).body("Usuario no encontrado");
            }
            if (ruta.getIdTipoServicio() == null) {
                ruta.setIdTipoServicio(GoPoliConstants.TIPO_SERVICIO_PASAJERO_GRUPO);
            }
            ruta.setIdRuta(null);
            RutaHabitual guardada = rutaRepo.save(ruta);
            return ResponseEntity.ok(enriquecer(guardada));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al guardar la ruta");
        }
    }

    @PutMapping("/agenda/rutas/{idRuta}")
    public ResponseEntity<?> actualizar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idRuta,
            @RequestBody RutaHabitual body) {
        try {
            Integer actor = jwtService.parseUserId(auth);
            if (actor == null) {
                return ResponseEntity.status(401).body("Token inválido o ausente");
            }
            Optional<RutaHabitual> opt = rutaRepo.findById(idRuta);
            if (opt.isEmpty()) {
                return ResponseEntity.status(404).body("Ruta no encontrada");
            }
            RutaHabitual existente = opt.get();
            if (!actor.equals(existente.getIdUsuario())) {
                return ResponseEntity.status(403).body("No puedes editar esta ruta");
            }

            body.setIdRuta(idRuta);
            body.setIdUsuario(existente.getIdUsuario());
            String error = validar(body, false);
            if (error != null) {
                return ResponseEntity.status(400).body(error);
            }
            if (body.getIdTipoServicio() == null) {
                body.setIdTipoServicio(
                        existente.getIdTipoServicio() != null
                                ? existente.getIdTipoServicio()
                                : GoPoliConstants.TIPO_SERVICIO_PASAJERO_GRUPO);
            }

            existente.setIdLugarSalida(body.getIdLugarSalida());
            existente.setIdLugarLlegada(body.getIdLugarLlegada());
            existente.setDiasSemana(normalizarDias(body.getDiasSemana()));
            existente.setHoraSalida(body.getHoraSalida());
            existente.setCapacidad(body.getCapacidad());
            existente.setIdTipoServicio(body.getIdTipoServicio());
            existente.setDescripcion(body.getDescripcion());

            RutaHabitual guardada = rutaRepo.save(existente);
            return ResponseEntity.ok(enriquecer(guardada));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al actualizar la ruta");
        }
    }

    @DeleteMapping("/agenda/rutas/{idRuta}")
    public ResponseEntity<?> eliminar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idRuta,
            @RequestParam(required = false) Integer idUsuario) {
        try {
            Integer actor = jwtService.parseUserId(auth);
            if (actor == null) {
                return ResponseEntity.status(401).body("Token inválido o ausente");
            }
            Optional<RutaHabitual> opt = rutaRepo.findById(idRuta);
            if (opt.isEmpty()) {
                return ResponseEntity.status(404).body("Ruta no encontrada");
            }
            RutaHabitual ruta = opt.get();
            if (!actor.equals(ruta.getIdUsuario())
                    || (idUsuario != null && !idUsuario.equals(ruta.getIdUsuario()))) {
                return ResponseEntity.status(403).body("No puedes eliminar esta ruta");
            }
            rutaRepo.delete(ruta);
            return ResponseEntity.ok("Ruta eliminada");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al eliminar");
        }
    }

    private String validar(RutaHabitual ruta, boolean crear) {
        if (crear && ruta.getIdUsuario() == null) {
            return "El usuario es obligatorio";
        }
        if (ruta.getIdLugarSalida() == null) {
            return "El lugar de salida es obligatorio";
        }
        if (ruta.getIdLugarLlegada() == null) {
            return "El lugar de llegada es obligatorio";
        }
        if (ruta.getIdLugarSalida().equals(ruta.getIdLugarLlegada())) {
            return "Salida y llegada deben ser distintas";
        }
        if (ubicacionRepo.findById(ruta.getIdLugarSalida()).isEmpty()) {
            return "Lugar de salida no encontrado";
        }
        if (ubicacionRepo.findById(ruta.getIdLugarLlegada()).isEmpty()) {
            return "Lugar de llegada no encontrado";
        }
        if (ruta.getHoraSalida() == null) {
            return "La hora de salida es obligatoria";
        }
        if (ruta.getCapacidad() == null || ruta.getCapacidad() < 2 || ruta.getCapacidad() > 4) {
            return "La capacidad debe ser entre 2 y 4 personas";
        }
        String dias = normalizarDias(ruta.getDiasSemana());
        if (dias == null || dias.isBlank()) {
            return "Selecciona al menos un día de la semana";
        }
        ruta.setDiasSemana(dias);

        Integer idTipo = ruta.getIdTipoServicio();
        if (idTipo != null
                && !idTipo.equals(GoPoliConstants.TIPO_SERVICIO_PASAJERO_GRUPO)
                && !idTipo.equals(GoPoliConstants.TIPO_SERVICIO_CONDUCTOR_GRUPO)) {
            return "Tipo de viaje no válido";
        }
        return null;
    }

    /** Normaliza "1, 3, 5" → "1,3,5" ordenado, solo 1–7. */
    private static String normalizarDias(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> {
                    try {
                        return Integer.parseInt(s);
                    } catch (NumberFormatException e) {
                        return -1;
                    }
                })
                .filter(n -> n >= 1 && n <= 7)
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }

    private Map<String, Object> enriquecer(RutaHabitual r) {
        Map<String, Object> map = new HashMap<>();
        map.put("idRuta", r.getIdRuta());
        map.put("idUsuario", r.getIdUsuario());
        map.put("idLugarSalida", r.getIdLugarSalida());
        map.put("idLugarLlegada", r.getIdLugarLlegada());
        map.put("diasSemana", r.getDiasSemana());
        map.put("horaSalida", r.getHoraSalida());
        map.put("capacidad", r.getCapacidad());
        map.put("idTipoServicio", r.getIdTipoServicio());
        map.put("descripcion", r.getDescripcion());

        Optional<Ubicacion> salida = ubicacionRepo.findById(r.getIdLugarSalida());
        Optional<Ubicacion> llegada = ubicacionRepo.findById(r.getIdLugarLlegada());
        map.put("nombreSalida", salida.map(Ubicacion::getNombreUbicacion).orElse(null));
        map.put("nombreLlegada", llegada.map(Ubicacion::getNombreUbicacion).orElse(null));
        return map;
    }
}
