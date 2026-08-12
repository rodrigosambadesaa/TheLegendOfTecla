package com.legendoftecla.inventory;
import com.legendoftecla.model.items.Objeto;
/** Resultado explicito de una fabricacion. */
public record ResultadoFabricacion(boolean exito, String mensaje, Objeto objeto) { }
