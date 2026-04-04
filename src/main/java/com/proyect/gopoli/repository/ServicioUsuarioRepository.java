package com.proyect.gopoli.repository;

import com.proyect.gopoli.model.ServicioUsuario;
import com.proyect.gopoli.model.ServicioUsuarioId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ServicioUsuarioRepository extends JpaRepository<ServicioUsuario, ServicioUsuarioId> {
    List<ServicioUsuario> findByIdServicio(Integer idServicio);
    List<ServicioUsuario> findByIdUsuario(Integer idUsuario);
}