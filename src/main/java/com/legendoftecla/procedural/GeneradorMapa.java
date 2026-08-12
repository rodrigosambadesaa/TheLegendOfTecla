package com.legendoftecla.procedural;
import com.legendoftecla.model.world.Mapa;
/** Generador reproducible por semilla. */
public interface GeneradorMapa { Mapa generar(long seed, ConfiguracionGeneracion config); }
