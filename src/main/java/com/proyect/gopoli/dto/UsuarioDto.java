package com.proyect.gopoli.dto;

import com.proyect.gopoli.model.GoPoliConstants;
import com.proyect.gopoli.model.Usuario;
import com.proyect.gopoli.model.Vehiculo;

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
    private boolean isDriver;
    private VehiculoDto vehiculo;

    public static UsuarioDto from(Usuario u) {
        return from(u, null);
    }

    public static UsuarioDto from(Usuario u, Vehiculo vehiculo) {
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
        dto.isDriver = GoPoliConstants.esConductor(u.getIdTipoUsuario());
        dto.vehiculo = VehiculoDto.from(vehiculo);
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
    public boolean isDriver() { return isDriver; }
    public VehiculoDto getVehiculo() { return vehiculo; }
}
