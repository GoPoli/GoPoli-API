package com.proyect.gopoli.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehiculo")
public class Vehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_vehiculo")
    private Integer idVehiculo;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(name = "marca", length = 60)
    private String marca;

    @Column(name = "modelo", length = 60)
    private String modelo;

    @Column(name = "matricula", length = 20)
    private String matricula;

    @Column(name = "color", length = 30)
    private String color;

    @Column(name = "capacidad")
    private Integer capacidad;

    @Column(name = "id_tipovehiculo")
    private Integer idTipoVehiculo;

    public Vehiculo() {}

    public Integer getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(Integer idVehiculo) { this.idVehiculo = idVehiculo; }

    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public Integer getCapacidad() { return capacidad; }
    public void setCapacidad(Integer capacidad) { this.capacidad = capacidad; }

    public Integer getIdTipoVehiculo() { return idTipoVehiculo; }
    public void setIdTipoVehiculo(Integer idTipoVehiculo) { this.idTipoVehiculo = idTipoVehiculo; }
}
