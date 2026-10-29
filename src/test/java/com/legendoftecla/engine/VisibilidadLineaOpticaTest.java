package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.elements.ParedDebil;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisibilidadLineaOpticaTest {
    @Test
    void enemigoDetrasDeUnaParedNoSeRevelaHastaDestruirla() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion objetivo = new Posicion(0, 2);
        ParedDebil pared = pared(juego, new Posicion(0, 1));
        agregarEnemigo(juego, objetivo);
        MotorPartida motor = new MotorPartida(juego);

        assertFalse(juego.getMapa().hayLineaVision(new Posicion(0, 0), objetivo));
        assertFalse(motor.getEnemigosVisibles().contains(objetivo));

        pared.recibirDanio(10);

        assertTrue(juego.getMapa().hayLineaVision(new Posicion(0, 0), objetivo));
        assertTrue(motor.getEnemigosVisibles().contains(objetivo));
    }

    @Test
    void visibilidadDiagonalNoAtraviesaMurosEnLaTrayectoria() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().setVisionBase(5);
        Posicion objetivo = new Posicion(2, 2);
        ParedDebil pared = pared(juego, new Posicion(1, 1));
        agregarEnemigo(juego, objetivo);
        MotorPartida motor = new MotorPartida(juego);

        assertFalse(motor.getEnemigosVisibles().contains(objetivo));
        pared.recibirDanio(10);
        assertTrue(motor.getEnemigosVisibles().contains(objetivo));
    }

    @Test
    void noPermiteVisionDiagonalPorEsquinaCerradaEnDosLados() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion origen = juego.getJugador().getPosicion();
        Posicion objetivo = new Posicion(1, 1);
        ParedDebil este = pared(juego, new Posicion(0, 1));
        pared(juego, new Posicion(1, 0));
        agregarEnemigo(juego, objetivo);
        MotorPartida motor = new MotorPartida(juego);

        assertFalse(juego.getMapa().hayLineaVision(origen, objetivo));
        assertFalse(motor.getEnemigosVisibles().contains(objetivo));

        este.recibirDanio(10);

        assertTrue(juego.getMapa().hayLineaVision(origen, objetivo));
        assertTrue(motor.getEnemigosVisibles().contains(objetivo));
    }

    @Test
    void modoEspectadorMuestraEnemigosVivosAunqueLaParedBloquee() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion objetivo = new Posicion(0, 2);
        pared(juego, new Posicion(0, 1));
        Sectoid vivo = agregarEnemigo(juego, objetivo);
        MotorPartida motor = new MotorPartida(juego);

        assertFalse(motor.getEnemigosVisibles().contains(objetivo));
        motor.setModoEspectador(true);
        assertTrue(motor.getEnemigosVisibles().contains(objetivo));
        vivo.setSalud(0);
        assertFalse(motor.getEnemigosVisibles().contains(objetivo));
    }

    @Test
    void consultasFueraDeMapaNoAccedenACeldasInvalidas() {
        Mapa mapa = TestFixtures.juegoBasico(TestFixtures.consola()).getMapa();
        assertFalse(mapa.hayLineaVision(new Posicion(-1, 0), new Posicion(0, 0)));
        assertFalse(mapa.hayLineaVision(new Posicion(0, 0), new Posicion(300, 300)));
        assertTrue(mapa.hayLineaVision(new Posicion(0, 0), new Posicion(0, 0)));
    }

    private static ParedDebil pared(Juego juego, Posicion posicion) {
        ParedDebil pared = new ParedDebil("pared-" + posicion, 10);
        juego.getMapa().getCelda(posicion).agregarElemento(pared);
        return pared;
    }

    private static Sectoid agregarEnemigo(Juego juego, Posicion posicion) {
        Sectoid enemigo = new Sectoid("Visible", posicion, new Mochila(3, 10), 2);
        juego.agregarEnemigo(enemigo);
        return enemigo;
    }
}
