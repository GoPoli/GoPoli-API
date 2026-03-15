package com.proyect.gopoli.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyect.gopoli.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario,Integer> {

}