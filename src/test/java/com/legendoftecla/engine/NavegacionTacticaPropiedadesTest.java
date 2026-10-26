package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NavegacionTacticaPropiedadesTest {
    @Test
    void contrastaCacheConBuscadorTacticoEnMilesDeRutasYObstaculos() {
        Random random = new Random(20261026);
        for (int escenario = 0; escenario < 100; escenario++) {
            Mapa mapa = TestFixtures.juegoBasico(TestFixtures.consola()).getMapa();
            for (int fila = 0; fila < mapa.getFilas(); fila++) {
                for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                    mapa.getCelda(new Posicion(fila, columna))
                            .setTransitable(random.nextInt(5) != 0);
                }
            }
            CacheNavegacion cache = new CacheNavegacion(mapa);
            for (int fila = 0; fila < mapa.getFilas(); fila++) {
                for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                    for (int destinoFila = 0; destinoFila < mapa.getFilas(); destinoFila++) {
                        for (int destinoColumna = 0;
                                destinoColumna < mapa.getColumnas(); destinoColumna++) {
                            Posicion origen = new Posicion(fila, columna);
                            Posicion destino = new Posicion(destinoFila, destinoColumna);
                            assertEquals(NavegacionTactica.distancia(mapa, origen, destino),
                                    cache.distancia(origen, destino));
                            assertEquals(NavegacionTactica.primerPaso(mapa, origen, destino),
                                    cache.primerPaso(origen, destino));
                        }
                    }
                }
            }
        }
    }

    @Test
    void devuelveInaccesibleSiLaPosicionEsExternaOIntransitable() {
        Mapa mapa = TestFixtures.juegoBasico(TestFixtures.consola()).getMapa();
        CacheNavegacion cache = new CacheNavegacion(mapa);
        Posicion externa = new Posicion(1_000_000, 1_000_000);
        Posicion valida = new Posicion(0, 0);

        assertEquals(-1, NavegacionTactica.distancia(mapa, externa, valida));
        assertEquals(-1, cache.distancia(externa, valida));
        assertNull(NavegacionTactica.primerPaso(mapa, externa, valida));
        assertNull(cache.primerPaso(externa, valida));
        assertEquals(-1, NavegacionTactica.distancia(mapa, externa, externa));
        assertEquals(-1, cache.distancia(externa, externa));

        mapa.getCelda(valida).setTransitable(false);
        assertEquals(-1, NavegacionTactica.distancia(mapa, valida, valida));
        assertEquals(-1, cache.distancia(valida, valida));
        assertNull(NavegacionTactica.primerPaso(mapa, valida, new Posicion(1, 1)));
        assertNull(cache.primerPaso(valida, new Posicion(1, 1)));
    }

    @Test
    void origenMutableNoAlteraUnaClaveGuardadaNiLosResultados() {
        Mapa mapa = TestFixtures.juegoBasico(TestFixtures.consola()).getMapa();
        CacheNavegacion cache = new CacheNavegacion(mapa);
        Posicion origenMutable = new Posicion(0, 0);
        Posicion objetivo = new Posicion(2, 2);

        int distanciaOriginal = cache.distancia(origenMutable, objetivo);
        origenMutable.setFila(2);
        origenMutable.setColumna(2);

        assertEquals(4, distanciaOriginal);
        assertEquals(0, cache.distancia(origenMutable, objetivo));
        assertEquals(distanciaOriginal, cache.distancia(new Posicion(0, 0), objetivo));
    }

    @Test
    void validaArgumentosNulos() {
        Mapa mapa = TestFixtures.juegoBasico(TestFixtures.consola()).getMapa();
        CacheNavegacion cache = new CacheNavegacion(mapa);
        Posicion valida = new Posicion(0, 0);

        assertThrows(NullPointerException.class, () -> cache.distancia(null, valida));
        assertThrows(NullPointerException.class, () -> cache.distancia(valida, null));
        assertThrows(NullPointerException.class, () -> cache.primerPaso(null, valida));
        assertThrows(NullPointerException.class, () -> cache.primerPaso(valida, null));
    }
}
