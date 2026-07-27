/**
 * Jerarquia de participantes e inventario personal.
 *
 * <p>{@link com.legendoftecla.model.characters.Personaje} conserva nombre,
 * posicion, salud, energia, vision, mochila y efectos temporales.
 * {@link com.legendoftecla.model.characters.Jugador} especializa el control
 * humano mediante Marine, Francotirador y Zapador.
 * {@link com.legendoftecla.model.characters.Enemigo} define la raiz de Sectoid
 * y Floaters, mientras que {@link com.legendoftecla.model.characters.Aliado}
 * incorpora estado de asistencia, combate y evacuacion.</p>
 *
 * <p>{@link com.legendoftecla.model.characters.Mochila} limita simultaneamente
 * numero de objetos y peso, devuelve vistas protegidas y centraliza las
 * operaciones de guardar o retirar. La energia y la salud se acotan siempre
 * en los setters del personaje.</p>
 */
package com.legendoftecla.model.characters;
