/**
 * Orquestacion central de la partida.
 *
 * <p>{@link com.legendoftecla.engine.FabricaJuego} recibe una
 * {@link com.legendoftecla.engine.ConfiguracionPartida}, selecciona el cargador
 * correcto y devuelve un mundo listo para jugar.
 * {@link com.legendoftecla.engine.MotorPartida} ejecuta comandos y coordina los
 * turnos, el coste energetico, el combate, la IA, la asistencia, la exploracion
 * y las condiciones de salida.</p>
 *
 * <p>El motor consulta y modifica el modelo mediante su API publica, pero no
 * construye widgets ni lee archivos directamente. El estado definitivo
 * pertenece a {@link com.legendoftecla.model.world.Juego}; el motor aporta
 * secuencia y decisiones temporales.</p>
 */
package com.legendoftecla.engine;
