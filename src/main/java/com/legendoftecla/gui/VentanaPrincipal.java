package com.legendoftecla.gui;

import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.engine.FabricaJuego;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.world.Juego;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.nio.file.Path;

/** Ventana unica que contiene configuracion, juego completo y editor. */
public final class VentanaPrincipal extends JFrame {
    private static final String CONFIGURACION = "configuracion";
    private static final String JUEGO = "juego";
    private static final String EDITOR = "editor";

    /**
     * Ejecuta la operacion publica {@code CardLayout}.
     */
    private final CardLayout tarjetas = new CardLayout();
    /**
     * Ejecuta la operacion publica {@code JPanel}.
     */
    private final JPanel contenido = new JPanel(tarjetas);
    /**
     * Valor publico {@code configuracion} utilizado por el modelo del juego.
     */
    private final PanelConfiguracion configuracion;
    /**
     * Valor publico {@code panelJuego} utilizado por el modelo del juego.
     */
    private PanelJuego panelJuego;
    /**
     * Valor publico {@code panelEditor} utilizado por el modelo del juego.
     */
    private PanelEditorMapa panelEditor;

    /**
     * Crea una instancia de {@code VentanaPrincipal}.
      * @param abrirEditor valor de {@code abrirEditor}
     */
    public VentanaPrincipal(boolean abrirEditor) {
        super("The Legend of Tecla");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 760));
        setSize(1500, 950);
        setLocationRelativeTo(null);

        configuracion = new PanelConfiguracion(this::iniciarPartida, this::mostrarEditor);
        setPanelJuego(null);
        setPanelEditor(null);
        contenido.add(configuracion, CONFIGURACION);
        setContentPane(contenido);
        if (abrirEditor) {
            mostrarEditor();
        } else {
            tarjetas.show(contenido, CONFIGURACION);
        }
    }

    /** @return panel de juego actual o {@code null} */
    public PanelJuego getPanelJuego() {
        return panelJuego;
    }

    /** @param panelJuego panel de juego opcional */
    public void setPanelJuego(PanelJuego panelJuego) {
        this.panelJuego = panelJuego;
    }

    /** @return editor actual o {@code null} */
    public PanelEditorMapa getPanelEditor() {
        return panelEditor;
    }

    /** @param panelEditor editor opcional */
    public void setPanelEditor(PanelEditorMapa panelEditor) {
        this.panelEditor = panelEditor;
    }

    /**
     * Ejecuta la operacion publica {@code iniciar}.
      * @param abrirEditor valor de {@code abrirEditor}
     */
    public static void iniciar(boolean abrirEditor) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
            // Swing conserva su apariencia multiplataforma si el tema del sistema no esta disponible.
        }
        new VentanaPrincipal(abrirEditor).setVisible(true);
    }

    private void iniciarPartida(ConfiguracionPartida datos) {
        try {
            ConsolaGrafica consola = new ConsolaGrafica();
            Juego juego = FabricaJuego.crear(consola, datos);
            MotorPartida motor = new MotorPartida(juego);
            if (panelJuego != null) {
                contenido.remove(panelJuego);
            }
            setPanelJuego(new PanelJuego(motor, consola, this::mostrarConfiguracion));
            contenido.add(panelJuego, JUEGO);
            tarjetas.show(contenido, JUEGO);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(),
                    "No se pudo iniciar la partida", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarConfiguracion() {
        tarjetas.show(contenido, CONFIGURACION);
    }

    private void mostrarEditor() {
        if (panelEditor != null) {
            contenido.remove(panelEditor);
        }
        setPanelEditor(new PanelEditorMapa(this::escenarioGuardado, this::mostrarConfiguracion));
        contenido.add(panelEditor, EDITOR);
        tarjetas.show(contenido, EDITOR);
    }

    private void escenarioGuardado(Path directorio, boolean conAliados) {
        configuracion.seleccionarDirectorio(directorio, conAliados);
    }
}
