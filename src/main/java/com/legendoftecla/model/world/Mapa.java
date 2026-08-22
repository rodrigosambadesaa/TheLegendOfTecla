package com.legendoftecla.model.world;

import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.Collections;
import java.util.Set;


/**
 * Representa la entidad Mapa del juego.
 */
public class Mapa {
    private static final boolean MODO_DOCKER = new java.io.File("/.dockerenv").exists();
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
        int f0 = origen.getFila();
        int c0 = origen.getColumna();
        int f1 = destino.getFila();
        int c1 = destino.getColumna();
        if (f0 == f1 && c0 == c1) {
            return true;
        }
        int df = Integer.compare(f1, f0);
        int dc = Integer.compare(c1, c0);
        if (df != 0 && dc != 0) {
            return false;
        }
        int currF = f0 + df;
        int currC = c0 + dc;
        while (currF != f1 || currC != c1) {
            if (celdas[currF][currC].bloqueaVision()) {
                return false;
            }
            currF += df;
            currC += dc;
        }
        return !celdas[f1][c1].bloqueaVision();
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
        Set<Posicion> iluminadas = new java.util.HashSet<>();
        for (int f = 0; f < getFilas(); f++) {
            for (int c = 0; c < getColumnas(); c++) {
                Celda celda = celdas[f][c];
                if (!celda.isOscura() || celda.estaArdiendo() || celda.hasAntorchaMural()) {
                    iluminadas.add(new Posicion(f, c));
                }
            }
        }
        return renderAscii(jugador, enemigosVisibles, aliadosVisibles, celdasInspeccionadas, iluminadas);
    }

    /** Renderiza tambien el estado ambiental y las zonas iluminadas dinamicamente. */
    public String renderAscii(Posicion jugador, Set<Posicion> enemigosVisibles,
            Set<Posicion> aliadosVisibles, Set<Posicion> celdasInspeccionadas,
            Set<Posicion> celdasIluminadas) {
        Validaciones.noNulo(jugador, "Posicion del jugador");
        Validaciones.noNulo(enemigosVisibles, "Enemigos visibles");
        Validaciones.noNulo(aliadosVisibles, "Aliados visibles");
        Validaciones.noNulo(celdasInspeccionadas, "Celdas inspeccionadas");
        Validaciones.noNulo(celdasIluminadas, "Celdas iluminadas");
        
        int filas = getFilas();
        int columnas = getColumnas();
        boolean[][] iluminadas = new boolean[filas][columnas];
        for (Posicion p : celdasIluminadas) {
            if (estaDentro(p, filas, columnas)) iluminadas[p.getFila()][p.getColumna()] = true;
        }
        boolean[][] enemigosV = new boolean[filas][columnas];
        for (Posicion p : enemigosVisibles) {
            if (estaDentro(p, filas, columnas)) enemigosV[p.getFila()][p.getColumna()] = true;
        }
        boolean[][] aliadosV = new boolean[filas][columnas];
        for (Posicion p : aliadosVisibles) {
            if (estaDentro(p, filas, columnas)) aliadosV[p.getFila()][p.getColumna()] = true;
        }
        boolean[][] inspeccionadas = new boolean[filas][columnas];
        for (Posicion p : celdasInspeccionadas) {
            if (estaDentro(p, filas, columnas)) inspeccionadas[p.getFila()][p.getColumna()] = true;
        }
        
        boolean modoDocker = MODO_DOCKER;

        int charsPorCelda = modoDocker ? 2 : 2;
        StringBuilder sb = new StringBuilder(filas * (columnas * charsPorCelda + 1));
        for (int f = 0; f < filas; f++) {
            for (int c = 0; c < columnas; c++) {
                boolean esJugador = (f == jugador.getFila() && c == jugador.getColumna());
                boolean esObjetivo = (objetivo != null && f == objetivo.getFila() && c == objetivo.getColumna());
                Celda celda = celdas[f][c];
                
                if (modoDocker) {
                    String tile = "⬜";
                    if (esJugador) {
                        tile = "👤";
                    } else if (celda.estaArdiendo()) {
                        tile = "🔥";
                    } else if (!iluminadas[f][c]) {
                        tile = "⬛";
                    } else if (esObjetivo) {
                        tile = "🎯";
                    } else if (celda.simboloElemento() != 0) {
                        char sim = celda.simboloElemento();
                        if (sim == '+' || sim == '/') tile = "🚪";
                        else if (sim == '^') tile = "🕳️";
                        else if (sim == 'C') tile = "🧰";
                        else if (sim == 'O') tile = "🛢️";
                        else if (sim == '=') tile = "🧱";
                        else tile = String.valueOf(sim) + " ";
                    } else if (!celda.isTransitable()) {
                        tile = "🧱";
                    } else if (celda.hasEnemigos() && enemigosV[f][c]) {
                        tile = "🧟";
                    } else if (celda.hasAliados() && aliadosV[f][c]) {
                        tile = "👮";
                    } else if (celda.hasObjetos() && inspeccionadas[f][c]) {
                        tile = "🎁";
                    } else if (celda.hasFuenteAgua()) {
                        tile = "💧";
                    } else if (celda.hasAntorchaMural()) {
                        tile = "🕯️";
                    } else if (celda.getTipoSuelo() == TipoSuelo.MADERA) {
                        tile = "🟫";
                    }
                    sb.append(tile);
                } else {
                    if (esJugador) {
                        sb.append('J');
                    } else if (celda.estaArdiendo()) {
                        sb.append('F');
                    } else if (!iluminadas[f][c]) {
                        sb.append('?');
                    } else if (esObjetivo) {
                        sb.append('X');
                    } else if (celda.simboloElemento() != 0) {
                        sb.append(celda.simboloElemento());
                    } else if (!celda.isTransitable()) {
                        sb.append('#');
                    } else if (celda.hasEnemigos() && enemigosV[f][c]) {
                        sb.append('E');
                    } else if (celda.hasAliados() && aliadosV[f][c]) {
                        sb.append('A');
                    } else if (celda.hasObjetos() && inspeccionadas[f][c]) {
                        sb.append('o');
                    } else if (celda.hasFuenteAgua()) {
                        sb.append('U');
                    } else if (celda.hasAntorchaMural()) {
                        sb.append('T');
                    } else if (celda.getTipoSuelo() == TipoSuelo.MADERA) {
                        sb.append('=');
                    } else {
                        sb.append('.');
                    }
                    sb.append(' ');
                }
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

