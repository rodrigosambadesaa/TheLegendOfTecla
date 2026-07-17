package com.legendoftecla.model.world;

import java.util.Collections;
import java.util.Set;


/**
 * Representa la entidad Mapa del juego.
 */
public class Mapa {
    private final String nombre;
    private final String descripcion;
    private final Celda[][] celdas;
    private final Posicion inicio;
    private final Posicion objetivo;

    /**
     * Ejecuta Mapa.
     */
    public Mapa(String nombre, String descripcion, int filas, int columnas, Posicion inicio, Posicion objetivo) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.celdas = new Celda[filas][columnas];
        this.inicio = inicio;
        this.objetivo = objetivo;
    }

    /**
     * Ejecuta getNombre.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Ejecuta getDescripcion.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Ejecuta getInicio.
     */
    public Posicion getInicio() {
        return inicio;
    }

    /**
     * Ejecuta getObjetivo.
     */
    public Posicion getObjetivo() {
        return objetivo;
    }

    /**
     * Ejecuta getFilas.
     */
    public int getFilas() {
        return celdas.length;
    }

    /**
     * Ejecuta getColumnas.
     */
    public int getColumnas() {
        return celdas[0].length;
    }

    /**
     * Ejecuta setCelda.
     */
    public void setCelda(int fila, int columna, Celda celda) {
        celdas[fila][columna] = celda;
    }

    /**
     * Ejecuta getCelda.
     */
    public Celda getCelda(Posicion posicion) {
        return celdas[posicion.getFila()][posicion.getColumna()];
    }

    /**
     * Ejecuta estaDentro.
     */
    public boolean estaDentro(Posicion posicion) {
        return posicion.getFila() >= 0 && posicion.getFila() < getFilas()
                && posicion.getColumna() >= 0 && posicion.getColumna() < getColumnas();
    }

    /**
     * Ejecuta esTransitable.
     */
    public boolean esTransitable(Posicion posicion) {
        return estaDentro(posicion) && getCelda(posicion).isTransitable();
    }

    /**
     * Ejecuta hayLineaAtaque.
     */
    public boolean hayLineaAtaque(Posicion origen, Posicion destino) {
        if (!estaDentro(origen) || !estaDentro(destino)) {
            return false;
        }
        if (origen.equals(destino)) {
            return true;
        }
        int df = Integer.compare(destino.getFila(), origen.getFila());
        int dc = Integer.compare(destino.getColumna(), origen.getColumna());
        if (df != 0 && dc != 0) {
            return false;
        }
        Posicion cursor = new Posicion(origen.getFila() + df, origen.getColumna() + dc);
        while (!cursor.equals(destino)) {
            if (!esTransitable(cursor)) {
                return false;
            }
            cursor = new Posicion(cursor.getFila() + df, cursor.getColumna() + dc);
        }
        return esTransitable(destino);
    }

    /**
     * Ejecuta renderAscii.
     */
    public String renderAscii(Posicion jugador) {
        return renderAscii(jugador, Collections.emptySet(), Collections.emptySet());
    }

    /**
     * Ejecuta renderAscii.
     */
    public String renderAscii(Posicion jugador, Set<Posicion> enemigosVisibles) {
        return renderAscii(jugador, enemigosVisibles, Collections.emptySet());
    }

    /**
     * Ejecuta renderAscii.
     */
    public String renderAscii(Posicion jugador, Set<Posicion> enemigosVisibles, Set<Posicion> aliadosVisibles) {
        StringBuilder sb = new StringBuilder();
        for (int f = 0; f < getFilas(); f++) {
            for (int c = 0; c < getColumnas(); c++) {
                Posicion actual = new Posicion(f, c);
                if (actual.equals(jugador)) {
                    sb.append('J');
                } else if (actual.equals(objetivo)) {
                    sb.append('X');
                } else if (!celdas[f][c].isTransitable()) {
                    sb.append('#');
                } else if (!celdas[f][c].getEnemigos().isEmpty() && enemigosVisibles.contains(actual)) {
                    sb.append('E');
                } else if (!celdas[f][c].getAliados().isEmpty() && aliadosVisibles.contains(actual)) {
                    sb.append('A');
                } else if (!celdas[f][c].getObjetos().isEmpty()) {
                    sb.append('o');
                } else {
                    sb.append('.');
                }
                sb.append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}

