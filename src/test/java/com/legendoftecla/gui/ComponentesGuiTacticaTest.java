package com.legendoftecla.gui;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayDeque;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComponentesGuiTacticaTest {
    @BeforeAll
    static void headless() {
        System.setProperty("java.awt.headless", "true");
    }

    @Test
    void panelEstadoMantieneElNombreHeadlessDelEstadoAliado() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("A", new Posicion(0, 1), new Mochila(2, 10), 3);
        juego.agregarAliado(aliado);
        PanelEstado[] panel = new PanelEstado[1];

        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new PanelEstado();
            panel[0].actualizar(new MotorPartida(juego));
        });

        JTextArea estado = (JTextArea) buscar(panel[0], "estado.aliados");
        assertNotNull(estado);
        assertTrue(estado.getText().contains("A"));
        assertTrue(estado.getText().contains("Puntuacion"));
    }

    @Test
    void panelAccionesConservaOrdenesRapidas() throws Exception {
        java.util.List<String> ejecutadas = new java.util.ArrayList<>();
        PanelAcciones[] panel = new PanelAcciones[1];
        SwingUtilities.invokeAndWait(() -> panel[0] = new PanelAcciones(
                ejecutadas::add, () -> { }, () -> { }, () -> { }, () -> { },
                () -> { }, () -> { }, () -> { }, () -> { }));

        SwingUtilities.invokeAndWait(() -> panel[0].getPedirAyuda().doClick());

        assertEquals(java.util.List.of("pedir ayuda"), ejecutadas);
        assertNotNull(panel[0].getLanzarExplosivo());
    }

    @Test
    void configuracionMantieneAccesiblesControlesInferioresConDesplazamiento() throws Exception {
        PanelConfiguracion[] panel = new PanelConfiguracion[1];
        SwingUtilities.invokeAndWait(() -> panel[0] = new PanelConfiguracion(
                configuracion -> { }, () -> { }));

        JScrollPane desplazamiento = (JScrollPane) buscar(panel[0], "configuracion.scroll");
        JButton iniciar = (JButton) buscar(panel[0], "configuracion.iniciar");
        JButton editor = (JButton) buscar(panel[0], "configuracion.editor");

        assertNotNull(desplazamiento);
        assertEquals(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                desplazamiento.getVerticalScrollBarPolicy());
        assertEquals(20, desplazamiento.getVerticalScrollBar().getUnitIncrement());
        assertNotNull(iniciar);
        assertNotNull(editor);
    }

    @Test
    void juegoExponeVentanasMoviblesYPlayParaContinuarConAliados() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("A", juego.getMapa().getInicio(), new Mochila(2, 10), 3);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
        MotorPartida motor = new MotorPartida(juego);
        juego.getJugador().recibirDanio(juego.getJugador().getSaludMaxima());
        motor.ejecutarComando("mirar");
        PanelJuego[] panel = new PanelJuego[1];

        SwingUtilities.invokeAndWait(() -> panel[0] = new PanelJuego(
                motor, new ConsolaGrafica(), () -> { }));

        JInternalFrame[] ventanas = panel[0].getEscritorio().getAllFrames();
        assertEquals(5, ventanas.length);
        for (JInternalFrame ventana : ventanas) {
            assertTrue(ventana.isResizable());
            assertTrue(ventana.isMaximizable());
            assertTrue(ventana.isIconifiable());
        }
        assertTrue(panel[0].getReproducir().isVisible());
        assertTrue(panel[0].getReproducir().isEnabled());
        assertTrue(panel[0].getReproducir().getText().contains("Turbo"));
        assertEquals(100, panel[0].getRetardoReproduccion());
        assertNotNull(panel[0].getResultadoEspectador());
    }

    private Component buscar(Container raiz, String nombre) {
        ArrayDeque<Component> pendientes = new ArrayDeque<>();
        pendientes.add(raiz);
        while (!pendientes.isEmpty()) {
            Component actual = pendientes.removeFirst();
            if (nombre.equals(actual.getName())) return actual;
            if (actual instanceof Container contenedor) {
                java.util.Collections.addAll(pendientes, contenedor.getComponents());
            }
        }
        return null;
    }
}
