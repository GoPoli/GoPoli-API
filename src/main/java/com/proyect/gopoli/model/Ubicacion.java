package com.proyect.gopoli.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ubicacion")
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ubicacion")
    private Integer idUbicacion;

    @Column(name = "nombre_ubicacion")
    private String nombreUbicacion;

    /** WGS84 — requerido para mapa y ruta en la app (actualiza filas en BD). */
    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "longitud")
    private Double longitud;

    public Ubicacion() {}

    public Integer getIdUbicacion() { 
        return idUbicacion; 
    }
    public void setIdUbicacion(Integer idUbicacion) { 
        this.idUbicacion = idUbicacion; 
    }

    public String getNombreUbicacion() { 
        return nombreUbicacion; 
    }
    public void setNombreUbicacion(String nombreUbicacion) { 
        this.nombreUbicacion = nombreUbicacion; 
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }
}