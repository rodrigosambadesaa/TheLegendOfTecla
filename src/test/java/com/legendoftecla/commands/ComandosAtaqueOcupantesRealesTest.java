package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Armeria;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandosAtaqueOcupantesRealesTest {
    @Test
    void dispararAlEnemigoRegistradoAunqueFalteEnLaListaDeLaCelda() throws Exception {
        Juego juego = juegoConRifle();
        Sectoid enemigo = enemigo("Activo", new Posicion(0, 1));
        juego.agregarEnemigo(enemigo);
        assertTrue(juego.getMapa().getCelda(enemigo.getPosicion()).getEnemigos().isEmpty());

        new ComandoAtacar(new CommandContext(juego), "1e", "todos").ejecutar();

        assertTrue(enemigo.getSalud() < enemigo.getSaludMaxima());
    }

    @Test
    void unOcupanteNoRegistradoNoPuedeSerAtacado() throws Exception {
        Juego juego = juegoConRifle();
        Sectoid fantasma = enemigo("Fantasma", new Posicion(0, 1));
        juego.getMapa().getCelda(fantasma.getPosicion()).agregarEnemigo(fantasma);
        int municion = juego.getJugador().getArmasEquipadas().get(0).getMunicionActual();

        assertThrows(ComandoException.class,
                () -> new ComandoAtacar(new CommandContext(juego), "1e", "todos").ejecutar());

        assertEquals(70, fantasma.getSalud());
        assertEquals(municion,
                juego.getJugador().getArmasEquipadas().get(0).getMunicionActual());
    }

    @Test
    void seAtacaLaPosicionActualSinUsarLaOcupacionAntigua() throws Exception {
        Juego juego = juegoConRifle();
        Sectoid enemigo = enemigo("Movido", new Posicion(0, 1));
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarEnemigo(enemigo);
        enemigo.setPosicion(new Posicion(1, 0));

        assertThrows(ComandoException.class,
                () -> new ComandoAtacar(new CommandContext(juego), "1e", "todos").ejecutar());
        new ComandoAtacar(new CommandContext(juego), "1s", "todos").ejecutar();

        assertTrue(enemigo.getSalud() < enemigo.getSaludMaxima());
    }

    @Test
    void laGranadaDaniaAEnemigosActivosSinOcupacionEnLaCelda() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid enemigo = enemigo("Objetivo", new Posicion(0, 1));
        juego.agregarEnemigo(enemigo);
        juego.getJugador().getMochila().guardar(Armeria.granadaFragmentacion("Granada"));

        new ComandoLanzarExplosivo(new CommandContext(juego), "1e", "Granada")
                .ejecutar();

        assertTrue(enemigo.getSalud() < enemigo.getSaludMaxima());
        assertTrue(juego.getJugador().getMochila().getObjetos().isEmpty());
    }

    @Test
    void laGranadaNoSeDesperdiciaContraUnOcupanteNoRegistrado() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid fantasma = enemigo("Fantasma", new Posicion(0, 1));
        juego.getMapa().getCelda(fantasma.getPosicion()).agregarEnemigo(fantasma);
        var granada = Armeria.granadaFragmentacion("Granada");
        juego.getJugador().getMochila().guardar(granada);

        assertThrows(ComandoException.class,
                () -> new ComandoLanzarExplosivo(new CommandContext(juego), "1e", "Granada")
                        .ejecutar());

        assertEquals(70, fantasma.getSalud());
        assertEquals(1, juego.getJugador().getMochila().getObjetos().size());
    }

    private static Juego juegoConRifle() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().equipar(Armeria.rifle("Rifle", 5, 5));
        return juego;
    }

    private static Sectoid enemigo(String nombre, Posicion posicion) {
        return new Sectoid(nombre, posicion, new Mochila(3, 10), 2);
    }
}
