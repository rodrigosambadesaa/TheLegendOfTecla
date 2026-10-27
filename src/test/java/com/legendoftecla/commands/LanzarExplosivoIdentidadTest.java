package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.Granada;
import com.legendoftecla.model.items.TipoGranada;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanzarExplosivoIdentidadTest {
    @Test
    void ignoraComponentesConElMismoNombreYLanzaLaGranada() throws Exception {
        Juego juego = juegoConObjetivo();
        Componente componente = new Componente("Carga", "No explosiva", 1);
        Granada granada = granada("Carga", TipoGranada.FRAGMENTACION);
        juego.getJugador().getMochila().setObjetos(List.of(componente, granada));
        int saludEnemigo = juego.getEnemigos().get(0).getSalud();

        new ComandoLanzarExplosivo(new CommandContext(juego), "1e", "Carga").ejecutar();

        assertEquals(1, juego.getJugador().getMochila().getObjetos().size());
        assertSame(componente, juego.getJugador().getMochila().getObjetos().get(0));
        assertTrue(juego.getEnemigos().get(0).getSalud() < saludEnemigo);
    }

    @Test
    void retiraSoloLaInstanciaSeleccionadaCuandoHayVariosObjetosHomónimos()
            throws Exception {
        Juego juego = juegoConObjetivo();
        Componente componente = new Componente("Carga", "Normal", 1);
        Granada primera = granada("Carga", TipoGranada.ATURDIDORA);
        Granada segunda = granada("Carga", TipoGranada.FRAGMENTACION);
        juego.getJugador().getMochila().setObjetos(List.of(componente, primera, segunda));

        new ComandoLanzarExplosivo(new CommandContext(juego), "1e", "Carga").ejecutar();

        assertEquals(2, juego.getJugador().getMochila().getObjetos().size());
        assertSame(componente, juego.getJugador().getMochila().getObjetos().get(0));
        assertSame(segunda, juego.getJugador().getMochila().getObjetos().get(1));
    }

    @Test
    void noConsumeComponentesCuandoNoHayExplosivoConEseNombre() {
        Juego juego = juegoConObjetivo();
        Componente componente = new Componente("Carga", "No explosiva", 1);
        juego.getJugador().getMochila().setObjetos(List.of(componente));
        int salud = juego.getEnemigos().get(0).getSalud();

        assertThrows(ComandoException.class,
                () -> new ComandoLanzarExplosivo(new CommandContext(juego), "1e", "Carga")
                        .ejecutar());

        assertEquals(1, juego.getJugador().getMochila().getObjetos().size());
        assertSame(componente, juego.getJugador().getMochila().getObjetos().get(0));
        assertEquals(salud, juego.getEnemigos().get(0).getSalud());
    }

    private static Juego juegoConObjetivo() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion destino = new Posicion(0, 1);
        Sectoid enemigo = new Sectoid("Objetivo", destino, new Mochila(3, 10), 2);
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(destino).agregarEnemigo(enemigo);
        return juego;
    }

    private static Granada granada(String nombre, TipoGranada tipo) {
        return new Granada(nombre, "Ensayo", 1, tipo);
    }
}
