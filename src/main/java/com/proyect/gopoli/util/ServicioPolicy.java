package com.proyect.gopoli.util;

import com.proyect.gopoli.model.GoPoliConstants;

public final class ServicioPolicy {

    private ServicioPolicy() {
    }

    public static String errorTransicion(Integer estadoActual, Integer estadoDestino) {
        if (estadoDestino == GoPoliConstants.ESTADO_SERVICIO_EN_CURSO
                && estadoActual != GoPoliConstants.ESTADO_SERVICIO_ACTIVO) {
            return "El viaje solo puede iniciarse desde planificación";
        }
        if (estadoDestino == GoPoliConstants.ESTADO_SERVICIO_FINALIZADO
                && estadoActual != GoPoliConstants.ESTADO_SERVICIO_EN_CURSO) {
            return "Solo un viaje en curso puede finalizarse";
        }
        if (estadoDestino == GoPoliConstants.ESTADO_SERVICIO_CANCELADO
                && estadoActual != GoPoliConstants.ESTADO_SERVICIO_ACTIVO) {
            return "Solo un viaje en planificación puede cancelarse";
        }
        return null;
    }

    public static String errorCreacion(
            Integer idLugarSalida,
            Integer idLugarLlegada,
            Integer capacidad) {
        if (idLugarSalida == null) {
            return "El lugar de salida es obligatorio";
        }
        if (idLugarLlegada == null) {
            return "El lugar de llegada es obligatorio";
        }
        if (idLugarSalida.equals(idLugarLlegada)) {
            return "Salida y llegada deben ser distintas";
        }
        if (capacidad == null || capacidad < 2 || capacidad > 4) {
            return "La capacidad debe ser entre 2 y 4 personas";
        }
        return null;
    }
}
