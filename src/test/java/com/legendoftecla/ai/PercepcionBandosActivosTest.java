package com.legendoftecla.ai;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class PercepcionBandosActivosTest {
    @Test
    void ruidoSePercibeAunqueLaCeldaNoContengaAlEnemigoRegistrado() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Scout cercano = scout("Cercano", new Posicion(0, 1));
        Scout lejano = scout("Lejano", new Posicion(2, 2));
        juego.agregarEnemigo(cercano);
        juego.agregarEnemigo(lejano);

        try (SistemaRuido ruido = new SistemaRuido(juego)) {
            ruido.generar(new Posicion(0, 0), FuenteRuido.CAMINAR);
        }

        assertEquals(NivelAlerta.INVESTIGANDO, cercano.getControladorIA().getEstado());
        assertEquals(new Posicion(0, 0),
                cercano.getControladorIA().getUltimaPosicionConocida());
        assertEquals(NivelAlerta.PATRULLA, lejano.getControladorIA().getEstado());
        assertNull(lejano.getControladorIA().getUltimaPosicionConocida());
    }

    @Test
    void ruidoSeCalculaDesdePosicionActualYNoDesdeAntiguaCelda() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Scout enemigo = scout("Movido", new Posicion(2, 2));
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(new Posicion(2, 2)).agregarEnemigo(enemigo);
        enemigo.setPosicion(new Posicion(0, 1));

        try (SistemaRuido ruido = new SistemaRuido(juego)) {
            ruido.generar(new Posicion(0, 0), FuenteRuido.CAMINAR);
        }

        assertEquals(NivelAlerta.INVESTIGANDO, enemigo.getControladorIA().getEstado());
        assertEquals(new Posicion(0, 0),
                enemigo.getControladorIA().getUltimaPosicionConocida());
    }

    @Test
    void grupoGrandeDeAliadosSeDetectaSinRegistrosDeOcupacionEnCeldas() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado herido = null;
        for (int i = 0; i < 140; i++) {
            Aliado aliado = new Aliado("A" + i, new Posicion(1, 2),
                    new Mochila(1, 1), 2);
            if (i == 139) {
                aliado.setSalud(1);
                herido = aliado;
            }
            juego.agregarAliado(aliado);
        }
        Scout observador = scout("Observador", new Posicion(1, 1));

        ContextoIA contexto = new PercepcionIA().percibir(juego, observador);

        assertEquals(140, juego.getAliados().size());
        assertEquals(0, juego.getMapa().getCelda(new Posicion(1, 2))
                .getAliados().size());
        assertSame(herido, contexto.objetivo());
    }

    private static Scout scout(String nombre, Posicion posicion) {
        return new Scout(nombre, posicion, new Mochila(2, 10), 4);
    }
}
