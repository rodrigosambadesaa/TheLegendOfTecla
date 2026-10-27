package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.effects.TipoEstado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.progression.CatalogoHabilidades;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SupresionAtaquesMultiplesTest {
    @Test
    void unAtaqueColectivoAplicaSupresionATodosLosEnemigosSupervivientes() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().getProgresion().restaurar(3, 0,
                Set.of(CatalogoHabilidades.FUEGO_SUPRESION));
        List<Sectoid> enemigos = enemigos(juego, 3);

        SistemaCombate.atacarTodos(juego, juego.getJugador(), enemigos, new Random(17));

        for (Sectoid enemigo : enemigos) {
            assertTrue(enemigo.getSalud() > 0);
            assertTrue(enemigo.getEstados().contiene(TipoEstado.ASUSTADO),
                    "La supresion debe afectar a " + enemigo.getNombre());
        }
    }

    @Test
    void sinHabilidadNoSeAplicaNingunEfectoDeSupresion() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        List<Sectoid> enemigos = enemigos(juego, 3);

        SistemaCombate.atacarTodos(juego, juego.getJugador(), enemigos, new Random(17));

        for (Sectoid enemigo : enemigos) {
            assertFalse(enemigo.getEstados().contiene(TipoEstado.ASUSTADO));
        }
    }

    @Test
    void listaVaciaNoConsumeRecursosNiPublicaEfectos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().getProgresion().restaurar(3, 0,
                Set.of(CatalogoHabilidades.FUEGO_SUPRESION));
        int salud = juego.getJugador().getSalud();

        assertTrue(SistemaCombate.atacarTodos(
                juego, juego.getJugador(), List.of(), new Random(17)).isEmpty());

        assertEquals(salud, juego.getJugador().getSalud());
    }

    private static List<Sectoid> enemigos(Juego juego, int cantidad) {
        java.util.ArrayList<Sectoid> enemigos = new java.util.ArrayList<>();
        Posicion posicion = juego.getJugador().getPosicion();
        for (int i = 0; i < cantidad; i++) {
            Sectoid enemigo = new Sectoid("Objetivo" + i, posicion, new Mochila(3, 10), 2);
            juego.agregarEnemigo(enemigo);
            enemigos.add(enemigo);
        }
        return enemigos;
    }
}
