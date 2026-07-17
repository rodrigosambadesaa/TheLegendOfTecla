package com.legendoftecla.loader;

import java.util.ArrayList;
import java.util.List;

/** Modelo serializable del formato escenario.json utilizado por el editor. */
public class EscenarioDefinicion {
    /**
     * Crea una definicion de escenario con los valores predeterminados del editor.
     */
    public EscenarioDefinicion() {
        // Los valores editables se inicializan en sus declaraciones.
    }

    /**
     * Valor publico {@code version} utilizado por el modelo del juego.
     */
    public int version = 1;
    /**
     * Valor publico {@code nombre} utilizado por el modelo del juego.
     */
    public String nombre = "Nuevo escenario";
    /**
     * Valor publico {@code descripcion} utilizado por el modelo del juego.
     */
    public String descripcion = "Escenario creado con el editor grafico";
    /**
     * Valor publico {@code filas} utilizado por el modelo del juego.
     */
    public int filas = 10;
    /**
     * Valor publico {@code columnas} utilizado por el modelo del juego.
     */
    public int columnas = 10;
    /**
     * Valor publico {@code pasosMaximos} utilizado por el modelo del juego.
     */
    public int pasosMaximos = 160;
    /** Indica si el escenario propone activar aliados generados automaticamente. */
    public boolean conAliados = false;
    /** Punto inicial del jugador dentro del escenario. */
    public Punto inicio = new Punto(0, 0);
    /** Punto que el jugador debe alcanzar para completar el escenario. */
    public Punto objetivo = new Punto(9, 9);
    /**
     * Valor publico {@code celdas} utilizado por el modelo del juego.
     */
    public List<CeldaDef> celdas = new ArrayList<>();
    /**
     * Valor publico {@code enemigos} utilizado por el modelo del juego.
     */
    public List<PersonajeDef> enemigos = new ArrayList<>();
    /**
     * Valor publico {@code objetos} utilizado por el modelo del juego.
     */
    public List<ObjetoDef> objetos = new ArrayList<>();

    /**
     * Ejecuta la operacion publica {@code nuevo}.
      * @param columnas valor de {@code columnas}
      * @param filas valor de {@code filas}
      * @return resultado de la operacion
     */
    public static EscenarioDefinicion nuevo(int filas, int columnas) {
        EscenarioDefinicion escenario = new EscenarioDefinicion();
        escenario.filas = filas;
        escenario.columnas = columnas;
        escenario.inicio = new Punto(0, 0);
        escenario.objetivo = new Punto(filas - 1, columnas - 1);
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                escenario.celdas.add(new CeldaDef(fila, columna, "Celda " + fila + "," + columna, true));
            }
        }
        return escenario;
    }

    /**
     * Ejecuta la operacion publica {@code normalizar}.
     */
    public void normalizar() {
        if (nombre == null) nombre = "Escenario sin nombre";
        if (descripcion == null) descripcion = "";
        if (inicio == null) inicio = new Punto(0, 0);
        if (objetivo == null) objetivo = new Punto(Math.max(0, filas - 1), Math.max(0, columnas - 1));
        if (celdas == null) celdas = new ArrayList<>();
        if (enemigos == null) enemigos = new ArrayList<>();
        if (objetos == null) objetos = new ArrayList<>();
        if (pasosMaximos <= 0) pasosMaximos = Math.max(80, filas * columnas * 2);
    }

    /**
     * Ejecuta la operacion publica {@code celda}.
      * @param columna valor de {@code columna}
      * @param fila valor de {@code fila}
      * @return resultado de la operacion
     */
    public CeldaDef celda(int fila, int columna) {
        return celdas.stream()
                .filter(c -> c.fila == fila && c.columna == columna)
                .findFirst()
                .orElse(null);
    }

    /**
     * Representa {@code Punto} dentro del dominio del juego.
     */
    public static class Punto {
        /**
         * Valor publico {@code fila} utilizado por el modelo del juego.
         */
        public int fila;
        /**
         * Valor publico {@code columna} utilizado por el modelo del juego.
         */
        public int columna;

        /**
         * Ejecuta la operacion publica {@code Punto}.
         */
        public Punto() {
        }

        /**
         * Ejecuta la operacion publica {@code Punto}.
          * @param columna valor de {@code columna}
          * @param fila valor de {@code fila}
         */
        public Punto(int fila, int columna) {
            this.fila = fila;
            this.columna = columna;
        }
    }

    /**
     * Representa {@code CeldaDef} dentro del dominio del juego.
     */
    public static class CeldaDef extends Punto {
        /**
         * Valor publico {@code descripcion} utilizado por el modelo del juego.
         */
        public String descripcion = "Celda";
        /**
         * Valor publico {@code transitable} utilizado por el modelo del juego.
         */
        public boolean transitable = true;

        /**
         * Ejecuta la operacion publica {@code CeldaDef}.
         */
        public CeldaDef() {
        }

        /**
         * Ejecuta la operacion publica {@code CeldaDef}.
          * @param columna valor de {@code columna}
          * @param descripcion valor de {@code descripcion}
          * @param fila valor de {@code fila}
          * @param transitable valor de {@code transitable}
         */
        public CeldaDef(int fila, int columna, String descripcion, boolean transitable) {
            super(fila, columna);
            this.descripcion = descripcion;
            this.transitable = transitable;
        }
    }

    /**
     * Representa {@code PersonajeDef} dentro del dominio del juego.
     */
    public static class PersonajeDef extends Punto {
        /**
         * Valor publico {@code tipo} utilizado por el modelo del juego.
         */
        public String tipo = "sectoid";
        /**
         * Valor publico {@code nombre} utilizado por el modelo del juego.
         */
        public String nombre = "Personaje";
        /**
         * Valor publico {@code salud} utilizado por el modelo del juego.
         */
        public int salud = 70;
        /**
         * Valor publico {@code energia} utilizado por el modelo del juego.
         */
        public int energia = 70;
        /**
         * Valor publico {@code vision} utilizado por el modelo del juego.
         */
        public int vision = 2;

        /**
         * Ejecuta la operacion publica {@code PersonajeDef}.
         */
        public PersonajeDef() {
        }
    }

    /**
     * Representa {@code ObjetoDef} dentro del dominio del juego.
     */
    public static class ObjetoDef extends Punto {
        /**
         * Valor publico {@code tipo} utilizado por el modelo del juego.
         */
        public String tipo = "botiquin";
        /**
         * Valor publico {@code nombre} utilizado por el modelo del juego.
         */
        public String nombre = "Objeto";
        /**
         * Valor publico {@code descripcion} utilizado por el modelo del juego.
         */
        public String descripcion = "Objeto del escenario";
        /**
         * Valor publico {@code peso} utilizado por el modelo del juego.
         */
        public double peso = 1.0;
        /**
         * Valor publico {@code valor} utilizado por el modelo del juego.
         */
        public int valor = 20;
        /**
         * Valor publico {@code valorSecundario} utilizado por el modelo del juego.
         */
        public int valorSecundario = 0;
        /**
         * Valor publico {@code valorTerciario} utilizado por el modelo del juego.
         */
        public int valorTerciario = 0;
        /**
         * Valor publico {@code dosManos} utilizado por el modelo del juego.
         */
        public boolean dosManos = false;

        /**
         * Ejecuta la operacion publica {@code ObjetoDef}.
         */
        public ObjetoDef() {
        }
    }
}
