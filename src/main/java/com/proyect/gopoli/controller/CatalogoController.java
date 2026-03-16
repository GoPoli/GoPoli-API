package com.proyect.gopoli.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.proyect.gopoli.model.Carrera;
import com.proyect.gopoli.model.TipoUsuario;
import com.proyect.gopoli.repository.CarreraRepository;
import com.proyect.gopoli.repository.TipoUsuarioRepository;

@RestController
@CrossOrigin(origins="*")
public class CatalogoController {

    @Autowired
    CarreraRepository carreraRepo;

    @Autowired
    TipoUsuarioRepository tipoUsuarioRepo;


    @GetMapping("/carreras")
    public List<Carrera> getCarreras(){
        return carreraRepo.findAll();
    }


    @GetMapping("/tipo-usuario")
    public List<TipoUsuario> getTiposUsuario(){
        return tipoUsuarioRepo.findAll();
    }

}