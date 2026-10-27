package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.ai.SistemaTurnosIA;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.CategoriaArma;
import com.legendoftecla.model.items.FaccionEquipo;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TurnoEnemigosAlcanceArmasTest {
    @Test
    void enemigoConVisionMayorQueAlcanceSeAcercaEnLugarDeAtacar() {
        Juego juego = juego(7, 3);
        Sectoid enemigo = (Sectoid) juego.getEnemigos().get(0);
        Arma arma = enemigo.getArmasEquipadas().get(0);
        int saludJugador = juego.getJugador().getSalud();

        TurnoEnemigos.ejecutar(juego, new SistemaTurnosIA(), unPaso(), true);

        assertEquals(new Posicion(0, 6), enemigo.getPosicion());
        assertEquals(3, arma.getMunicionActual());
        assertEquals(saludJugador, juego.getJugador().getSalud());
    }

    @Test
    void enemigoDentroDelAlcanceAtacaYConsumeUnaBala() {
        Juego juego = juego(4, 3);
        Sectoid enemigo = (Sectoid) juego.getEnemigos().get(0);
        Arma arma = enemigo.getArmasEquipadas().get(0);
        int saludJugador = juego.getJugador().getSalud();

        TurnoEnemigos.ejecutar(juego, new SistemaTurnosIA(), unPaso(), true);

        assertEquals(new Posicion(0, 4), enemigo.getPosicion());
        assertEquals(2, arma.getMunicionActual());
        assertTrue(juego.getJugador().getSalud() < saludJugador);
    }

    @Test
    void cargadorVacioDentroDeVisionRecargaSinAtacar() {
        Juego juego = juego(4, 0);
        Sectoid enemigo = (Sectoid) juego.getEnemigos().get(0);
        enemigo.getMochila().guardar(new Municion(
                "Cartuchos", 1, TipoMunicion.ESCOPETA, 3));
        Arma arma = enemigo.getArmasEquipadas().get(0);
        int saludJugador = juego.getJugador().getSalud();

        TurnoEnemigos.ejecutar(juego, new SistemaTurnosIA(), unPaso(), true);

        assertEquals(3, arma.getMunicionActual());
        assertEquals(saludJugador, juego.getJugador().getSalud());
    }

    private static Juego juego(int distancia, int cargador) {
        Mapa mapa = new Mapa("Alcance", "Vision mayor que alcance", 10, 10,
                new Posicion(0, 0), new Posicion(9, 9));
        for (int fila = 0; fila < 10; fila++) {
            for (int columna = 0; columna < 10; columna++) {
                mapa.setCelda(fila, columna, new Celda("Suelo libre", true));
            }
        }
        Marine jugador = new Marine("Jugador", new Posicion(0, 0),
                new Mochila(4, 20), 10);
        Juego juego = new Juego(TestFixtures.consola(), mapa, jugador, 100);
        Sectoid enemigo = new Sectoid("Enemigo", new Posicion(0, distancia),
                new Mochila(4, 10), 10);
        Arma escopeta = new Arma("Escopeta", "Arma de corto alcance", 2, 12,
                true, CategoriaArma.FUEGO, TipoMunicion.ESCOPETA, 6, cargador,
                FaccionEquipo.ENEMIGA);
        enemigo.setArmasEquipadas(List.of(escopeta));
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
        return juego;
    }

    private static Random unPaso() {
        return new Random(20261027) {
            @Override
            public int nextInt(int limite) {
                return limite == 1 ? 0 : 1;
            }
        };
    }
}
