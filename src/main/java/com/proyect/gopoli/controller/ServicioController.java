package com.proyect.gopoli.controller;

import com.proyect.gopoli.model.Servicio;
import com.proyect.gopoli.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyect.gopoli.model.ServicioUsuario;
import com.proyect.gopoli.model.ServicioUsuarioId;
import com.proyect.gopoli.repository.ServicioUsuarioRepository;
import com.proyect.gopoli.model.Usuario;
import com.proyect.gopoli.repository.UsuarioRepository;
import java.util.List;
import java.util.Map;

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
            if (servicio.getFecha().isEqual(java.time.LocalDate.now()) && servicio.getHoraSalida().isBefore(java.time.LocalTime.now())) {
                return ResponseEntity.status(400).body("La hora no puede ser en el pasado");
            }
            if (servicio.getCapacidad() == null || servicio.getCapacidad() < 2 || servicio.getCapacidad() > 4) {
                return ResponseEntity.status(400).body("La capacidad debe ser entre 2 y 4 personas");
            }

            List<Servicio> serviciosActivos = servicioRepo
                .findByIdCreadorAndIdEstadoServicio(servicio.getIdCreador(), 1);
            
            if (!serviciosActivos.isEmpty()) {
                return ResponseEntity.status(400).body("Ya tienes un servicio activo, no puedes crear otro");
            }

            servicio.setIdEstadoServicio(1);
            Servicio guardado = servicioRepo.save(servicio);

            ServicioUsuario miembro = new ServicioUsuario();
            miembro.setIdServicio(guardado.getIdServicio());
            miembro.setIdUsuario(guardado.getIdCreador());
            miembro.setRol("Creador");
            servicioUsuarioRepo.save(miembro);

            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al crear el servicio: " + e.getMessage());
        }
    }

    @PutMapping("/servicio/cancelar/{idServicio}")
    public ResponseEntity<?> cancelarServicio(@PathVariable Integer idServicio) {
        try {
            return servicioRepo.findById(idServicio).map(servicio -> {
                servicio.setIdEstadoServicio(2);
                servicioRepo.save(servicio);
                List<ServicioUsuario> miembros = servicioUsuarioRepo.findByIdServicio(idServicio);
                servicioUsuarioRepo.deleteAll(miembros);
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
                servicio.setIdEstadoServicio(3);
                servicioRepo.save(servicio);
                List<ServicioUsuario> miembros = servicioUsuarioRepo.findByIdServicio(idServicio);
                servicioUsuarioRepo.deleteAll(miembros);
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
                servicio.setIdEstadoServicio(4);
                servicioRepo.save(servicio);
                return ResponseEntity.ok("Viaje iniciado");
            }).orElse(ResponseEntity.status(404).body("Servicio no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al iniciar: " + e.getMessage());
        }
    }

    // Traer miembros del grupo
    @GetMapping("/servicio/{idServicio}/miembros")
    public ResponseEntity<?> getMiembros(@PathVariable Integer idServicio) {
        try {
            List<ServicioUsuario> miembros = servicioUsuarioRepo.findByIdServicio(idServicio);
            
            List<Map<String, Object>> resultado = miembros.stream().map(m -> {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("idUsuario", m.getIdUsuario());
                item.put("rol", m.getRol());
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
            List<Servicio> activos = servicioRepo.findByIdEstadoServicio(1);
            return ResponseEntity.ok(activos);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/servicio/unirse")
    public ResponseEntity<?> unirse(@RequestBody Map<String, Integer> body) {
        try {
            Integer idServicio = body.get("idServicio");
            Integer idUsuario = body.get("idUsuario");

            // Verificar que el servicio existe y está activo
            Servicio servicio = servicioRepo.findById(idServicio).orElse(null);
            if (servicio == null) {
                return ResponseEntity.status(404).body("Servicio no encontrado");
            }
            if (servicio.getIdEstadoServicio() != 1) {
                return ResponseEntity.status(400).body("El servicio no está activo");
            }

            // Verificar que el usuario no sea el creador
            if (servicio.getIdCreador().equals(idUsuario)) {
                return ResponseEntity.status(400).body("Ya eres el creador de este grupo");
            }

            // Verificar que el usuario no esté en otro grupo activo o en curso
            List<ServicioUsuario> gruposUsuario = servicioUsuarioRepo.findByIdUsuario(idUsuario);
            for (ServicioUsuario su : gruposUsuario) {
                Servicio s = servicioRepo.findById(su.getIdServicio()).orElse(null);
                if (s != null && (s.getIdEstadoServicio() == 1 || s.getIdEstadoServicio() == 4)) {
                    return ResponseEntity.status(400).body("Ya perteneces a un grupo activo o en curso");
                }
            }

            // Verificar que el usuario no esté ya en el grupo
            List<ServicioUsuario> miembros = servicioUsuarioRepo.findByIdServicio(idServicio);
            boolean yaEsMiembro = miembros.stream()
                    .anyMatch(m -> m.getIdUsuario().equals(idUsuario));
            if (yaEsMiembro) {
                return ResponseEntity.status(400).body("Ya eres miembro de este grupo");
            }

            // Verificar capacidad
            if (miembros.size() >= servicio.getCapacidad()) {
                return ResponseEntity.status(400).body("El grupo está lleno");
            }

            // Unirse al grupo
            ServicioUsuario nuevoMiembro = new ServicioUsuario();
            nuevoMiembro.setIdServicio(idServicio);
            nuevoMiembro.setIdUsuario(idUsuario);
            nuevoMiembro.setRol("Miembro");
            servicioUsuarioRepo.save(nuevoMiembro);

            return ResponseEntity.ok("Te uniste al grupo exitosamente");

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al unirse: " + e.getMessage());
        }
    }

    @DeleteMapping("/servicio/salir/{idServicio}/{idUsuario}")
    public ResponseEntity<?> salirGrupo(@PathVariable Integer idServicio, @PathVariable Integer idUsuario) {
        try {
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
            List<Servicio> activos = servicioRepo.findByIdCreadorAndIdEstadoServicio(idUsuario, 1);
            if (activos.isEmpty()) {
                return ResponseEntity.status(404).body("Sin servicio activo");
            }
            return ResponseEntity.ok(activos.get(0));
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
                if (servicio != null && servicio.getIdEstadoServicio() == 1) {
                    return ResponseEntity.ok(servicio);
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
            // Verificar como creador
            List<Servicio> encurso = servicioRepo.findByIdCreadorAndIdEstadoServicio(idUsuario, 4);
            if (!encurso.isEmpty()) {
                return ResponseEntity.ok(encurso.get(0));
            }

            // Verificar como miembro
            List<ServicioUsuario> grupos = servicioUsuarioRepo.findByIdUsuario(idUsuario);
            for (ServicioUsuario su : grupos) {
                Servicio servicio = servicioRepo.findById(su.getIdServicio()).orElse(null);
                if (servicio != null && servicio.getIdEstadoServicio() == 4) {
                    return ResponseEntity.ok(servicio);
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
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(404).build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
}