/**
 * Construccion, carga y persistencia de escenarios.
 *
 * <p>{@link com.legendoftecla.loader.CargadorJuego} abstrae cualquier origen.
 * {@link com.legendoftecla.loader.CargadorJuegoPorDefecto} genera mapas
 * pequenos; {@link com.legendoftecla.loader.CargadorJuegoGrandeConAliados}
 * ofrece cincuenta variantes deterministas; y
 * {@link com.legendoftecla.loader.CargadorJuegoDeFicheros} acepta el formato
 * historico TXT o delega en {@link com.legendoftecla.loader.CargadorJuegoJson}.
 * Todos producen un {@link com.legendoftecla.model.world.Juego} equivalente.</p>
 *
 * <p>{@link com.legendoftecla.loader.EscenarioDefinicion} es el DTO completo
 * usado por el editor. {@link com.legendoftecla.loader.SerializadorEscenarioJson}
 * lo valida y persiste. Los generadores internos distribuyen aliados,
 * suministros y enemigos con semillas conocidas para que una misma
 * configuracion sea reproducible.</p>
 */
package com.legendoftecla.loader;
