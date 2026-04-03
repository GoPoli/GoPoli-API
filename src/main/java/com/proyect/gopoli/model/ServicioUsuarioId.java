package com.proyect.gopoli.model;

import java.io.Serializable;
import java.util.Objects;

public class ServicioUsuarioId implements Serializable {

    private Integer idServicio;
    private Integer idUsuario;

    public ServicioUsuarioId() {}

    public ServicioUsuarioId(Integer idServicio, Integer idUsuario) {
        this.idServicio = idServicio;
        this.idUsuario = idUsuario;
    }

    public Integer getIdServicio() { 
        return idServicio; 
    }
    public Integer getIdUsuario() { 
        return idUsuario; 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServicioUsuarioId)) return false;
        ServicioUsuarioId that = (ServicioUsuarioId) o;
        return Objects.equals(idServicio, that.idServicio) && Objects.equals(idUsuario, that.idUsuario);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idServicio, idUsuario);
    }
}