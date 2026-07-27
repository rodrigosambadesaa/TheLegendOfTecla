/**
 * Estado espacial y agregado raiz de una partida.
 *
 * <p>{@link com.legendoftecla.model.world.Juego} relaciona jugador, mapa,
 * enemigos, aliados, recorrido, exploracion y pasos disponibles.
 * {@link com.legendoftecla.model.world.Mapa} contiene una matriz rectangular de
 * {@link com.legendoftecla.model.world.Celda}, valida limites, transitabilidad
 * y lineas de ataque. {@link com.legendoftecla.model.world.Posicion} y
 * {@link com.legendoftecla.model.world.Direccion} expresan desplazamientos sin
 * exponer indices internos.</p>
 *
 * <p>{@link com.legendoftecla.model.world.SistemaPuntuacion} clasifica el final
 * y calcula el resultado a partir del estado observable. Las colecciones y
 * posiciones se devuelven defensivamente para impedir cambios fuera de las
 * operaciones previstas.</p>
 */
package com.legendoftecla.model.world;
