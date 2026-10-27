package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComandosInventarioRollbackTest {
    @Test
    void recogerSinCapacidadNoReordenaElSuelo() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Celda celda = juego.getMapa().getCelda(juego.getJugador().getPosicion());
        Objeto primero = pieza("Primero");
        Objeto segundo = pieza("Segundo");
        Objeto tercero = pieza("Tercero");
        celda.setObjetos(List.of(primero, segundo, tercero));
        juego.setCeldasInspeccionadas(Set.of(juego.getJugador().getPosicion()));
        juego.getJugador().getMochila().setObjetos(List.of(
                pieza("A"), pieza("B"), pieza("C"), pieza("D")));

        assertThrows(ComandoException.class,
                () -> new ComandoCoger(new CommandContext(juego), "Segundo").ejecutar());

        assertEquals(3, celda.getObjetos().size());
        assertSame(primero, celda.getObjetos().get(0));
        assertSame(segundo, celda.getObjetos().get(1));
        assertSame(tercero, celda.getObjetos().get(2));
        assertEquals(4, juego.getJugador().getMochila().getObjetos().size());
    }

    @Test
    void recogerCorrectamenteMantieneOrdenDeLosObjetosRestantes() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Celda celda = juego.getMapa().getCelda(juego.getJugador().getPosicion());
        Objeto primero = pieza("Primero");
        Objeto segundo = pieza("Segundo");
        Objeto tercero = pieza("Tercero");
        celda.setObjetos(List.of(primero, segundo, tercero));
        juego.setCeldasInspeccionadas(Set.of(juego.getJugador().getPosicion()));

        new ComandoCoger(new CommandContext(juego), "Segundo").ejecutar();

        assertEquals(List.of(primero, tercero), celda.getObjetos());
        assertSame(segundo, juego.getJugador().getMochila().getObjetos().get(0));
    }

    @Test
    void equiparUnComponenteRechazadoNoModificaLaMochila() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Objeto primero = pieza("Primero");
        Objeto segundo = pieza("Segundo");
        Objeto tercero = pieza("Tercero");
        juego.getJugador().getMochila().setObjetos(List.of(primero, segundo, tercero));

        assertThrows(ComandoException.class,
                () -> new ComandoEquipar(new CommandContext(juego), "Segundo").ejecutar());

        assertEquals(3, juego.getJugador().getMochila().getObjetos().size());
        assertSame(primero, juego.getJugador().getMochila().getObjetos().get(0));
        assertSame(segundo, juego.getJugador().getMochila().getObjetos().get(1));
        assertSame(tercero, juego.getJugador().getMochila().getObjetos().get(2));
    }

    private static Componente pieza(String nombre) {
        return new Componente(nombre, "Objeto de prueba", 1);
    }
}
