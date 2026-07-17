package com.legendoftecla.loader;

import java.util.ArrayList;
import java.util.List;

/** Modelo serializable del formato escenario.json utilizado por el editor. */
public class EscenarioDefinicion {
    public int version = 1;
    public String nombre = "Nuevo escenario";
    public String descripcion = "Escenario creado con el editor grafico";
    public int filas = 10;
    public int columnas = 10;
    public int pasosMaximos = 160;
    public Punto inicio = new Punto(0, 0);
    public Punto objetivo = new Punto(9, 9);
    public List<CeldaDef> celdas = new ArrayList<>();
    public List<PersonajeDef> enemigos = new ArrayList<>();
    public List<PersonajeDef> aliados = new ArrayList<>();
    public List<ObjetoDef> objetos = new ArrayList<>();

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

    public void normalizar() {
        if (nombre == null) nombre = "Escenario sin nombre";
        if (descripcion == null) descripcion = "";
        if (inicio == null) inicio = new Punto(0, 0);
        if (objetivo == null) objetivo = new Punto(Math.max(0, filas - 1), Math.max(0, columnas - 1));
        if (celdas == null) celdas = new ArrayList<>();
        if (enemigos == null) enemigos = new ArrayList<>();
        if (aliados == null) aliados = new ArrayList<>();
        if (objetos == null) objetos = new ArrayList<>();
        if (pasosMaximos <= 0) pasosMaximos = Math.max(80, filas * columnas * 2);
    }

    public CeldaDef celda(int fila, int columna) {
        return celdas.stream()
                .filter(c -> c.fila == fila && c.columna == columna)
                .findFirst()
                .orElse(null);
    }

    public static class Punto {
        public int fila;
        public int columna;

        public Punto() {
        }

        public Punto(int fila, int columna) {
            this.fila = fila;
            this.columna = columna;
        }
    }

    public static class CeldaDef extends Punto {
        public String descripcion = "Celda";
        public boolean transitable = true;

        public CeldaDef() {
        }

        public CeldaDef(int fila, int columna, String descripcion, boolean transitable) {
            super(fila, columna);
            this.descripcion = descripcion;
            this.transitable = transitable;
        }
    }

    public static class PersonajeDef extends Punto {
        public String tipo = "sectoid";
        public String nombre = "Personaje";
        public int salud = 70;
        public int energia = 70;
        public int vision = 2;

        public PersonajeDef() {
        }
    }

    public static class ObjetoDef extends Punto {
        public String tipo = "botiquin";
        public String nombre = "Objeto";
        public String descripcion = "Objeto del escenario";
        public double peso = 1.0;
        public int valor = 20;
        public int valorSecundario = 0;
        public int valorTerciario = 0;
        public boolean dosManos = false;

        public ObjetoDef() {
        }
    }
}
