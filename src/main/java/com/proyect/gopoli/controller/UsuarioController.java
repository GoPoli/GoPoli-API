package com.proyect.gopoli.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyect.gopoli.dto.UsuarioDto;
import com.proyect.gopoli.model.Usuario;
import com.proyect.gopoli.model.UsuarioEstado;
import com.proyect.gopoli.repository.UsuarioRepository;
import com.proyect.gopoli.security.JwtService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/usuario")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private JwtService jwtService;

    private Optional<Usuario> usuarioAutenticado(String authorization) {
        Integer id = jwtService.parseUserId(authorization);
        if (id == null) {
            return Optional.empty();
        }
        return repo.findById(id);
    }

    private ResponseEntity<?> noAutorizado() {
        return ResponseEntity.status(401).body("Token inválido o ausente");
    }

    @GetMapping("/me")
    public ResponseEntity<?> obtenerPerfil(@RequestHeader(value = "Authorization", required = false) String auth) {
        Optional<Usuario> usuario = usuarioAutenticado(auth);
        if (usuario.isEmpty()) {
            return noAutorizado();
        }
        if (usuario.get().getIdEstado() != null
                && usuario.get().getIdEstado() == UsuarioEstado.INHABILITADO) {
            return ResponseEntity.status(403).body("Cuenta inhabilitada");
        }
        return ResponseEntity.ok(UsuarioDto.from(usuario.get()));
    }

    @PutMapping("/me")
    public ResponseEntity<?> actualizarPerfil(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, String> body) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        Usuario usuario = opt.get();
        if (usuario.getIdEstado() != null && usuario.getIdEstado() == UsuarioEstado.INHABILITADO) {
            return ResponseEntity.status(403).body("Cuenta inhabilitada");
        }

        String nombre = body.get("nombre");
        String tel = body.get("tel");
        String correo = body.get("correo");

        if (nombre != null && !nombre.isBlank()) {
            usuario.setNombre(nombre.trim());
        }
        if (tel != null) {
            usuario.setTel(tel.trim());
        }
        if (correo != null && !correo.isBlank()) {
            String nuevoCorreo = correo.trim();
            Optional<Usuario> otro = repo.findByCorreo(nuevoCorreo);
            if (otro.isPresent() && !otro.get().getIdUsuario().equals(usuario.getIdUsuario())) {
                return ResponseEntity.status(409).body("El correo ya está en uso");
            }
            usuario.setCorreo(nuevoCorreo);
        }

        return ResponseEntity.ok(UsuarioDto.from(repo.save(usuario)));
    }

    @PutMapping("/me/foto")
    public ResponseEntity<?> actualizarFoto(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody Map<String, String> body) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        String foto = body.get("fotoBase64");
        if (foto == null || foto.isBlank()) {
            return ResponseEntity.status(400).body("fotoBase64 es obligatorio");
        }
        if (foto.length() > 2_000_000) {
            return ResponseEntity.status(400).body("La imagen es demasiado grande");
        }
        Usuario usuario = opt.get();
        usuario.setFotoPerfil(foto);
        return ResponseEntity.ok(UsuarioDto.from(repo.save(usuario)));
    }

    @PostMapping("/me/inhabilitar")
    public ResponseEntity<?> inhabilitarCuenta(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        Usuario usuario = opt.get();
        usuario.setIdEstado(UsuarioEstado.INHABILITADO);
        repo.save(usuario);
        return ResponseEntity.ok("Cuenta inhabilitada");
    }

    @DeleteMapping("/me")
    public ResponseEntity<?> eliminarCuenta(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        Optional<Usuario> opt = usuarioAutenticado(auth);
        if (opt.isEmpty()) {
            return noAutorizado();
        }
        repo.delete(opt.get());
        return ResponseEntity.ok("Cuenta eliminada permanentemente");
    }
}
