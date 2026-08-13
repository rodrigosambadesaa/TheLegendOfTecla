package com.legendoftecla.gui;

import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/** Hace equivalente la fila numerica y el bloque numerico en campos Swing. */
final class SoporteTecladoNumerico {
    private static final String PROPIEDAD = "legendoftecla.tecladoNumerico";

    private SoporteTecladoNumerico() { }

    /** Instala el soporte en todos los campos presentes bajo un componente. */
    static void instalar(Component componente) {
        if (componente instanceof JTextComponent texto) instalar(texto);
        if (componente instanceof Container contenedor) {
            for (Component hijo : contenedor.getComponents()) instalar(hijo);
        }
    }

    /** Instala una sola vez el adaptador que normaliza las teclas del numpad. */
    static void instalar(JTextComponent campo) {
        if (Boolean.TRUE.equals(campo.getClientProperty(PROPIEDAD))) return;
        campo.putClientProperty(PROPIEDAD, Boolean.TRUE);
        campo.addKeyListener(new KeyAdapter() {
            private Character pendiente;

            @Override
            public void keyPressed(KeyEvent evento) {
                Character caracter = caracterNumerico(evento.getKeyCode());
                if (caracter == null) return;
                campo.replaceSelection(String.valueOf(caracter));
                pendiente = caracter;
                evento.consume();
            }

            @Override
            public void keyTyped(KeyEvent evento) {
                if (pendiente != null && evento.getKeyChar() == pendiente) {
                    evento.consume();
                    pendiente = null;
                }
            }

            @Override
            public void keyReleased(KeyEvent evento) {
                pendiente = null;
            }
        });
    }

    private static Character caracterNumerico(int codigo) {
        if (codigo >= KeyEvent.VK_NUMPAD0 && codigo <= KeyEvent.VK_NUMPAD9) {
            return (char) ('0' + codigo - KeyEvent.VK_NUMPAD0);
        }
        if (codigo == KeyEvent.VK_SUBTRACT) return '-';
        return null;
    }
}
