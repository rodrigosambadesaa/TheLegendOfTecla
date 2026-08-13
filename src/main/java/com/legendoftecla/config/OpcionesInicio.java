package com.legendoftecla.config;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.CondicionVictoria;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.nio.file.Path;
import java.util.Locale;

/** Opciones de arranque encapsuladas para consola y GUI. */
public final class OpcionesInicio {
    private String nombre;
    private String clase;
    private String modo;
    private Dificultad dificultad;
    private DimensionesMapa dimensiones;
    private Path directorioDatos;
    private Boolean conAliados;
    /** {@code null} sin especificar, {@code -1} automatico, cero ninguno o cantidad exacta. */
    private Integer cantidadAliados;
    /** {@code null} sin indicar, cero automatico o nivel exacto. */
    private Integer nivelAliados;
    private CondicionVictoria condicionVictoria;
    private Integer varianteMapa;
    private boolean rapido;
    private boolean mostrarAyuda;
    private boolean gui;
    private boolean editor;
    private Long seed;

    /**
     * Crea las opciones usando sus setters delimitados.
     *
     * @param nombre nombre opcional
     * @param clase clase opcional
     * @param modo modo opcional
     * @param dificultad dificultad opcional
     * @param dimensiones dimensiones opcionales
     * @param directorioDatos directorio opcional
     * @param conAliados seleccion opcional
     * @param varianteMapa variante opcional
     * @param rapido inicio rapido
     * @param mostrarAyuda muestra la ayuda
     * @param gui inicia la GUI
     * @param editor inicia el editor
     */
    public OpcionesInicio(String nombre, String clase, String modo, Dificultad dificultad,
            DimensionesMapa dimensiones, Path directorioDatos, Boolean conAliados,
            Integer varianteMapa, boolean rapido,
            boolean mostrarAyuda, boolean gui, boolean editor) {
        this(nombre, clase, modo, dificultad, dimensiones, directorioDatos, conAliados,
                null, varianteMapa, rapido, mostrarAyuda, gui, editor);
    }

    /** Crea las opciones incluyendo una condicion de victoria opcional. */
    public OpcionesInicio(String nombre, String clase, String modo, Dificultad dificultad,
            DimensionesMapa dimensiones, Path directorioDatos, Boolean conAliados,
            CondicionVictoria condicionVictoria, Integer varianteMapa, boolean rapido,
            boolean mostrarAyuda, boolean gui, boolean editor) {
        setNombre(nombre);
        setClase(clase);
        setModo(modo);
        setDificultad(dificultad);
        setDimensiones(dimensiones);
        setDirectorioDatos(directorioDatos);
        setConAliados(conAliados);
        setCondicionVictoria(condicionVictoria);
        setVarianteMapa(varianteMapa);
        setRapido(rapido);
        setMostrarAyuda(mostrarAyuda);
        setGui(gui);
        setEditor(editor);
    }

    /** @return nombre opcional */
    public String getNombre() { return nombre; }
    /** @param nombre nombre opcional y acotado */
    public void setNombre(String nombre) {
        this.nombre = Validaciones.textoOpcional(nombre, "Nombre", Limites.TEXTO_CORTO);
    }
    /** @return clase opcional */
    public String getClase() { return clase; }
    /** @param clase marine, francotirador, zapador o {@code null} */
    public void setClase(String clase) {
        this.clase = clase == null ? null : normalizarClase(clase);
    }
    /** @return modo opcional */
    public String getModo() { return modo; }
    /** @param modo default, grande, ficheros o {@code null} */
    public void setModo(String modo) {
        this.modo = modo == null ? null : normalizarModo(modo);
    }
    /** @return dificultad opcional */
    public Dificultad getDificultad() { return dificultad; }
    /** @param dificultad dificultad opcional */
    public void setDificultad(Dificultad dificultad) { this.dificultad = dificultad; }
    /** @return dimensiones opcionales */
    public DimensionesMapa getDimensiones() {
        return dimensiones == null ? null
                : new DimensionesMapa(dimensiones.getFilas(), dimensiones.getColumnas());
    }
    /** @param dimensiones dimensiones opcionales ya delimitadas */
    public void setDimensiones(DimensionesMapa dimensiones) {
        this.dimensiones = dimensiones == null ? null
                : new DimensionesMapa(dimensiones.getFilas(), dimensiones.getColumnas());
    }
    /** @return directorio opcional */
    public Path getDirectorioDatos() { return directorioDatos; }
    /** @param directorioDatos directorio opcional */
    public void setDirectorioDatos(Path directorioDatos) {
        this.directorioDatos = directorioDatos == null ? null : directorioDatos.normalize();
    }
    /** @return seleccion de aliados o {@code null} */
    public Boolean getConAliados() { return conAliados; }
    /** @param conAliados seleccion opcional */
    public void setConAliados(Boolean conAliados) {
        this.conAliados = conAliados;
        this.cantidadAliados = conAliados == null ? null : (conAliados ? -1 : 0);
    }
    /** @return politica de cantidad de aliados o {@code null} si no se indico */
    public Integer getCantidadAliados() { return cantidadAliados; }
    /** @param cantidadAliados menos uno, cero, cantidad explicita o {@code null} */
    public void setCantidadAliados(Integer cantidadAliados) {
        if (cantidadAliados != null
                && (cantidadAliados < -1 || cantidadAliados > Limites.ALIADOS_MAXIMOS)) {
            throw new IllegalArgumentException("Cantidad de aliados invalida: usa auto, no o un valor entre 1 y "
                    + Limites.ALIADOS_MAXIMOS + ".");
        }
        this.cantidadAliados = cantidadAliados;
        this.conAliados = cantidadAliados == null ? null : cantidadAliados != 0;
    }
    /** @return condicion de victoria opcional */
    public CondicionVictoria getCondicionVictoria() { return condicionVictoria; }
    /** @param condicionVictoria condicion opcional */
    public void setCondicionVictoria(CondicionVictoria condicionVictoria) {
        this.condicionVictoria = condicionVictoria;
    }
    /** @return variante opcional */
    public Integer getVarianteMapa() { return varianteMapa; }
    /** @param varianteMapa variante opcional entre 1 y 50 */
    public void setVarianteMapa(Integer varianteMapa) {
        this.varianteMapa = varianteMapa == null ? null
                : Validaciones.enteroEntre(varianteMapa, 1, 50, "Variante del mapa");
    }
    /** @return si se usa inicio rapido */
    public boolean isRapido() { return rapido; }
    /** @param rapido inicio rapido */
    public void setRapido(boolean rapido) { this.rapido = rapido; }
    /** @return si se muestra la ayuda */
    public boolean isMostrarAyuda() { return mostrarAyuda; }
    /** @param mostrarAyuda estado solicitado */
    public void setMostrarAyuda(boolean mostrarAyuda) { this.mostrarAyuda = mostrarAyuda; }
    /** @return si se inicia la GUI */
    public boolean isGui() { return gui; }
    /** @param gui estado solicitado */
    public void setGui(boolean gui) { this.gui = gui; }
    /** @return si se inicia el editor */
    public boolean isEditor() { return editor; }
    /** @param editor estado solicitado */
    public void setEditor(boolean editor) {
        this.editor = editor;
        if (editor) {
            setGui(true);
        }
    }

    /** @return nombre conservando la API anterior */
    public String nombre() { return getNombre(); }
    /** @return clase conservando la API anterior */
    public String clase() { return getClase(); }
    /** @return modo conservando la API anterior */
    public String modo() { return getModo(); }
    /** @return dificultad conservando la API anterior */
    public Dificultad dificultad() { return getDificultad(); }
    /** @return dimensiones conservando la API anterior */
    public DimensionesMapa dimensiones() { return getDimensiones(); }
    /** @return directorio conservando la API anterior */
    public Path directorioDatos() { return getDirectorioDatos(); }
    /** @return aliados conservando la API anterior */
    public Boolean conAliados() { return getConAliados(); }
    /** @return politica de cantidad de aliados */
    public Integer cantidadAliados() { return getCantidadAliados(); }
    /** @return nivel de aliados opcional; cero representa automatico */
    public Integer getNivelAliados() { return nivelAliados; }
    /** @param nivelAliados nivel opcional entre cero y cien */
    public void setNivelAliados(Integer nivelAliados) {
        this.nivelAliados = nivelAliados == null ? null : Validaciones.enteroEntre(
                nivelAliados, 0, Limites.NIVEL_ALIADO_MAXIMO, "Nivel de aliados");
    }
    /** @return nivel aliado conservando acceso compacto */
    public Integer nivelAliados() { return getNivelAliados(); }
    /** @return condicion de victoria conservando el estilo de acceso compacto */
    public CondicionVictoria condicionVictoria() { return getCondicionVictoria(); }
    /** @return variante conservando la API anterior */
    public Integer varianteMapa() { return getVarianteMapa(); }
    /** @return inicio rapido conservando la API anterior */
    public boolean rapido() { return isRapido(); }
    /** @return ayuda conservando la API anterior */
    public boolean mostrarAyuda() { return isMostrarAyuda(); }
    /** @return GUI conservando la API anterior */
    public boolean gui() { return isGui(); }
    /** @return editor conservando la API anterior */
    public boolean editor() { return isEditor(); }
    public Long getSeed() { return seed; }
    public void setSeed(Long seed) { this.seed = seed; }
    public Long seed() { return seed; }

    /**
     * Interpreta las opciones de la linea de comandos.
     *
     * @param args argumentos no nulos
     * @return opciones validadas
     */
    public static OpcionesInicio desdeArgumentos(String[] args) {
        Validaciones.noNulo(args, "Argumentos");
        String nombre = null;
        String clase = null;
        String modo = null;
        Dificultad dificultad = null;
        DimensionesMapa dimensiones = null;
        Path directorioDatos = null;
        Integer cantidadAliados = null;
        Integer nivelAliados = null;
        CondicionVictoria condicionVictoria = null;
        Integer varianteMapa = null;
        boolean rapido = false;
        boolean mostrarAyuda = false;
        boolean gui = false;
        boolean editor = false;
        Long seed = null;

        for (int i = 0; i < args.length; i++) {
            String argumento = Validaciones.textoObligatorio(args[i], "Opcion", Limites.DESCRIPCION);
            switch (argumento) {
                case "--rapido" -> rapido = true;
                case "--interactivo" -> rapido = false;
                case "--help", "-h" -> mostrarAyuda = true;
                case "--gui" -> gui = true;
                case "--editor" -> { gui = true; editor = true; }
                case "--nombre" -> nombre = siguienteValor(args, ++i, argumento);
                case "--clase" -> clase = normalizarClase(siguienteValor(args, ++i, argumento));
                case "--modo" -> modo = normalizarModo(siguienteValor(args, ++i, argumento));
                case "--dificultad" -> dificultad = parsearDificultad(siguienteValor(args, ++i, argumento));
                case "--dimensiones" -> dimensiones = parsearDimensiones(siguienteValor(args, ++i, argumento));
                case "--datos" -> directorioDatos = Path.of(siguienteValor(args, ++i, argumento));
                case "--aliados" -> cantidadAliados = parsearAliados(
                        siguienteValor(args, ++i, argumento));
                case "--nivel-aliados" -> nivelAliados = parsearNivelAliados(
                        siguienteValor(args, ++i, argumento));
                case "--victoria" -> condicionVictoria = parsearCondicionVictoria(
                        siguienteValor(args, ++i, argumento));
                case "--variante" -> varianteMapa = parsearVariante(siguienteValor(args, ++i, argumento));
                case "--seed" -> seed = parsearSeed(siguienteValor(args, ++i, argumento));
                default -> throw new IllegalArgumentException("Opcion desconocida: " + argumento);
            }
        }

        if (rapido) {
            nombre = nombre == null ? "Tecla" : nombre;
            clase = clase == null ? "marine" : clase;
            modo = modo == null ? "default" : modo;
            dificultad = dificultad == null ? Dificultad.NORMAL : dificultad;
            cantidadAliados = cantidadAliados == null ? 0 : cantidadAliados;
            nivelAliados = nivelAliados == null ? 0 : nivelAliados;
            condicionVictoria = condicionVictoria == null
                    ? CondicionVictoria.JUGADOR_Y_ALIADOS
                    : condicionVictoria;
            varianteMapa = varianteMapa == null ? 1 : varianteMapa;
            if ("ficheros".equals(modo) && directorioDatos == null) {
                directorioDatos = Path.of("data", "escenario_basico");
            }
        }

        OpcionesInicio opciones = new OpcionesInicio(nombre, clase, modo, dificultad, dimensiones,
                directorioDatos, cantidadAliados == null ? null : cantidadAliados != 0,
                condicionVictoria, varianteMapa,
                rapido, mostrarAyuda, gui, editor);
        opciones.setCantidadAliados(cantidadAliados);
        opciones.setNivelAliados(nivelAliados);
        opciones.setSeed(seed);
        return opciones;
    }

    /** @return ayuda de la linea de comandos */
    public static String ayuda() {
        return """
                Uso: java -jar the-legend-of-tecla.jar [opciones]

                  --rapido                 Inicia con valores predeterminados, sin asistente inicial
                  --interactivo            Fuerza el asistente inicial (util en Docker)
                  --gui                     Abre la interfaz grafica completa
                  --editor                  Abre directamente el editor grafico de mapas
                  --nombre <nombre>         Nombre del personaje
                  --clase <clase>           marine, francotirador o zapador
                  --modo <modo>             default, grande, ficheros o procedural
                  --dificultad <nivel>      muy_facil, facil, normal, dificil,
                                            muy_dificil, pesadilla o demente
                  --dimensiones <FxC>       Tamano del mapa; por ejemplo, 12x20
                  --datos <directorio>      Directorio con escenario.json o los tres ficheros TXT
                  --aliados <no|auto|N>     Sin aliados, calculados o cantidad exacta (1-1000)
                  --nivel-aliados <auto|N>  Nivel automatico o exacto para todos (1-100)
                  --victoria <condicion>    solo_jugador o jugador_y_aliados
                  --variante <1-50>         Variante determinista del mapa grande
                  --seed <entero>           Semilla del modo procedural
                  --help, -h                Muestra esta ayuda

                Las opciones indicadas reemplazan sus preguntas del asistente. Combina
                --rapido con otras opciones para cambiar solamente los valores deseados.
                """;
    }

    private static String siguienteValor(String[] args, int indice, String opcion) {
        if (indice >= args.length || args[indice] == null || args[indice].startsWith("--")) {
            throw new IllegalArgumentException("Falta el valor de " + opcion + ".");
        }
        return Validaciones.textoObligatorio(args[indice], opcion, Limites.DESCRIPCION);
    }

    private static String normalizarClase(String valor) {
        String normalizado = Validaciones.textoObligatorio(
                valor, "Clase", Limites.TEXTO_CORTO).toLowerCase(Locale.ROOT);
        return switch (normalizado) {
            case "marine", "francotirador", "zapador" -> normalizado;
            default -> throw new IllegalArgumentException("Clase invalida: " + valor + ".");
        };
    }

    private static String normalizarModo(String valor) {
        String normalizado = Validaciones.textoObligatorio(
                valor, "Modo", Limites.TEXTO_CORTO).toLowerCase(Locale.ROOT);
        return switch (normalizado) {
            case "1", "default" -> "default";
            case "2", "grande" -> "grande";
            case "3", "ficheros" -> "ficheros";
            case "4", "procedural" -> "procedural";
            default -> throw new IllegalArgumentException("Modo invalido: " + valor + ".");
        };
    }

    private static Dificultad parsearDificultad(String valor) {
        Dificultad resultado = Dificultad.desdeTexto(valor);
        if (resultado == null) {
            throw new IllegalArgumentException("Dificultad invalida: " + valor + ".");
        }
        return resultado;
    }

    private static DimensionesMapa parsearDimensiones(String valor) {
        String[] partes = valor.toLowerCase(Locale.ROOT).split("x");
        if (partes.length != 2) {
            throw new IllegalArgumentException("Dimensiones invalidas: usa el formato filasxcolumnas.");
        }
        try {
            return new DimensionesMapa(Integer.parseInt(partes[0].trim()), Integer.parseInt(partes[1].trim()));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Dimensiones invalidas: " + valor + ".", e);
        }
    }

    private static int parsearAliados(String valor) {
        String normalizado = valor.trim().toLowerCase(Locale.ROOT);
        if (normalizado.equals("si") || normalizado.equals("sí") || normalizado.equals("s")
                || normalizado.equals("true") || normalizado.equals("auto")
                || normalizado.equals("automatico") || normalizado.equals("automático")) {
            return -1;
        }
        if (normalizado.equals("no") || normalizado.equals("n")
                || normalizado.equals("false") || normalizado.equals("0")) {
            return 0;
        }
        try {
            return Validaciones.enteroEntre(Integer.parseInt(normalizado), 1,
                    Limites.ALIADOS_MAXIMOS, "Cantidad de aliados");
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(
                    "Valor de aliados invalido: usa no, auto o una cantidad entre 1 y "
                            + Limites.ALIADOS_MAXIMOS + ".", error);
        }
    }

    private static int parsearNivelAliados(String valor) {
        String normalizado = valor.trim().toLowerCase(Locale.ROOT);
        if (normalizado.equals("auto") || normalizado.equals("automatico")
                || normalizado.equals("automático")) {
            return 0;
        }
        try {
            return Validaciones.enteroEntre(Integer.parseInt(normalizado), 1,
                    Limites.NIVEL_ALIADO_MAXIMO, "Nivel de aliados");
        } catch (NumberFormatException error) {
            throw new IllegalArgumentException(
                    "Nivel de aliados invalido: usa auto o un valor entre 1 y "
                            + Limites.NIVEL_ALIADO_MAXIMO + ".", error);
        }
    }

    private static CondicionVictoria parsearCondicionVictoria(String valor) {
        CondicionVictoria resultado = CondicionVictoria.desdeTexto(valor);
        if (resultado == null) {
            throw new IllegalArgumentException(
                    "Condicion de victoria invalida: usa solo_jugador o jugador_y_aliados.");
        }
        return resultado;
    }

    private static int parsearVariante(String valor) {
        try {
            return Validaciones.enteroEntre(Integer.parseInt(valor), 1, 50, "Variante");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Variante invalida: " + valor + ".", e);
        }
    }

    private static long parsearSeed(String valor) {
        try {
            return Long.parseLong(valor);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Seed invalida: " + valor + ".", e);
        }
    }
}
