package com.proyect.gopoli.model;

import jakarta.persistence.*;

@Entity
@Table(name = "servicio_usuario")
@IdClass(ServicioUsuarioId.class)
public class ServicioUsuario {

    @Id
    @Column(name = "id_servicio")
    private Integer idServicio;

    @Id
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "rol")
    private String rol;

    public ServicioUsuario() {}

    public Integer getIdServicio() { 
        return idServicio; 
    }
    public void setIdServicio(Integer idServicio) { 
        this.idServicio = idServicio; 
    }

    public Integer getIdUsuario() { 
        return idUsuario; 
    }
    public void setIdUsuario(Integer idUsuario) { 
        this.idUsuario = idUsuario; 
    }

    public String getRol() { 
        return rol; 
    }
    public void setRol(String rol) { 
        this.rol = rol; 
    }
}