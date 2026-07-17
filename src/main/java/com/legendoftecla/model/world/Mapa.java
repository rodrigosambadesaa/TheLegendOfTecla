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
      * @param columnas valor de {@code columnas}
      * @param descripcion valor de {@code descripcion}
      * @param filas valor de {@code filas}
      * @param inicio valor de {@code inicio}
      * @param nombre valor de {@code nombre}
      * @param objetivo valor de {@code objetivo}
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
      * @return resultado de la operacion
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Ejecuta getDescripcion.
      * @return resultado de la operacion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Ejecuta getInicio.
      * @return resultado de la operacion
     */
    public Posicion getInicio() {
        return inicio;
    }

    /**
     * Ejecuta getObjetivo.
      * @return resultado de la operacion
     */
    public Posicion getObjetivo() {
        return objetivo;
    }

    /**
     * Ejecuta getFilas.
      * @return resultado de la operacion
     */
    public int getFilas() {
        return celdas.length;
    }

    /**
     * Ejecuta getColumnas.
      * @return resultado de la operacion
     */
    public int getColumnas() {
        return celdas[0].length;
    }

    /**
     * Ejecuta setCelda.
      * @param celda valor de {@code celda}
      * @param columna valor de {@code columna}
      * @param fila valor de {@code fila}
     */
    public void setCelda(int fila, int columna, Celda celda) {
        celdas[fila][columna] = celda;
    }

    /**
     * Ejecuta getCelda.
      * @param posicion valor de {@code posicion}
      * @return resultado de la operacion
     */
    public Celda getCelda(Posicion posicion) {
        return celdas[posicion.getFila()][posicion.getColumna()];
    }

    /**
     * Ejecuta estaDentro.
      * @param posicion valor de {@code posicion}
      * @return resultado de la operacion
     */
    public boolean estaDentro(Posicion posicion) {
        return posicion.getFila() >= 0 && posicion.getFila() < getFilas()
                && posicion.getColumna() >= 0 && posicion.getColumna() < getColumnas();
    }

    /**
     * Ejecuta esTransitable.
      * @param posicion valor de {@code posicion}
      * @return resultado de la operacion
     */
    public boolean esTransitable(Posicion posicion) {
        return estaDentro(posicion) && getCelda(posicion).isTransitable();
    }

    /**
     * Ejecuta hayLineaAtaque.
      * @param destino valor de {@code destino}
      * @param origen valor de {@code origen}
      * @return resultado de la operacion
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
      * @param jugador valor de {@code jugador}
      * @return resultado de la operacion
     */
    public String renderAscii(Posicion jugador) {
        return renderAscii(jugador, Collections.emptySet(), Collections.emptySet());
    }

    /**
     * Ejecuta renderAscii.
      * @param enemigosVisibles valor de {@code enemigosVisibles}
      * @param jugador valor de {@code jugador}
      * @return resultado de la operacion
     */
    public String renderAscii(Posicion jugador, Set<Posicion> enemigosVisibles) {
        return renderAscii(jugador, enemigosVisibles, Collections.emptySet());
    }

    /**
     * Ejecuta renderAscii.
      * @param aliadosVisibles valor de {@code aliadosVisibles}
      * @param enemigosVisibles valor de {@code enemigosVisibles}
      * @param jugador valor de {@code jugador}
      * @return resultado de la operacion
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

