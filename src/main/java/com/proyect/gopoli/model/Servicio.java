package com.proyect.gopoli.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "servicio")
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_servicio")
    private Integer idServicio;

    @Column(name = "fecha")
    private LocalDate fecha;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "id_lugarsalida")
    private Integer idLugarSalida;

    @Column(name = "id_lugarllegada")
    private Integer idLugarLlegada;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "id_creador")
    private Integer idCreador;

    @Column(name = "id_tiposervicio")
    private Integer idTipoServicio;

    @Column(name = "id_estadoservicio")
    private Integer idEstadoServicio;

    @Column(name = "capacidad")
    private Integer capacidad;

    public Servicio() {}

    public Integer getIdServicio() { 
        return idServicio; 
    }
    public void setIdServicio(Integer id) { 
        this.idServicio = id; 
    }

    public LocalDate getFecha() { 
        return fecha; 
    }
    public void setFecha(LocalDate fecha) { 
        this.fecha = fecha; 
    }

    public String getDescripcion() { 
        return descripcion; 
    }
    public void setDescripcion(String descripcion) { 
        this.descripcion = descripcion; 
    }

    public Integer getIdLugarSalida() { 
        return idLugarSalida; 
    }
    public void setIdLugarSalida(Integer idLugarSalida) { 
        this.idLugarSalida = idLugarSalida; 
    }

    public Integer getIdLugarLlegada() { 
        return idLugarLlegada; 
    }
    public void setIdLugarLlegada(Integer idLugarLlegada) { 
        this.idLugarLlegada = idLugarLlegada; 
    }

    public LocalTime getHoraSalida() { 
        return horaSalida; 
    }
    public void setHoraSalida(LocalTime horaSalida) { 
        this.horaSalida = horaSalida; 
    }

    public Integer getIdCreador() { 
        return idCreador; 
    }
    public void setIdCreador(Integer idCreador) { 
        this.idCreador = idCreador; 
    }

    public Integer getIdTipoServicio() { 
        return idTipoServicio; 
    }
    public void setIdTipoServicio(Integer idTipoServicio) { 
        this.idTipoServicio = idTipoServicio; 
    }
    public Integer getIdEstadoServicio() { 
        return idEstadoServicio; 
    }
    public void setIdEstadoServicio(Integer idEstadoServicio) { 
        this.idEstadoServicio = idEstadoServicio; 
    }

    public Integer getCapacidad() { 
        return capacidad; 
    }
    public void setCapacidad(Integer capacidad) { 
        this.capacidad = capacidad; 
    }
}