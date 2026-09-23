package com.Proyect.GoPoli.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.proyect.gopoli.model.GoPoliConstants;
import com.proyect.gopoli.util.ServicioPolicy;

class ServicioPolicyTest {

    @Test
    void permiteIniciarSoloDesdePlanificacion() {
        assertNull(ServicioPolicy.errorTransicion(
                GoPoliConstants.ESTADO_SERVICIO_ACTIVO,
                GoPoliConstants.ESTADO_SERVICIO_EN_CURSO));
        assertEquals("El viaje solo puede iniciarse desde planificación",
                ServicioPolicy.errorTransicion(
                        GoPoliConstants.ESTADO_SERVICIO_EN_CURSO,
                        GoPoliConstants.ESTADO_SERVICIO_EN_CURSO));
    }

    @Test
    void permiteFinalizarSoloUnViajeEnCurso() {
        assertNull(ServicioPolicy.errorTransicion(
                GoPoliConstants.ESTADO_SERVICIO_EN_CURSO,
                GoPoliConstants.ESTADO_SERVICIO_FINALIZADO));
        assertEquals("Solo un viaje en curso puede finalizarse",
                ServicioPolicy.errorTransicion(
                        GoPoliConstants.ESTADO_SERVICIO_ACTIVO,
                        GoPoliConstants.ESTADO_SERVICIO_FINALIZADO));
    }

    @Test
    void permiteCancelarSoloMientrasEstaEnPlanificacion() {
        assertNull(ServicioPolicy.errorTransicion(
                GoPoliConstants.ESTADO_SERVICIO_ACTIVO,
                GoPoliConstants.ESTADO_SERVICIO_CANCELADO));
        assertEquals("Solo un viaje en planificación puede cancelarse",
                ServicioPolicy.errorTransicion(
                        GoPoliConstants.ESTADO_SERVICIO_EN_CURSO,
                        GoPoliConstants.ESTADO_SERVICIO_CANCELADO));
    }

    @Test
    void validaSalidaDestinoYCapacidad() {
        assertEquals("Salida y llegada deben ser distintas",
                ServicioPolicy.errorCreacion(4, 4, 3));
        assertEquals("La capacidad debe ser entre 2 y 4 personas",
                ServicioPolicy.errorCreacion(4, 5, 1));
        assertNull(ServicioPolicy.errorCreacion(4, 5, 4));
    }
}
