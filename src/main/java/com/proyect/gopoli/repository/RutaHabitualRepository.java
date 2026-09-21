package com.proyect.gopoli.repository;

import com.proyect.gopoli.model.RutaHabitual;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RutaHabitualRepository extends JpaRepository<RutaHabitual, Integer> {

    List<RutaHabitual> findByIdUsuarioOrderByIdRutaAsc(Integer idUsuario);
}
