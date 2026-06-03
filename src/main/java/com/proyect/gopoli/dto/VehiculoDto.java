package com.proyect.gopoli.dto;

import com.proyect.gopoli.model.Vehiculo;

public class VehiculoDto {
    private Integer idVehiculo;
    private String marca;
    private String modelo;
    private String color;
    private String placa;

    public static VehiculoDto from(Vehiculo v) {
        if (v == null) return null;
        VehiculoDto dto = new VehiculoDto();
        dto.idVehiculo = v.getIdVehiculo();
        dto.marca = v.getMarca();
        dto.modelo = v.getModelo();
        dto.color = v.getColor();
        dto.placa = v.getMatricula();
        return dto;
    }

    public Integer getIdVehiculo() { return idVehiculo; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public String getColor() { return color; }
    public String getPlaca() { return placa; }
}
