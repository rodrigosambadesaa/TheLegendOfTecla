package com.legendoftecla.gui;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.CondicionVictoria;
import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.loader.SerializadorEscenarioJson;
import com.legendoftecla.model.world.DimensionesMapa;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.util.function.Consumer;

/** Asistente grafico para los modos predeterminado, grande, procedural y ficheros. */
public final class PanelConfiguracion extends JPanel {
    private record Opcion(String etiqueta, String valor) {
        @Override
        public String toString() {
            return etiqueta;
        }
    }

    /**
     * Ejecuta la operacion publica {@code JTextField}.
     */
    private final JTextField nombre = new JTextField("Tecla", 24);
    /**
     * Valor publico {@code clase} utilizado por el modelo del juego.
     */
    private final JComboBox<Opcion> clase = new JComboBox<>(new Opcion[]{
            new Opcion("Marine", "marine"),
            new Opcion("Francotirador", "francotirador"),
            new Opcion("Zapador", "zapador")
    });
    /**
     * Valor publico {@code modo} utilizado por el modelo del juego.
     */
    private final JComboBox<Opcion> modo = new JComboBox<>(new Opcion[]{
            new Opcion("Mapa predeterminado", "default"),
            new Opcion("Mapa grande (50 variantes)", "grande"),
            new Opcion("Escenario desde ficheros / JSON", "ficheros"),
            new Opcion("Mapa procedural por semilla", "procedural")
    });
    /**
     * Ejecuta la operacion publica {@code values}.
     */
    private final JComboBox<Dificultad> dificultad = new JComboBox<>(Dificultad.values());
    /** Selector editable para el numero de filas del mapa. */
    private final JSpinner filas = ControlesNumericos.entero("dimensiones.filas", 10, 3, 100, 1);
    /** Selector editable para el numero de columnas del mapa. */
    private final JSpinner columnas = ControlesNumericos.entero("dimensiones.columnas", 10, 3, 100, 1);
    /** Selector para activar aliados calculados automaticamente. */
    private final JCheckBox conAliados = new JCheckBox("Incluir aliados");
    /** Permite elegir entre el calculo del juego y una cantidad indicada. */
    private final JComboBox<Opcion> modoAliados = new JComboBox<>(new Opcion[]{
            new Opcion("Cantidad calculada por el juego", "auto"),
            new Opcion("Cantidad especificada", "manual")
    });
    /** Cantidad exacta cuando se selecciona el modo manual. */
    private final JSpinner cantidadAliados = ControlesNumericos.entero(
            "aliados.cantidad", 1, 1, com.legendoftecla.validation.Limites.ALIADOS_MAXIMOS, 1);
    /** Nivel comun; cero conserva el nivel automatico. */
    private final JSpinner nivelAliados = ControlesNumericos.entero(
            "aliados.nivel", 0, 0, com.legendoftecla.validation.Limites.NIVEL_ALIADO_MAXIMO, 1);
    /** Nivel inicial del jugador, disponible en solitario y con aliados. */
    private final JSpinner nivelJugador = ControlesNumericos.entero(
            "jugador.nivel", 1, 1, com.legendoftecla.validation.Limites.NIVEL_ALIADO_MAXIMO, 1);
    /** Autoriza por defecto la sustitucion de armas y armaduras aliadas. */
    private final JCheckBox mejorasAliados = new JCheckBox("Sustituir equipo por mejoras", true);
    /** Autoriza por defecto la entrega automatica de municion al jugador. */
    private final JCheckBox municionAliada = new JCheckBox("Compartir municion", true);
    /** Selector de los participantes que deben alcanzar la salida. */
    private final JComboBox<CondicionVictoria> condicionVictoria =
            new JComboBox<>(CondicionVictoria.values());
    /** Variante determinista del mapa grande. */
    private final JSpinner varianteMapa = ControlesNumericos.entero("mapa.variante", 1, 1, 50, 1);
    /** Semilla reproducible del modo procedural. */
    private final JSpinner seed = ControlesNumericos.entero("mapa.seed", 12345, -1_000_000, 1_000_000, 1);
    /**
     * Ejecuta la operacion publica {@code JTextField}.
     */
    private final JTextField directorio = new JTextField(30);
    /**
     * Ejecuta la operacion publica {@code JButton}.
     */
    private final JButton examinar = new JButton("Examinar...");

    /**
     * Crea una instancia de {@code PanelConfiguracion}.
      * @param abrirEditor valor de {@code abrirEditor}
      * @param iniciar valor de {@code iniciar}
     */
    public PanelConfiguracion(Consumer<ConfiguracionPartida> iniciar, Runnable abrirEditor) {
        super(new BorderLayout());
        conAliados.setName("aliados.activados");
        modoAliados.setName("aliados.modo");
        condicionVictoria.setName("victoria.condicion");
        mejorasAliados.setName("aliados.mejorasEquipo");
        municionAliada.setName("aliados.municionAutomatica");
        JPanel contenidoFormulario = new JPanel(new BorderLayout());
        contenidoFormulario.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titulo = new JLabel("THE LEGEND OF TECLA", SwingConstants.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 28f));
        JLabel subtitulo = new JLabel("Configuracion de partida", SwingConstants.CENTER);
        subtitulo.setFont(subtitulo.getFont().deriveFont(Font.PLAIN, 16f));
        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.add(titulo, BorderLayout.CENTER);
        cabecera.add(subtitulo, BorderLayout.SOUTH);
        contenidoFormulario.add(cabecera, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        int fila = 0;
        agregarFila(formulario, fila++, "Nombre del personaje", nombre);
        agregarFila(formulario, fila++, "Clase", clase);
        agregarFila(formulario, fila++, "Nivel inicial", nivelJugador);
        agregarFila(formulario, fila++, "Modo", modo);
        dificultad.setSelectedItem(Dificultad.NORMAL);
        dificultad.setRenderer((lista, valor, indice, seleccionado, foco) -> {
            JLabel etiqueta = new JLabel(valor == null ? "" : valor.getEtiqueta());
            etiqueta.setOpaque(true);
            if (seleccionado) {
                etiqueta.setBackground(lista.getSelectionBackground());
                etiqueta.setForeground(lista.getSelectionForeground());
            } else {
                etiqueta.setBackground(lista.getBackground());
                etiqueta.setForeground(lista.getForeground());
            }
            return etiqueta;
        });
        agregarFila(formulario, fila++, "Dificultad", dificultad);
        condicionVictoria.setSelectedItem(CondicionVictoria.JUGADOR_Y_ALIADOS);
        agregarFila(formulario, fila++, "Condicion de victoria", condicionVictoria);

        JPanel dimensiones = new JPanel();
        dimensiones.add(filas);
        dimensiones.add(new JLabel("filas  x"));
        dimensiones.add(columnas);
        dimensiones.add(new JLabel("columnas (3-100, se puede escribir)"));
        agregarFila(formulario, fila++, "Dimensiones", dimensiones);
        JPanel aliados = new JPanel();
        aliados.add(conAliados);
        aliados.add(modoAliados);
        aliados.add(cantidadAliados);
        aliados.add(new JLabel("Nivel (0=auto)"));
        aliados.add(nivelAliados);
        aliados.add(mejorasAliados);
        aliados.add(municionAliada);
        agregarFila(formulario, fila++, "Aliados", aliados);
        agregarFila(formulario, fila++, "Variante del mapa", varianteMapa);
        agregarFila(formulario, fila++, "Semilla procedural", seed);

        JPanel selectorDirectorio = new JPanel(new BorderLayout(5, 0));
        selectorDirectorio.add(directorio, BorderLayout.CENTER);
        selectorDirectorio.add(examinar, BorderLayout.EAST);
        agregarFila(formulario, fila, "Directorio del escenario", selectorDirectorio);
        contenidoFormulario.add(formulario, BorderLayout.CENTER);

        examinar.addActionListener(e -> seleccionarDirectorioConDialogo());
        conAliados.addActionListener(e -> actualizarAliados());
        modoAliados.addActionListener(e -> actualizarAliados());
        modo.addActionListener(e -> actualizarModo());
        actualizarModo();
        actualizarAliados();

        JButton jugar = new JButton("Iniciar partida en GUI");
        jugar.setName("configuracion.iniciar");
        jugar.setFont(jugar.getFont().deriveFont(Font.BOLD, 15f));
        jugar.addActionListener(e -> {
            try {
                iniciar.accept(crearConfiguracion());
            } catch (RuntimeException error) {
                JOptionPane.showMessageDialog(this, error.getMessage(),
                        "Configuracion no valida", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton editor = new JButton("Editor grafico de mapas");
        editor.setName("configuracion.editor");
        editor.addActionListener(e -> abrirEditor.run());
        JPanel botones = new JPanel();
        botones.add(jugar);
        botones.add(editor);
        contenidoFormulario.add(botones, BorderLayout.SOUTH);

        JScrollPane desplazamiento = new JScrollPane(contenidoFormulario,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        desplazamiento.setName("configuracion.scroll");
        desplazamiento.setBorder(null);
        desplazamiento.getVerticalScrollBar().setUnitIncrement(20);
        desplazamiento.getHorizontalScrollBar().setUnitIncrement(20);
        add(desplazamiento, BorderLayout.CENTER);
        SoporteTecladoNumerico.instalar(this);
    }

    /**
     * Ejecuta la operacion publica {@code seleccionarDirectorio}.
      * @param ruta valor de {@code ruta}
     */
    public void seleccionarDirectorio(Path ruta) {
        boolean aliadosEscenario = false;
        try {
            aliadosEscenario = SerializadorEscenarioJson.cargar(ruta).isConAliados();
        } catch (Exception ignored) {
            // Los escenarios TXT no contienen estos metadatos JSON.
        }
        seleccionarDirectorio(ruta, aliadosEscenario);
    }

    /**
     * Selecciona un escenario y aplica su preferencia de aliados.
     *
     * @param ruta directorio del escenario
     * @param usarAliados indica si se activara la generacion automatica
     */
    public void seleccionarDirectorio(Path ruta, boolean usarAliados) {
        directorio.setText(ruta.toAbsolutePath().toString());
        conAliados.setSelected(usarAliados);
        modoAliados.setSelectedIndex(0);
        actualizarAliados();
        modo.setSelectedIndex(2);
    }

    private ConfiguracionPartida crearConfiguracion() {
        Opcion claseElegida = (Opcion) clase.getSelectedItem();
        Opcion modoElegido = (Opcion) modo.getSelectedItem();
        DimensionesMapa dimensiones = new DimensionesMapa(
                ControlesNumericos.valorEntero(filas),
                ControlesNumericos.valorEntero(columnas));
        Path ruta = directorio.getText().isBlank() ? null : Path.of(directorio.getText().trim());
        Opcion modoAliadosElegido = (Opcion) modoAliados.getSelectedItem();
        int aliadosSolicitados = !conAliados.isSelected() ? 0
                : modoAliadosElegido != null && "manual".equals(modoAliadosElegido.valor())
                        ? ControlesNumericos.valorEntero(cantidadAliados)
                        : -1;
        ConfiguracionPartida configuracion = new ConfiguracionPartida(
                nombre.getText().trim(),
                claseElegida.valor(),
                modoElegido.valor(),
                (Dificultad) dificultad.getSelectedItem(),
                dimensiones,
                ruta,
                aliadosSolicitados,
                (CondicionVictoria) condicionVictoria.getSelectedItem(),
                ControlesNumericos.valorEntero(varianteMapa));
        configuracion.setSeed(ControlesNumericos.valorEntero(seed));
        configuracion.setNivelAliados(ControlesNumericos.valorEntero(nivelAliados));
        configuracion.setNivelJugador(ControlesNumericos.valorEntero(nivelJugador));
        configuracion.setMejorasEquipoAliado(mejorasAliados.isSelected());
        configuracion.setMunicionAliadaAutomatica(municionAliada.isSelected());
        return configuracion;
    }

    private void seleccionarDirectorioConDialogo() {
        JFileChooser selector = new JFileChooser();
        selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        selector.setDialogTitle("Selecciona el directorio del escenario");
        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            seleccionarDirectorio(selector.getSelectedFile().toPath());
        }
    }

    private void actualizarModo() {
        Opcion seleccion = (Opcion) modo.getSelectedItem();
        boolean usaFicheros = seleccion != null && "ficheros".equals(seleccion.valor());
        boolean usaVariantes = seleccion != null && "grande".equals(seleccion.valor());
        boolean usaSeed = seleccion != null && "procedural".equals(seleccion.valor());
        directorio.setEnabled(usaFicheros);
        examinar.setEnabled(usaFicheros);
        varianteMapa.setEnabled(usaVariantes);
        seed.setEnabled(usaSeed);
        if (usaVariantes) {
            filas.setValue(50);
            columnas.setValue(50);
        } else if (seleccion != null && !usaFicheros) {
            filas.setValue(10);
            columnas.setValue(10);
        }
    }

    private void actualizarAliados() {
        condicionVictoria.setEnabled(conAliados.isSelected());
        modoAliados.setEnabled(conAliados.isSelected());
        Opcion opcion = (Opcion) modoAliados.getSelectedItem();
        cantidadAliados.setEnabled(conAliados.isSelected()
                && opcion != null && "manual".equals(opcion.valor()));
        nivelAliados.setEnabled(conAliados.isSelected());
        mejorasAliados.setEnabled(conAliados.isSelected());
        municionAliada.setEnabled(conAliados.isSelected());
    }

    private void agregarFila(JPanel panel, int fila, String etiqueta, Component componente) {
        GridBagConstraints izquierda = new GridBagConstraints();
        izquierda.gridx = 0;
        izquierda.gridy = fila;
        izquierda.anchor = GridBagConstraints.LINE_END;
        izquierda.insets = new Insets(7, 7, 7, 14);
        panel.add(new JLabel(etiqueta + ":"), izquierda);

        GridBagConstraints derecha = new GridBagConstraints();
        derecha.gridx = 1;
        derecha.gridy = fila;
        derecha.weightx = 1;
        derecha.fill = GridBagConstraints.HORIZONTAL;
        derecha.insets = new Insets(7, 7, 7, 7);
        panel.add(componente, derecha);
    }
}
