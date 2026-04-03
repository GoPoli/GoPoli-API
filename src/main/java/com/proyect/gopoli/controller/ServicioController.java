package com.proyect.gopoli.controller;

import com.proyect.gopoli.model.Servicio;
import com.proyect.gopoli.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.proyect.gopoli.model.ServicioUsuario;
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
                servicio.setIdEstadoServicio(3);
                servicioRepo.save(servicio);
                return ResponseEntity.ok("Servicio cancelado");
            }).orElse(ResponseEntity.status(404).body("Servicio no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al cancelar: " + e.getMessage());
        }
    }

    @PutMapping("/servicio/iniciar/{idServicio}")
    public ResponseEntity<?> iniciarViaje(@PathVariable Integer idServicio) {
        try {
            return servicioRepo.findById(idServicio).map(servicio -> {
                servicio.setIdEstadoServicio(2);
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
}