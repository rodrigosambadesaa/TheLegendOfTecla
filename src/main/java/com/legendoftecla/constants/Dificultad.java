package com.legendoftecla.constants;

/**
 * Configuracion de dificultad global de la partida.
 */
public enum Dificultad {
    MUY_FACIL("muy facil", 0.50, 0.70, 0.65),
    FACIL("facil", 0.75, 0.85, 0.85),
    NORMAL("normal", 1.00, 1.00, 1.00),
    DIFICIL("dificil", 1.25, 1.20, 1.20),
    MUY_DIFICIL("muy dificil", 1.50, 1.40, 1.40),
    PESADILLA("pesadilla", 1.80, 1.65, 1.70),
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

    public String getEtiqueta() {
        return etiqueta;
    }

    public double getMultiplicadorEnemigos() {
        return multiplicadorEnemigos;
    }

    public double getMultiplicadorSaludEnemigo() {
        return multiplicadorSaludEnemigo;
    }

    public double getMultiplicadorDanioEnemigo() {
        return multiplicadorDanioEnemigo;
    }

    public int ajustarCantidadEnemigos(int base) {
        if (base <= 0) {
            return 0;
        }
        return Math.max(1, (int) Math.round(base * multiplicadorEnemigos));
    }

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