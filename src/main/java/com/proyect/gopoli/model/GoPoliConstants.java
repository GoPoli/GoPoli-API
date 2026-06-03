package com.proyect.gopoli.model;

public final class GoPoliConstants {

    private GoPoliConstants() {}

    public static final int TIPO_USUARIO_PASAJERO = 1;
    public static final int TIPO_USUARIO_CONDUCTOR = 2;

    /** Grupo de viaje (pasajeros) — Viaje Compartido */
    public static final int TIPO_SERVICIO_PASAJERO_GRUPO = 1;
    /** Grupo conductor — Viaje Conductor */
    public static final int TIPO_SERVICIO_CONDUCTOR_GRUPO = 3;

    public static final int ESTADO_SERVICIO_ACTIVO = 1;
    public static final int ESTADO_SERVICIO_CANCELADO = 2;
    public static final int ESTADO_SERVICIO_FINALIZADO = 3;
    public static final int ESTADO_SERVICIO_EN_CURSO = 4;

    public static final String ROL_GRUPO_CREADOR = "Creador";
    public static final String ROL_GRUPO_MIEMBRO = "Miembro";

    public static final String ROL_PARTICIPACION_PASSENGER = "passenger";
    public static final String ROL_PARTICIPACION_DRIVER = "driver";

    public static boolean esConductor(Integer idTipoUsuario) {
        return idTipoUsuario != null && idTipoUsuario == TIPO_USUARIO_CONDUCTOR;
    }

    public static boolean esViajeConductor(Integer idTipoServicio) {
        return idTipoServicio != null && idTipoServicio == TIPO_SERVICIO_CONDUCTOR_GRUPO;
    }
}
