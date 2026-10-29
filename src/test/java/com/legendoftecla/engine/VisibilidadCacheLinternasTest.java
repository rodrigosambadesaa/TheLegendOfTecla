package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisibilidadCacheLinternasTest {
    @Test
    void iluminacionCacheadaCoincideConConsultaOriginalEnMapaOscuro() {
        Juego juego = mapaOscuro(12);
        juego.getJugador().setLinternaActiva(true);
        juego.getJugador().setAlcanceLinterna(4);
        Aliado aliado = aliado("Explorador", new Posicion(8, 8), 2, true);
        juego.agregarAliado(aliado);
        MotorPartida motor = new MotorPartida(juego);

        assertEquals(iluminadasPorConsultaDirecta(juego), motor.getCeldasIluminadas());

        juego.getJugador().setLinternaActiva(false);
        aliado.setSalud(0);
        assertEquals(iluminadasPorConsultaDirecta(juego), motor.getCeldasIluminadas());
        assertFalse(motor.getCeldasIluminadas().contains(new Posicion(8, 8)));

        aliado.setSalud(25);
        assertEquals(iluminadasPorConsultaDirecta(juego), motor.getCeldasIluminadas());
        assertTrue(motor.getCeldasIluminadas().contains(new Posicion(8, 8)));
    }

    @Test
    void visibilidadDeAliadosYEnemigosCoincideConLuzRealYLineaVisual() {
        Juego juego = mapaOscuro(12);
        juego.getJugador().setLinternaActiva(true);
        juego.getJugador().setAlcanceLinterna(4);
        Aliado cerca = aliado("Cerca", new Posicion(0, 2), 0, false);
        Aliado lejos = aliado("Lejos", new Posicion(10, 10), 0, false);
        juego.agregarAliado(cerca);
        juego.agregarAliado(lejos);
        Sectoid visible = new Sectoid("Visible", new Posicion(0, 3),
                new Mochila(1, 1), 4);
        Sectoid oculto = new Sectoid("Oculto", new Posicion(11, 11),
                new Mochila(1, 1), 4);
        juego.agregarEnemigo(visible);
        juego.agregarEnemigo(oculto);
        MotorPartida motor = new MotorPartida(juego);

        assertEquals(Set.of(cerca.getPosicion()), motor.getAliadosVisibles());
        assertEquals(Set.of(visible.getPosicion()), motor.getEnemigosVisibles());

        juego.getJugador().setLinternaActiva(false);
        assertTrue(motor.getAliadosVisibles().isEmpty());
        assertTrue(motor.getEnemigosVisibles().isEmpty());
    }

    @Test
    void mapasConMuchosAliadosNoRecalculanLinternasPorCadaCelda() {
        Juego juego = mapaOscuro(40);
        for (int i = 0; i < 240; i++) {
            Aliado aliado = aliado("A" + i, new Posicion(i / 40, i % 40),
                    i % 4, i % 3 == 0);
            juego.agregarAliado(aliado);
        }
        MotorPartida motor = new MotorPartida(juego);
        Set<Posicion> esperado = iluminadasPorConsultaDirecta(juego);

        assertTimeoutPreemptively(Duration.ofSeconds(8), () -> {
            for (int i = 0; i < 6; i++) {
                assertEquals(esperado, motor.getCeldasIluminadas());
            }
        });
    }

    private static Set<Posicion> iluminadasPorConsultaDirecta(Juego juego) {
        Set<Posicion> posiciones = new HashSet<>();
        Mapa mapa = juego.getMapa();
        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                Posicion p = new Posicion(fila, columna);
                if (SistemaIluminacion.hayLuz(juego, p)) posiciones.add(p);
            }
        }
        return posiciones;
    }

    private static Juego mapaOscuro(int lado) {
        Mapa mapa = new Mapa("Oscuridad", "Iluminacion", lado, lado,
                new Posicion(0, 0), new Posicion(lado - 1, lado - 1));
        for (int fila = 0; fila < lado; fila++) {
            for (int columna = 0; columna < lado; columna++) {
                Celda celda = new Celda("Suelo", true);
                celda.setOscura(true);
                mapa.setCelda(fila, columna, celda);
            }
        }
        Marine jugador = new Marine("Tecla", new Posicion(0, 0),
                new Mochila(3, 10), 10);
        return new Juego(TestFixtures.consola(), mapa, jugador, 30);
    }

    private static Aliado aliado(String nombre, Posicion posicion, int radio, boolean activa) {
        Aliado aliado = new Aliado(nombre, posicion, new Mochila(1, 1), 3);
        aliado.setAlcanceLinterna(radio);
        aliado.setLinternaActiva(activa);
        return aliado;
    }
}
