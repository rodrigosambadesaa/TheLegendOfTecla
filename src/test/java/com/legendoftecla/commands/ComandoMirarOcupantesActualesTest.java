package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandoMirarOcupantesActualesTest {
    @Test
    void muestraEnemigosRegistradosAunqueLaListaDeLaCeldaEsteVacia() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        juego.agregarEnemigo(enemigo("Presente", new Posicion(0, 0)));

        new ComandoMirar(new CommandContext(juego)).ejecutar();

        assertTrue(consola.salida().contains("Enemigos aqui: Presente"));
    }

    @Test
    void noMuestraOcupantesAjenoALaPartidaNiEnemigosYaFallecidos() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Sectoid ajeno = enemigo("Fantasma", new Posicion(0, 0));
        Sectoid muerto = enemigo("Muerto", new Posicion(0, 0));
        muerto.setSalud(0);
        juego.agregarEnemigo(muerto);
        juego.getMapa().getCelda(new Posicion(0, 0)).agregarEnemigo(ajeno);
        juego.getMapa().getCelda(new Posicion(0, 0)).agregarEnemigo(muerto);

        new ComandoMirar(new CommandContext(juego)).ejecutar();

        assertFalse(consola.salida().contains("Enemigos aqui:"));
        assertFalse(consola.salida().contains("Fantasma"));
        assertFalse(consola.salida().contains("Muerto"));
    }

    @Test
    void enemigoMovidoSeVeEnLaPosicionActualYNoEnSuCeldaAnterior() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Sectoid enemigo = enemigo("Movido", new Posicion(0, 0));
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(new Posicion(0, 0)).agregarEnemigo(enemigo);
        enemigo.setPosicion(new Posicion(0, 1));

        new ComandoMirar(new CommandContext(juego)).ejecutar();
        assertFalse(consola.salida().contains("Enemigos aqui:"));
        consola.limpiar();

        new ComandoMirar(new CommandContext(juego), Direccion.ESTE, 1,
                "Movido").ejecutar();
        assertTrue(consola.salida().contains("Movido"));
    }

    @Test
    void miradaRemotaRechazaFichaDeEnemigoQueYaNoEstaAlli() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Sectoid enemigo = enemigo("Saliente", new Posicion(0, 1));
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarEnemigo(enemigo);
        enemigo.setPosicion(new Posicion(1, 1));

        org.junit.jupiter.api.Assertions.assertThrows(
                com.legendoftecla.exceptions.ComandoException.class, () ->
                new ComandoMirar(new CommandContext(juego), Direccion.ESTE, 1,
                        "Saliente").ejecutar());
    }

    private static Sectoid enemigo(String nombre, Posicion posicion) {
        return new Sectoid(nombre, posicion, new Mochila(3, 10), 2);
    }
}
