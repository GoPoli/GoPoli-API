package com.proyect.gopoli.controller;

import com.proyect.gopoli.model.Servicio;
import com.proyect.gopoli.repository.ServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class ServicioController {

    @Autowired
    ServicioRepository servicioRepo;

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

            List<Servicio> serviciosActivos = servicioRepo
                .findByIdCreadorAndIdEstadoServicio(servicio.getIdCreador(), 1);
            
            if (!serviciosActivos.isEmpty()) {
                return ResponseEntity.status(400).body("Ya tienes un servicio activo, no puedes crear otro");
            }

            servicio.setIdEstadoServicio(1);
            Servicio guardado = servicioRepo.save(servicio);
            return ResponseEntity.ok(guardado);

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al crear el servicio: " + e.getMessage());
        }
    }
}