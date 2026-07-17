package com.legendoftecla.loader;

import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.world.Juego;


/**
 * Representa la entidad CargadorJuego del juego.
 */
public interface CargadorJuego {
    Juego cargarJuego() throws JuegoException;
}

