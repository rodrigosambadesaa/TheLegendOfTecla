package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.CuboAgua;
import com.legendoftecla.model.items.Linterna;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Juego;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandoUsarOrdenMochilaTest {
    @Test
    void linternaReutilizableMantieneSuIndiceDespuesDeVariosUsos() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Objeto primero = pieza("Primero");
        Linterna linterna = new Linterna("Luz", "Linterna reutilizable", 1, 2);
        Objeto ultimo = pieza("Ultimo");
        juego.getJugador().getMochila().setObjetos(List.of(primero, linterna, ultimo));

        for (int i = 0; i < 8; i++) {
            new ComandoUsar(new CommandContext(juego), "Luz").ejecutar();

            assertSame(primero, juego.getJugador().getMochila().getObjetos().get(0));
            assertSame(linterna, juego.getJugador().getMochila().getObjetos().get(1));
            assertSame(ultimo, juego.getJugador().getMochila().getObjetos().get(2));
        }
        assertFalse(juego.getJugador().isLinternaActiva());
        assertEquals(3, juego.getJugador().getMochila().getObjetos().size());
    }

    @Test
    void cuboVacioSinFuenteDevuelveObjetoASuPosicionOriginal() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Objeto primero = pieza("Primero");
        CuboAgua cubo = new CuboAgua("Cubo", "Vacio", 1, false);
        Objeto ultimo = pieza("Ultimo");
        juego.getJugador().getMochila().setObjetos(List.of(primero, cubo, ultimo));

        assertThrows(ComandoException.class,
                () -> new ComandoUsar(new CommandContext(juego), "Cubo").ejecutar());

        assertSame(primero, juego.getJugador().getMochila().getObjetos().get(0));
        assertSame(cubo, juego.getJugador().getMochila().getObjetos().get(1));
        assertSame(ultimo, juego.getJugador().getMochila().getObjetos().get(2));
        assertFalse(cubo.isLleno());
    }

    @Test
    void cuboEnFuenteSeLlenaSinAlterarOrden() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Objeto primero = pieza("Primero");
        CuboAgua cubo = new CuboAgua("Cubo", "Recipiente", 1, false);
        Objeto ultimo = pieza("Ultimo");
        juego.getMapa().getCelda(juego.getJugador().getPosicion()).setFuenteAgua(true);
        juego.getJugador().getMochila().setObjetos(List.of(primero, cubo, ultimo));

        new ComandoUsar(new CommandContext(juego), "Cubo").ejecutar();

        assertTrue(cubo.isLleno());
        assertSame(primero, juego.getJugador().getMochila().getObjetos().get(0));
        assertSame(cubo, juego.getJugador().getMochila().getObjetos().get(1));
        assertSame(ultimo, juego.getJugador().getMochila().getObjetos().get(2));
    }

    private static Componente pieza(String nombre) {
        return new Componente(nombre, "Prueba", 1);
    }
}
