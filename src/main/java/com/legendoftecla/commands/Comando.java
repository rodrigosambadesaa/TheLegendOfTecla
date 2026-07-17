package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;


/**
 * Representa la entidad Comando del juego.
 */
public interface Comando {
    /**
     * Ejecuta la operacion publica {@code ejecutar}.
      * @throws com.legendoftecla.exceptions.ComandoException si la operacion no puede completarse
     */
    void ejecutar() throws ComandoException;
}

