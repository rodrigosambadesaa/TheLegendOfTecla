package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.ai.PercepcionIA;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Limites;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NivelYEscalabilidadBandosTest {
    @Test
    void nivelInicialEscalaJugadorSinAlterarElNivelUnoHistorico() {
        Juego nivelUno = TestFixtures.juegoBasico(TestFixtures.consola());
        int saludBase = nivelUno.getJugador().getSaludMaxima();
        ServicioNivelInicial.aplicar(nivelUno.getJugador(), 1);
        assertEquals(saludBase, nivelUno.getJugador().getSaludMaxima());

        ServicioNivelInicial.aplicar(nivelUno.getJugador(), 20);
        assertEquals(20, nivelUno.getJugador().getProgresion().getNivel());
        assertTrue(nivelUno.getJugador().getSaludMaxima() > saludBase);
    }

    @Test
    void equilibrioRecortaEnemigosYSellaRefuerzos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        for (int i = 0; i < 3; i++) {
            juego.agregarEnemigo(new Sectoid("E" + i, new Posicion(1, 1),
                    new Mochila(1, 1), 2));
        }

        assertEquals(2, EquilibradorBandos.aplicar(juego));
        assertEquals(1, juego.getEnemigos().size());
        assertThrows(IllegalStateException.class, () -> juego.agregarEnemigo(
                new Sectoid("Refuerzo", new Posicion(1, 1), new Mochila(1, 1), 2)));
    }

    @Test
    void despliegueMaximoCumplePresupuestoDeCpuYMemoria() {
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        long memoriaAntes = runtime.totalMemory() - runtime.freeMemory();
        long inicio = System.nanoTime();
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion posicion = new Posicion(1, 1);
        for (int i = 0; i < Limites.ALIADOS_MAXIMOS; i++) {
            juego.agregarAliado(new Aliado("A" + i, posicion, new Mochila(1, 1), 2));
        }
        for (int i = 0; i < Limites.COMBATIENTES_POR_BANDO; i++) {
            juego.agregarEnemigo(new Sectoid("E" + i, posicion, new Mochila(1, 1), 2));
        }
        EquilibradorBandos.aplicar(juego);
        PercepcionIA percepcion = new PercepcionIA();
        for (int i = 0; i < 100; i++) {
            percepcion.percibir(juego, juego.getEnemigos().get(i * 50));
        }
        long milisegundos = (System.nanoTime() - inicio) / 1_000_000;
        long memoria = runtime.totalMemory() - runtime.freeMemory() - memoriaAntes;

        assertEquals(5_000, juego.getAliadosRegistrados().size() + 1);
        assertEquals(5_000, juego.getEnemigos().size());
        assertTrue(milisegundos < 15_000,
                () -> "Despliegue/percepcion demasiado lentos: " + milisegundos + " ms");
        assertTrue(memoria < 512L * 1024 * 1024,
                () -> "Consumo incremental excesivo: " + memoria + " bytes");
    }
}
