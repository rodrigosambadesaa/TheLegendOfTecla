package com.legendoftecla.gui;

import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.text.ParseException;

/** Utilidades internas para que los selectores numericos admitan escritura directa. */
final class ControlesNumericos {
    private ControlesNumericos() {
    }

    static JSpinner entero(String nombre, int valor, int minimo, int maximo, int paso) {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(valor, minimo, maximo, paso));
        spinner.setName(nombre);
        JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner, "0");
        JFormattedTextField campo = editor.getTextField();
        campo.setName(nombre + ".texto");
        campo.setColumns(5);
        campo.setEditable(true);
        campo.setFocusable(true);
        campo.setHorizontalAlignment(SwingConstants.CENTER);
        campo.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent evento) {
                SwingUtilities.invokeLater(campo::selectAll);
            }
        });
        campo.addActionListener(evento -> confirmar(spinner));
        spinner.setEditor(editor);
        return spinner;
    }

    static int valorEntero(JSpinner spinner) {
        confirmar(spinner);
        return ((Number) spinner.getValue()).intValue();
    }

    private static void confirmar(JSpinner spinner) {
        try {
            spinner.commitEdit();
        } catch (ParseException error) {
            throw new IllegalArgumentException("Introduce un numero valido.", error);
        }
    }
}
