package com.proyect.gopoli.model;

import jakarta.persistence.*;
import java.time.LocalTime;

@Entity
@Table(name = "ruta_habitual")
public class RutaHabitual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ruta")
    private Integer idRuta;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "id_lugar_salida")
    private Integer idLugarSalida;

    @Column(name = "id_lugar_llegada")
    private Integer idLugarLlegada;

    /** Días ISO (1=lun … 7=dom), separados por coma, p. ej. "1,2,3,4,5". */
    @Column(name = "dias_semana")
    private String diasSemana;

    @Column(name = "hora_salida")
    private LocalTime horaSalida;

    @Column(name = "capacidad")
    private Integer capacidad;

    @Column(name = "id_tipo_servicio")
    private Integer idTipoServicio;

    @Column(name = "descripcion")
    private String descripcion;

    public RutaHabitual() {}

    public Integer getIdRuta() {
        return idRuta;
    }

    public void setIdRuta(Integer idRuta) {
        this.idRuta = idRuta;
    }

    public Integer getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario) {
        this.idUsuario = idUsuario;
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

    public String getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(String diasSemana) {
        this.diasSemana = diasSemana;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public Integer getIdTipoServicio() {
        return idTipoServicio;
    }

    public void setIdTipoServicio(Integer idTipoServicio) {
        this.idTipoServicio = idTipoServicio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
