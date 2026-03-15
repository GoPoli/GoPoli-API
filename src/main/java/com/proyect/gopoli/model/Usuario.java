package com.proyect.gopoli.model;

import jakarta.persistence.*;

@Entity
@Table(name="usuario")
public class Usuario {

 @Id
 @GeneratedValue(strategy = GenerationType.IDENTITY)
 private Integer idUsuario;

 private String correo;
 private String contrasena;
 private String nombre;
 private String tel;

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

}