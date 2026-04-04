package com.proyect.gopoli.repository;

import com.proyect.gopoli.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;


public interface ServicioRepository extends JpaRepository<Servicio, Integer> {

    List<Servicio> findByIdCreadorAndIdEstadoServicio(Integer idCreador, Integer idEstadoServicio);
    List<Servicio> findByIdEstadoServicio(Integer idEstadoServicio);
}   