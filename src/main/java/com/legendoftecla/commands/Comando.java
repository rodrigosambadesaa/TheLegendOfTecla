package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;


/**
 * Representa la entidad Comando del juego.
 */
public interface Comando {
    void ejecutar() throws ComandoException;
}

