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
        this.salir = false;
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() {
        salir = true;
    }

    /**
     * Ejecuta isSalir.
     */
    public boolean isSalir() {
        return salir;
    }
}

