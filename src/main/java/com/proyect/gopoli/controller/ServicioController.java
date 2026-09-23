package com.proyect.gopoli.controller;

import com.proyect.gopoli.model.GoPoliConstants;
import com.proyect.gopoli.model.Servicio;
import com.proyect.gopoli.model.ServicioUsuario;
import com.proyect.gopoli.model.ServicioUsuarioId;
import com.proyect.gopoli.model.Usuario;
import com.proyect.gopoli.repository.ServicioRepository;
import com.proyect.gopoli.repository.ServicioUsuarioRepository;
import com.proyect.gopoli.repository.UbicacionRepository;
import com.proyect.gopoli.repository.UsuarioRepository;
import com.proyect.gopoli.security.JwtService;
import com.proyect.gopoli.util.ServicioPolicy;
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
    @Autowired
    JwtService jwtService;
    @Autowired
    UbicacionRepository ubicacionRepo;

    private Integer actorId(String authorization) {
        return jwtService.parseUserId(authorization);
    }

    private ResponseEntity<String> sinSesion() {
        return ResponseEntity.status(401).body("Token inválido o ausente");
    }

    @PostMapping("/servicio/crear")
    public ResponseEntity<?> crearServicio(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Servicio servicio) {
        try {
            Integer actor = actorId(auth);
            if (actor == null) {
                return sinSesion();
            }
            servicio.setIdCreador(actor);
            String errorCreacion = ServicioPolicy.errorCreacion(
                    servicio.getIdLugarSalida(),
                    servicio.getIdLugarLlegada(),
                    servicio.getCapacidad());
            if (errorCreacion != null) {
                return ResponseEntity.status(400).body(errorCreacion);
            }
            if (!ubicacionRepo.existsById(servicio.getIdLugarSalida())
                    || !ubicacionRepo.existsById(servicio.getIdLugarLlegada())) {
                return ResponseEntity.status(400).body("La salida o el destino no existe");
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
            return ResponseEntity.status(500).body("Error al crear el servicio");
        }
    }

    @PutMapping("/servicio/cancelar/{idServicio}")
    public ResponseEntity<?> cancelarServicio(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idServicio) {
        return cambiarEstado(auth, idServicio, GoPoliConstants.ESTADO_SERVICIO_CANCELADO, "Servicio cancelado", "Error al cancelar");
    }

    @PutMapping("/servicio/finalizar/{idServicio}")
    public ResponseEntity<?> finalizarViaje(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idServicio) {
        return cambiarEstado(auth, idServicio, GoPoliConstants.ESTADO_SERVICIO_FINALIZADO, "Viaje finalizado", "Error al finalizar");
    }

    @PutMapping("/servicio/iniciar/{idServicio}")
    public ResponseEntity<?> iniciarViaje(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idServicio) {
        return cambiarEstado(auth, idServicio, GoPoliConstants.ESTADO_SERVICIO_EN_CURSO, "Viaje iniciado", "Error al iniciar");
    }

    private ResponseEntity<?> cambiarEstado(
            String auth, Integer idServicio, int estado, String ok, String error) {
        try {
            Integer actor = actorId(auth);
            if (actor == null) {
                return sinSesion();
            }
            return servicioRepo.findById(idServicio).map(servicio -> {
                if (!actor.equals(servicio.getIdCreador())) {
                    return ResponseEntity.status(403).body("Solo el creador puede cambiar el estado del viaje");
                }
                String errorTransicion = ServicioPolicy.errorTransicion(
                        servicio.getIdEstadoServicio(), estado);
                if (errorTransicion != null) {
                    return ResponseEntity.status(409).body(errorTransicion);
                }
                servicio.setIdEstadoServicio(estado);
                servicioRepo.save(servicio);
                return ResponseEntity.ok(ok);
            }).orElse(ResponseEntity.status(404).body("Servicio no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/servicio/{idServicio}/miembros")
    public ResponseEntity<?> getMiembros(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idServicio) {
        try {
            Integer actor = actorId(auth);
            if (actor == null) {
                return sinSesion();
            }
            if (!servicioUsuarioRepo.existsById(new ServicioUsuarioId(idServicio, actor))) {
                return ResponseEntity.status(403).body("Solo los miembros pueden consultar el grupo");
            }
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
            return ResponseEntity.status(500).body("Error al traer miembros");
        }
    }

    @GetMapping("/servicios/activos")
    public ResponseEntity<?> getServiciosActivos(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            if (actorId(auth) == null) {
                return sinSesion();
            }
            List<Servicio> activos = servicioRepo.findByIdEstadoServicio(GoPoliConstants.ESTADO_SERVICIO_ACTIVO);
            List<Map<String, Object>> resultado = activos.stream().map(this::enriquecerServicio).toList();
            return ResponseEntity.ok(resultado);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al listar servicios");
        }
    }

    @PostMapping("/servicio/unirse")
    public ResponseEntity<?> unirse(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, Integer> body) {
        try {
            Integer actor = actorId(auth);
            if (actor == null) {
                return sinSesion();
            }
            Integer idServicio = body.get("idServicio");
            Integer idUsuario = actor;
            if (idServicio == null) {
                return ResponseEntity.status(400).body("El servicio es obligatorio");
            }

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
            return ResponseEntity.status(500).body("Error al unirse");
        }
    }

    @DeleteMapping("/servicio/salir/{idServicio}/{idUsuario}")
    public ResponseEntity<?> salirGrupo(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idServicio,
            @PathVariable Integer idUsuario) {
        try {
            Integer actor = actorId(auth);
            if (actor == null) {
                return sinSesion();
            }
            if (!actor.equals(idUsuario)) {
                return ResponseEntity.status(403).body("No puedes salir en nombre de otra persona");
            }
            Servicio servicio = servicioRepo.findById(idServicio).orElse(null);
            if (servicio == null) {
                return ResponseEntity.status(404).body("Servicio no encontrado");
            }
            if (servicio.getIdCreador().equals(actor)) {
                return ResponseEntity.status(400).body("El creador no puede salir. Cancela el viaje.");
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
            return ResponseEntity.status(500).body("Error al salir");
        }
    }

    private ResponseEntity<String> exigirDueno(String auth, Integer idUsuario) {
        Integer actor = actorId(auth);
        if (actor == null) {
            return sinSesion();
        }
        if (!actor.equals(idUsuario)) {
            return ResponseEntity.status(403).body("No puedes consultar los viajes de otra persona");
        }
        return null;
    }

    @GetMapping("/servicio/usuario/activo/{idUsuario}")
    public ResponseEntity<?> getServicioActivoUsuario(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idUsuario) {
        ResponseEntity<String> acceso = exigirDueno(auth, idUsuario);
        if (acceso != null) {
            return acceso;
        }
        try {
            List<Servicio> activos = servicioRepo.findByIdCreadorAndIdEstadoServicio(
                    idUsuario, GoPoliConstants.ESTADO_SERVICIO_ACTIVO);
            if (activos.isEmpty()) {
                return ResponseEntity.status(404).body("Sin servicio activo");
            }
            return ResponseEntity.ok(enriquecerServicio(activos.get(0)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al consultar el servicio activo");
        }
    }

    @GetMapping("/servicio/usuario/miembro/{idUsuario}")
    public ResponseEntity<?> getServicioComoMiembro(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idUsuario) {
        ResponseEntity<String> acceso = exigirDueno(auth, idUsuario);
        if (acceso != null) {
            return acceso;
        }
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
            return ResponseEntity.status(500).body("Error al consultar el grupo");
        }
    }

    @GetMapping("/servicio/usuario/encurso/{idUsuario}")
    public ResponseEntity<?> getServicioEnCurso(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idUsuario) {
        ResponseEntity<String> acceso = exigirDueno(auth, idUsuario);
        if (acceso != null) {
            return acceso;
        }
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
            return ResponseEntity.status(500).body("Error al consultar el viaje en curso");
        }
    }

    @GetMapping("/servicio/{idServicio}")
    public ResponseEntity<?> getServicio(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable Integer idServicio) {
        try {
            Integer actor = actorId(auth);
            if (actor == null) {
                return sinSesion();
            }
            if (!servicioUsuarioRepo.existsById(new ServicioUsuarioId(idServicio, actor))) {
                return ResponseEntity.status(403).body("Solo los miembros pueden consultar el viaje");
            }
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
