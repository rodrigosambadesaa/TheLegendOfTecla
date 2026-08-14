package com.legendoftecla.gui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.function.Consumer;

/** Construye acciones rápidas y expone sólo los botones cuyo estado es contextual. */
public final class PanelAcciones extends JPanel {
    private final JButton coger;
    private final JButton usar;
    private final JButton tirar;
    private final JButton equipar;
    private final JButton desequipar;
    private final JButton atacar;
    private final JButton lanzarExplosivo;
    private final JButton pedirAyuda;

    public PanelAcciones(Consumer<String> ejecutar, Runnable cogerAccion,
            Runnable usarAccion, Runnable tirarAccion, Runnable equiparAccion,
            Runnable desequiparAccion, Runnable atacarAccion,
            Runnable explosivoAccion, Runnable volver) {
        super(new BorderLayout(5, 5));
        setBorder(BorderFactory.createTitledBorder("Acciones rapidas"));
        JPanel movimiento = new JPanel(new GridLayout(3, 3, 4, 4));
        movimiento.add(new JLabel());
        movimiento.add(boton("N", "mover norte", ejecutar));
        movimiento.add(new JLabel());
        movimiento.add(boton("O", "mover oeste", ejecutar));
        movimiento.add(boton("Mirar", "mirar", ejecutar));
        movimiento.add(boton("E", "mover este", ejecutar));
        movimiento.add(new JLabel());
        movimiento.add(boton("S", "mover sur", ejecutar));
        movimiento.add(new JLabel());
        add(movimiento, BorderLayout.NORTH);

        JPanel utilidades = new JPanel(new GridLayout(0, 2, 4, 4));
        coger = boton("Coger", cogerAccion);
        usar = boton("Usar", usarAccion);
        tirar = boton("Tirar", tirarAccion);
        equipar = boton("Equipar", equiparAccion);
        desequipar = boton("Desequipar", desequiparAccion);
        atacar = boton("Atacar", atacarAccion);
        lanzarExplosivo = boton("Lanzar explosivo", explosivoAccion);
        pedirAyuda = boton("Pedir ayuda", "pedir ayuda", ejecutar);
        java.util.List.of(coger, usar, tirar, equipar, desequipar, atacar,
                lanzarExplosivo, pedirAyuda).forEach(utilidades::add);
        String[][] comandos = {
            {"Formacion defensiva", "reagrupar defensiva"},
            {"Formacion ofensiva", "reagrupar ofensiva"},
            {"Romper formacion", "romper formacion"}, {"Inventario", "inventario"},
            {"Estado", "mirar"}, {"Ayuda", "ayuda"}, {"Recorrido", "recorrido"},
            {"Descansar", "descansar"}, {"Recargar", "recargar"},
            {"Estado arma", "estado arma"}, {"Abrir puerta", "abrir puerta"},
            {"Desactivar trampa", "desactivar trampa"}, {"Recetas", "recetas"},
            {"Guardar", "guardar partida"}, {"Cargar", "cargar partida"},
            {"Estadisticas", "estadisticas"}, {"Salir", "salir"}
        };
        for (String[] comando : comandos) {
            utilidades.add(boton(comando[0], comando[1], ejecutar));
        }
        add(utilidades, BorderLayout.CENTER);

        JButton nueva = new JButton("Nueva partida / menu");
        nueva.addActionListener(evento -> volver.run());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        pie.add(nueva);
        add(pie, BorderLayout.SOUTH);
    }

    public JButton getCoger() { return coger; }
    public JButton getUsar() { return usar; }
    public JButton getTirar() { return tirar; }
    public JButton getEquipar() { return equipar; }
    public JButton getDesequipar() { return desequipar; }
    public JButton getAtacar() { return atacar; }
    public JButton getLanzarExplosivo() { return lanzarExplosivo; }
    public JButton getPedirAyuda() { return pedirAyuda; }

    private JButton boton(String etiqueta, String comando, Consumer<String> ejecutar) {
        JButton boton = new JButton(etiqueta);
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.addActionListener(evento -> ejecutar.accept(comando));
        return boton;
    }

    private JButton boton(String etiqueta, Runnable accion) {
        JButton boton = new JButton(etiqueta);
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.addActionListener(evento -> accion.run());
        return boton;
    }
}
