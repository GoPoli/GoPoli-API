package com.proyect.gopoli.controller;

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

        usuario.setNota(0.0);
        usuario.setIdEstado(2);
        usuario.setIdTipoUsuario(1);
        return repo.save(usuario);
    }

}