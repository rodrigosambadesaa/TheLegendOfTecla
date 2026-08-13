package com.legendoftecla;

import com.legendoftecla.console.Consola;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.CondicionVictoria;
import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.engine.FabricaJuego;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.gui.ConsolaGrafica;
import com.legendoftecla.gui.MapaGraficoPanel;
import com.legendoftecla.gui.PanelConfiguracion;
import com.legendoftecla.gui.PanelJuego;
import com.legendoftecla.gui.PanelEditorMapa;
import com.legendoftecla.loader.EscenarioDefinicion;
import com.legendoftecla.loader.SerializadorEscenarioJson;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.SistemaPuntuacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;
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
    void losObjetosSoloSeRevelanAlLlegarYMirarEnConsolaYGui() {
        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        Mapa mapa = new Mapa("Secreto", "Prueba de exploracion", 3, 3,
                new Posicion(0, 0), new Posicion(2, 2));
        for (int fila = 0; fila < 3; fila++) {
            for (int columna = 0; columna < 3; columna++) {
                mapa.setCelda(fila, columna, new Celda("Celda", true));
            }
        }
        mapa.getCelda(new Posicion(0, 1)).agregarObjeto(
                new Botiquin("secreto", "Objeto oculto", 1.0, 20));
        Marine jugador = new Marine("Explorador", new Posicion(0, 0), new Mochila(4, 20), 2);
        Juego juego = new Juego(consola, mapa, jugador, 30);
        MotorPartida motor = new MotorPartida(juego);

        String inicial = mapa.renderAscii(jugador.getPosicion(), Set.of(), Set.of(),
                juego.getCeldasInspeccionadas());
        assertFalse(inicial.contains("o"));

        consola.salida.setLength(0);
        motor.ejecutarComando("mirar este 1");
        assertFalse(consola.salida.toString().contains("secreto"));
        assertFalse(juego.isCeldaInspeccionada(new Posicion(0, 1)));

        motor.ejecutarComando("mover este");
        MapaGraficoPanel panelMapa = new MapaGraficoPanel(motor);
        MouseEvent sobreObjeto = new MouseEvent(panelMapa, MouseEvent.MOUSE_MOVED,
                System.currentTimeMillis(), 0, 33, 1, 0, false);
        assertFalse(panelMapa.getToolTipText(sobreObjeto).contains("secreto"));

        motor.ejecutarComando("coger secreto");
        assertEquals(1, mapa.getCelda(new Posicion(0, 1)).getObjetos().size());

        consola.salida.setLength(0);
        motor.ejecutarComando("mirar");
        assertTrue(consola.salida.toString().contains("secreto"));
        assertTrue(juego.isCeldaInspeccionada(new Posicion(0, 1)));
        assertTrue(panelMapa.getToolTipText(sobreObjeto).contains("secreto"));

        motor.ejecutarComando("mover oeste");
        String descubierto = mapa.renderAscii(jugador.getPosicion(), Set.of(), Set.of(),
                juego.getCeldasInspeccionadas());
        assertTrue(descubierto.contains("o"));
    }

    @Test
    void consolaYMotorCompartenElFlujoCompleto() throws Exception {
        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        Juego juego = FabricaJuego.crear(consola, new ConfiguracionPartida(
                "Test", "marine", "default", Dificultad.NORMAL,
                new DimensionesMapa(8, 8), null, false, 1));
        MotorPartida motor = new MotorPartida(juego);

        assertTrue(motor.ejecutarComando("mirar"));
        assertTrue(motor.ejecutarComando("ayuda"));
        assertTrue(consola.salida.toString().contains("lanzar <distancia><direccion>"));
        assertTrue(consola.salida.toString().contains("pedir ayuda"));
        assertTrue(consola.salida.toString().contains("descansar"));
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
        assertEquals(1, recargado.getEnemigos().size());
        assertTrue(recargado.isConAliados());
        assertEquals(1, recargado.getObjetos().size());
        assertFalse(recargado.celda(1, 1).isTransitable());

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
    void cargarSustituyeLaPartidaActivaDesdeConsolaYGui() throws Exception {
        SerializadorEscenarioJson.guardar(crearEscenarioCompleto(), temporal);

        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        MotorPartida motorConsola = new MotorPartida(FabricaJuego.crear(consola,
                new ConfiguracionPartida("Carga consola", "marine", "default",
                        Dificultad.NORMAL, new DimensionesMapa(8, 8), null, false, 1)));
        assertTrue(motorConsola.ejecutarComando("cargar " + temporal));
        assertEquals("Escenario de prueba", motorConsola.getJuego().getMapa().getNombre());

        ConsolaGrafica consolaGrafica = new ConsolaGrafica();
        MotorPartida motorGui = new MotorPartida(FabricaJuego.crear(consolaGrafica,
                new ConfiguracionPartida("Carga GUI", "marine", "default",
                        Dificultad.NORMAL, new DimensionesMapa(8, 8), null, false, 1)));
        SwingUtilities.invokeAndWait(() -> {
            PanelJuego panel = new PanelJuego(motorGui, consolaGrafica, () -> { });
            JTextField entrada = (JTextField) buscarPorNombre(panel, "comando.entrada");
            assertNotNull(entrada);
            entrada.setText("cargar " + temporal);
            entrada.postActionEvent();
        });
        assertEquals("Escenario de prueba", motorGui.getJuego().getMapa().getNombre());
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
                    "Pedir ayuda",
                    "Formacion defensiva", "Formacion ofensiva",
                    "Inventario", "Estado", "Ayuda", "Recorrido", "Descansar", "Salir"
            }) {
                assertTrue(contieneBoton(panel, etiqueta), "Falta el boton " + etiqueta);
            }
        });
    }

    @Test
    void laGuiMuestraPermanentementeElEstadoCompletoDeLosAliados() throws Exception {
        ConsolaGrafica consola = new ConsolaGrafica();
        MotorPartida motor = new MotorPartida(FabricaJuego.crear(consola, new ConfiguracionPartida(
                "EstadoGui", "marine", "default", Dificultad.NORMAL,
                new DimensionesMapa(8, 8), null, true, 1)));

        SwingUtilities.invokeAndWait(() -> {
            PanelJuego panel = new PanelJuego(motor, consola, () -> { });
            JTextArea estadoAliados = (JTextArea) buscarPorNombre(panel, "estado.aliados");
            assertNotNull(estadoAliados);
            assertTrue(estadoAliados.getText().contains("ALIADOS"));
            assertTrue(estadoAliados.getText().contains("Vida"));
            assertTrue(estadoAliados.getText().contains("Energia"));
            assertTrue(estadoAliados.getText().contains("Combate FUERA DE COMBATE"));
            assertTrue(estadoAliados.getText().contains("Objetos:"));
            assertTrue(estadoAliados.getText().contains("Equipo:"));
        });
    }

    @Test
    void elEstadoAliadoDistingueCombateSalidaDeCombateYEvacuacion() throws Exception {
        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        Juego juego = crearJuegoAsistencia(consola);
        Aliado combatiente = crearAliado(juego, "Combatiente", new Posicion(2, 3));
        combatiente.getMochila().guardar(new Botiquin("reserva", "Reserva", 1.0, 20));
        Sectoid enemigo = new Sectoid("Amenaza", new Posicion(2, 4), new Mochila(1, 5), 2);
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
        MotorPartida motor = new MotorPartida(juego);

        motor.ejecutarComando("mirar");
        String enCombate = motor.getEstadoAliados();
        assertTrue(enCombate.contains("Combatiente | Estado EN COMBATE | Combate EN COMBATE"));
        assertTrue(enCombate.contains("Objetos: reserva"));

        enemigo.recibirDanio(999);
        motor.ejecutarComando("mirar");
        assertTrue(motor.getEstadoAliados().contains("Combate FUERA DE COMBATE"),
                motor.getEstadoAliados());

        Juego evacuacion = crearJuegoAsistencia(consola);
        Aliado evacuado = crearAliado(evacuacion, "Explorador", evacuacion.getMapa().getObjetivo());
        evacuado.getMochila().guardar(new ToritoRojo("torito final", "Reserva", 0.5, 20));
        MotorPartida motorEvacuacion = new MotorPartida(evacuacion);
        motorEvacuacion.ejecutarComando("mirar");
        String estadoEvacuado = motorEvacuacion.getEstadoAliados();
        assertTrue(estadoEvacuado.contains("Estado EVACUADO: LLEGO A LA SALIDA"));
        assertTrue(estadoEvacuado.contains("Posicion salida"));
        assertTrue(estadoEvacuado.contains("Objetos: torito final"));
        assertEquals(1, evacuacion.getAliadosRegistrados().size());
        assertTrue(evacuacion.getAliados().isEmpty());
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
        int pasosRuta = ruta.size() - 1;
        int energiaMinimaJugador = ((int) Math.ceil(pasosRuta * 0.70) + 8)
                * juego.getJugador().estimarCosteMovimiento();
        assertTrue(juego.getJugador().getEnergiaMaxima() >= energiaMinimaJugador,
                "La energia base debe cubrir una parte sustancial de la ruta grande y una reserva");
        for (Aliado aliado : juego.getAliados()) {
            int energiaMinimaAliado = ((int) Math.ceil(pasosRuta * 0.70) + 8)
                    * aliado.estimarCosteMovimiento();
            assertTrue(aliado.getEnergiaMaxima() >= energiaMinimaAliado,
                    "Los aliados tambien necesitan energia proporcional al mapa");
        }
        for (int i = 5; i < ruta.size() - 1; i += 5) {
            boolean tieneTorito = mapa.getCelda(ruta.get(i)).getObjetos().stream()
                    .anyMatch(ToritoRojo.class::isInstance);
            assertTrue(tieneTorito, "Falta un Torito de ruta en el paso " + i);
        }

        Juego pequeno = FabricaJuego.crear(new ConsolaSilenciosa(), new ConfiguracionPartida(
                "Energia pequena", "marine", "default", Dificultad.NORMAL,
                new DimensionesMapa(8, 8), null, false, 1));
        assertEquals(90, pequeno.getJugador().getEnergiaMaxima(),
                "Los mapas pequenos deben conservar el balance original");
    }

    @Test
    void tirarDesdeLaEntradaGuiDejaElObjetoEnLaCeldaDelJugador() throws Exception {
        ConsolaGrafica consola = new ConsolaGrafica();
        Juego juego = FabricaJuego.crear(consola, new ConfiguracionPartida(
                "TirarGui", "marine", "default", Dificultad.NORMAL,
                new DimensionesMapa(8, 8), null, false, 1));
        Botiquin objeto = new Botiquin("objeto gui", "Prueba", 1, 10);
        juego.getJugador().getMochila().guardar(objeto);
        MotorPartida motor = new MotorPartida(juego);

        SwingUtilities.invokeAndWait(() -> {
            PanelJuego panel = new PanelJuego(motor, consola, () -> { });
            JTextField entrada = (JTextField) buscarPorNombre(panel, "comando.entrada");
            assertNotNull(entrada);
            entrada.setText("tirar objeto gui");
            entrada.postActionEvent();
        });

        assertFalse(juego.getJugador().getMochila().getObjetos().contains(objeto));
        assertTrue(juego.getMapa().getCelda(juego.getJugador().getPosicion()).getObjetos().contains(objeto));
    }

    @Test
    void lasDificultadesFacilesAgreganBotiquinesYToritosEnTodosLosMapas() throws Exception {
        SerializadorEscenarioJson.guardar(crearEscenarioCompleto(), temporal);

        verificarSuministrosFaciles("default", new DimensionesMapa(10, 10), null, 1);
        verificarSuministrosFaciles("grande", new DimensionesMapa(21, 21), null, 17);
        verificarSuministrosFaciles("ficheros", null, temporal, 1);
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
        enemigo.setFila(0);
        enemigo.setColumna(2);
        enemigo.setNombre("ObjetivoExplosivo");
        enemigo.setSalud(70);
        enemigo.setEnergia(70);
        enemigo.setVision(2);
        escenario.agregarEnemigo(enemigo);
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
        int defensa = juego.getEnemigos().get(0).getArmaduraEquipada().getDefensa();
        motor.ejecutarComando("lanzar 2e carga prueba");
        assertEquals(saludInicial - Math.max(0, explosivo.getDanio() - defensa),
                juego.getEnemigos().get(0).getSalud());
        assertFalse(juego.getJugador().getMochila().getObjetos().contains(explosivo));
    }

    @Test
    void laAyudaAliadaPriorizaVidaYEnergiaDelJugador() throws Exception {
        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        Juego juego = crearJuegoAsistencia(consola);
        Aliado donante = crearAliado(juego, "Medico", new Posicion(2, 3));
        Aliado herido = crearAliado(juego, "Herido", new Posicion(3, 3));
        donante.getMochila().guardar(new Botiquin("botiquin aliado", "Apoyo", 1.0, 25));
        donante.getMochila().guardar(new ToritoRojo("torito aliado", "Apoyo", 0.5, 30));
        juego.getJugador().recibirDanio(40);
        juego.getJugador().gastarEnergia(20);
        herido.recibirDanio(40);
        int saludHerido = herido.getSalud();
        MotorPartida motor = new MotorPartida(juego);

        motor.ejecutarComando("pedir ayuda");
        assertEquals(105, juego.getJugador().getSalud());
        assertEquals(saludHerido, herido.getSalud(), "El jugador debe recibir el primer botiquin");
        assertTrue(motor.isAyudaAliadaActiva());

        motor.ejecutarComando("socorro");
        assertEquals(juego.getJugador().getEnergiaMaxima(), juego.getJugador().getEnergia());
        assertTrue(consola.salida.toString().contains("para dar vida a Jugador"));
        assertTrue(consola.salida.toString().contains("para dar energia a Jugador"));
    }

    @Test
    void elAliadoSinSuministrosExploraAntesDeAcudirAlJugador() throws Exception {
        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        Juego juego = crearJuegoAsistencia(consola);
        Aliado aliado = crearAliado(juego, "Explorador", new Posicion(2, 3));
        Posicion celdaSuministro = new Posicion(1, 3);
        juego.getMapa().getCelda(celdaSuministro).agregarObjeto(
                new Botiquin("botiquin encontrado", "Apoyo localizado", 1.0, 25));
        juego.getJugador().recibirDanio(40);
        int saludInicial = juego.getJugador().getSalud();
        MotorPartida motor = new MotorPartida(juego);

        motor.ejecutarComando("pedir ayuda");

        assertEquals(celdaSuministro, aliado.getPosicion());
        assertFalse(juego.isCeldaInspeccionada(aliado, celdaSuministro),
                "El objeto no debe revelarse hasta inspeccionar la celda en el siguiente turno");
        assertTrue(aliado.getMochila().getObjetos().isEmpty());

        motor.ejecutarComando("mirar");

        assertTrue(juego.isCeldaInspeccionada(aliado, celdaSuministro));
        assertTrue(aliado.getMochila().getObjetos().stream().anyMatch(Botiquin.class::isInstance));

        motor.ejecutarComando("mirar");

        assertEquals(saludInicial + 25, juego.getJugador().getSalud());
        assertTrue(aliado.getMochila().getObjetos().isEmpty());
        assertTrue(consola.salida.toString().contains(
                "explora una celda desconocida para buscar suministros para el jugador"));
        assertTrue(consola.salida.toString().contains("para dar vida a Jugador"));
    }

    @Test
    void losAliadosRecogenObjetosYSeAsistenEntreEllos() throws Exception {
        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        Juego juego = crearJuegoAsistencia(consola);
        Aliado donante = crearAliado(juego, "Recolector", new Posicion(2, 3));
        Aliado herido = crearAliado(juego, "Companero", new Posicion(3, 3));
        herido.recibirDanio(50);
        juego.getMapa().getCelda(donante.getPosicion()).agregarObjeto(
                new Botiquin("botiquin suelo", "Encontrado", 1.0, 25));
        int saludAnterior = herido.getSalud();

        new MotorPartida(juego).ejecutarComando("mirar");

        assertTrue(juego.isCeldaInspeccionada(donante, donante.getPosicion()));
        assertEquals(saludAnterior + 25, herido.getSalud());
        assertTrue(juego.getMapa().getCelda(donante.getPosicion()).getObjetos().isEmpty());
        assertTrue(consola.salida.toString().contains("recoge botiquin suelo"));
        assertTrue(consola.salida.toString().contains("para dar vida a Companero"));
    }

    @Test
    void laOrdenAcercaAliadosSegurosPeroNoArriesgaAHeridos() throws Exception {
        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        Juego juego = crearJuegoAsistencia(consola);
        Aliado seguro = crearAliado(juego, "Seguro", new Posicion(4, 2));
        Aliado enPeligro = crearAliado(juego, "EnPeligro", new Posicion(4, 1));
        enPeligro.recibirDanio(50);
        int distanciaSegura = seguro.getPosicion().distanciaManhattan(juego.getJugador().getPosicion());
        Posicion posicionPeligro = enPeligro.getPosicion();

        new MotorPartida(juego).ejecutarComando("asistir");

        assertTrue(seguro.getPosicion().distanciaManhattan(juego.getJugador().getPosicion()) < distanciaSegura);
        assertEquals(posicionPeligro, enPeligro.getPosicion());
        assertTrue(consola.salida.toString().contains("su vida correria peligro"));
    }

    @Test
    void elAliadoReponeSusRecursosAntesDeAsistirSinSacrificarLaReservaDelJugador() throws Exception {
        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        Juego juego = crearJuegoAsistencia(consola);
        Aliado aliado = crearAliado(juego, "Preparado", new Posicion(2, 3));
        aliado.recibirDanio(45);
        aliado.gastarEnergia(125);
        aliado.getMochila().guardar(new Botiquin("botiquin propio", "Reserva", 1.0, 30));
        aliado.getMochila().guardar(new Botiquin("botiquin jugador", "Reserva", 1.0, 30));
        aliado.getMochila().guardar(new ToritoRojo("torito propio", "Reserva", 0.5, 35));
        aliado.getMochila().guardar(new ToritoRojo("torito jugador", "Reserva", 0.5, 35));
        juego.getJugador().recibirDanio(30);
        juego.getJugador().gastarEnergia(20);
        int vidaJugador = juego.getJugador().getSalud();
        MotorPartida motor = new MotorPartida(juego);

        motor.ejecutarComando("pedir ayuda");
        assertEquals(vidaJugador, juego.getJugador().getSalud(),
                "El aliado debe curarse antes de entregar el botiquin del jugador");
        assertTrue(aliado.getSalud() > 45);
        assertTrue(motor.getEstadoAliados().contains("REPONIENDO SU VIDA O ENERGIA"));

        motor.ejecutarComando("pedir ayuda");
        assertTrue(aliado.getEnergia() > 15, "Debe reponer su energia antes de desplazarse o entregar reservas");
        assertEquals(vidaJugador, juego.getJugador().getSalud());
        assertEquals(2, aliado.getMochila().getObjetos().size(),
                "Debe conservar el Torito reservado para el jugador");
        assertTrue(aliado.getMochila().getObjetos().stream().anyMatch(ToritoRojo.class::isInstance));

        motor.ejecutarComando("pedir ayuda");
        assertEquals(vidaJugador + 30, juego.getJugador().getSalud());
        motor.ejecutarComando("pedir ayuda");
        assertEquals(juego.getJugador().getEnergiaMaxima(), juego.getJugador().getEnergia());
        assertTrue(aliado.getMochila().getObjetos().isEmpty());
    }

    @Test
    void laEnergiaCeroEsRescatableSoloSiExisteUnToritoEntregable() throws Exception {
        ConsolaSilenciosa consolaRescate = new ConsolaSilenciosa();
        Juego rescatable = crearJuegoAsistencia(consolaRescate);
        Aliado socorrista = crearAliado(rescatable, "Socorrista", new Posicion(2, 3));
        socorrista.getMochila().guardar(new ToritoRojo("torito rescate", "Emergencia", 0.5, 30));
        rescatable.getJugador().gastarEnergia(rescatable.getJugador().getEnergia());
        MotorPartida motorRescate = new MotorPartida(rescatable);

        assertFalse(motorRescate.isFinalizada(), "Debe concederse tiempo si el rescate es realmente posible");
        motorRescate.ejecutarComando("pedir ayuda");
        assertEquals(30, rescatable.getJugador().getEnergia());
        assertFalse(motorRescate.isFinalizada());

        ConsolaSilenciosa consolaSinSuministros = new ConsolaSilenciosa();
        Juego imposible = crearJuegoAsistencia(consolaSinSuministros);
        crearAliado(imposible, "SinToritos", new Posicion(2, 3));
        imposible.getJugador().gastarEnergia(imposible.getJugador().getEnergia());
        MotorPartida motorImposible = new MotorPartida(imposible);

        assertTrue(motorImposible.isFinalizada());
        assertEquals(SistemaPuntuacion.EstadoFinalPartida.MUERTE, motorImposible.getEstadoFinal());
        assertTrue(consolaSinSuministros.salida.toString().contains("Rescate imposible"));
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
            JComboBox<?> modoAliados = (JComboBox<?>) buscarPorNombre(panel, "aliados.modo");
            JSpinner cantidadAliados = (JSpinner) buscarPorNombre(panel, "aliados.cantidad");
            JSpinner nivelAliados = (JSpinner) buscarPorNombre(panel, "aliados.nivel");
            JComboBox<?> victoriaConfiguracion = (JComboBox<?>) buscarPorNombre(panel, "victoria.condicion");
            assertNotNull(filasConfiguracion);
            assertNotNull(columnasConfiguracion);
            assertNotNull(aliadosConfiguracion);
            assertNotNull(modoAliados);
            assertNotNull(cantidadAliados);
            assertNotNull(nivelAliados);
            assertNotNull(victoriaConfiguracion);
            assertFalse(victoriaConfiguracion.isEnabled());
            escribirNumero(filasConfiguracion, "37");
            escribirNumero(columnasConfiguracion, "42");
            aliadosConfiguracion.doClick();
            modoAliados.setSelectedIndex(1);
            cantidadAliados.setValue(17);
            nivelAliados.setValue(9);
            assertTrue(victoriaConfiguracion.isEnabled());
            victoriaConfiguracion.setSelectedItem(CondicionVictoria.SOLO_JUGADOR);
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
        assertEquals(17, resultado.get().cantidadAliados());
        assertEquals(9, resultado.get().nivelAliados());
        assertEquals(CondicionVictoria.SOLO_JUGADOR, resultado.get().condicionVictoria());
        assertTrue(Files.size(captura) > 1000);
    }

    private EscenarioDefinicion crearEscenarioCompleto() {
        EscenarioDefinicion escenario = EscenarioDefinicion.nuevo(6, 7);
        escenario.setNombre("Escenario de prueba");
        escenario.setConAliados(true);
        escenario.celda(1, 1).setTransitable(false);

        EscenarioDefinicion.PersonajeDef enemigo = new EscenarioDefinicion.PersonajeDef();
        enemigo.setFila(2);
        enemigo.setColumna(3);
        enemigo.setTipo("heavyfloater");
        enemigo.setNombre("Prueba");
        enemigo.setSalud(95);
        enemigo.setEnergia(80);
        enemigo.setVision(4);
        escenario.agregarEnemigo(enemigo);

        EscenarioDefinicion.ObjetoDef objeto = new EscenarioDefinicion.ObjetoDef();
        objeto.setFila(0);
        objeto.setColumna(1);
        objeto.setTipo("arma");
        objeto.setNombre("Arma test");
        objeto.setDescripcion("Objeto completo");
        objeto.setPeso(2.5);
        objeto.setValor(22);
        objeto.setDosManos(true);
        escenario.agregarObjeto(objeto);
        return escenario;
    }

    private Juego crearJuegoAsistencia(Consola consola) {
        Mapa mapa = new Mapa("Asistencia", "Prueba de aliados", 5, 5,
                new Posicion(0, 0), new Posicion(4, 4));
        for (int fila = 0; fila < 5; fila++) {
            for (int columna = 0; columna < 5; columna++) {
                mapa.setCelda(fila, columna, new Celda("Celda", true));
            }
        }
        Marine jugador = new Marine("Jugador", new Posicion(2, 2), new Mochila(10, 40), 4);
        return new Juego(consola, mapa, jugador, 100);
    }

    private Aliado crearAliado(Juego juego, String nombre, Posicion posicion) {
        Aliado aliado = new Aliado(nombre, posicion, new Mochila(6, 30), 3);
        juego.getMapa().getCelda(posicion).agregarAliado(aliado);
        juego.agregarAliado(aliado);
        return aliado;
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

    private void verificarSuministrosFaciles(String modo, DimensionesMapa dimensiones,
            Path directorio, int variante) throws Exception {
        Juego normal = FabricaJuego.crear(new ConsolaSilenciosa(), new ConfiguracionPartida(
                "Normal", "marine", modo, Dificultad.NORMAL,
                dimensiones, directorio, false, variante));
        Juego facil = FabricaJuego.crear(new ConsolaSilenciosa(), new ConfiguracionPartida(
                "Facil", "marine", modo, Dificultad.FACIL,
                dimensiones, directorio, false, variante));
        Juego muyFacil = FabricaJuego.crear(new ConsolaSilenciosa(), new ConfiguracionPartida(
                "Muy facil", "marine", modo, Dificultad.MUY_FACIL,
                dimensiones, directorio, false, variante));

        int celdas = normal.getMapa().getFilas() * normal.getMapa().getColumnas();
        for (Class<?> tipo : List.of(Botiquin.class, ToritoRojo.class)) {
            long base = contarObjetos(normal.getMapa(), tipo);
            assertEquals(base + Dificultad.FACIL.calcularSuministrosExtra(celdas),
                    contarObjetos(facil.getMapa(), tipo),
                    "Facil debe agregar suministros " + tipo.getSimpleName() + " en " + modo);
            assertEquals(base + Dificultad.MUY_FACIL.calcularSuministrosExtra(celdas),
                    contarObjetos(muyFacil.getMapa(), tipo),
                    "Muy facil debe agregar suministros " + tipo.getSimpleName() + " en " + modo);
        }
    }

    private long contarObjetos(Mapa mapa, Class<?> tipo) {
        long cantidad = 0;
        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                cantidad += mapa.getCelda(new Posicion(fila, columna)).getObjetos().stream()
                        .filter(tipo::isInstance)
                        .count();
            }
        }
        return cantidad;
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
