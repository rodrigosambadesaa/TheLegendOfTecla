package com.legendoftecla.gui;

import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.characters.Zapador;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Posicion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Vista completa de juego: mapa, estado, acciones, comandos y registro. */
public final class PanelJuego extends JPanel {
    /**
     * Valor publico {@code motor} utilizado por el modelo del juego.
     */
    private final MotorPartida motor;
    /**
     * Valor publico {@code consola} utilizado por el modelo del juego.
     */
    private final ConsolaGrafica consola;
    /**
     * Valor publico {@code mapaPanel} utilizado por el modelo del juego.
     */
    private final MapaGraficoPanel mapaPanel;
    /**
     * Valor publico {@code scrollMapa} utilizado por el modelo del juego.
     */
    private final JScrollPane scrollMapa;
    /**
     * Valor publico {@code registro} utilizado por el modelo del juego.
     */
    private final JTextPane registro;
    /**
     * Valor publico {@code comando} utilizado por el modelo del juego.
     */
    private final JTextField comando;
    /**
     * Valor publico {@code estado} utilizado por el modelo del juego.
     */
    private final JLabel estado;
    /**
     * Valor publico {@code mochila} utilizado por el modelo del juego.
     */
    private final JLabel mochila;
    /** Panel persistente con el estado completo de todos los aliados. */
    private final JTextArea estadoAliados;
    /**
     * Valor publico {@code ejecutar} utilizado por el modelo del juego.
     */
    private final JButton ejecutar;
    /**
     * Valor publico {@code coger} utilizado por el modelo del juego.
     */
    private JButton coger;
    /**
     * Valor publico {@code usar} utilizado por el modelo del juego.
     */
    private JButton usar;
    /**
     * Valor publico {@code tirar} utilizado por el modelo del juego.
     */
    private JButton tirar;
    /**
     * Valor publico {@code equipar} utilizado por el modelo del juego.
     */
    private JButton equipar;
    /**
     * Valor publico {@code desequipar} utilizado por el modelo del juego.
     */
    private JButton desequipar;
    /**
     * Valor publico {@code atacar} utilizado por el modelo del juego.
     */
    private JButton atacar;
    /** Boton contextual para lanzar un explosivo del zapador. */
    private JButton lanzarExplosivo;
    /** Boton que activa la orden temporal de asistencia aliada. */
    private JButton pedirAyuda;

    /**
     * Crea una instancia de {@code PanelJuego}.
      * @param consola valor de {@code consola}
      * @param motor valor de {@code motor}
      * @param volver valor de {@code volver}
     */
    public PanelJuego(MotorPartida motor, ConsolaGrafica consola, Runnable volver) {
        super(new BorderLayout(8, 8));
        this.motor = motor;
        this.consola = consola;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        estado = new JLabel();
        estado.setFont(estado.getFont().deriveFont(Font.BOLD, 15f));
        mochila = new JLabel();
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(estado, BorderLayout.WEST);
        cabecera.add(mochila, BorderLayout.EAST);
        add(cabecera, BorderLayout.NORTH);

        mapaPanel = new MapaGraficoPanel(motor);
        scrollMapa = new JScrollPane(mapaPanel);
        scrollMapa.getViewport().setBackground(new Color(20, 24, 31));

        registro = new JTextPane();
        registro.setEditable(false);
        registro.setBackground(new Color(25, 29, 35));
        registro.setForeground(new Color(225, 230, 235));
        registro.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane scrollRegistro = new JScrollPane(registro);
        scrollRegistro.setPreferredSize(new Dimension(380, 300));

        JPanel acciones = crearPanelAcciones(volver);
        estadoAliados = new JTextArea(6, 32);
        estadoAliados.setName("estado.aliados");
        estadoAliados.setEditable(false);
        estadoAliados.setLineWrap(true);
        estadoAliados.setWrapStyleWord(true);
        estadoAliados.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        estadoAliados.setBackground(new Color(245, 247, 249));
        JScrollPane scrollAliados = new JScrollPane(estadoAliados);
        scrollAliados.setBorder(BorderFactory.createTitledBorder("Estado de aliados"));
        scrollAliados.setPreferredSize(new Dimension(380, 145));

        JPanel seguimiento = new JPanel(new BorderLayout(6, 6));
        seguimiento.add(acciones, BorderLayout.NORTH);
        seguimiento.add(scrollAliados, BorderLayout.CENTER);
        JPanel lateral = new JPanel(new BorderLayout(6, 6));
        lateral.add(seguimiento, BorderLayout.NORTH);
        lateral.add(scrollRegistro, BorderLayout.CENTER);

        JSplitPane divisor = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrollMapa, lateral);
        divisor.setResizeWeight(0.72);
        add(divisor, BorderLayout.CENTER);

        comando = new JTextField();
        comando.addActionListener(e -> ejecutarTexto());
        ejecutar = new JButton("Ejecutar comando");
        ejecutar.addActionListener(e -> ejecutarTexto());
        JPanel entrada = new JPanel(new BorderLayout(6, 0));
        entrada.add(new JLabel("Comando:"), BorderLayout.WEST);
        entrada.add(comando, BorderLayout.CENTER);
        entrada.add(ejecutar, BorderLayout.EAST);
        add(entrada, BorderLayout.SOUTH);

        consola.getHistorial().forEach(this::agregarMensaje);
        consola.setReceptor(this::agregarMensaje);
        actualizarVista();
        SwingUtilities.invokeLater(() -> {
            centrarJugador();
            comando.requestFocusInWindow();
        });
    }

    private JPanel crearPanelAcciones(Runnable volver) {
        JPanel contenedor = new JPanel(new BorderLayout(5, 5));
        contenedor.setBorder(BorderFactory.createTitledBorder("Acciones rapidas"));
        JPanel movimiento = new JPanel(new GridLayout(3, 3, 4, 4));
        movimiento.add(new JLabel());
        movimiento.add(boton("N", "mover norte"));
        movimiento.add(new JLabel());
        movimiento.add(boton("O", "mover oeste"));
        JButton mirar = boton("Mirar", "mirar");
        movimiento.add(mirar);
        movimiento.add(boton("E", "mover este"));
        movimiento.add(new JLabel());
        movimiento.add(boton("S", "mover sur"));
        movimiento.add(new JLabel());
        contenedor.add(movimiento, BorderLayout.NORTH);

        JPanel utilidades = new JPanel(new GridLayout(0, 2, 4, 4));
        coger = botonContextual("Coger", this::cogerObjeto);
        usar = botonContextual("Usar", this::usarObjeto);
        tirar = botonContextual("Tirar", this::tirarObjeto);
        equipar = botonContextual("Equipar", this::equiparObjeto);
        desequipar = botonContextual("Desequipar", this::desequiparObjeto);
        atacar = botonContextual("Atacar", this::atacarEnemigo);
        lanzarExplosivo = botonContextual("Lanzar explosivo", this::lanzarExplosivo);
        pedirAyuda = boton("Pedir ayuda", "pedir ayuda");
        utilidades.add(coger);
        utilidades.add(usar);
        utilidades.add(tirar);
        utilidades.add(equipar);
        utilidades.add(desequipar);
        utilidades.add(atacar);
        utilidades.add(lanzarExplosivo);
        utilidades.add(pedirAyuda);
        utilidades.add(boton("Inventario", "inventario"));
        utilidades.add(boton("Estado", "mirar"));
        utilidades.add(boton("Ayuda", "ayuda"));
        utilidades.add(boton("Recorrido", "recorrido"));
        utilidades.add(boton("Salir", "salir"));
        contenedor.add(utilidades, BorderLayout.CENTER);

        JButton nueva = new JButton("Nueva partida / menu");
        nueva.addActionListener(e -> volver.run());
        JPanel pie = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
        pie.add(nueva);
        contenedor.add(pie, BorderLayout.SOUTH);
        return contenedor;
    }

    private JButton boton(String etiqueta, String accion) {
        JButton boton = new JButton(etiqueta);
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.addActionListener(e -> ejecutar(accion));
        return boton;
    }

    private JButton botonContextual(String etiqueta, Runnable accion) {
        JButton boton = new JButton(etiqueta);
        boton.setHorizontalAlignment(SwingConstants.CENTER);
        boton.addActionListener(e -> accion.run());
        return boton;
    }

    private void cogerObjeto() {
        seleccionarYEjecutar("Coger objeto", "coger", celdaJugador().getObjetos().stream()
                .map(objeto -> new OpcionAccion(describir(objeto), "coger " + objeto.getNombre()))
                .toList());
    }

    private void usarObjeto() {
        seleccionarYEjecutar("Usar objeto", "usar", objetosMochila().stream()
                .filter(objeto -> !(objeto instanceof Arma)
                        && !(objeto instanceof Armadura)
                        && !(objeto instanceof Explosivo))
                .map(objeto -> new OpcionAccion(describir(objeto), "usar " + objeto.getNombre()))
                .toList());
    }

    private void tirarObjeto() {
        seleccionarYEjecutar("Tirar objeto", "tirar", objetosMochila().stream()
                .map(objeto -> new OpcionAccion(describir(objeto), "tirar " + objeto.getNombre()))
                .toList());
    }

    private void equiparObjeto() {
        seleccionarYEjecutar("Equipar objeto", "equipar", objetosMochila().stream()
                .filter(objeto -> objeto instanceof Arma || objeto instanceof Armadura)
                .map(objeto -> new OpcionAccion(describir(objeto), "equipar " + objeto.getNombre()))
                .toList());
    }

    private void desequiparObjeto() {
        Personaje jugador = motor.getJuego().getJugador();
        List<OpcionAccion> opciones = new ArrayList<>();
        jugador.getArmasEquipadas().forEach(arma -> opciones.add(
                new OpcionAccion(describir(arma), "desequipar " + arma.getNombre())));
        if (jugador.getArmaduraEquipada() != null) {
            Armadura armadura = jugador.getArmaduraEquipada();
            opciones.add(new OpcionAccion(describir(armadura), "desequipar " + armadura.getNombre()));
        }
        seleccionarYEjecutar("Desequipar objeto", "desequipar", opciones);
    }

    private void atacarEnemigo() {
        Posicion origen = motor.getJuego().getJugador().getPosicion();
        List<OpcionAccion> opciones = new ArrayList<>();
        for (Enemigo enemigo : motor.getJuego().getEnemigos()) {
            if (enemigo.getSalud() <= 0) {
                continue;
            }
            String alcance = alcanceAtaque(origen, enemigo.getPosicion());
            if (alcance != null && motor.getJuego().getMapa().hayLineaAtaque(origen, enemigo.getPosicion())) {
                String comandoAtaque = "atacar " + (alcance.isBlank() ? "" : alcance + " ") + enemigo.getNombre();
                opciones.add(new OpcionAccion(
                        enemigo.getNombre() + " - salud " + enemigo.getSalud() + " - "
                                + describirDistancia(origen, enemigo.getPosicion()),
                        comandoAtaque));
            }
        }
        seleccionarYEjecutar("Atacar enemigo", "atacar", opciones);
    }

    private void lanzarExplosivo() {
        Posicion origen = motor.getJuego().getJugador().getPosicion();
        List<Explosivo> explosivos = objetosMochila().stream()
                .filter(Explosivo.class::isInstance)
                .map(Explosivo.class::cast)
                .toList();
        List<OpcionAccion> opciones = new ArrayList<>();
        Set<Posicion> destinosIncluidos = new HashSet<>();
        for (Enemigo enemigo : motor.getJuego().getEnemigos()) {
            Posicion destino = enemigo.getPosicion();
            if (enemigo.getSalud() <= 0 || !destinosIncluidos.add(destino)) {
                continue;
            }
            String alcance = alcanceAtaque(origen, destino);
            if (alcance == null || alcance.isBlank()
                    || !motor.getJuego().getMapa().hayLineaAtaque(origen, destino)) {
                continue;
            }
            int distancia = origen.distanciaManhattan(destino);
            long enemigosEnCelda = motor.getJuego().getMapa().getCelda(destino).getEnemigos().stream()
                    .filter(objetivo -> objetivo.getSalud() > 0)
                    .count();
            for (Explosivo explosivo : explosivos) {
                if (distancia <= explosivo.getAlcanceMaximo()) {
                    opciones.add(new OpcionAccion(
                            explosivo.getNombre() + " a " + alcance + " - " + enemigosEnCelda + " enemigo(s)",
                            "lanzar " + alcance + " " + explosivo.getNombre()));
                }
            }
        }
        seleccionarYEjecutar("Lanzar explosivo", "lanzar", opciones);
    }

    private void seleccionarYEjecutar(String titulo, String verbo, List<OpcionAccion> opciones) {
        if (opciones.isEmpty()) {
            agregarMensaje(new ConsolaGrafica.Mensaje(
                    "No hay opciones disponibles para " + verbo + ".", TipoMensaje.ADVERTENCIA));
            return;
        }
        OpcionAccion seleccion = (OpcionAccion) JOptionPane.showInputDialog(
                this, "Selecciona una opcion:", titulo, JOptionPane.PLAIN_MESSAGE,
                null, opciones.toArray(), opciones.get(0));
        if (seleccion != null) {
            ejecutar(seleccion.comando());
        }
    }

    private Celda celdaJugador() {
        return motor.getJuego().getMapa().getCelda(motor.getJuego().getJugador().getPosicion());
    }

    private List<Objeto> objetosMochila() {
        return motor.getJuego().getJugador().getMochila().getObjetos();
    }

    private String describir(Objeto objeto) {
        return objeto.getNombre() + " (" + objeto.getClass().getSimpleName() + ", "
                + String.format("%.1f", objeto.getPeso()) + " kg)";
    }

    private String alcanceAtaque(Posicion origen, Posicion destino) {
        int filas = destino.getFila() - origen.getFila();
        int columnas = destino.getColumna() - origen.getColumna();
        if (filas == 0 && columnas == 0) {
            return "";
        }
        if (filas != 0 && columnas != 0) {
            return null;
        }
        if (filas < 0) {
            return -filas + "n";
        }
        if (filas > 0) {
            return filas + "s";
        }
        return columnas < 0 ? -columnas + "o" : columnas + "e";
    }

    private String describirDistancia(Posicion origen, Posicion destino) {
        String alcance = alcanceAtaque(origen, destino);
        return alcance == null || alcance.isBlank() ? "misma celda" : alcance;
    }

    private void ejecutarTexto() {
        String texto = comando.getText().trim();
        if (texto.isEmpty()) {
            return;
        }
        comando.setText("");
        ejecutar(texto);
    }

    private void ejecutar(String accion) {
        if (motor.isFinalizada()) {
            return;
        }
        agregarMensaje(new ConsolaGrafica.Mensaje("> " + accion, TipoMensaje.ESTADO));
        motor.ejecutarComando(accion);
        actualizarVista();
        centrarJugador();
    }

    private void actualizarVista() {
        estado.setText(motor.getEstadoJugador());
        estadoAliados.setText(motor.getEstadoAliados());
        estadoAliados.setCaretPosition(0);
        Mochila inventario = motor.getJuego().getJugador().getMochila();
        mochila.setText(String.format("Mochila %d/%d  %.1f/%.1f kg",
                inventario.getObjetos().size(), inventario.getCapacidadMax(),
                inventario.getPesoActual(), inventario.getPesoMax()));
        mapaPanel.repaint();
        boolean activa = !motor.isFinalizada();
        comando.setEnabled(activa);
        ejecutar.setEnabled(activa);
        coger.setEnabled(activa && !celdaJugador().getObjetos().isEmpty());
        usar.setEnabled(activa && objetosMochila().stream()
                .anyMatch(objeto -> !(objeto instanceof Arma)
                        && !(objeto instanceof Armadura)
                        && !(objeto instanceof Explosivo)));
        tirar.setEnabled(activa && !objetosMochila().isEmpty());
        equipar.setEnabled(activa && objetosMochila().stream()
                .anyMatch(objeto -> objeto instanceof Arma || objeto instanceof Armadura));
        Personaje jugador = motor.getJuego().getJugador();
        desequipar.setEnabled(activa && (!jugador.getArmasEquipadas().isEmpty()
                || jugador.getArmaduraEquipada() != null));
        atacar.setEnabled(activa && hayEnemigoAtacable());
        lanzarExplosivo.setEnabled(activa && hayLanzamientoExplosivoDisponible());
        pedirAyuda.setEnabled(activa && motor.getJuego().getAliados().stream()
                .anyMatch(aliado -> aliado.getSalud() > 0));
    }

    private boolean hayEnemigoAtacable() {
        Posicion origen = motor.getJuego().getJugador().getPosicion();
        return motor.getJuego().getEnemigos().stream().anyMatch(enemigo ->
                enemigo.getSalud() > 0
                        && alcanceAtaque(origen, enemigo.getPosicion()) != null
                        && motor.getJuego().getMapa().hayLineaAtaque(origen, enemigo.getPosicion()));
    }

    private boolean hayLanzamientoExplosivoDisponible() {
        if (!(motor.getJuego().getJugador() instanceof Zapador)) {
            return false;
        }
        int alcanceMaximo = objetosMochila().stream()
                .filter(Explosivo.class::isInstance)
                .map(Explosivo.class::cast)
                .mapToInt(Explosivo::getAlcanceMaximo)
                .max()
                .orElse(0);
        if (alcanceMaximo == 0) {
            return false;
        }
        Posicion origen = motor.getJuego().getJugador().getPosicion();
        return motor.getJuego().getEnemigos().stream().anyMatch(enemigo ->
                enemigo.getSalud() > 0
                        && origen.distanciaManhattan(enemigo.getPosicion()) <= alcanceMaximo
                        && alcanceAtaque(origen, enemigo.getPosicion()) != null
                        && !origen.equals(enemigo.getPosicion())
                        && motor.getJuego().getMapa().hayLineaAtaque(origen, enemigo.getPosicion()));
    }

    private void centrarJugador() {
        Posicion jugador = motor.getJuego().getJugador().getPosicion();
        int x = jugador.getColumna() * 32;
        int y = jugador.getFila() * 32;
        mapaPanel.scrollRectToVisible(new java.awt.Rectangle(x - 160, y - 120, 352, 272));
    }

    private void agregarMensaje(ConsolaGrafica.Mensaje mensaje) {
        StyledDocument documento = registro.getStyledDocument();
        SimpleAttributeSet estilo = new SimpleAttributeSet();
        StyleConstants.setForeground(estilo, color(mensaje.tipo()));
        try {
            documento.insertString(documento.getLength(), mensaje.texto() + "\n", estilo);
            registro.setCaretPosition(documento.getLength());
        } catch (BadLocationException ignored) {
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

    private record OpcionAccion(String etiqueta, String comando) {
        @Override
        public String toString() {
            return etiqueta;
        }
    }
}
