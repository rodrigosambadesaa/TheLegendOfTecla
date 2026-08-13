package com.legendoftecla.validation;

/** Limites defensivos compartidos por todas las capas del juego. */
public final class Limites {
    /** Longitud maxima de nombres, tipos y comandos almacenados. */
    public static final int TEXTO_CORTO = 100;
    /** Longitud maxima de una descripcion. */
    public static final int DESCRIPCION = 2_000;
    /** Valor maximo de salud, energia, vision, dano o bonificaciones. */
    public static final int ESTADISTICA = 1_000_000;
    /** Dimension minima admitida para cualquier mapa jugable. */
    public static final int MAPA_MINIMO = 3;
    /** Dimension maxima admitida para evitar reservas de memoria descontroladas. */
    public static final int MAPA_MAXIMO = 200;
    /** Numero maximo de pasos de una partida o escenario. */
    public static final int PASOS_MAXIMOS = 10_000_000;
    /** Capacidad maxima de objetos en una mochila. */
    public static final int CAPACIDAD_MOCHILA = 10_000;
    /** Peso maximo individual o acumulado admitido. */
    public static final double PESO_MAXIMO = 1_000_000.0;
    /** Valor absoluto maximo de una coordenada temporal. */
    public static final int COORDENADA_ABSOLUTA = 1_000_000;
    /** Longitud maxima de una salida completa de consola o GUI. */
    public static final int MENSAJE = 100_000;
    /** Numero maximo de mensajes retenidos por la consola grafica. */
    public static final int HISTORIAL_MENSAJES = 100_000;
    /** Numero maximo de aliados solicitables para preservar un turno procesable. */
    public static final int ALIADOS_MAXIMOS = 1_000;
    /** Nivel maximo configurable para un aliado generado. */
    public static final int NIVEL_ALIADO_MAXIMO = 100;

    private Limites() {
    }
}
