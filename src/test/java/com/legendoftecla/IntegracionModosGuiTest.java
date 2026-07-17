package com.legendoftecla;

import com.legendoftecla.console.Consola;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.engine.FabricaJuego;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.gui.ConsolaGrafica;
import com.legendoftecla.gui.PanelConfiguracion;
import com.legendoftecla.gui.PanelJuego;
import com.legendoftecla.gui.PanelEditorMapa;
import com.legendoftecla.loader.EscenarioDefinicion;
import com.legendoftecla.loader.SerializadorEscenarioJson;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.SistemaPuntuacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegracionModosGuiTest {
    @TempDir
    Path temporal;

    @Test
    void consolaYMotorCompartenElFlujoCompleto() throws Exception {
        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        Juego juego = FabricaJuego.crear(consola, new ConfiguracionPartida(
                "Test", "marine", "default", Dificultad.NORMAL,
                new DimensionesMapa(8, 8), null, false, 1));
        MotorPartida motor = new MotorPartida(juego);

        assertTrue(motor.ejecutarComando("mirar"));
        assertFalse(motor.ejecutarComando("salir"));
        assertEquals(SistemaPuntuacion.EstadoFinalPartida.SALIDA_MANUAL, motor.getEstadoFinal());
        assertTrue(consola.salida.toString().contains("Partida finalizada"));
    }

    @Test
    void jsonConMapaPersonajesYObjetosHaceRoundTrip() throws Exception {
        EscenarioDefinicion escenario = crearEscenarioCompleto();
        Path archivo = SerializadorEscenarioJson.guardar(escenario, temporal);
        assertTrue(Files.isRegularFile(archivo));

        EscenarioDefinicion recargado = SerializadorEscenarioJson.cargar(temporal);
        assertEquals(1, recargado.enemigos.size());
        assertTrue(recargado.conAliados);
        assertEquals(1, recargado.objetos.size());
        assertFalse(recargado.celda(1, 1).transitable);

        Juego juego = FabricaJuego.crear(new ConsolaSilenciosa(), new ConfiguracionPartida(
                "Json", "zapador", "ficheros", Dificultad.NORMAL, null, temporal, true, 1));
        assertEquals("Escenario de prueba", juego.getMapa().getNombre());
        assertEquals(1, juego.getEnemigos().size());
        assertEquals(95, juego.getEnemigos().get(0).getSalud());
        assertEquals(1, juego.getAliados().size());
        assertEquals(1, juego.getMapa().getCelda(new com.legendoftecla.model.world.Posicion(0, 1))
                .getObjetos().size());
    }

    @Test
    void laVistaGraficaRenderizaLosTresModosSinConsolaDeTexto() throws Exception {
        SerializadorEscenarioJson.guardar(crearEscenarioCompleto(), temporal);
        ConfiguracionPartida[] configuraciones = {
                new ConfiguracionPartida("Gui", "marine", "default", Dificultad.NORMAL,
                        new DimensionesMapa(8, 8), null, true, 1),
                new ConfiguracionPartida("Gui", "francotirador", "grande", Dificultad.FACIL,
                        new DimensionesMapa(21, 21), null, true, 25),
                new ConfiguracionPartida("Gui", "zapador", "ficheros", Dificultad.NORMAL,
                        null, temporal, true, 1)
        };
        Path capturas = Path.of("target", "gui-smoke");
        Files.createDirectories(capturas);

        for (int indice = 0; indice < configuraciones.length; indice++) {
            ConsolaGrafica consola = new ConsolaGrafica();
            MotorPartida motor = new MotorPartida(FabricaJuego.crear(consola, configuraciones[indice]));
            Path captura = capturas.resolve("modo-" + indice + ".png");
            SwingUtilities.invokeAndWait(() -> renderizarPanel(motor, consola, captura));
            assertTrue(Files.size(captura) > 1000, "La representacion grafica debe contener el mapa y controles");
        }
    }

    @Test
    void elEditorGraficoSeConstruyeYRenderizaEnUnaSolaVista() throws Exception {
        Path captura = Path.of("target", "gui-smoke", "editor.png");
        Files.createDirectories(captura.getParent());
        SwingUtilities.invokeAndWait(() -> {
            try {
                PanelEditorMapa editor = new PanelEditorMapa((ruta, aliados) -> { }, () -> { });
                editor.setSize(1200, 760);
                distribuir(editor);
                BufferedImage imagen = new BufferedImage(1200, 760, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = imagen.createGraphics();
                editor.printAll(graphics);
                graphics.dispose();
                ImageIO.write(imagen, "png", captura.toFile());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(Files.size(captura) > 1000);
    }

    @Test
    void laGuiExponeTodasLasAccionesDelJuegoComoBotones() throws Exception {
        ConsolaGrafica consola = new ConsolaGrafica();
        MotorPartida motor = new MotorPartida(FabricaJuego.crear(consola, new ConfiguracionPartida(
                "Botones", "marine", "default", Dificultad.NORMAL,
                new DimensionesMapa(8, 8), null, false, 1)));

        SwingUtilities.invokeAndWait(() -> {
            PanelJuego panel = new PanelJuego(motor, consola, () -> { });
            for (String etiqueta : new String[] {
                    "Coger", "Usar", "Tirar", "Equipar", "Desequipar", "Atacar",
                    "Lanzar explosivo",
                    "Inventario", "Estado", "Ayuda", "Recorrido", "Salir"
            }) {
                assertTrue(contieneBoton(panel, etiqueta), "Falta el boton " + etiqueta);
            }
        });
    }

    @Test
    void elMapaGrandeIncluyeToritosFrecuentesEnUnaRutaCompletable() throws Exception {
        Juego juego = FabricaJuego.crear(new ConsolaSilenciosa(), new ConfiguracionPartida(
                "Energia", "marine", "grande", Dificultad.NORMAL,
                new DimensionesMapa(50, 50), null, true, 1));
        Mapa mapa = juego.getMapa();

        long toritos = 0;
        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                toritos += mapa.getCelda(new Posicion(fila, columna)).getObjetos().stream()
                        .filter(ToritoRojo.class::isInstance)
                        .count();
            }
        }
        assertTrue(toritos >= 50, "El mapa 50x50 debe tener suficientes suministros de energia");

        List<Posicion> ruta = rutaMasCorta(mapa);
        assertFalse(ruta.isEmpty(), "Debe existir una ruta transitable hasta el objetivo");
        for (int i = 5; i < ruta.size() - 1; i += 5) {
            boolean tieneTorito = mapa.getCelda(ruta.get(i)).getObjetos().stream()
                    .anyMatch(ToritoRojo.class::isInstance);
            assertTrue(tieneTorito, "Falta un Torito de ruta en el paso " + i);
        }
    }

    @Test
    void existenCincuentaVariantesGrandesDistintasYReproducibles() throws Exception {
        Set<String> distribuciones = new HashSet<>();
        for (int variante = 1; variante <= 50; variante++) {
            ConfiguracionPartida configuracion = new ConfiguracionPartida(
                    "Variantes", "marine", "grande", Dificultad.NORMAL,
                    new DimensionesMapa(24, 24), null, false, variante);
            Juego primero = FabricaJuego.crear(new ConsolaSilenciosa(), configuracion);
            Juego segundo = FabricaJuego.crear(new ConsolaSilenciosa(), configuracion);
            String firma = firmaTransitabilidad(primero.getMapa());
            assertEquals(firma, firmaTransitabilidad(segundo.getMapa()));
            assertFalse(rutaMasCorta(primero.getMapa()).isEmpty());
            distribuciones.add(firma);
        }
        assertEquals(50, distribuciones.size());
    }

    @Test
    void losAliadosSeActivanConUnSiNoYSuCantidadEsAutomatica() throws Exception {
        ConfiguracionPartida sinAliados = new ConfiguracionPartida(
                "Sin", "marine", "grande", Dificultad.NORMAL,
                new DimensionesMapa(50, 50), null, false, 12);
        ConfiguracionPartida conAliados = new ConfiguracionPartida(
                "Con", "marine", "grande", Dificultad.NORMAL,
                new DimensionesMapa(50, 50), null, true, 12);

        assertEquals(0, FabricaJuego.crear(new ConsolaSilenciosa(), sinAliados).getAliados().size());
        assertEquals(5, FabricaJuego.crear(new ConsolaSilenciosa(), conAliados).getAliados().size());

        Juego pequeno = FabricaJuego.crear(new ConsolaSilenciosa(), new ConfiguracionPartida(
                "Pequeno", "marine", "default", Dificultad.NORMAL,
                new DimensionesMapa(10, 10), null, true, 1));
        assertEquals(1, pequeno.getAliados().size());
    }

    @Test
    void elZapadorPuedeLanzarUnExplosivoDesdeComandoYEsteSeConsume() throws Exception {
        EscenarioDefinicion escenario = EscenarioDefinicion.nuevo(5, 5);
        EscenarioDefinicion.PersonajeDef enemigo = new EscenarioDefinicion.PersonajeDef();
        enemigo.fila = 0;
        enemigo.columna = 2;
        enemigo.nombre = "ObjetivoExplosivo";
        enemigo.salud = 70;
        enemigo.energia = 70;
        enemigo.vision = 2;
        escenario.enemigos.add(enemigo);
        SerializadorEscenarioJson.guardar(escenario, temporal);

        Juego juego = FabricaJuego.crear(new ConsolaSilenciosa(), new ConfiguracionPartida(
                "Zapador", "zapador", "ficheros", Dificultad.NORMAL,
                null, temporal, false, 1));
        Explosivo explosivo = new Explosivo("carga prueba", "Carga de integracion", 1.0);
        juego.getJugador().coger(explosivo);
        MotorPartida motor = new MotorPartida(juego);

        motor.ejecutarComando("lanzar 5s carga prueba");
        assertTrue(juego.getJugador().getMochila().getObjetos().contains(explosivo),
                "Un lanzamiento invalido no debe consumir el explosivo");

        int saludInicial = juego.getEnemigos().get(0).getSalud();
        motor.ejecutarComando("lanzar 2e carga prueba");
        assertEquals(saludInicial - explosivo.getDanio(), juego.getEnemigos().get(0).getSalud());
        assertFalse(juego.getJugador().getMochila().getObjetos().contains(explosivo));
    }

    @Test
    void lasDimensionesAdmitenNumerosEscritosEnConfiguracionYEditor() throws Exception {
        AtomicReference<ConfiguracionPartida> resultado = new AtomicReference<>();
        Path captura = Path.of("target", "gui-smoke", "configuracion-dimensiones.png");
        Files.createDirectories(captura.getParent());
        SwingUtilities.invokeAndWait(() -> {
            PanelConfiguracion panel = new PanelConfiguracion(resultado::set, () -> { });
            JSpinner filasConfiguracion = (JSpinner) buscarPorNombre(panel, "dimensiones.filas");
            JSpinner columnasConfiguracion = (JSpinner) buscarPorNombre(panel, "dimensiones.columnas");
            JCheckBox aliadosConfiguracion = (JCheckBox) buscarPorNombre(panel, "aliados.activados");
            assertNotNull(filasConfiguracion);
            assertNotNull(columnasConfiguracion);
            assertNotNull(aliadosConfiguracion);
            escribirNumero(filasConfiguracion, "37");
            escribirNumero(columnasConfiguracion, "42");
            aliadosConfiguracion.setSelected(true);
            renderizarComponente(panel, captura, 1100, 720);
            JButton iniciar = buscarBoton(panel, "Iniciar partida en GUI");
            assertNotNull(iniciar);
            iniciar.doClick();

            PanelEditorMapa editor = new PanelEditorMapa((ruta, aliados) -> { }, () -> { });
            JSpinner filasEditor = (JSpinner) buscarPorNombre(editor, "editor.dimensiones.filas");
            JSpinner columnasEditor = (JSpinner) buscarPorNombre(editor, "editor.dimensiones.columnas");
            assertNotNull(filasEditor);
            assertNotNull(columnasEditor);
            escribirNumero(filasEditor, "18");
            escribirNumero(columnasEditor, "23");
            assertEquals(18, filasEditor.getValue());
            assertEquals(23, columnasEditor.getValue());
        });

        assertNotNull(resultado.get());
        assertEquals(37, resultado.get().dimensiones().filas());
        assertEquals(42, resultado.get().dimensiones().columnas());
        assertTrue(resultado.get().conAliados());
        assertTrue(Files.size(captura) > 1000);
    }

    private EscenarioDefinicion crearEscenarioCompleto() {
        EscenarioDefinicion escenario = EscenarioDefinicion.nuevo(6, 7);
        escenario.nombre = "Escenario de prueba";
        escenario.conAliados = true;
        escenario.celda(1, 1).transitable = false;

        EscenarioDefinicion.PersonajeDef enemigo = new EscenarioDefinicion.PersonajeDef();
        enemigo.fila = 2;
        enemigo.columna = 3;
        enemigo.tipo = "heavyfloater";
        enemigo.nombre = "Prueba";
        enemigo.salud = 95;
        enemigo.energia = 80;
        enemigo.vision = 4;
        escenario.enemigos.add(enemigo);

        EscenarioDefinicion.ObjetoDef objeto = new EscenarioDefinicion.ObjetoDef();
        objeto.fila = 0;
        objeto.columna = 1;
        objeto.tipo = "arma";
        objeto.nombre = "Arma test";
        objeto.descripcion = "Objeto completo";
        objeto.peso = 2.5;
        objeto.valor = 22;
        objeto.dosManos = true;
        escenario.objetos.add(objeto);
        return escenario;
    }

    private void renderizarPanel(MotorPartida motor, ConsolaGrafica consola, Path destino) {
        try {
            PanelJuego panel = new PanelJuego(motor, consola, () -> { });
            panel.setSize(1100, 720);
            distribuir(panel);
            BufferedImage imagen = new BufferedImage(1100, 720, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = imagen.createGraphics();
            panel.printAll(graphics);
            graphics.dispose();
            ImageIO.write(imagen, "png", destino.toFile());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void distribuir(Container contenedor) {
        contenedor.doLayout();
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof Container hijo) {
                distribuir(hijo);
            }
        }
    }

    private boolean contieneBoton(Container contenedor, String texto) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JButton boton && texto.equals(boton.getText())) {
                return true;
            }
            if (componente instanceof Container hijo && contieneBoton(hijo, texto)) {
                return true;
            }
        }
        return false;
    }

    private JButton buscarBoton(Container contenedor, String texto) {
        for (Component componente : contenedor.getComponents()) {
            if (componente instanceof JButton boton && texto.equals(boton.getText())) {
                return boton;
            }
            if (componente instanceof Container hijo) {
                JButton boton = buscarBoton(hijo, texto);
                if (boton != null) {
                    return boton;
                }
            }
        }
        return null;
    }

    private Component buscarPorNombre(Container contenedor, String nombre) {
        for (Component componente : contenedor.getComponents()) {
            if (nombre.equals(componente.getName())) {
                return componente;
            }
            if (componente instanceof Container hijo) {
                Component encontrado = buscarPorNombre(hijo, nombre);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private void escribirNumero(JSpinner spinner, String texto) {
        JFormattedTextField campo = ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField();
        assertTrue(campo.isEditable());
        assertTrue(campo.isFocusable());
        campo.setText(texto);
        try {
            spinner.commitEdit();
        } catch (java.text.ParseException error) {
            throw new AssertionError("El selector numerico debe aceptar " + texto, error);
        }
    }

    private void renderizarComponente(Component componente, Path destino, int ancho, int alto) {
        try {
            componente.setSize(ancho, alto);
            if (componente instanceof Container contenedor) {
                distribuir(contenedor);
            }
            BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = imagen.createGraphics();
            componente.printAll(graphics);
            graphics.dispose();
            ImageIO.write(imagen, "png", destino.toFile());
        } catch (Exception error) {
            throw new RuntimeException(error);
        }
    }

    private List<Posicion> rutaMasCorta(Mapa mapa) {
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Map<Posicion, Posicion> anterior = new HashMap<>();
        pendientes.add(mapa.getInicio());
        anterior.put(mapa.getInicio(), null);
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            if (actual.equals(mapa.getObjetivo())) {
                break;
            }
            for (Direccion direccion : Direccion.values()) {
                Posicion siguiente = actual.mover(direccion);
                if (mapa.esTransitable(siguiente) && !anterior.containsKey(siguiente)) {
                    anterior.put(siguiente, actual);
                    pendientes.addLast(siguiente);
                }
            }
        }
        if (!anterior.containsKey(mapa.getObjetivo())) {
            return List.of();
        }
        List<Posicion> ruta = new ArrayList<>();
        for (Posicion posicion = mapa.getObjetivo(); posicion != null; posicion = anterior.get(posicion)) {
            ruta.add(posicion);
        }
        Collections.reverse(ruta);
        return ruta;
    }

    private String firmaTransitabilidad(Mapa mapa) {
        StringBuilder firma = new StringBuilder(mapa.getFilas() * mapa.getColumnas());
        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                firma.append(mapa.esTransitable(new Posicion(fila, columna)) ? '1' : '0');
            }
        }
        return firma.toString();
    }

    private static final class ConsolaSilenciosa implements Consola {
        private final StringBuilder salida = new StringBuilder();

        @Override
        public void imprimir(String mensaje) {
            salida.append(mensaje).append('\n');
        }

        @Override
        public void imprimir(String mensaje, TipoMensaje tipo) {
            imprimir(mensaje);
        }

        @Override
        public String leer(String descripcion) {
            throw new UnsupportedOperationException();
        }
    }
}
