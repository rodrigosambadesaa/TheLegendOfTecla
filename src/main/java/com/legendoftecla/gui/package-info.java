/**
 * Interfaz grafica Swing y editor visual de escenarios.
 *
 * <p>{@link com.legendoftecla.gui.VentanaPrincipal} contiene la navegacion
 * general. {@link com.legendoftecla.gui.PanelConfiguracion} crea una
 * configuracion compatible con consola; {@link com.legendoftecla.gui.PanelJuego}
 * presenta mapa, acciones y estado; y
 * {@link com.legendoftecla.gui.PanelEditorMapa} permite crear o modificar un
 * escenario JSON completo.</p>
 *
 * <p>{@link com.legendoftecla.gui.MapaGraficoPanel} solo representa informacion
 * visible o inspeccionada. Los botones contextuales terminan invocando el mismo
 * {@code MotorPartida} que la consola, por lo que la GUI no introduce una
 * segunda implementacion de las reglas.</p>
 */
package com.legendoftecla.gui;
