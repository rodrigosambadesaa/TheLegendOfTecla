package com.legendoftecla.model.world;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** La representacion debe usar posiciones visibles, no ocupantes obsoletos. */
class MapaVisibilidadCombatientesTest {
    @Test
    void pintaEnemigosYAliadosVisiblesAunqueLasCeldasNoLosRegistren() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion enemigo = new Posicion(0, 1);
        Posicion aliado = new Posicion(1, 0);
        juego.agregarEnemigo(new Sectoid("Visible", enemigo, new Mochila(2, 5), 3));
        juego.agregarAliado(new Aliado("Visible", aliado, new Mochila(2, 5), 3));

        assertFalse(juego.getMapa().getCelda(enemigo).hasEnemigos());
        assertFalse(juego.getMapa().getCelda(aliado).hasAliados());

        String representacion = juego.getMapa().renderAscii(
                juego.getJugador().getPosicion(), Set.of(enemigo), Set.of(aliado),
                Set.of(), Set.of(enemigo, aliado));

        assertTrue(tieneEnemigo(representacion), representacion);
        assertTrue(tieneAliado(representacion), representacion);
    }

    @Test
    void losOcupantesObsoletosNoSeMuestranSinVisibilidadActual() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion enemigo = new Posicion(0, 1);
        Posicion aliado = new Posicion(1, 0);
        juego.getMapa().getCelda(enemigo).agregarEnemigo(
                new Sectoid("Obsoleto", enemigo, new Mochila(2, 5), 3));
        juego.getMapa().getCelda(aliado).agregarAliado(
                new Aliado("Obsoleto", aliado, new Mochila(2, 5), 3));

        String representacion = juego.getMapa().renderAscii(
                juego.getJugador().getPosicion(), Set.of(), Set.of(),
                Set.of(), Set.of(enemigo, aliado));

        assertFalse(tieneEnemigo(representacion), representacion);
        assertFalse(tieneAliado(representacion), representacion);
    }

    @Test
    void laOscuridadSigueOcultandoUnEnemigoMarcadoComoVisible() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion enemigo = new Posicion(0, 1);
        juego.getMapa().getCelda(enemigo).setOscura(true);

        String oscuro = juego.getMapa().renderAscii(
                juego.getJugador().getPosicion(), Set.of(enemigo), Set.of(),
                Set.of(), Set.of());
        String iluminado = juego.getMapa().renderAscii(
                juego.getJugador().getPosicion(), Set.of(enemigo), Set.of(),
                Set.of(), Set.of(enemigo));

        assertFalse(tieneEnemigo(oscuro), oscuro);
        assertTrue(tieneEnemigo(iluminado), iluminado);
    }

    private static boolean tieneEnemigo(String salida) {
        return salida.contains("E ") || salida.contains("🧟");
    }

    private static boolean tieneAliado(String salida) {
        return salida.contains("A ") || salida.contains("👮");
    }
}
