package com.legendoftecla.constants;

/**
 * Configuracion de dificultad global de la partida.
 */
public enum Dificultad {
    /**
     * Opcion {@code MUY_FACIL}.
     */
    MUY_FACIL("muy facil", 0.50, 0.70, 0.65),
    /**
     * Opcion {@code FACIL}.
     */
    FACIL("facil", 0.75, 0.85, 0.85),
    /**
     * Opcion {@code NORMAL}.
     */
    NORMAL("normal", 1.00, 1.00, 1.00),
    /**
     * Opcion {@code DIFICIL}.
     */
    DIFICIL("dificil", 1.25, 1.20, 1.20),
    /**
     * Opcion {@code MUY_DIFICIL}.
     */
    MUY_DIFICIL("muy dificil", 1.50, 1.40, 1.40),
    /**
     * Opcion {@code PESADILLA}.
     */
    PESADILLA("pesadilla", 1.80, 1.65, 1.70),
    /**
     * Opcion {@code DEMENTE}.
     */
    DEMENTE("demente", 2.20, 2.00, 2.20);

    private final String etiqueta;
    private final double multiplicadorEnemigos;
    private final double multiplicadorSaludEnemigo;
    private final double multiplicadorDanioEnemigo;

    Dificultad(String etiqueta, double multiplicadorEnemigos, double multiplicadorSaludEnemigo,
            double multiplicadorDanioEnemigo) {
        this.etiqueta = etiqueta;
        this.multiplicadorEnemigos = multiplicadorEnemigos;
        this.multiplicadorSaludEnemigo = multiplicadorSaludEnemigo;
        this.multiplicadorDanioEnemigo = multiplicadorDanioEnemigo;
    }

    /**
     * Obtiene el valor de {@code Etiqueta}.
      * @return resultado de la operacion
     */
    public String getEtiqueta() {
        return etiqueta;
    }

    /**
     * Obtiene el valor de {@code MultiplicadorEnemigos}.
      * @return resultado de la operacion
     */
    public double getMultiplicadorEnemigos() {
        return multiplicadorEnemigos;
    }

    /**
     * Obtiene el valor de {@code MultiplicadorSaludEnemigo}.
      * @return resultado de la operacion
     */
    public double getMultiplicadorSaludEnemigo() {
        return multiplicadorSaludEnemigo;
    }

    /**
     * Obtiene el valor de {@code MultiplicadorDanioEnemigo}.
      * @return resultado de la operacion
     */
    public double getMultiplicadorDanioEnemigo() {
        return multiplicadorDanioEnemigo;
    }

    /**
     * Ejecuta la operacion publica {@code ajustarCantidadEnemigos}.
      * @param base valor de {@code base}
      * @return resultado de la operacion
     */
    public int ajustarCantidadEnemigos(int base) {
        if (base <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(base * multiplicadorEnemigos));
    }

    /**
     * Calcula cuantos botiquines y cuantos Toritos adicionales recibe un mapa.
     * Las dificultades normales y superiores conservan la distribucion original.
     *
     * @param cantidadCeldas superficie total del mapa
     * @return cantidad adicional de cada tipo de suministro
     */
    public int calcularSuministrosExtra(int cantidadCeldas) {
        if (cantidadCeldas <= 0) {
            return 0;
        }
        return switch (this) {
            case MUY_FACIL -> Math.max(4, (int) Math.ceil(cantidadCeldas / 25.0));
            case FACIL -> Math.max(2, (int) Math.ceil(cantidadCeldas / 50.0));
            default -> 0;
        };
    }

    /**
     * Calcula paquetes de municion de apoyo: cuanto menor la dificultad, mas reservas.
     *
     * @param cantidadCeldas superficie total del mapa
     * @return paquetes adicionales de rifle
     */
    public int calcularMunicionExtra(int cantidadCeldas) {
        if (cantidadCeldas <= 0) {
            return 0;
        }
        return switch (this) {
            case MUY_FACIL -> Math.max(4, (int) Math.ceil(cantidadCeldas / 50.0));
            case FACIL -> Math.max(3, (int) Math.ceil(cantidadCeldas / 80.0));
            case NORMAL -> 2;
            default -> 1;
        };
    }

    /**
     * Ejecuta la operacion publica {@code desdeTexto}.
      * @param texto valor de {@code texto}
      * @return resultado de la operacion
     */
    public static Dificultad desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) {
            return NORMAL;
        }
        String normalizado = texto.trim().toLowerCase();
        return switch (normalizado) {
            case "muyfacil", "muy_facil", "muy facil" -> MUY_FACIL;
            case "facil" -> FACIL;
            case "normal" -> NORMAL;
            case "dificil" -> DIFICIL;
            case "muydificil", "muy_dificil", "muy dificil" -> MUY_DIFICIL;
            case "pesadilla" -> PESADILLA;
            case "demente" -> DEMENTE;
            default -> null;
        };
    }
}
