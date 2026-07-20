package com.legendoftecla.model.world;

import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.Collections;
import java.util.Set;


/**
 * Representa la entidad Mapa del juego.
 */
public class Mapa {
    private String nombre;
    private String descripcion;
    private Celda[][] celdas;
    private Posicion inicio;
    private Posicion objetivo;

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
        setNombre(nombre);
        setDescripcion(descripcion);
        int filasValidadas = Validaciones.enteroEntre(
                filas, Limites.MAPA_MINIMO, Limites.MAPA_MAXIMO, "Filas");
        int columnasValidadas = Validaciones.enteroEntre(
                columnas, Limites.MAPA_MINIMO, Limites.MAPA_MAXIMO, "Columnas");
        setCeldas(new Celda[filasValidadas][columnasValidadas]);
        setInicio(inicio);
        setObjetivo(objetivo);
    }

    /**
     * Ejecuta getNombre.
      * @return resultado de la operacion
     */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre nombre obligatorio y acotado */
    public void setNombre(String nombre) {
        this.nombre = Validaciones.textoObligatorio(nombre, "Nombre del mapa", Limites.TEXTO_CORTO);
    }

    /**
     * Ejecuta getDescripcion.
      * @return resultado de la operacion
     */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion descripcion no nula y acotada */
    public void setDescripcion(String descripcion) {
        this.descripcion = Validaciones.texto(descripcion, "Descripcion del mapa", Limites.DESCRIPCION);
    }

    /**
     * Ejecuta getInicio.
      * @return resultado de la operacion
     */
    public Posicion getInicio() {
        return copiarPosicion(inicio);
    }

    /** @param inicio posicion inicial dentro del mapa */
    public void setInicio(Posicion inicio) {
        this.inicio = copiarPosicion(validarPosicionInterna(Validaciones.noNulo(inicio, "Inicio")));
    }

    /**
     * Ejecuta getObjetivo.
      * @return resultado de la operacion
     */
    public Posicion getObjetivo() {
        return copiarPosicion(objetivo);
    }

    /** @param objetivo posicion objetivo dentro del mapa */
    public void setObjetivo(Posicion objetivo) {
        this.objetivo = copiarPosicion(validarPosicionInterna(Validaciones.noNulo(objetivo, "Objetivo")));
    }

    /**
     * Devuelve una copia de la matriz de celdas.
     *
     * @return matriz defensiva
     */
    public Celda[][] getCeldas() {
        Celda[][] copia = new Celda[celdas.length][];
        for (int fila = 0; fila < celdas.length; fila++) {
            copia[fila] = celdas[fila].clone();
        }
        return copia;
    }

    /**
     * Sustituye la matriz por una copia rectangular de dimensiones permitidas.
     *
     * @param celdas nueva matriz
     */
    public void setCeldas(Celda[][] celdas) {
        Validaciones.noNulo(celdas, "Celdas");
        Validaciones.enteroEntre(celdas.length,
                Limites.MAPA_MINIMO, Limites.MAPA_MAXIMO, "Filas");
        int columnas = celdas[0] == null ? 0 : celdas[0].length;
        Validaciones.enteroEntre(columnas,
                Limites.MAPA_MINIMO, Limites.MAPA_MAXIMO, "Columnas");
        Celda[][] copia = new Celda[celdas.length][columnas];
        if (inicio != null && !estaDentro(inicio, celdas.length, columnas)) {
            throw new IllegalArgumentException("La nueva matriz dejaria el inicio fuera del mapa.");
        }
        if (objetivo != null && !estaDentro(objetivo, celdas.length, columnas)) {
            throw new IllegalArgumentException("La nueva matriz dejaria el objetivo fuera del mapa.");
        }
        for (int fila = 0; fila < celdas.length; fila++) {
            if (celdas[fila] == null || celdas[fila].length != columnas) {
                throw new IllegalArgumentException("La matriz de celdas debe ser rectangular.");
            }
            copia[fila] = celdas[fila].clone();
        }
        this.celdas = copia;
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
        Validaciones.enteroEntre(fila, 0, getFilas() - 1, "Fila de la celda");
        Validaciones.enteroEntre(columna, 0, getColumnas() - 1, "Columna de la celda");
        celdas[fila][columna] = Validaciones.noNulo(celda, "Celda");
    }

    /**
     * Ejecuta getCelda.
      * @param posicion valor de {@code posicion}
      * @return resultado de la operacion
     */
    public Celda getCelda(Posicion posicion) {
        if (!estaDentro(posicion)) {
            throw new IllegalArgumentException("La posicion no pertenece al mapa: " + posicion);
        }
        return celdas[posicion.getFila()][posicion.getColumna()];
    }

    /**
     * Ejecuta estaDentro.
      * @param posicion valor de {@code posicion}
      * @return resultado de la operacion
     */
    public boolean estaDentro(Posicion posicion) {
        if (posicion == null) {
            return false;
        }
        return posicion.getFila() >= 0 && posicion.getFila() < getFilas()
                && posicion.getColumna() >= 0 && posicion.getColumna() < getColumnas();
    }

    /**
     * Ejecuta esTransitable.
      * @param posicion valor de {@code posicion}
      * @return resultado de la operacion
     */
    public boolean esTransitable(Posicion posicion) {
        return estaDentro(posicion) && getCelda(posicion) != null && getCelda(posicion).isTransitable();
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
        return renderAscii(jugador, Collections.emptySet(), Collections.emptySet(), Collections.emptySet());
    }

    /**
     * Ejecuta renderAscii.
      * @param enemigosVisibles valor de {@code enemigosVisibles}
      * @param jugador valor de {@code jugador}
      * @return resultado de la operacion
     */
    public String renderAscii(Posicion jugador, Set<Posicion> enemigosVisibles) {
        return renderAscii(jugador, enemigosVisibles, Collections.emptySet(), Collections.emptySet());
    }

    /**
     * Ejecuta renderAscii.
      * @param aliadosVisibles valor de {@code aliadosVisibles}
      * @param enemigosVisibles valor de {@code enemigosVisibles}
      * @param jugador valor de {@code jugador}
      * @return resultado de la operacion
     */
    public String renderAscii(Posicion jugador, Set<Posicion> enemigosVisibles, Set<Posicion> aliadosVisibles) {
        return renderAscii(jugador, enemigosVisibles, aliadosVisibles, Collections.emptySet());
    }

    /**
     * Renderiza el mapa mostrando objetos exclusivamente en celdas inspeccionadas.
     *
     * @param jugador posicion del jugador
     * @param enemigosVisibles posiciones de enemigos visibles
     * @param aliadosVisibles posiciones de aliados visibles
     * @param celdasInspeccionadas posiciones cuyos objetos ya conoce el jugador
     * @return representacion ASCII sin filtrar objetos ocultos
     */
    public String renderAscii(Posicion jugador, Set<Posicion> enemigosVisibles,
            Set<Posicion> aliadosVisibles, Set<Posicion> celdasInspeccionadas) {
        Validaciones.noNulo(jugador, "Posicion del jugador");
        Validaciones.noNulo(enemigosVisibles, "Enemigos visibles");
        Validaciones.noNulo(aliadosVisibles, "Aliados visibles");
        Validaciones.noNulo(celdasInspeccionadas, "Celdas inspeccionadas");
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
                } else if (!celdas[f][c].getObjetos().isEmpty()
                        && celdasInspeccionadas.contains(actual)) {
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

    private Posicion validarPosicionInterna(Posicion posicion) {
        if (posicion.getFila() < 0 || posicion.getFila() >= getFilas()
                || posicion.getColumna() < 0 || posicion.getColumna() >= getColumnas()) {
            throw new IllegalArgumentException("La posicion " + posicion + " queda fuera del mapa.");
        }
        return posicion;
    }

    private boolean estaDentro(Posicion posicion, int filas, int columnas) {
        return posicion.getFila() >= 0 && posicion.getFila() < filas
                && posicion.getColumna() >= 0 && posicion.getColumna() < columnas;
    }

    private Posicion copiarPosicion(Posicion posicion) {
        return new Posicion(posicion.getFila(), posicion.getColumna());
    }
}

