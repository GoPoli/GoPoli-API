package com.proyect.gopoli.model;

import jakarta.persistence.*;

@Entity
@Table(name = "tipo_usuario")
public class TipoUsuario {

    @Id
    @Column(name = "id_tipousuario")
    private Integer idTipoUsuario;  

    @Column(name = "nombre_tipousuario")
    private String nombreTipoUsuario;

    public TipoUsuario(){}

    public Integer getIdTipoUsuario(){
        return idTipoUsuario;
    }

    public void setIdTipoUsuario(Integer idTipoUsuario){
        this.idTipoUsuario = idTipoUsuario;
    }

    public String getNombreTipoUsuario(){
        return nombreTipoUsuario;
    }

    public void setNombreTipoUsuario(String nombreTipoUsuario){
        this.nombreTipoUsuario = nombreTipoUsuario;
    }

}