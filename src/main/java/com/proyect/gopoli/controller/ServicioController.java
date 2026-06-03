package com.proyect.gopoli.controller;

import com.proyect.gopoli.model.GoPoliConstants;
import com.proyect.gopoli.model.Servicio;
import com.proyect.gopoli.model.ServicioUsuario;
import com.proyect.gopoli.model.ServicioUsuarioId;
import com.proyect.gopoli.model.Usuario;
import com.proyect.gopoli.repository.ServicioRepository;
import com.proyect.gopoli.repository.ServicioUsuarioRepository;
import com.proyect.gopoli.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class ServicioController {

    @Autowired
    ServicioRepository servicioRepo;
    @Autowired
    ServicioUsuarioRepository servicioUsuarioRepo;
    @Autowired
    UsuarioRepository usuarioRepo;

    @PostMapping("/servicio/crear")
    public ResponseEntity<?> crearServicio(@RequestBody Servicio servicio) {
        try {
            if (servicio.getIdLugarSalida() == null) {
                return ResponseEntity.status(400).body("El lugar de salida es obligatorio");
            }
            if (servicio.getIdLugarLlegada() == null) {
                return ResponseEntity.status(400).body("El lugar de llegada es obligatorio");
            }
            if (servicio.getFecha() == null) {
                return ResponseEntity.status(400).body("La fecha es obligatoria");
            }
            if (servicio.getFecha().isBefore(java.time.LocalDate.now())) {
                return ResponseEntity.status(400).body("La fecha no puede ser en el pasado");
            }
            if (servicio.getHoraSalida() == null) {
                return ResponseEntity.status(400).body("La hora de salida es obligatoria");
            }
            if (servicio.getFecha().isEqual(java.time.LocalDate.now())
                    && servicio.getHoraSalida().isBefore(java.time.LocalTime.now())) {
                return ResponseEntity.status(400).body("La hora no puede ser en el pasado");
            }
            if (servicio.getCapacidad() == null || servicio.getCapacidad() < 2 || servicio.getCapacidad() > 4) {
                return ResponseEntity.status(400).body("La capacidad debe ser entre 2 y 4 personas");
            }

            Integer idTipoServicio = servicio.getIdTipoServicio();
            if (idTipoServicio == null) {
                idTipoServicio = GoPoliConstants.TIPO_SERVICIO_PASAJERO_GRUPO;
                servicio.setIdTipoServicio(idTipoServicio);
            }

            if (!idTipoServicio.equals(GoPoliConstants.TIPO_SERVICIO_PASAJERO_GRUPO)
                    && !idTipoServicio.equals(GoPoliConstants.TIPO_SERVICIO_CONDUCTOR_GRUPO)) {
                return ResponseEntity.status(400).body("Tipo de viaje no válido");
            }

            Optional<Usuario> creadorOpt = usuarioRepo.findById(servicio.getIdCreador());
            if (creadorOpt.isEmpty()) {
                return ResponseEntity.status(404).body("Usuario creador no encontrado");
            }
            Usuario creador = creadorOpt.get();

            if (GoPoliConstants.esViajeConductor(idTipoServicio)
                    && !GoPoliConstants.esConductor(creador.getIdTipoUsuario())) {
                return ResponseEntity.status(403).body("Solo un conductor puede crear viajes tipo grupo conductor");
            }

            List<Servicio> serviciosActivos = servicioRepo
                    .findByIdCreadorAndIdEstadoServicio(servicio.getIdCreador(), GoPoliConstants.ESTADO_SERVICIO_ACTIVO);

            if (!serviciosActivos.isEmpty()) {
                return ResponseEntity.status(400).body("Ya tienes un servicio activo, no puedes crear otro");
            }

            servicio.setIdEstadoServicio(GoPoliConstants.ESTADO_SERVICIO_ACTIVO);
            Servicio guardado = servicioRepo.save(servicio);

            ServicioUsuario miembro = new ServicioUsuario();
            miembro.setIdServicio(guardado.getIdServicio());
            miembro.setIdUsuario(guardado.getIdCreador());
            miembro.setRol(GoPoliConstants.ROL_GRUPO_CREADOR);
            if (GoPoliConstants.esViajeConductor(idTipoServicio)) {
                miembro.setRolParticipacion(GoPoliConstants.ROL_PARTICIPACION_DRIVER);
            } else {
                miembro.setRolParticipacion(GoPoliConstants.ROL_PARTICIPACION_PASSENGER);
            }
            servicioUsuarioRepo.save(miembro);

            return ResponseEntity.ok(enriquecerServicio(guardado));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al crear el servicio: " + e.getMessage());
        }
    }

    @PutMapping("/servicio/cancelar/{idServicio}")
    public ResponseEntity<?> cancelarServicio(@PathVariable Integer idServicio) {
        try {
            return servicioRepo.findById(idServicio).map(servicio -> {
                servicio.setIdEstadoServicio(GoPoliConstants.ESTADO_SERVICIO_CANCELADO);
                servicioRepo.save(servicio);
                return ResponseEntity.ok("Servicio cancelado");
            }).orElse(ResponseEntity.status(404).body("Servicio no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al cancelar: " + e.getMessage());
        }
    }

    @PutMapping("/servicio/finalizar/{idServicio}")
    public ResponseEntity<?> finalizarViaje(@PathVariable Integer idServicio) {
        try {
            return servicioRepo.findById(idServicio).map(servicio -> {
                servicio.setIdEstadoServicio(GoPoliConstants.ESTADO_SERVICIO_FINALIZADO);
                servicioRepo.save(servicio);
                return ResponseEntity.ok("Viaje finalizado");
            }).orElse(ResponseEntity.status(404).body("Servicio no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al finalizar: " + e.getMessage());
        }
    }

    @PutMapping("/servicio/iniciar/{idServicio}")
    public ResponseEntity<?> iniciarViaje(@PathVariable Integer idServicio) {
        try {
            return servicioRepo.findById(idServicio).map(servicio -> {
                servicio.setIdEstadoServicio(GoPoliConstants.ESTADO_SERVICIO_EN_CURSO);
                servicioRepo.save(servicio);
                return ResponseEntity.ok("Viaje iniciado");
            }).orElse(ResponseEntity.status(404).body("Servicio no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al iniciar: " + e.getMessage());
        }
    }

    @GetMapping("/servicio/{idServicio}/miembros")
    public ResponseEntity<?> getMiembros(@PathVariable Integer idServicio) {
        try {
            List<ServicioUsuario> miembros = servicioUsuarioRepo.findByIdServicio(idServicio);

            List<Map<String, Object>> resultado = miembros.stream().map(m -> {
                Map<String, Object> item = new HashMap<>();
                item.put("idUsuario", m.getIdUsuario());
                item.put("rol", m.getRol());
                item.put("rolParticipacion", m.getRolParticipacion());
                item.put("rolParticipacionLabel", etiquetaRolParticipacion(m.getRolParticipacion()));
                usuarioRepo.findById(m.getIdUsuario()).ifPresent(u -> {
                    item.put("nombreUsuario", u.getNombre());
                });
                return item;
            }).toList();

            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al traer miembros: " + e.getMessage());
        }
    }

    @GetMapping("/servicios/activos")
    public ResponseEntity<?> getServiciosActivos() {
        try {
            List<Servicio> activos = servicioRepo.findByIdEstadoServicio(GoPoliConstants.ESTADO_SERVICIO_ACTIVO);
            List<Map<String, Object>> resultado = activos.stream().map(this::enriquecerServicio).toList();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/servicio/unirse")
    public ResponseEntity<?> unirse(@RequestBody Map<String, Integer> body) {
        try {
            Integer idServicio = body.get("idServicio");
            Integer idUsuario = body.get("idUsuario");

            Servicio servicio = servicioRepo.findById(idServicio).orElse(null);
            if (servicio == null) {
                return ResponseEntity.status(404).body("Servicio no encontrado");
            }
            if (servicio.getIdEstadoServicio() != GoPoliConstants.ESTADO_SERVICIO_ACTIVO) {
                return ResponseEntity.status(400).body("El servicio no está activo");
            }

            if (servicio.getIdCreador().equals(idUsuario)) {
                return ResponseEntity.status(400).body("Ya eres el creador de este grupo");
            }

            List<ServicioUsuario> gruposUsuario = servicioUsuarioRepo.findByIdUsuario(idUsuario);
            for (ServicioUsuario su : gruposUsuario) {
                Servicio s = servicioRepo.findById(su.getIdServicio()).orElse(null);
                if (s != null && (s.getIdEstadoServicio() == GoPoliConstants.ESTADO_SERVICIO_ACTIVO
                        || s.getIdEstadoServicio() == GoPoliConstants.ESTADO_SERVICIO_EN_CURSO)) {
                    return ResponseEntity.status(400).body("Ya perteneces a un grupo activo o en curso");
                }
            }

            List<ServicioUsuario> miembros = servicioUsuarioRepo.findByIdServicio(idServicio);
            boolean yaEsMiembro = miembros.stream()
                    .anyMatch(m -> m.getIdUsuario().equals(idUsuario));
            if (yaEsMiembro) {
                return ResponseEntity.status(400).body("Ya eres miembro de este grupo");
            }

            if (miembros.size() >= servicio.getCapacidad()) {
                return ResponseEntity.status(400).body("El grupo está lleno");
            }

            ServicioUsuario nuevoMiembro = new ServicioUsuario();
            nuevoMiembro.setIdServicio(idServicio);
            nuevoMiembro.setIdUsuario(idUsuario);
            nuevoMiembro.setRol(GoPoliConstants.ROL_GRUPO_MIEMBRO);
            nuevoMiembro.setRolParticipacion(GoPoliConstants.ROL_PARTICIPACION_PASSENGER);
            servicioUsuarioRepo.save(nuevoMiembro);

            return ResponseEntity.ok("Te uniste al grupo exitosamente");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al unirse: " + e.getMessage());
        }
    }

    @DeleteMapping("/servicio/salir/{idServicio}/{idUsuario}")
    public ResponseEntity<?> salirGrupo(@PathVariable Integer idServicio, @PathVariable Integer idUsuario) {
        try {
            Servicio servicio = servicioRepo.findById(idServicio).orElse(null);
            if (servicio == null) {
                return ResponseEntity.status(404).body("Servicio no encontrado");
            }
            if (servicio.getIdEstadoServicio() != GoPoliConstants.ESTADO_SERVICIO_ACTIVO) {
                return ResponseEntity.status(400).body("Solo puedes salir de grupos en planificación");
            }

            ServicioUsuarioId id = new ServicioUsuarioId(idServicio, idUsuario);
            if (!servicioUsuarioRepo.existsById(id)) {
                return ResponseEntity.status(404).body("No eres miembro de este grupo");
            }
            servicioUsuarioRepo.deleteById(id);
            return ResponseEntity.ok("Saliste del grupo");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al salir: " + e.getMessage());
        }
    }

    @GetMapping("/servicio/usuario/activo/{idUsuario}")
    public ResponseEntity<?> getServicioActivoUsuario(@PathVariable Integer idUsuario) {
        try {
            List<Servicio> activos = servicioRepo.findByIdCreadorAndIdEstadoServicio(
                    idUsuario, GoPoliConstants.ESTADO_SERVICIO_ACTIVO);
            if (activos.isEmpty()) {
                return ResponseEntity.status(404).body("Sin servicio activo");
            }
            return ResponseEntity.ok(enriquecerServicio(activos.get(0)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/servicio/usuario/miembro/{idUsuario}")
    public ResponseEntity<?> getServicioComoMiembro(@PathVariable Integer idUsuario) {
        try {
            List<ServicioUsuario> grupos = servicioUsuarioRepo.findByIdUsuario(idUsuario);
            for (ServicioUsuario su : grupos) {
                Servicio servicio = servicioRepo.findById(su.getIdServicio()).orElse(null);
                if (servicio != null && servicio.getIdEstadoServicio() == GoPoliConstants.ESTADO_SERVICIO_ACTIVO) {
                    return ResponseEntity.ok(enriquecerServicio(servicio));
                }
            }
            return ResponseEntity.status(404).body("Sin grupo activo");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/servicio/usuario/encurso/{idUsuario}")
    public ResponseEntity<?> getServicioEnCurso(@PathVariable Integer idUsuario) {
        try {
            List<Servicio> encurso = servicioRepo.findByIdCreadorAndIdEstadoServicio(
                    idUsuario, GoPoliConstants.ESTADO_SERVICIO_EN_CURSO);
            if (!encurso.isEmpty()) {
                return ResponseEntity.ok(enriquecerServicio(encurso.get(0)));
            }

            List<ServicioUsuario> grupos = servicioUsuarioRepo.findByIdUsuario(idUsuario);
            for (ServicioUsuario su : grupos) {
                Servicio servicio = servicioRepo.findById(su.getIdServicio()).orElse(null);
                if (servicio != null && servicio.getIdEstadoServicio() == GoPoliConstants.ESTADO_SERVICIO_EN_CURSO) {
                    return ResponseEntity.ok(enriquecerServicio(servicio));
                }
            }
            return ResponseEntity.status(404).body("Sin viaje en curso");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/servicio/{idServicio}")
    public ResponseEntity<?> getServicio(@PathVariable Integer idServicio) {
        try {
            return servicioRepo.findById(idServicio)
                    .map(s -> ResponseEntity.ok(enriquecerServicio(s)))
                    .orElse(ResponseEntity.status(404).build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }

    private Map<String, Object> enriquecerServicio(Servicio s) {
        Map<String, Object> map = new HashMap<>();
        map.put("idServicio", s.getIdServicio());
        map.put("fecha", s.getFecha());
        map.put("descripcion", s.getDescripcion());
        map.put("idLugarSalida", s.getIdLugarSalida());
        map.put("idLugarLlegada", s.getIdLugarLlegada());
        map.put("horaSalida", s.getHoraSalida());
        map.put("idCreador", s.getIdCreador());
        map.put("idTipoServicio", s.getIdTipoServicio());
        map.put("idEstadoServicio", s.getIdEstadoServicio());
        map.put("capacidad", s.getCapacidad());
        map.put("tripType", tripTypeKey(s.getIdTipoServicio()));
        map.put("tripTypeLabel", tripTypeLabel(s.getIdTipoServicio()));
        return map;
    }

    private static String tripTypeKey(Integer idTipoServicio) {
        if (GoPoliConstants.esViajeConductor(idTipoServicio)) {
            return "driver_group";
        }
        return "passenger_group";
    }

    private static String tripTypeLabel(Integer idTipoServicio) {
        if (GoPoliConstants.esViajeConductor(idTipoServicio)) {
            return "Grupo conductor";
        }
        return "Grupo de viaje";
    }

    private static String etiquetaRolParticipacion(String rol) {
        if (GoPoliConstants.ROL_PARTICIPACION_DRIVER.equals(rol)) {
            return "Conductor";
        }
        return "Pasajero";
    }
}
