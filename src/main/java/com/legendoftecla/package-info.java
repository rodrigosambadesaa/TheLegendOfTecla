/**
 * Punto de entrada de The Legend of Tecla.
 *
 * <p>{@link com.legendoftecla.Main} conecta las opciones de arranque con la
 * interfaz solicitada. En modo consola crea la partida y ejecuta su bucle de
 * comandos; en modo grafico delega la inicializacion en Swing. Las reglas no
 * viven en este paquete: se concentran en {@code engine} para que ambas
 * interfaces compartan exactamente el mismo comportamiento.</p>
 *
 * @see com.legendoftecla.Main
 * @see com.legendoftecla.engine.FabricaJuego
 * @see com.legendoftecla.engine.MotorPartida
 */
package com.legendoftecla;
