package com.legendoftecla.gui;

import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.loader.EscenarioDefinicion;
import com.legendoftecla.loader.SerializadorEscenarioJson;

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
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.nio.file.Path;
import java.util.function.BiConsumer;

/** Editor visual de escenarios completos guardados en escenario.json. */
public final class PanelEditorMapa extends JPanel {
    private enum Herramienta {
        SUELO("Suelo transitable"),
        MURO("Muro"),
        INICIO("Inicio del jugador"),
        OBJETIVO("Objetivo"),
        ENEMIGO("Anadir enemigo"),
        OBJETO("Anadir objeto"),
        EDITAR_CELDA("Editar celda"),
        BORRAR("Borrar contenido");

        private final String etiqueta;

        Herramienta(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        @Override
        public String toString() {
            return etiqueta;
        }
    }

    /**
     * Valor publico {@code escenarioGuardado} utilizado por el modelo del juego.
     */
    private final BiConsumer<Path, Boolean> escenarioGuardado;
    /**
     * Valor publico {@code volver} utilizado por el modelo del juego.
     */
    private final Runnable volver;
    /**
     * Ejecuta la operacion publica {@code JTextField}.
     */
    private final JTextField nombre = new JTextField(18);
    /**
     * Ejecuta la operacion publica {@code JTextField}.
     */
    private final JTextField descripcion = new JTextField(26);
    /**
     * Ejecuta la operacion publica {@code SpinnerNumberModel}.
     */
    private final JSpinner pasos = new JSpinner(new SpinnerNumberModel(160, 1, 100000, 10));
    /**
     * Ejecuta la operacion publica {@code SpinnerNumberModel}.
     */
    private final JSpinner filas = ControlesNumericos.entero("editor.dimensiones.filas", 10, 3, 100, 1);
    /**
     * Ejecuta la operacion publica {@code SpinnerNumberModel}.
     */
    private final JSpinner columnas = ControlesNumericos.entero("editor.dimensiones.columnas", 10, 3, 100, 1);
    /**
     * Ejecuta la operacion publica {@code values}.
     */
    private final JComboBox<Herramienta> herramienta = new JComboBox<>(Herramienta.values());
    /**
     * Ejecuta la operacion publica {@code JPanel}.
     */
    private final JPanel cuadricula = new JPanel();
    /**
     * Valor publico {@code escenario} utilizado por el modelo del juego.
     */
    private EscenarioDefinicion escenario;
    /**
     * Valor publico {@code directorioActual} utilizado por el modelo del juego.
     */
    private Path directorioActual;
    /** Selector persistido que activa la generacion automatica de aliados. */
    private final JCheckBox conAliados = new JCheckBox("Incluir aliados automaticos");

    /**
     * Crea una instancia de {@code PanelEditorMapa}.
      * @param escenarioGuardado valor de {@code escenarioGuardado}
      * @param volver valor de {@code volver}
     */
    public PanelEditorMapa(BiConsumer<Path, Boolean> escenarioGuardado, Runnable volver) {
        super(new BorderLayout(6, 6));
        this.escenarioGuardado = escenarioGuardado;
        this.volver = volver;
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        escenario = EscenarioDefinicion.nuevo(10, 10);

        add(crearCabecera(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(cuadricula);
        scroll.getViewport().setBackground(new Color(35, 39, 45));
        add(scroll, BorderLayout.CENTER);
        add(crearLeyenda(), BorderLayout.SOUTH);
        cargarCamposDesdeModelo();
        reconstruirCuadricula();
    }

    private JPanel crearCabecera() {
        JPanel contenedor = new JPanel(new BorderLayout(4, 4));
        JLabel titulo = new JLabel("Editor grafico de escenarios completos");
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 20f));
        contenedor.add(titulo, BorderLayout.NORTH);

        JPanel metadatos = new JPanel(new FlowLayout(FlowLayout.LEFT));
        metadatos.add(new JLabel("Nombre:"));
        metadatos.add(nombre);
        metadatos.add(new JLabel("Descripcion:"));
        metadatos.add(descripcion);
        metadatos.add(new JLabel("Pasos max.:"));
        metadatos.add(pasos);
        metadatos.add(conAliados);
        contenedor.add(metadatos, BorderLayout.CENTER);

        JToolBar barra = new JToolBar();
        barra.setFloatable(false);
        barra.add(new JLabel("Tamano "));
        barra.add(filas);
        barra.add(new JLabel(" x "));
        barra.add(columnas);
        JButton nuevo = new JButton("Nuevo mapa");
        nuevo.addActionListener(e -> crearNuevoMapa());
        barra.add(nuevo);
        barra.addSeparator();
        barra.add(new JLabel("Herramienta: "));
        barra.add(herramienta);
        barra.addSeparator();
        JButton cargar = new JButton("Abrir JSON");
        cargar.addActionListener(e -> cargarEscenario());
        barra.add(cargar);
        JButton guardar = new JButton("Guardar");
        guardar.addActionListener(e -> guardarEscenario(false));
        barra.add(guardar);
        JButton guardarComo = new JButton("Guardar como...");
        guardarComo.addActionListener(e -> guardarEscenario(true));
        barra.add(guardarComo);
        barra.addSeparator();
        JButton regresar = new JButton("Volver al menu");
        regresar.addActionListener(e -> volver.run());
        barra.add(regresar);
        contenedor.add(barra, BorderLayout.SOUTH);
        return contenedor;
    }

    private JPanel crearLeyenda() {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 3));
        leyenda.setBorder(BorderFactory.createTitledBorder("Leyenda"));
        leyenda.add(new JLabel("Verde: inicio"));
        leyenda.add(new JLabel("Dorado: objetivo"));
        leyenda.add(new JLabel("Rojo: enemigo"));
        leyenda.add(new JLabel("Amarillo: objeto"));
        leyenda.add(new JLabel("Gris oscuro: muro"));
        return leyenda;
    }

    private void crearNuevoMapa() {
        int respuesta = JOptionPane.showConfirmDialog(this,
                "Se reemplazara el mapa actual. ¿Continuar?", "Nuevo mapa", JOptionPane.YES_NO_OPTION);
        if (respuesta != JOptionPane.YES_OPTION) {
            return;
        }
        int nuevasFilas = numero(filas);
        int nuevasColumnas = numero(columnas);
        escenario = EscenarioDefinicion.nuevo(nuevasFilas, nuevasColumnas);
        directorioActual = null;
        cargarCamposDesdeModelo();
        reconstruirCuadricula();
    }

    private void reconstruirCuadricula() {
        cuadricula.removeAll();
        cuadricula.setLayout(new GridLayout(escenario.filas, escenario.columnas, 1, 1));
        for (int fila = 0; fila < escenario.filas; fila++) {
            for (int columna = 0; columna < escenario.columnas; columna++) {
                int f = fila;
                int c = columna;
                JButton celda = new JButton();
                celda.setPreferredSize(new Dimension(42, 42));
                celda.setMargin(new java.awt.Insets(1, 1, 1, 1));
                celda.addActionListener(e -> aplicarHerramienta(f, c));
                actualizarBoton(celda, fila, columna);
                cuadricula.add(celda);
            }
        }
        cuadricula.revalidate();
        cuadricula.repaint();
    }

    private void aplicarHerramienta(int fila, int columna) {
        Herramienta seleccion = (Herramienta) herramienta.getSelectedItem();
        EscenarioDefinicion.CeldaDef celda = asegurarCelda(fila, columna);
        try {
            switch (seleccion) {
                case SUELO -> celda.transitable = true;
                case MURO -> convertirEnMuro(fila, columna, celda);
                case INICIO -> {
                    celda.transitable = true;
                    escenario.inicio = new EscenarioDefinicion.Punto(fila, columna);
                }
                case OBJETIVO -> {
                    celda.transitable = true;
                    escenario.objetivo = new EscenarioDefinicion.Punto(fila, columna);
                }
                case ENEMIGO -> anadirPersonaje(fila, columna);
                case OBJETO -> anadirObjeto(fila, columna);
                case EDITAR_CELDA -> editarCelda(celda);
                case BORRAR -> borrarContenido(fila, columna);
            }
            reconstruirCuadricula();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Operacion no valida", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void convertirEnMuro(int fila, int columna, EscenarioDefinicion.CeldaDef celda) {
        if (esPunto(escenario.inicio, fila, columna) || esPunto(escenario.objetivo, fila, columna)) {
            throw new IllegalArgumentException("Mueve primero el inicio o el objetivo a otra celda.");
        }
        borrarContenido(fila, columna);
        celda.transitable = false;
        celda.descripcion = "Muro";
    }

    private void anadirPersonaje(int fila, int columna) {
        exigirSuelo(fila, columna);
        JComboBox<String> tipo = new JComboBox<>(new String[]{"sectoid", "lightfloater", "heavyfloater"});
        JTextField nombrePersonaje = new JTextField("Enemigo");
        JSpinner salud = new JSpinner(new SpinnerNumberModel(70, 1, 5000, 5));
        JSpinner energia = new JSpinner(new SpinnerNumberModel(70, 1, 5000, 5));
        JSpinner vision = new JSpinner(new SpinnerNumberModel(3, 1, 100, 1));
        JPanel formulario = formulario(
                new JLabel("Tipo:"), tipo,
                new JLabel("Nombre:"), nombrePersonaje,
                new JLabel("Salud:"), salud,
                new JLabel("Energia:"), energia,
                new JLabel("Vision:"), vision);
        int respuesta = JOptionPane.showConfirmDialog(this, formulario,
                "Configurar enemigo",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }
        if (nombrePersonaje.getText().isBlank()) {
            throw new IllegalArgumentException("El personaje necesita un nombre.");
        }
        EscenarioDefinicion.PersonajeDef personaje = new EscenarioDefinicion.PersonajeDef();
        personaje.fila = fila;
        personaje.columna = columna;
        personaje.tipo = (String) tipo.getSelectedItem();
        personaje.nombre = nombrePersonaje.getText().trim();
        personaje.salud = numero(salud);
        personaje.energia = numero(energia);
        personaje.vision = numero(vision);
        escenario.enemigos.add(personaje);
    }

    private void anadirObjeto(int fila, int columna) {
        exigirSuelo(fila, columna);
        JComboBox<String> tipo = new JComboBox<>(new String[]{
                "botiquin", "arma", "armadura", "binocular", "torito", "explosivo"});
        JTextField nombreObjeto = new JTextField("Objeto");
        JTextField descripcionObjeto = new JTextField("Objeto personalizado");
        JSpinner peso = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1000.0, 0.1));
        JSpinner valor = new JSpinner(new SpinnerNumberModel(20, 0, 5000, 1));
        JSpinner valor2 = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
        JSpinner valor3 = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
        JCheckBox dosManos = new JCheckBox("Arma a dos manos");
        JLabel ayuda = new JLabel("Valor: cura/danio/defensa/rango/energia; valores 2 y 3: bonus salud y energia de armadura");
        JPanel formulario = formulario(
                new JLabel("Tipo:"), tipo,
                new JLabel("Nombre:"), nombreObjeto,
                new JLabel("Descripcion:"), descripcionObjeto,
                new JLabel("Peso:"), peso,
                new JLabel("Valor principal:"), valor,
                new JLabel("Valor secundario:"), valor2,
                new JLabel("Valor terciario:"), valor3,
                new JLabel("Opciones:"), dosManos,
                new JLabel("Ayuda:"), ayuda);
        int respuesta = JOptionPane.showConfirmDialog(this, formulario, "Configurar objeto",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (respuesta != JOptionPane.OK_OPTION) {
            return;
        }
        if (nombreObjeto.getText().isBlank()) {
            throw new IllegalArgumentException("El objeto necesita un nombre.");
        }
        EscenarioDefinicion.ObjetoDef objeto = new EscenarioDefinicion.ObjetoDef();
        objeto.fila = fila;
        objeto.columna = columna;
        objeto.tipo = (String) tipo.getSelectedItem();
        objeto.nombre = nombreObjeto.getText().trim();
        objeto.descripcion = descripcionObjeto.getText().trim();
        objeto.peso = ((Number) peso.getValue()).doubleValue();
        objeto.valor = numero(valor);
        objeto.valorSecundario = numero(valor2);
        objeto.valorTerciario = numero(valor3);
        objeto.dosManos = dosManos.isSelected();
        escenario.objetos.add(objeto);
    }

    private void editarCelda(EscenarioDefinicion.CeldaDef celda) {
        JTextField texto = new JTextField(celda.descripcion, 25);
        JCheckBox transitable = new JCheckBox("Transitable", celda.transitable);
        JPanel formulario = formulario(new JLabel("Descripcion:"), texto, new JLabel("Tipo:"), transitable);
        if (JOptionPane.showConfirmDialog(this, formulario, "Editar celda",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (!transitable.isSelected()
                    && (esPunto(escenario.inicio, celda.fila, celda.columna)
                    || esPunto(escenario.objetivo, celda.fila, celda.columna))) {
                throw new IllegalArgumentException("Inicio y objetivo deben ser transitables.");
            }
            celda.descripcion = texto.getText().trim();
            celda.transitable = transitable.isSelected();
            if (!celda.transitable) {
                borrarContenido(celda.fila, celda.columna);
            }
        }
    }

    private void borrarContenido(int fila, int columna) {
        escenario.enemigos.removeIf(p -> p.fila == fila && p.columna == columna);
        escenario.objetos.removeIf(p -> p.fila == fila && p.columna == columna);
    }

    private void actualizarBoton(JButton boton, int fila, int columna) {
        EscenarioDefinicion.CeldaDef celda = asegurarCelda(fila, columna);
        boton.setOpaque(true);
        boton.setForeground(Color.WHITE);
        boton.setBackground(celda.transitable ? new Color(70, 78, 88) : new Color(35, 38, 43));
        String simbolo = "";
        if (esPunto(escenario.inicio, fila, columna)) {
            boton.setBackground(new Color(48, 145, 92));
            simbolo += "▶";
        }
        if (esPunto(escenario.objetivo, fila, columna)) {
            boton.setBackground(new Color(185, 135, 35));
            simbolo += "★";
        }
        if (escenario.enemigos.stream().anyMatch(p -> p.fila == fila && p.columna == columna)) {
            boton.setBackground(new Color(170, 55, 60));
            simbolo += "◆";
        }
        if (escenario.objetos.stream().anyMatch(p -> p.fila == fila && p.columna == columna)) {
            simbolo += "■";
        }
        boton.setText(simbolo);
        boton.setToolTipText(crearTooltip(fila, columna, celda));
    }

    private String crearTooltip(int fila, int columna, EscenarioDefinicion.CeldaDef celda) {
        long enemigos = escenario.enemigos.stream().filter(p -> p.fila == fila && p.columna == columna).count();
        long objetos = escenario.objetos.stream().filter(p -> p.fila == fila && p.columna == columna).count();
        return "<html><b>" + fila + "," + columna + "</b> " + celda.descripcion
                + "<br>" + (celda.transitable ? "Transitable" : "Muro")
                + "<br>Enemigos: " + enemigos + " | Objetos: " + objetos + "</html>";
    }

    private void guardarEscenario(boolean elegirDirectorio) {
        try {
            sincronizarMetadatos();
            if (directorioActual == null || elegirDirectorio) {
                JFileChooser selector = new JFileChooser();
                selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                selector.setDialogTitle("Directorio donde guardar escenario.json");
                if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {
                    return;
                }
                directorioActual = selector.getSelectedFile().toPath();
            }
            Path archivo = SerializadorEscenarioJson.guardar(escenario, directorioActual);
            JOptionPane.showMessageDialog(this, "Escenario guardado en:\n" + archivo,
                    "Guardado correcto", JOptionPane.INFORMATION_MESSAGE);
            escenarioGuardado.accept(archivo.getParent(), escenario.conAliados);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarEscenario() {
        JFileChooser selector = new JFileChooser();
        selector.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        selector.setDialogTitle("Selecciona escenario.json o su directorio");
        if (selector.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            Path ruta = selector.getSelectedFile().toPath();
            escenario = SerializadorEscenarioJson.cargar(ruta);
            directorioActual = ruta.getFileName().toString().toLowerCase().endsWith(".json")
                    ? ruta.getParent() : ruta;
            cargarCamposDesdeModelo();
            reconstruirCuadricula();
        } catch (JuegoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo abrir", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCamposDesdeModelo() {
        nombre.setText(escenario.nombre);
        descripcion.setText(escenario.descripcion);
        pasos.setValue(escenario.pasosMaximos);
        filas.setValue(escenario.filas);
        columnas.setValue(escenario.columnas);
        conAliados.setSelected(escenario.conAliados);
    }

    private void sincronizarMetadatos() {
        if (nombre.getText().isBlank()) {
            throw new IllegalArgumentException("El escenario necesita un nombre.");
        }
        escenario.nombre = nombre.getText().trim();
        escenario.descripcion = descripcion.getText().trim();
        escenario.pasosMaximos = numero(pasos);
        escenario.conAliados = conAliados.isSelected();
    }

    private EscenarioDefinicion.CeldaDef asegurarCelda(int fila, int columna) {
        EscenarioDefinicion.CeldaDef celda = escenario.celda(fila, columna);
        if (celda == null) {
            celda = new EscenarioDefinicion.CeldaDef(fila, columna, "Celda " + fila + "," + columna, true);
            escenario.celdas.add(celda);
        }
        return celda;
    }

    private void exigirSuelo(int fila, int columna) {
        if (!asegurarCelda(fila, columna).transitable) {
            throw new IllegalArgumentException("No se pueden colocar elementos sobre un muro.");
        }
    }

    private boolean esPunto(EscenarioDefinicion.Punto punto, int fila, int columna) {
        return punto != null && punto.fila == fila && punto.columna == columna;
    }

    private int numero(JSpinner spinner) {
        return ControlesNumericos.valorEntero(spinner);
    }

    private JPanel formulario(java.awt.Component... componentes) {
        JPanel panel = new JPanel(new GridLayout(0, 2, 6, 6));
        for (java.awt.Component componente : componentes) {
            if (componente != null) {
                panel.add(componente);
            }
        }
        return panel;
    }
}
