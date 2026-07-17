package com.legendoftecla.loader;

import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.world.Juego;


/**
 * Representa la entidad CargadorJuego del juego.
 */
public interface CargadorJuego {
    /**
     * Ejecuta la operacion publica {@code cargarJuego}.
      * @return resultado de la operacion
      * @throws com.legendoftecla.exceptions.JuegoException si la operacion no puede completarse
     */
    Juego cargarJuego() throws JuegoException;
}

