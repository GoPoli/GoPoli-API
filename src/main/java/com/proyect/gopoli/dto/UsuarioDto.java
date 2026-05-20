package com.proyect.gopoli.dto;

import com.proyect.gopoli.model.Usuario;

public class UsuarioDto {
    private Integer idUsuario;
    private String correo;
    private String nombre;
    private String tel;
    private Integer idCarrera;
    private Integer idEstado;
    private Integer idTipoUsuario;
    private Double nota;
    private String fotoPerfil;

    public static UsuarioDto from(Usuario u) {
        UsuarioDto dto = new UsuarioDto();
        dto.idUsuario = u.getIdUsuario();
        dto.correo = u.getCorreo();
        dto.nombre = u.getNombre();
        dto.tel = u.getTel();
        dto.idCarrera = u.getIdCarrera();
        dto.idEstado = u.getIdEstado();
        dto.idTipoUsuario = u.getIdTipoUsuario();
        dto.nota = u.getNota();
        dto.fotoPerfil = u.getFotoPerfil();
        return dto;
    }

    public Integer getIdUsuario() { return idUsuario; }
    public String getCorreo() { return correo; }
    public String getNombre() { return nombre; }
    public String getTel() { return tel; }
    public Integer getIdCarrera() { return idCarrera; }
    public Integer getIdEstado() { return idEstado; }
    public Integer getIdTipoUsuario() { return idTipoUsuario; }
    public Double getNota() { return nota; }
    public String getFotoPerfil() { return fotoPerfil; }
}
