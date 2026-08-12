package com.legendoftecla.model.characters;

import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Validaciones;

/**
 * Representa la entidad Enemigo del juego.
 */
public abstract class Enemigo extends Personaje {
    private static double multiplicadorDanioGlobal = 1.0;
    private int rangoAudicion;
    private final com.legendoftecla.ai.ControladorIA controladorIA;

    /**
     * Ejecuta Enemigo.
      * @param energia valor de {@code energia}
      * @param mochila valor de {@code mochila}
      * @param nombre valor de {@code nombre}
      * @param posicion valor de {@code posicion}
      * @param salud valor de {@code salud}
      * @param visionBase valor de {@code visionBase}
     */
    protected Enemigo(String nombre, int salud, int energia, Posicion posicion, Mochila mochila, int visionBase) {
        super(nombre, salud, energia, posicion, mochila, visionBase);
        rangoAudicion = 6;
        controladorIA = new com.legendoftecla.ai.ControladorIA(this);
    }

    @Override
    /**
     * Ejecuta aplicarModificadorDanio.
     */
    protected int aplicarModificadorDanio(int base, Personaje objetivo) {
        return Math.max(1, (int) Math.round(base * multiplicadorDanioGlobal));
    }

    /**
     * Ejecuta la operacion publica {@code setMultiplicadorDanioGlobal}.
      * @param multiplicador valor de {@code multiplicador}
     */
    public static void setMultiplicadorDanioGlobal(double multiplicador) {
        multiplicadorDanioGlobal = Validaciones.decimalEntre(
                multiplicador, 0.1, 100.0, "Multiplicador de dano enemigo");
    }

    /** @return multiplicador global aplicado al dano enemigo */
    public static double getMultiplicadorDanioGlobal() {
        return multiplicadorDanioGlobal;
    }

    /** @return alcance auditivo base */
    public int getRangoAudicion() { return rangoAudicion; }
    /** @param rangoAudicion alcance no negativo */
    public void setRangoAudicion(int rangoAudicion) {
        if (rangoAudicion < 0) throw new IllegalArgumentException("Audicion invalida");
        this.rangoAudicion = rangoAudicion;
    }
    /** @return controlador State/Strategy propio */
    public com.legendoftecla.ai.ControladorIA getControladorIA() { return controladorIA; }
}
