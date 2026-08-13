package com.legendoftecla.gui;

import org.junit.jupiter.api.Test;

import javax.swing.JTextField;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SoporteTecladoNumericoTest {
    @Test
    void numpadInsertaUnaSolaCifraAunqueWindowsEmitaPressedYTyped() {
        JTextField campo = new JTextField();
        SoporteTecladoNumerico.instalar(campo);
        KeyEvent pulsada = new KeyEvent(campo, KeyEvent.KEY_PRESSED, 1L, 0,
                KeyEvent.VK_NUMPAD7, '7');
        KeyEvent escrita = new KeyEvent(campo, KeyEvent.KEY_TYPED, 2L, 0,
                KeyEvent.VK_UNDEFINED, '7');

        for (KeyListener listener : campo.getKeyListeners()) listener.keyPressed(pulsada);
        for (KeyListener listener : campo.getKeyListeners()) listener.keyTyped(escrita);

        assertEquals("7", campo.getText());
        assertTrue(pulsada.isConsumed());
        assertTrue(escrita.isConsumed());
    }

    @Test
    void filaNumericaConservaElComportamientoSwingNormal() {
        JTextField campo = new JTextField();
        SoporteTecladoNumerico.instalar(campo);
        KeyEvent pulsada = new KeyEvent(campo, KeyEvent.KEY_PRESSED, 1L, 0,
                KeyEvent.VK_7, '7');

        for (KeyListener listener : campo.getKeyListeners()) listener.keyPressed(pulsada);

        assertEquals("", campo.getText());
        assertFalse(pulsada.isConsumed());
    }

    @Test
    void seInstalaRecursivamenteSinDuplicarListeners() {
        javax.swing.JPanel panel = new javax.swing.JPanel();
        JTextField campo = new JTextField();
        panel.add(campo);

        SoporteTecladoNumerico.instalar(panel);
        int listeners = campo.getKeyListeners().length;
        SoporteTecladoNumerico.instalar(panel);

        assertEquals(listeners, campo.getKeyListeners().length);
    }
}
