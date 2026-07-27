/**
 * Puertos de entrada y salida independientes de la interfaz.
 *
 * <p>{@link com.legendoftecla.console.Consola} define el contrato minimo que
 * necesita el juego para solicitar texto y publicar mensajes clasificados.
 * {@link com.legendoftecla.console.ConsolaNormal} lo implementa para terminal,
 * mientras que la GUI proporciona un adaptador que acumula los mismos mensajes
 * para Swing. Esta frontera mantiene el motor libre de detalles visuales.</p>
 *
 * <p>{@link com.legendoftecla.console.TipoMensaje} permite distinguir
 * informacion, exito, advertencia, error y combate. Las representaciones de
 * enemigos y lore son elementos de presentacion y no modifican el estado.</p>
 */
package com.legendoftecla.console;
