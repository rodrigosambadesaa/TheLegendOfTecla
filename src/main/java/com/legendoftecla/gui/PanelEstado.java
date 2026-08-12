package com.legendoftecla.gui;

import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.characters.Mochila;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;

/** Presenta jugador, efectos, misión, inventario y aliados sin lógica de comandos. */
public final class PanelEstado extends JPanel {
    private final JLabel estado = new JLabel();
    private final JLabel mochila = new JLabel();
    private final JTextArea aliados = new JTextArea(6, 32);

    public PanelEstado() {
        super(new BorderLayout(6, 6));
        estado.setFont(estado.getFont().deriveFont(Font.BOLD, 15f));
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(estado, BorderLayout.WEST);
        cabecera.add(mochila, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);

        aliados.setName("estado.aliados");
        aliados.setEditable(false);
        aliados.setLineWrap(true);
        aliados.setWrapStyleWord(true);
        aliados.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        aliados.setBackground(new java.awt.Color(245, 247, 249));
        JScrollPane scroll = new JScrollPane(aliados);
        scroll.setBorder(BorderFactory.createTitledBorder("Estado de aliados"));
        scroll.setPreferredSize(new Dimension(380, 145));
        add(scroll, BorderLayout.CENTER);
    }

    /** Actualiza todas las proyecciones de estado desde el motor. */
    public void actualizar(MotorPartida motor) {
        String efectos = motor.getJuego().getJugador().getEstados().getActivos().stream()
                .map(efecto -> efecto.tipo() + "(" + efecto.turnosRestantes() + ")")
                .collect(java.util.stream.Collectors.joining(", "));
        String mision = motor.getJuego().getMision() == null ? ""
                : " | Mision: " + motor.getJuego().getMision().getNombre();
        estado.setText(motor.getEstadoJugador()
                + (efectos.isEmpty() ? "" : " | " + efectos) + mision);
        aliados.setText(motor.getEstadoAliados());
        aliados.setCaretPosition(0);
        Mochila inventario = motor.getJuego().getJugador().getMochila();
        mochila.setText(String.format("Mochila %d/%d  %.1f/%.1f kg",
                inventario.getObjetos().size(), inventario.getCapacidadMax(),
                inventario.getPesoActual(), inventario.getPesoMax()));
    }
}
