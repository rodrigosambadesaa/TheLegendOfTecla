package com.legendoftecla.gui;

import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.characters.Zapador;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Validaciones;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Vista completa de juego: mapa, estado, acciones, comandos y registro. */
public final class PanelJuego extends JPanel {
    private static final int RETARDO_TURBO_MS = 100;
    /**
     * Valor publico {@code motor} utilizado por el modelo del juego.
     */
    private final MotorPartida motor;
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
    private final PanelRegistro registro;
    /**
     * Valor publico {@code comando} utilizado por el modelo del juego.
     */
    private final JTextField comando;
    /**
     * Valor publico {@code estado} utilizado por el modelo del juego.
     */
    private final PanelEstado panelEstado;
    private final PanelAcciones panelAcciones;
    /**
     * Valor publico {@code ejecutar} utilizado por el modelo del juego.
     */
    private final JButton ejecutar;
    private final JButton reproducir;
    private final JLabel resultadoEspectador;
    private final Timer temporizadorEspectador;
    private final JDesktopPane escritorio;
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
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        panelEstado = new PanelEstado();
        mapaPanel = new MapaGraficoPanel(motor);
        scrollMapa = new JScrollPane(mapaPanel);
        scrollMapa.getViewport().setBackground(new Color(20, 24, 31));

        registro = new PanelRegistro();
        panelAcciones = crearPanelAcciones(() -> {
            detenerReproduccion();
            volver.run();
        });

        JLabel leyendaMapa = new JLabel("J jugador · △ aliado · ◆ enemigo · 🔥 fuego · ? oscuridad · "
                + "antorcha naranja · fuente azul · suelo marrón madera");
        leyendaMapa.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));
        JPanel mapaConLeyenda = new JPanel(new BorderLayout());
        mapaConLeyenda.add(scrollMapa, BorderLayout.CENTER);
        mapaConLeyenda.add(leyendaMapa, BorderLayout.SOUTH);

        comando = new JTextField();
        comando.setName("comando.entrada");
        SoporteTecladoNumerico.instalar(comando);
        comando.addActionListener(e -> ejecutarTexto());
        ejecutar = new JButton("Ejecutar comando");
        ejecutar.addActionListener(e -> ejecutarTexto());
        reproducir = new JButton("▶ Turbo");
        reproducir.setName("espectador.play");
        reproducir.addActionListener(e -> alternarReproduccion());
        temporizadorEspectador = new Timer(RETARDO_TURBO_MS, e -> avanzarReproduccion());
        temporizadorEspectador.setInitialDelay(0);
        resultadoEspectador = new JLabel("", JLabel.CENTER);
        resultadoEspectador.setName("espectador.resultado");
        resultadoEspectador.setFont(resultadoEspectador.getFont().deriveFont(
                java.awt.Font.BOLD, 17f));
        JPanel entrada = new JPanel(new BorderLayout(6, 0));
        entrada.add(new JLabel("Comando:"), BorderLayout.WEST);
        entrada.add(comando, BorderLayout.CENTER);
        JPanel controles = new JPanel(new java.awt.GridLayout(1, 2, 6, 0));
        controles.add(reproducir);
        controles.add(ejecutar);
        entrada.add(controles, BorderLayout.EAST);
        entrada.add(resultadoEspectador, BorderLayout.SOUTH);

        escritorio = new JDesktopPane();
        escritorio.setName("juego.escritorio");
        escritorio.setPreferredSize(new java.awt.Dimension(1460, 860));
        escritorio.setBackground(new Color(31, 36, 46));
        add(escritorio, BorderLayout.CENTER);
        agregarVentana("Mapa tactico", "ventana.mapa", mapaConLeyenda,
                8, 8, 870, 570);
        agregarVentana("Estado del escuadron", "ventana.estado", panelEstado,
                888, 8, 540, 210);
        agregarVentana("Acciones", "ventana.acciones", new JScrollPane(panelAcciones),
                888, 228, 540, 340);
        agregarVentana("Registro de eventos", "ventana.registro", registro,
                8, 588, 870, 230);
        agregarVentana("Comandos y reproduccion", "ventana.comandos", entrada,
                888, 588, 540, 150);

        consola.getHistorial().forEach(this::agregarMensaje);
        consola.setReceptor(this::agregarMensaje);
        actualizarVista();
        SwingUtilities.invokeLater(() -> {
            centrarJugador();
            comando.requestFocusInWindow();
        });
    }

    /** @return boton de coger */
    public JButton getCoger() { return coger; }
    /** @param coger boton no nulo */
    public void setCoger(JButton coger) { this.coger = Validaciones.noNulo(coger, "Boton coger"); }
    /** @return boton de usar */
    public JButton getUsar() { return usar; }
    /** @param usar boton no nulo */
    public void setUsar(JButton usar) { this.usar = Validaciones.noNulo(usar, "Boton usar"); }
    /** @return boton de tirar */
    public JButton getTirar() { return tirar; }
    /** @param tirar boton no nulo */
    public void setTirar(JButton tirar) { this.tirar = Validaciones.noNulo(tirar, "Boton tirar"); }
    /** @return boton de equipar */
    public JButton getEquipar() { return equipar; }
    /** @param equipar boton no nulo */
    public void setEquipar(JButton equipar) {
        this.equipar = Validaciones.noNulo(equipar, "Boton equipar");
    }
    /** @return boton de desequipar */
    public JButton getDesequipar() { return desequipar; }
    /** @param desequipar boton no nulo */
    public void setDesequipar(JButton desequipar) {
        this.desequipar = Validaciones.noNulo(desequipar, "Boton desequipar");
    }
    /** @return boton de atacar */
    public JButton getAtacar() { return atacar; }
    /** @param atacar boton no nulo */
    public void setAtacar(JButton atacar) { this.atacar = Validaciones.noNulo(atacar, "Boton atacar"); }
    /** @return boton de lanzamiento */
    public JButton getLanzarExplosivo() { return lanzarExplosivo; }
    /** @param lanzarExplosivo boton no nulo */
    public void setLanzarExplosivo(JButton lanzarExplosivo) {
        this.lanzarExplosivo = Validaciones.noNulo(lanzarExplosivo, "Boton lanzar explosivo");
    }
    /** @return boton de ayuda */
    public JButton getPedirAyuda() { return pedirAyuda; }
    /** @param pedirAyuda boton no nulo */
    public void setPedirAyuda(JButton pedirAyuda) {
        this.pedirAyuda = Validaciones.noNulo(pedirAyuda, "Boton pedir ayuda");
    }
    /** @return boton que reproduce los turnos posteriores a la muerte del jugador */
    public JButton getReproducir() { return reproducir; }

    /** @return etiqueta persistente con el bando ganador */
    public JLabel getResultadoEspectador() { return resultadoEspectador; }

    /** @return intervalo en milisegundos de la reproduccion turbo */
    public int getRetardoReproduccion() { return temporizadorEspectador.getDelay(); }

    /** @return superficie que contiene todas las ventanas movibles */
    public JDesktopPane getEscritorio() { return escritorio; }

    private void agregarVentana(String titulo, String nombre, java.awt.Component contenido,
            int x, int y, int ancho, int alto) {
        JInternalFrame ventana = new JInternalFrame(titulo, true, false, true, true);
        ventana.setName(nombre);
        ventana.setContentPane(contenido instanceof java.awt.Container contenedor
                ? contenedor : envolver(contenido));
        ventana.setBounds(x, y, ancho, alto);
        ventana.setMinimumSize(new java.awt.Dimension(260, 120));
        ventana.setVisible(true);
        escritorio.add(ventana);
    }

    private JPanel envolver(java.awt.Component componente) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(componente, BorderLayout.CENTER);
        return panel;
    }
    private PanelAcciones crearPanelAcciones(Runnable volver) {
        PanelAcciones acciones = new PanelAcciones(this::ejecutar,
                this::cogerObjeto, this::usarObjeto, this::tirarObjeto,
                this::equiparObjeto, this::desequiparObjeto,
                this::atacarEnemigo, this::lanzarExplosivo, volver);
        setCoger(acciones.getCoger());
        setUsar(acciones.getUsar());
        setTirar(acciones.getTirar());
        setEquipar(acciones.getEquipar());
        setDesequipar(acciones.getDesequipar());
        setAtacar(acciones.getAtacar());
        setLanzarExplosivo(acciones.getLanzarExplosivo());
        setPedirAyuda(acciones.getPedirAyuda());
        return acciones;
    }

    private void cogerObjeto() {
        seleccionarYEjecutar("Coger objeto", "coger", objetosCeldaJugadorVisibles().stream()
                .map(objeto -> new OpcionAccion(describir(objeto), "coger " + objeto.getNombre()))
                .toList());
    }

    private void usarObjeto() {
        List<OpcionAccion> opciones = new ArrayList<>(objetosMochila().stream()
                .filter(objeto -> !(objeto instanceof Arma)
                        && !(objeto instanceof Armadura)
                        && !(objeto instanceof Explosivo))
                .map(objeto -> new OpcionAccion(describir(objeto), "usar " + objeto.getNombre()))
                .toList());
        Binocular equipado = motor.getJuego().getJugador().getBinocularEquipado();
        if (equipado != null) {
            opciones.add(new OpcionAccion(describir(equipado) + " [equipado]",
                    "usar " + equipado.getNombre()));
        }
        seleccionarYEjecutar("Usar objeto", "usar", opciones);
    }

    private void tirarObjeto() {
        seleccionarYEjecutar("Tirar objeto", "tirar", objetosMochila().stream()
                .map(objeto -> new OpcionAccion(describir(objeto), "tirar " + objeto.getNombre()))
                .toList());
    }

    private void equiparObjeto() {
        seleccionarYEjecutar("Equipar objeto", "equipar", objetosMochila().stream()
                .filter(objeto -> objeto instanceof Arma
                        || objeto instanceof Armadura || objeto instanceof Binocular)
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
        if (jugador.getBinocularEquipado() != null) {
            Binocular binocular = jugador.getBinocularEquipado();
            opciones.add(new OpcionAccion(describir(binocular), "desequipar " + binocular.getNombre()));
        }
        seleccionarYEjecutar("Desequipar objeto", "desequipar", opciones);
    }

    private void atacarEnemigo() {
        Posicion origen = motor.getJuego().getJugador().getPosicion();
        List<OpcionAccion> opciones = new ArrayList<>();
        Set<Posicion> destinosIncluidos = new HashSet<>();
        for (Enemigo enemigo : motor.getJuego().getEnemigos()) {
            Posicion destino = enemigo.getPosicion();
            if (enemigo.getSalud() <= 0 || !destinosIncluidos.add(destino)) {
                continue;
            }
            String alcance = alcanceAtaque(origen, destino);
            if (alcance != null && motor.getJuego().getMapa().hayLineaAtaque(origen, destino)) {
                List<Enemigo> enemigosCelda = motor.getJuego().getMapa().getCelda(destino).getEnemigos().stream()
                        .filter(objetivo -> objetivo.getSalud() > 0)
                        .toList();
                String nombres = enemigosCelda.stream().map(Enemigo::getNombre)
                        .collect(java.util.stream.Collectors.joining(", "));
                String comandoAtaque = "atacar " + (alcance.isBlank() ? "" : alcance + " ") + "todos";
                opciones.add(new OpcionAccion(
                        nombres + " - " + enemigosCelda.size() + " enemigo(s) - "
                                + describirDistancia(origen, destino),
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

    private List<Objeto> objetosCeldaJugadorVisibles() {
        Posicion posicion = motor.getJuego().getJugador().getPosicion();
        return motor.getJuego().isCeldaInspeccionada(posicion)
                ? celdaJugador().getObjetos()
                : List.of();
    }

    private String describir(Objeto objeto) {
        if (objeto instanceof Arma arma) {
            return arma.estadoArma() + " | " + arma.getClaseArma().name().toLowerCase()
                    + " | " + String.format("%.1f", arma.getPeso()) + " kg";
        }
        if (objeto instanceof Armadura armadura) {
            return armadura.getNombre() + " | defensa " + armadura.getDefensa()
                    + " | salud +" + armadura.getBonusSalud() + " | energia +"
                    + armadura.getBonusEnergia() + " | "
                    + String.format("%.1f", armadura.getPeso()) + " kg";
        }
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

    private void alternarReproduccion() {
        if (temporizadorEspectador.isRunning()) {
            detenerReproduccion();
        } else if (motor.isModoEspectadorDisponible()) {
            reproducir.setText("⏸ Pausa turbo");
            temporizadorEspectador.start();
        }
    }

    private void avanzarReproduccion() {
        boolean continua = motor.avanzarTurnoEspectador();
        actualizarVista();
        centrarJugador();
        if (!continua) {
            detenerReproduccion();
        }
    }

    private void detenerReproduccion() {
        if (temporizadorEspectador != null) {
            temporizadorEspectador.stop();
        }
        if (reproducir != null) {
            reproducir.setText("▶ Turbo");
        }
    }

    /** Actualiza controles y mapa tras un cambio de estado externo al panel. */
    public void refrescarVista() {
        actualizarVista();
    }

    private void actualizarVista() {
        panelEstado.actualizar(motor);
        mapaPanel.repaint();
        boolean espectador = motor.isModoEspectadorDisponible();
        boolean activa = !motor.isFinalizada() && !espectador;
        MotorPartida.ResultadoBatalla resultado = motor.getResultadoBatalla();
        resultadoEspectador.setVisible(resultado != null);
        resultadoEspectador.setText(resultado == null ? "" : resultado.getEtiqueta());
        resultadoEspectador.setForeground(resultado == MotorPartida.ResultadoBatalla.VICTORIA_HUMANA
                ? new Color(20, 125, 55) : new Color(190, 45, 45));
        reproducir.setVisible(espectador
                || temporizadorEspectador.isRunning() && resultado == null);
        reproducir.setEnabled(espectador);
        comando.setEnabled(activa);
        ejecutar.setEnabled(activa);
        coger.setEnabled(activa && !objetosCeldaJugadorVisibles().isEmpty());
        usar.setEnabled(activa && objetosMochila().stream()
                .anyMatch(objeto -> !(objeto instanceof Arma)
                        && !(objeto instanceof Armadura)
                        && !(objeto instanceof Explosivo)));
        tirar.setEnabled(activa && !objetosMochila().isEmpty());
        equipar.setEnabled(activa && objetosMochila().stream()
                .anyMatch(objeto -> objeto instanceof Arma
                        || objeto instanceof Armadura || objeto instanceof Binocular));
        Personaje jugador = motor.getJuego().getJugador();
        desequipar.setEnabled(activa && (!jugador.getArmasEquipadas().isEmpty()
                || jugador.getArmaduraEquipada() != null || jugador.getBinocularEquipado() != null));
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
        Posicion centro = motor.isModoEspectadorDisponible()
                ? motor.getJuego().getAliados().stream().filter(aliado -> aliado.getSalud() > 0)
                        .map(Personaje::getPosicion).findFirst()
                        .orElse(motor.getJuego().getJugador().getPosicion())
                : motor.getJuego().getJugador().getPosicion();
        int x = centro.getColumna() * 32;
        int y = centro.getFila() * 32;
        mapaPanel.scrollRectToVisible(new java.awt.Rectangle(x - 160, y - 120, 352, 272));
    }

    private void agregarMensaje(ConsolaGrafica.Mensaje mensaje) {
        registro.agregar(mensaje);
    }

    private record OpcionAccion(String etiqueta, String comando) {
        @Override
        public String toString() {
            return etiqueta;
        }
    }
}
