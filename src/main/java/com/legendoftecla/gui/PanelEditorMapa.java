package com.legendoftecla.gui;

import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.loader.EscenarioDefinicion;
import com.legendoftecla.loader.SerializadorEscenarioJson;
import com.legendoftecla.validation.Validaciones;

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
        PUERTA("Anadir puerta"),
        TERMINAL("Anadir terminal"),
        INTERRUPTOR("Anadir interruptor"),
        COFRE("Anadir cofre"),
        MINA("Anadir mina"),
        TRAMPA_FUEGO("Anadir trampa de fuego"),
        TRAMPA_VENENO("Anadir trampa de veneno"),
        TRAMPA_ELECTRICA("Anadir trampa electrica"),
        ALARMA("Anadir alarma"),
        COBERTURA("Anadir cobertura completa"),
        PARED_DEBIL("Anadir pared debil"),
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
        setEscenario(EscenarioDefinicion.nuevo(10, 10));
        setDirectorioActual(null);

        add(crearCabecera(), BorderLayout.NORTH);
        JScrollPane scroll = new JScrollPane(cuadricula);
        scroll.getViewport().setBackground(new Color(35, 39, 45));
        add(scroll, BorderLayout.CENTER);
        add(crearLeyenda(), BorderLayout.SOUTH);
        cargarCamposDesdeModelo();
        reconstruirCuadricula();
    }

    /** @return escenario actualmente editado */
    public EscenarioDefinicion getEscenario() {
        return escenario;
    }

    /** @param escenario escenario no nulo */
    public void setEscenario(EscenarioDefinicion escenario) {
        this.escenario = Validaciones.noNulo(escenario, "Escenario del editor");
    }

    /** @return directorio actual o {@code null} */
    public Path getDirectorioActual() {
        return directorioActual;
    }

    /** @param directorioActual directorio opcional normalizado */
    public void setDirectorioActual(Path directorioActual) {
        this.directorioActual = directorioActual == null ? null : directorioActual.normalize();
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
        JButton mision = new JButton("Configurar mision");
        mision.addActionListener(e -> configurarMision());
        barra.add(mision);
        JButton validar = new JButton("Validar escenario");
        validar.addActionListener(e -> validarEscenario());
        barra.add(validar);
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
        setEscenario(EscenarioDefinicion.nuevo(nuevasFilas, nuevasColumnas));
        setDirectorioActual(null);
        cargarCamposDesdeModelo();
        reconstruirCuadricula();
    }

    private void reconstruirCuadricula() {
        cuadricula.removeAll();
        cuadricula.setLayout(new GridLayout(escenario.getFilas(), escenario.getColumnas(), 1, 1));
        for (int fila = 0; fila < escenario.getFilas(); fila++) {
            for (int columna = 0; columna < escenario.getColumnas(); columna++) {
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
                case SUELO -> celda.setTransitable(true);
                case MURO -> convertirEnMuro(fila, columna, celda);
                case INICIO -> {
                    celda.setTransitable(true);
                    escenario.setInicio(new EscenarioDefinicion.Punto(fila, columna));
                }
                case OBJETIVO -> {
                    celda.setTransitable(true);
                    escenario.setObjetivo(new EscenarioDefinicion.Punto(fila, columna));
                }
                case ENEMIGO -> anadirPersonaje(fila, columna);
                case OBJETO -> anadirObjeto(fila, columna);
                case PUERTA -> configurarElemento(celda, "puerta", "CERRADA");
                case TERMINAL -> configurarElemento(celda, "terminal", null);
                case INTERRUPTOR -> configurarElemento(celda, "interruptor", null);
                case COFRE -> configurarElemento(celda, "cofre", null);
                case MINA -> configurarElemento(celda, "mina", null);
                case TRAMPA_FUEGO -> configurarElemento(celda, "trampa_fuego", null);
                case TRAMPA_VENENO -> configurarElemento(celda, "trampa_veneno", null);
                case TRAMPA_ELECTRICA -> configurarElemento(celda, "trampa_electrica", null);
                case ALARMA -> configurarElemento(celda, "alarma", null);
                case COBERTURA -> configurarElemento(celda, "cobertura", null);
                case PARED_DEBIL -> configurarElemento(celda, "pared_debil", null);
                case EDITAR_CELDA -> editarCelda(celda);
                case BORRAR -> borrarContenido(fila, columna);
            }
            reconstruirCuadricula();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Operacion no valida", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void configurarElemento(EscenarioDefinicion.CeldaDef celda,
            String tipo, String estado) {
        celda.setElementoTipo(tipo);
        celda.setElementoId(tipo + "-" + celda.getFila() + "-" + celda.getColumna());
        celda.setElementoEstado(estado);
        celda.setTransitable(true);
    }

    private void validarEscenario() {
        try {
            sincronizarMetadatos();
            SerializadorEscenarioJson.validar(escenario);
            JOptionPane.showMessageDialog(this, "Escenario valido y objetivo alcanzable.");
        } catch (JuegoException | IllegalArgumentException error) {
            JOptionPane.showMessageDialog(this, error.getMessage(), "Escenario no valido",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void convertirEnMuro(int fila, int columna, EscenarioDefinicion.CeldaDef celda) {
        if (esPunto(escenario.getInicio(), fila, columna)
                || esPunto(escenario.getObjetivo(), fila, columna)) {
            throw new IllegalArgumentException("Mueve primero el inicio o el objetivo a otra celda.");
        }
        borrarContenido(fila, columna);
        celda.setTransitable(false);
        celda.setDescripcion("Muro");
    }

    private void anadirPersonaje(int fila, int columna) {
        exigirSuelo(fila, columna);
        JComboBox<String> tipo = new JComboBox<>(new String[]{"sectoid", "lightfloater",
                "heavyfloater", "berserker", "medic", "sniper", "pyro", "scout",
                "commander", "commanderprime", "pyrooverlord"});
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
        personaje.setFila(fila);
        personaje.setColumna(columna);
        personaje.setTipo((String) tipo.getSelectedItem());
        personaje.setNombre(nombrePersonaje.getText());
        personaje.setSalud(numero(salud));
        personaje.setEnergia(numero(energia));
        personaje.setVision(numero(vision));
        escenario.agregarEnemigo(personaje);
    }

    private void anadirObjeto(int fila, int columna) {
        exigirSuelo(fila, columna);
        JComboBox<String> tipo = new JComboBox<>(new String[]{
                "botiquin", "arma", "armadura", "binocular", "torito", "explosivo",
                "granada", "linterna", "cuboagua", "municion", "credencial", "componente"});
        JTextField nombreObjeto = new JTextField("Objeto");
        JTextField descripcionObjeto = new JTextField("Objeto personalizado");
        JSpinner peso = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1000.0, 0.1));
        JSpinner valor = new JSpinner(new SpinnerNumberModel(20, 0, 5000, 1));
        JSpinner valor2 = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
        JSpinner valor3 = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
        JCheckBox dosManos = new JCheckBox("Arma a dos manos");
        JComboBox<String> categoria = new JComboBox<>(new String[]{
                "", "MELE", "ARROJADIZA", "ARCO", "BALLESTA", "FUEGO"});
        JComboBox<String> municion = new JComboBox<>(new String[]{
                "", "INFINITA", "CUCHILLO_ARROJADIZO", "FLECHA", "VIROTE",
                "PISTOLA", "RIFLE", "PESADA", "COHETE", "ENERGIA"});
        JSpinner cargador = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
        JSpinner carga = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
        JSpinner cantidad = new JSpinner(new SpinnerNumberModel(0, 0, 5000, 1));
        JComboBox<String> granada = new JComboBox<>(new String[]{
                "FRAGMENTACION", "INCENDIARIA", "HUMO", "ATURDIDORA"});
        JLabel ayuda = new JLabel("Para armas finitas indica familia, municion y cargador; cantidad se usa en paquetes.");
        JPanel formulario = formulario(
                new JLabel("Tipo:"), tipo,
                new JLabel("Nombre:"), nombreObjeto,
                new JLabel("Descripcion:"), descripcionObjeto,
                new JLabel("Peso:"), peso,
                new JLabel("Valor principal:"), valor,
                new JLabel("Valor secundario:"), valor2,
                new JLabel("Valor terciario:"), valor3,
                new JLabel("Opciones:"), dosManos,
                new JLabel("Categoria de arma:"), categoria,
                new JLabel("Tipo de municion:"), municion,
                new JLabel("Capacidad / carga:"), formulario(cargador, carga),
                new JLabel("Cantidad de municion:"), cantidad,
                new JLabel("Tipo de granada:"), granada,
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
        objeto.setFila(fila);
        objeto.setColumna(columna);
        objeto.setTipo((String) tipo.getSelectedItem());
        objeto.setNombre(nombreObjeto.getText());
        objeto.setDescripcion(descripcionObjeto.getText());
        objeto.setPeso(((Number) peso.getValue()).doubleValue());
        objeto.setValor(numero(valor));
        objeto.setValorSecundario(numero(valor2));
        objeto.setValorTerciario(numero(valor3));
        objeto.setDosManos(dosManos.isSelected());
        objeto.setCategoriaArma(textoOpcional(categoria));
        objeto.setTipoMunicion(textoOpcional(municion));
        objeto.setCapacidadCargador(numero(cargador));
        objeto.setMunicionActual(numero(carga));
        objeto.setCantidad(numero(cantidad));
        objeto.setTipoGranada((String) granada.getSelectedItem());
        escenario.agregarObjeto(objeto);
    }

    private void configurarMision() {
        EscenarioDefinicion.MisionDef actual = escenario.getMision();
        JCheckBox victoriaClasica = new JCheckBox("Usar victoria clasica (sin mision)",
                actual == null);
        JTextField id = new JTextField(actual == null ? "mision-1" : actual.getId());
        JTextField nombreMision = new JTextField(
                actual == null ? "Operacion Tecla" : actual.getNombre());
        JComboBox<String> tipo = new JComboBox<>(new String[]{"alcanzar_salida",
                "eliminar_enemigo", "eliminar_jefe", "rescatar", "recuperar_objeto",
                "activar_terminal", "sobrevivir_turnos", "escoltar", "apagar_incendio",
                "no_perder_aliados", "sin_disparar"});
        if (actual != null) tipo.setSelectedItem(actual.getPrincipal().getTipo());
        JTextField argumento = new JTextField(actual == null ? "" : actual.getPrincipal().getArgumento());
        JSpinner valor = new JSpinner(new SpinnerNumberModel(
                actual == null ? 1 : actual.getPrincipal().getValor(), 0, 100000, 1));
        int filaInicial = actual != null && actual.getPrincipal().getPosicion() != null
                ? actual.getPrincipal().getPosicion().getFila() : escenario.getObjetivo().getFila();
        int columnaInicial = actual != null && actual.getPrincipal().getPosicion() != null
                ? actual.getPrincipal().getPosicion().getColumna() : escenario.getObjetivo().getColumna();
        JSpinner filaObjetivo = new JSpinner(new SpinnerNumberModel(
                filaInicial, 0, escenario.getFilas() - 1, 1));
        JSpinner columnaObjetivo = new JSpinner(new SpinnerNumberModel(
                columnaInicial, 0, escenario.getColumnas() - 1, 1));
        JCheckBox sinBajas = new JCheckBox("Secundario: no perder aliados",
                actual != null && actual.getSecundarios().stream()
                        .anyMatch(o -> "no_perder_aliados".equalsIgnoreCase(o.getTipo())));
        JTextField recompensas = new JTextField(actual == null ? ""
                : String.join(", ", actual.getRecompensas()));
        JPanel formulario = formulario(
                new JLabel("Modo:"), victoriaClasica,
                new JLabel("ID:"), id,
                new JLabel("Nombre:"), nombreMision,
                new JLabel("Objetivo principal:"), tipo,
                new JLabel("Nombre/ID asociado:"), argumento,
                new JLabel("Turnos/valor:"), valor,
                new JLabel("Fila/columna:"), formulario(filaObjetivo, columnaObjetivo),
                new JLabel("Objetivo secundario:"), sinBajas,
                new JLabel("Recompensas (coma):"), recompensas);
        if (JOptionPane.showConfirmDialog(this, formulario, "Configurar mision",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        if (victoriaClasica.isSelected()) {
            escenario.setMision(null);
            return;
        }
        EscenarioDefinicion.ObjetivoDef principal = new EscenarioDefinicion.ObjetivoDef();
        principal.setTipo((String) tipo.getSelectedItem());
        principal.setArgumento(argumento.getText().trim());
        principal.setValor(numero(valor));
        principal.setPosicion(new EscenarioDefinicion.Punto(
                numero(filaObjetivo), numero(columnaObjetivo)));
        EscenarioDefinicion.MisionDef mision = new EscenarioDefinicion.MisionDef();
        mision.setId(id.getText().trim());
        mision.setNombre(nombreMision.getText().trim());
        mision.setPrincipal(principal);
        if (sinBajas.isSelected()) {
            EscenarioDefinicion.ObjetivoDef secundario = new EscenarioDefinicion.ObjetivoDef();
            secundario.setTipo("no_perder_aliados");
            mision.setSecundarios(java.util.List.of(secundario));
        }
        mision.setRecompensas(java.util.Arrays.stream(recompensas.getText().split(","))
                .map(String::trim).filter(valorTexto -> !valorTexto.isEmpty()).toList());
        escenario.setMision(mision);
    }

    private void editarCelda(EscenarioDefinicion.CeldaDef celda) {
        JTextField texto = new JTextField(celda.getDescripcion(), 25);
        JCheckBox transitable = new JCheckBox("Transitable", celda.isTransitable());
        JCheckBox oscura = new JCheckBox("Oscuridad permanente", celda.isOscura());
        JCheckBox madera = new JCheckBox("Suelo de madera", celda.isSueloMadera());
        JCheckBox antorcha = new JCheckBox("Antorcha mural", celda.hasAntorchaMural());
        JCheckBox fuente = new JCheckBox("Fuente de agua", celda.hasFuenteAgua());
        JSpinner fuego = new JSpinner(new SpinnerNumberModel(celda.getNivelFuego(), 0, 3, 1));
        JPanel formulario = formulario(new JLabel("Descripcion:"), texto,
                new JLabel("Tipo:"), transitable,
                new JLabel("Ambiente:"), oscura,
                new JLabel("Suelo:"), madera,
                new JLabel("Iluminacion:"), antorcha,
                new JLabel("Agua:"), fuente,
                new JLabel("Fuego inicial (0-3):"), fuego);
        if (JOptionPane.showConfirmDialog(this, formulario, "Editar celda",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            if (!transitable.isSelected()
                    && (esPunto(escenario.getInicio(), celda.getFila(), celda.getColumna())
                    || esPunto(escenario.getObjetivo(), celda.getFila(), celda.getColumna()))) {
                throw new IllegalArgumentException("Inicio y objetivo deben ser transitables.");
            }
            celda.setDescripcion(texto.getText());
            celda.setTransitable(transitable.isSelected());
            celda.setOscura(oscura.isSelected());
            celda.setSueloMadera(madera.isSelected());
            celda.setAntorchaMural(antorcha.isSelected());
            celda.setFuenteAgua(fuente.isSelected());
            celda.setNivelFuego(numero(fuego));
            if (!celda.isTransitable()) {
                borrarContenido(celda.getFila(), celda.getColumna());
            }
        }
    }

    private void borrarContenido(int fila, int columna) {
        escenario.eliminarContenido(fila, columna);
    }

    private void actualizarBoton(JButton boton, int fila, int columna) {
        EscenarioDefinicion.CeldaDef celda = asegurarCelda(fila, columna);
        boton.setOpaque(true);
        boton.setForeground(Color.WHITE);
        boton.setBackground(celda.isTransitable() ? new Color(70, 78, 88) : new Color(35, 38, 43));
        String simbolo = "";
        if (esPunto(escenario.getInicio(), fila, columna)) {
            boton.setBackground(new Color(48, 145, 92));
            simbolo += "▶";
        }
        if (esPunto(escenario.getObjetivo(), fila, columna)) {
            boton.setBackground(new Color(185, 135, 35));
            simbolo += "★";
        }
        if (escenario.getEnemigos().stream()
                .anyMatch(p -> p.getFila() == fila && p.getColumna() == columna)) {
            boton.setBackground(new Color(170, 55, 60));
            simbolo += "◆";
        }
        if (escenario.getObjetos().stream()
                .anyMatch(p -> p.getFila() == fila && p.getColumna() == columna)) {
            simbolo += "■";
        }
        boton.setText(simbolo);
        boton.setToolTipText(crearTooltip(fila, columna, celda));
    }

    private String crearTooltip(int fila, int columna, EscenarioDefinicion.CeldaDef celda) {
        long enemigos = escenario.getEnemigos().stream()
                .filter(p -> p.getFila() == fila && p.getColumna() == columna).count();
        long objetos = escenario.getObjetos().stream()
                .filter(p -> p.getFila() == fila && p.getColumna() == columna).count();
        return "<html><b>" + fila + "," + columna + "</b> " + celda.getDescripcion()
                + "<br>" + (celda.isTransitable() ? "Transitable" : "Muro")
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
                setDirectorioActual(selector.getSelectedFile().toPath());
            }
            Path archivo = SerializadorEscenarioJson.guardar(escenario, directorioActual);
            JOptionPane.showMessageDialog(this, "Escenario guardado en:\n" + archivo,
                    "Guardado correcto", JOptionPane.INFORMATION_MESSAGE);
            escenarioGuardado.accept(archivo.getParent(), escenario.isConAliados());
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
            setEscenario(SerializadorEscenarioJson.cargar(ruta));
            setDirectorioActual(ruta.getFileName().toString().toLowerCase().endsWith(".json")
                    ? ruta.getParent() : ruta);
            cargarCamposDesdeModelo();
            reconstruirCuadricula();
        } catch (JuegoException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "No se pudo abrir", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarCamposDesdeModelo() {
        nombre.setText(escenario.getNombre());
        descripcion.setText(escenario.getDescripcion());
        pasos.setValue(escenario.getPasosMaximos());
        filas.setValue(escenario.getFilas());
        columnas.setValue(escenario.getColumnas());
        conAliados.setSelected(escenario.isConAliados());
    }

    private void sincronizarMetadatos() {
        if (nombre.getText().isBlank()) {
            throw new IllegalArgumentException("El escenario necesita un nombre.");
        }
        escenario.setNombre(nombre.getText());
        escenario.setDescripcion(descripcion.getText());
        escenario.setPasosMaximos(numero(pasos));
        escenario.setConAliados(conAliados.isSelected());
    }

    private EscenarioDefinicion.CeldaDef asegurarCelda(int fila, int columna) {
        EscenarioDefinicion.CeldaDef celda = escenario.celda(fila, columna);
        if (celda == null) {
            celda = new EscenarioDefinicion.CeldaDef(fila, columna, "Celda " + fila + "," + columna, true);
            escenario.agregarCelda(celda);
        }
        return celda;
    }

    private void exigirSuelo(int fila, int columna) {
        if (!asegurarCelda(fila, columna).isTransitable()) {
            throw new IllegalArgumentException("No se pueden colocar elementos sobre un muro.");
        }
    }

    private boolean esPunto(EscenarioDefinicion.Punto punto, int fila, int columna) {
        return punto != null && punto.getFila() == fila && punto.getColumna() == columna;
    }

    private int numero(JSpinner spinner) {
        return ControlesNumericos.valorEntero(spinner);
    }

    private String textoOpcional(JComboBox<String> selector) {
        String valor = (String) selector.getSelectedItem();
        return valor == null || valor.isBlank() ? null : valor;
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
