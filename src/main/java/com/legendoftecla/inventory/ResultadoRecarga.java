package com.legendoftecla.inventory;

import com.legendoftecla.model.items.Arma;

/** Resultado inmutable de mover municion de la mochila a un cargador. */
public record ResultadoRecarga(Arma arma, int cantidad) { }
