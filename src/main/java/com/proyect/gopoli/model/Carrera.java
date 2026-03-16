package com.proyect.gopoli.model;

import jakarta.persistence.*;

@Entity
@Table(name = "carrera")
public class Carrera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(name = "nombrecarrera")
    private String nombreCarrera;

    public Carrera(){}

    public Integer getIdCarrera(){
        return idCarrera;
    }

    public void setIdCarrera(Integer idCarrera){
        this.idCarrera = idCarrera;
    }

    public String getNombreCarrera(){
        return nombreCarrera;
    }

    public void setNombreCarrera(String nombreCarrera){
        this.nombreCarrera = nombreCarrera;
    }

}