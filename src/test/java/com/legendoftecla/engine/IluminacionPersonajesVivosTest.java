package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La iluminacion de equipo portatil depende del portador vivo. */
class IluminacionPersonajesVivosTest {
    @Test
    void linternaDelJugadorFallecidoNoIluminaNiConCacheNiSinElla() {
        Juego juego = oscuro();
        Posicion objetivo = new Posicion(0, 1);
        juego.getJugador().setLinternaActiva(true);
        juego.getJugador().setAlcanceLinterna(2);

        assertTrue(SistemaIluminacion.hayLuz(juego, objetivo));
        assertTrue(SistemaIluminacion.hayLuz(
                juego, objetivo, SistemaIluminacion.precalcularIluminacion(juego)));

        juego.getJugador().setSalud(0);

        assertFalse(SistemaIluminacion.hayLuz(juego, objetivo));
        assertFalse(SistemaIluminacion.hayLuz(
                juego, objetivo, SistemaIluminacion.precalcularIluminacion(juego)));
    }

    @Test
    void linternaDelAliadoVivoSigueFuncionandoSinElJugador() {
        Juego juego = oscuro();
        Posicion objetivo = new Posicion(1, 1);
        Aliado aliado = new Aliado("Apoyo", new Posicion(1, 0), new Mochila(3, 10), 2);
        aliado.setLinternaActiva(true);
        aliado.setAlcanceLinterna(1);
        juego.agregarAliado(aliado);
        juego.getJugador().setSalud(0);

        assertTrue(SistemaIluminacion.hayLuz(juego, objetivo));
        assertTrue(SistemaIluminacion.hayLuz(
                juego, objetivo, SistemaIluminacion.precalcularIluminacion(juego)));

        aliado.setSalud(0);
        assertFalse(SistemaIluminacion.hayLuz(juego, objetivo));
        assertFalse(SistemaIluminacion.hayLuz(
                juego, objetivo, SistemaIluminacion.precalcularIluminacion(juego)));
    }

    @Test
    void fuegoYAntorchasContinuanIluminandoSinPersonajesVivos() {
        Juego juego = oscuro();
        Posicion fuego = new Posicion(1, 1);
        Posicion antorcha = new Posicion(2, 2);
        juego.getJugador().setLinternaActiva(true);
        juego.getJugador().setAlcanceLinterna(5);
        juego.getJugador().setSalud(0);
        juego.getMapa().getCelda(fuego).setNivelFuego(2);
        juego.getMapa().getCelda(antorcha).setAntorchaMural(true);

        assertTrue(SistemaIluminacion.hayLuz(juego, fuego));
        assertTrue(SistemaIluminacion.hayLuz(juego, antorcha));
        var iluminadas = SistemaIluminacion.precalcularIluminacion(juego);
        assertTrue(SistemaIluminacion.hayLuz(juego, fuego, iluminadas));
        assertTrue(SistemaIluminacion.hayLuz(juego, antorcha, iluminadas));
        assertFalse(SistemaIluminacion.hayLuz(juego, new Posicion(0, 2), iluminadas));
    }

    private static Juego oscuro() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        for (int f = 0; f < juego.getMapa().getFilas(); f++) {
            for (int c = 0; c < juego.getMapa().getColumnas(); c++) {
                juego.getMapa().getCelda(new Posicion(f, c)).setOscura(true);
            }
        }
        return juego;
    }
}
