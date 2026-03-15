package com.proyect.gopoli.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.proyect.gopoli.model.Usuario;
import com.proyect.gopoli.repository.UsuarioRepository;

@RestController
@CrossOrigin(origins="*")
public class AuthController {

 @Autowired
 UsuarioRepository repo;

    @PostMapping("/register")
    public Usuario register(@RequestBody Usuario usuario){
        return repo.save(usuario);
    }

     @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String correo = body.get("correo");
        String contrasena = body.get("contrasena");

        Optional<Usuario> usuario = repo.findByCorreo(correo);

        if (usuario.isPresent() && usuario.get().getContrasena().equals(contrasena)) {
            return ResponseEntity.ok(usuario.get());
        } else {
            return ResponseEntity.status(401).body("Correo o contraseña incorrectos");
        }
    }
}

