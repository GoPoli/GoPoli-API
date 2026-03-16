package com.proyect.gopoli.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_Usuario")
    private Integer idUsuario;

    @Column(name = "correo")
    private String correo;

    @Column(name = "contrasena")
    private String contrasena;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "tel")
    private String tel;

    @Column(name = "id_carrera")
    private Integer idCarrera;

    @Column(name = "id_estado")
    private Integer idEstado;

    @Column(name = "id_tipousuario")
    private Integer idTipoUsuario;

    @Column(name = "nota")
    private Double nota;

    public Usuario(){}

    public Integer getIdUsuario(){
        return idUsuario;
    }

    public void setIdUsuario(Integer idUsuario){
        this.idUsuario = idUsuario;
    }

    public String getCorreo(){
        return correo;
    }

    public void setCorreo(String correo){
        this.correo = correo;
    }

    public String getContrasena(){
        return contrasena;
    }

    public void setContrasena(String contrasena){
        this.contrasena = contrasena;
    }

    public String getNombre(){
        return nombre;
    }

    public void setNombre(String nombre){
        this.nombre = nombre;
    }

    public String getTel(){
        return tel;
    }

    public void setTel(String tel){
        this.tel = tel;
    }

    public Integer getIdCarrera(){
        return idCarrera;
    }

    public void setIdCarrera(Integer idCarrera){
        this.idCarrera = idCarrera;
    }

    public Integer getIdEstado(){
        return idEstado;
    }

    public void setIdEstado(Integer idEstado){
        this.idEstado = idEstado;
    }

    public Integer getIdTipoUsuario(){
        return idTipoUsuario;
    }

    public void setIdTipoUsuario(Integer idTipoUsuario){
        this.idTipoUsuario = idTipoUsuario;
    }

    public Double getNota(){
        return nota;
    }

    public void setNota(Double nota){
        this.nota = nota;
    }
}