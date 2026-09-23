package com.Proyect.GoPoli.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.proyect.gopoli.model.GoPoliConstants;

class GoPoliConstantsTest {

    @Test
    void esConductor_trueOnlyForTipoConductor() {
        assertTrue(GoPoliConstants.esConductor(GoPoliConstants.TIPO_USUARIO_CONDUCTOR));
        assertFalse(GoPoliConstants.esConductor(GoPoliConstants.TIPO_USUARIO_PASAJERO));
        assertFalse(GoPoliConstants.esConductor(null));
        assertFalse(GoPoliConstants.esConductor(99));
    }

    @Test
    void esViajeConductor_trueOnlyForTipoServicioConductorGrupo() {
        assertTrue(GoPoliConstants.esViajeConductor(GoPoliConstants.TIPO_SERVICIO_CONDUCTOR_GRUPO));
        assertFalse(GoPoliConstants.esViajeConductor(GoPoliConstants.TIPO_SERVICIO_PASAJERO_GRUPO));
        assertFalse(GoPoliConstants.esViajeConductor(null));
        assertFalse(GoPoliConstants.esViajeConductor(2));
    }
}
