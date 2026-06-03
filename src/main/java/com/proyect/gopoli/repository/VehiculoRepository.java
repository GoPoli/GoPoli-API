package com.proyect.gopoli.repository;

import com.proyect.gopoli.model.Vehiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehiculoRepository extends JpaRepository<Vehiculo, Integer> {
    Optional<Vehiculo> findByIdUsuario(Integer idUsuario);
    boolean existsByMatricula(String matricula);
    boolean existsByMatriculaAndIdUsuarioNot(String matricula, Integer idUsuario);
}
