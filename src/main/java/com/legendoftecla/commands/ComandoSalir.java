package com.legendoftecla.commands;


/**
 * Representa la entidad ComandoSalir del juego.
 */
public class ComandoSalir implements Comando {
    private boolean salir;

    /**
     * Ejecuta ComandoSalir.
     */
    public ComandoSalir() {
        setSalir(false);
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() {
        setSalir(true);
    }

    /**
     * Ejecuta isSalir.
      * @return resultado de la operacion
     */
    public boolean isSalir() {
        return salir;
    }

    /** @param salir estado de salida solicitado */
    public void setSalir(boolean salir) {
        this.salir = salir;
    }
}

