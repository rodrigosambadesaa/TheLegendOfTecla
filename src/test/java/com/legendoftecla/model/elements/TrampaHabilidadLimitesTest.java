package com.legendoftecla.model.elements;

import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Zapador;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrampaHabilidadLimitesTest {
    @Test
    void percepcionMaximaSiempreDetectaConBonificacionDeZapador() {
        Zapador zapador = zapador();
        Mina mina = new Mina("max-detectar", 12, 1, false);

        assertTrue(mina.detectar(zapador, Integer.MAX_VALUE));
        assertTrue(mina.isDetectada());
    }

    @Test
    void habilidadMaximaSiempreDesactivaLaTrampaDetectada() {
        Zapador zapador = zapador();
        Mina mina = new Mina("max-desactivar", 12, 1, false);
        assertTrue(mina.detectar(zapador, 20));

        assertTrue(mina.desactivar(zapador, Integer.MAX_VALUE));
        assertFalse(mina.isActiva());
    }

    @Test
    void habilidadesInsuficientesNoCambianEstado() {
        Zapador zapador = zapador();
        Mina mina = new Mina("baja", 12, 1, false);

        assertFalse(mina.detectar(zapador, 0));
        assertFalse(mina.desactivar(zapador, 0));
        assertTrue(mina.isActiva());
    }

    private static Zapador zapador() {
        return new Zapador("Zapador", new Posicion(0, 0), new Mochila(2, 10), 2);
    }
}
