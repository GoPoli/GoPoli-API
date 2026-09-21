package com.proyect.gopoli.repository;

import com.proyect.gopoli.model.Mensaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {
    List<Mensaje> findByIdServicioOrderByFechaEnvioAsc(Integer idServicio);
}
