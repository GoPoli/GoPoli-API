package com.proyect.gopoli.controller;

import com.proyect.gopoli.model.Mensaje;
import com.proyect.gopoli.model.ServicioUsuarioId;
import com.proyect.gopoli.model.Usuario;
import com.proyect.gopoli.repository.MensajeRepository;
import com.proyect.gopoli.repository.ServicioUsuarioRepository;
import com.proyect.gopoli.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
public class MensajeController {

    private static final int TEXTO_MAX = 1000;

    @Autowired
    MensajeRepository mensajeRepo;
    @Autowired
    ServicioUsuarioRepository servicioUsuarioRepo;
    @Autowired
    UsuarioRepository usuarioRepo;

    @GetMapping("/servicio/{idServicio}/mensajes")
    public ResponseEntity<?> listarMensajes(
            @PathVariable Integer idServicio,
            @RequestParam Integer idUsuario) {
        try {
            if (!esMiembro(idServicio, idUsuario)) {
                return ResponseEntity.status(403).body("Solo los miembros del grupo pueden ver los mensajes");
            }
            List<Mensaje> mensajes = mensajeRepo.findByIdServicioOrderByFechaEnvioAsc(idServicio);
            List<Map<String, Object>> out = new ArrayList<>();
            for (Mensaje m : mensajes) {
                out.add(enriquecer(m));
            }
            return ResponseEntity.ok(out);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al listar mensajes: " + e.getMessage());
        }
    }

    @PostMapping("/servicio/{idServicio}/mensajes")
    public ResponseEntity<?> enviarMensaje(
            @PathVariable Integer idServicio,
            @RequestBody Map<String, Object> body) {
        try {
            Integer idUsuario = asInteger(body.get("idUsuario"));
            if (idUsuario == null) {
                return ResponseEntity.status(400).body("idUsuario es obligatorio");
            }
            if (!esMiembro(idServicio, idUsuario)) {
                return ResponseEntity.status(403).body("Solo los miembros del grupo pueden enviar mensajes");
            }
            Object textoRaw = body.get("texto");
            if (textoRaw == null) {
                return ResponseEntity.status(400).body("El texto es obligatorio");
            }
            String texto = String.valueOf(textoRaw).trim();
            if (texto.isEmpty()) {
                return ResponseEntity.status(400).body("El texto no puede estar vacío");
            }
            if (texto.length() > TEXTO_MAX) {
                return ResponseEntity.status(400).body("El texto no puede superar " + TEXTO_MAX + " caracteres");
            }

            Mensaje mensaje = new Mensaje();
            mensaje.setIdServicio(idServicio);
            mensaje.setIdUsuario(idUsuario);
            mensaje.setTexto(texto);
            mensaje.setFechaEnvio(LocalDateTime.now());
            Mensaje guardado = mensajeRepo.save(mensaje);
            return ResponseEntity.ok(enriquecer(guardado));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al enviar mensaje: " + e.getMessage());
        }
    }

    private boolean esMiembro(Integer idServicio, Integer idUsuario) {
        return servicioUsuarioRepo
                .findById(new ServicioUsuarioId(idServicio, idUsuario))
                .isPresent();
    }

    private Map<String, Object> enriquecer(Mensaje m) {
        Map<String, Object> map = new HashMap<>();
        map.put("idMensaje", m.getIdMensaje());
        map.put("idServicio", m.getIdServicio());
        map.put("idUsuario", m.getIdUsuario());
        map.put("texto", m.getTexto());
        map.put("fechaEnvio", m.getFechaEnvio());
        String nombre = usuarioRepo.findById(m.getIdUsuario())
                .map(Usuario::getNombre)
                .orElse(null);
        map.put("nombreUsuario", nombre);
        return map;
    }

    private static Integer asInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
