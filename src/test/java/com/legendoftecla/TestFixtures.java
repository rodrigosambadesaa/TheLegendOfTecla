package com.legendoftecla;

import com.legendoftecla.console.Consola;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

/** Utilidades de prueba compartidas para crear partidas mínimas y deterministas. */
public final class TestFixtures {
    private TestFixtures() {
    }

    public static CapturingConsole consola() {
        return new CapturingConsole();
    }

    public static Juego juegoBasico(CapturingConsole consola) {
        Mapa mapa = new Mapa("Prueba", "Mapa de prueba", 3, 3,
                new Posicion(0, 0), new Posicion(2, 2));
        for (int fila = 0; fila < 3; fila++) {
            for (int columna = 0; columna < 3; columna++) {
                mapa.setCelda(fila, columna, new Celda("Celda " + fila + "," + columna, true));
            }
        }
        Marine jugador = new Marine("Tecla", new Posicion(0, 0), new Mochila(4, 20), 2);
        return new Juego(consola, mapa, jugador, 30);
    }

    public static final class CapturingConsole implements Consola {
        private final StringBuilder salida = new StringBuilder();

        @Override
        public void imprimir(String mensaje) {
            salida.append(mensaje).append('\n');
        }

        @Override
        public void imprimir(String mensaje, TipoMensaje tipo) {
            salida.append('[').append(tipo).append("] ").append(mensaje).append('\n');
        }

        @Override
        public String leer(String descripcion) {
            return "";
        }

        public String salida() {
            return salida.toString();
        }

        public void limpiar() {
            salida.setLength(0);
        }
    }
}
