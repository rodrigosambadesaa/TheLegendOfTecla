package com.legendoftecla.gui;

import com.legendoftecla.console.TipoMensaje;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

/** Registro de combate coloreado y desplazable. */
public final class PanelRegistro extends JPanel {
    private final JTextPane registro = new JTextPane();

    public PanelRegistro() {
        super(new BorderLayout());
        registro.setEditable(false);
        registro.setBackground(new Color(25, 29, 35));
        registro.setForeground(new Color(225, 230, 235));
        registro.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(registro);
        scroll.setPreferredSize(new Dimension(380, 300));
        add(scroll, BorderLayout.CENTER);
    }

    /** Agrega una línea manteniendo colores semánticos y caret al final. */
    public void agregar(ConsolaGrafica.Mensaje mensaje) {
        StyledDocument documento = registro.getStyledDocument();
        SimpleAttributeSet estilo = new SimpleAttributeSet();
        StyleConstants.setForeground(estilo, color(mensaje.tipo()));
        try {
            documento.insertString(documento.getLength(), mensaje.texto() + "\n", estilo);
            registro.setCaretPosition(documento.getLength());
        } catch (BadLocationException ignorada) {
            // El documento solo se modifica desde el hilo de eventos de Swing.
        }
    }

    private Color color(TipoMensaje tipo) {
        return switch (tipo) {
            case EXITO -> new Color(100, 220, 140);
            case ERROR -> new Color(255, 105, 110);
            case ADVERTENCIA -> new Color(255, 196, 80);
            case ESTADO -> new Color(90, 205, 235);
            case INFO -> new Color(220, 225, 230);
        };
    }
}
