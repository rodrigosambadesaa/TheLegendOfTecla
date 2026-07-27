/**
 * Excepciones recuperables y finales del dominio.
 *
 * <p>{@link com.legendoftecla.exceptions.JuegoException} es la raiz comprobada
 * de errores que pueden comunicarse al jugador sin abortar el proceso.
 * Sus especializaciones distinguen comandos invalidos, acciones no permitidas
 * y objetos que no pueden usarse en el contexto actual. La finalizacion de la
 * entrada se representa por separado para cerrar el bucle limpiamente.</p>
 */
package com.legendoftecla.exceptions;
