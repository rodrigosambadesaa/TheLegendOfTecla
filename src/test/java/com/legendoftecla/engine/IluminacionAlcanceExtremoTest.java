package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IluminacionAlcanceExtremoTest {
    @Test
    void alcanceMaximoQuedaAcotadoALasNueveCeldasDelMapa() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().setPosicion(new Posicion(2, 2));
        juego.getJugador().setLinternaActiva(true);
        juego.getJugador().setAlcanceLinterna(1_000_000);

        Set<Posicion> iluminadas = assertTimeoutPreemptively(Duration.ofSeconds(3),
                () -> SistemaIluminacion.precalcularIluminacion(juego));

        assertEquals(9, iluminadas.size());
        for (int fila = 0; fila < 3; fila++) {
            for (int columna = 0; columna < 3; columna++) {
                assertTrue(iluminadas.contains(new Posicion(fila, columna)));
            }
        }
        assertFalse(iluminadas.contains(new Posicion(3, 2)));
        assertFalse(iluminadas.contains(new Posicion(-1, 0)));
    }

    @Test
    void comparacionAleatoriaConConsultaDirectaEnTodosLosPuntos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        for (int fila = 0; fila < 3; fila++) {
            for (int columna = 0; columna < 3; columna++) {
                juego.getMapa().getCelda(new Posicion(fila, columna)).setOscura(true);
            }
        }
        Aliado aliado = new Aliado("Linterna aliada", new Posicion(2, 2),
                new Mochila(3, 10), 3);
        juego.agregarAliado(aliado);
        Random random = new Random(20261028L);

        for (int caso = 0; caso < 500; caso++) {
            juego.getJugador().setPosicion(new Posicion(random.nextInt(3), random.nextInt(3)));
            aliado.setPosicion(new Posicion(random.nextInt(3), random.nextInt(3)));
            juego.getJugador().setLinternaActiva(random.nextBoolean());
            aliado.setLinternaActiva(random.nextBoolean());
            juego.getJugador().setAlcanceLinterna(random.nextInt(8));
            aliado.setAlcanceLinterna(random.nextInt(8));
            Set<Posicion> iluminadas = SistemaIluminacion.precalcularIluminacion(juego);
            assertTrue(iluminadas.size() <= 9);

            for (int fila = 0; fila < 3; fila++) {
                for (int columna = 0; columna < 3; columna++) {
                    Posicion punto = new Posicion(fila, columna);
                    assertEquals(SistemaIluminacion.hayLuz(juego, punto),
                            SistemaIluminacion.hayLuz(juego, punto, iluminadas),
                            "Caso " + caso + " posicion " + punto);
                }
            }
        }
    }

    @Test
    void mapaMedianoConAlcanceMillonarioNoExploraCoordenadasExternas() {
        int filas = 60;
        int columnas = 70;
        Mapa mapa = new Mapa("Prueba grande", "Luz", filas, columnas,
                new Posicion(0, 0), new Posicion(filas - 1, columnas - 1));
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                mapa.setCelda(fila, columna, new Celda("Terreno", true));
            }
        }
        Marine jugador = new Marine("Tecla", new Posicion(filas - 1, columnas - 1),
                new Mochila(4, 20), 3);
        Juego juego = new Juego(TestFixtures.consola(), mapa, jugador, 30);
        jugador.setLinternaActiva(true);
        jugador.setAlcanceLinterna(1_000_000);

        Set<Posicion> iluminadas = assertTimeoutPreemptively(Duration.ofSeconds(3),
                () -> SistemaIluminacion.precalcularIluminacion(juego));

        assertEquals(filas * columnas, iluminadas.size());
        assertTrue(iluminadas.contains(new Posicion(0, 0)));
        assertTrue(iluminadas.contains(new Posicion(filas - 1, columnas - 1)));
    }
}
