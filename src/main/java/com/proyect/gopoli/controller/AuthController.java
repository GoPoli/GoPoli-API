package com.proyect.gopoli.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.proyect.gopoli.dto.LoginResponse;
import com.proyect.gopoli.dto.UsuarioDto;
import com.proyect.gopoli.model.Usuario;
import com.proyect.gopoli.model.UsuarioEstado;
import com.proyect.gopoli.repository.UsuarioRepository;
import com.proyect.gopoli.security.JwtService;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    UsuarioRepository repo;

    @Autowired
    JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        if (usuario.getCorreo() == null || usuario.getCorreo().isBlank()) {
            return ResponseEntity.status(400).body("El correo es obligatorio");
        }
        if (repo.findByCorreo(usuario.getCorreo().trim()).isPresent()) {
            return ResponseEntity.status(409).body("El correo ya está registrado");
        }
        usuario.setCorreo(usuario.getCorreo().trim());
        usuario.setNota(0.0);
        usuario.setIdEstado(UsuarioEstado.ACTIVO);
        usuario.setIdTipoUsuario(1);
        Usuario guardado = repo.save(usuario);
        return ResponseEntity.ok(UsuarioDto.from(guardado));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String contrasena = body.get("contrasena");

        if (correo == null || contrasena == null) {
            return ResponseEntity.status(400).body("Correo y contraseña son obligatorios");
        }

        Optional<Usuario> usuario = repo.findByCorreo(correo.trim());

        if (usuario.isEmpty() || !usuario.get().getContrasena().equals(contrasena)) {
            return ResponseEntity.status(401).body("Correo o contraseña incorrectos");
        }

        Usuario u = usuario.get();
        if (u.getIdEstado() != null && u.getIdEstado() == UsuarioEstado.INHABILITADO) {
            return ResponseEntity.status(403).body("Tu cuenta está inhabilitada");
        }

        String token = jwtService.generateToken(u.getIdUsuario());
        return ResponseEntity.ok(new LoginResponse(token, UsuarioDto.from(u)));
    }
}
