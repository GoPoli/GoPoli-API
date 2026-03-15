package com.proyect.gopoli.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.proyect.gopoli.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findByCorreo(String correo);
}