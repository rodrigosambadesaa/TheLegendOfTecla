package com.legendoftecla.model.world;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LineaVisionSimetricaTest {
    @Test
    void visibilidadEsSimetricaEn129600ParesSobre100MapasAleatorios() {
        Random aleatorio = new Random(20261029L);
        assertTimeoutPreemptively(Duration.ofSeconds(15), () -> {
            for (int muestra = 0; muestra < 100; muestra++) {
                Mapa mapa = mapaLibre(6);
                for (int fila = 0; fila < 6; fila++) {
                    for (int columna = 0; columna < 6; columna++) {
                        mapa.getCelda(new Posicion(fila, columna))
                                .setTransitable(aleatorio.nextInt(4) != 0);
                    }
                }
                for (int filaA = 0; filaA < 6; filaA++) {
                    for (int colA = 0; colA < 6; colA++) {
                        Posicion a = new Posicion(filaA, colA);
                        for (int filaB = 0; filaB < 6; filaB++) {
                            for (int colB = 0; colB < 6; colB++) {
                                Posicion b = new Posicion(filaB, colB);
                                boolean ida = mapa.hayLineaVision(a, b);
                                boolean vuelta = mapa.hayLineaVision(b, a);
                                assertEquals(ida, vuelta, "muestra " + muestra
                                        + " entre " + a + " y " + b);
                            }
                        }
                    }
                }
            }
        });
    }

    @Test
    void paredEnPendienteOblicuaNoPermiteVerDesdeNingunExtremo() {
        Mapa mapa = mapaLibre(3);
        Posicion a = new Posicion(0, 1);
        Posicion b = new Posicion(2, 2);
        mapa.getCelda(new Posicion(1, 1)).setTransitable(false);
        mapa.getCelda(new Posicion(2, 1)).setTransitable(false);

        assertFalse(mapa.hayLineaVision(a, b));
        assertFalse(mapa.hayLineaVision(b, a));
    }

    @Test
    void esquinaCerradaEnDiagonalNoPermitePasoYUnaAperturaSi() {
        Mapa mapa = mapaLibre(3);
        Posicion a = new Posicion(0, 0);
        Posicion b = new Posicion(1, 1);
        mapa.getCelda(new Posicion(0, 1)).setTransitable(false);
        mapa.getCelda(new Posicion(1, 0)).setTransitable(false);

        assertFalse(mapa.hayLineaVision(a, b));
        assertFalse(mapa.hayLineaVision(b, a));

        mapa.getCelda(new Posicion(1, 0)).setTransitable(true);
        assertTrue(mapa.hayLineaVision(a, b));
        assertTrue(mapa.hayLineaVision(b, a));
    }

    @Test
    void bloqueosEnExtremosSeTratanDeFormaIndependienteDeLaDireccion() {
        Mapa mapa = mapaLibre(3);
        Posicion a = new Posicion(0, 0);
        Posicion b = new Posicion(0, 2);
        mapa.getCelda(a).setTransitable(false);

        assertFalse(mapa.hayLineaVision(a, b));
        assertFalse(mapa.hayLineaVision(b, a));
        mapa.getCelda(a).setTransitable(true);
        assertTrue(mapa.hayLineaVision(a, b));
        assertTrue(mapa.hayLineaVision(b, a));
    }

    private static Mapa mapaLibre(int lado) {
        Mapa mapa = new Mapa("Vision", "Regresion", lado, lado,
                new Posicion(0, 0), new Posicion(lado - 1, lado - 1));
        for (int fila = 0; fila < lado; fila++) {
            for (int columna = 0; columna < lado; columna++) {
                mapa.setCelda(fila, columna, new Celda("Libre", true));
            }
        }
        return mapa;
    }
}
