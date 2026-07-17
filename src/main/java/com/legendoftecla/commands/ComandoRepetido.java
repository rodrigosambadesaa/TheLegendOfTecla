package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;


/**
 * Representa la entidad ComandoRepetido del juego.
 */
public class ComandoRepetido implements Comando {
    private final Comando comando;
    private final int repeticiones;

    /**
     * Ejecuta ComandoRepetido.
      * @param comando valor de {@code comando}
      * @param repeticiones valor de {@code repeticiones}
     */
    public ComandoRepetido(Comando comando, int repeticiones) {
        this.comando = comando;
        this.repeticiones = repeticiones;
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        for (int i = 0; i < repeticiones; i++) {
            comando.ejecutar();
        }
    }
}

