/**
 * Acciones de usuario implementadas mediante el patron Command.
 *
 * <p>{@link com.legendoftecla.commands.CommandParser} transforma la entrada
 * textual en objetos {@link com.legendoftecla.commands.Comando}. Cada comando
 * conserva un {@link com.legendoftecla.commands.CommandContext}, valida sus
 * argumentos y delega la regla efectiva al motor. La misma abstraccion permite
 * ejecutar secuencias con {@link com.legendoftecla.commands.ComandoCompuesto}
 * y repeticiones acotadas con
 * {@link com.legendoftecla.commands.ComandoRepetido}.</p>
 *
 * <p>Las operaciones cubren exploracion, movimiento, inventario, equipo,
 * combate, explosivos, asistencia aliada, estado, ayuda, recorrido y
 * finalizacion. Un comando debe limitarse a representar intencion; no debe
 * duplicar reglas del dominio ni de la interfaz grafica.</p>
 */
package com.legendoftecla.commands;
