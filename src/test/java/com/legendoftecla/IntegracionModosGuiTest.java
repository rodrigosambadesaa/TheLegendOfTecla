package com.legendoftecla;

import com.legendoftecla.console.Consola;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.engine.FabricaJuego;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.gui.ConsolaGrafica;
import com.legendoftecla.gui.PanelJuego;
import com.legendoftecla.gui.PanelEditorMapa;
import com.legendoftecla.loader.EscenarioDefinicion;
import com.legendoftecla.loader.SerializadorEscenarioJson;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.SistemaPuntuacion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.SwingUtilities;
import java.awt.Graphics2D;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IntegracionModosGuiTest {
    @TempDir
    Path temporal;

    @Test
    void consolaYMotorCompartenElFlujoCompleto() throws Exception {
        ConsolaSilenciosa consola = new ConsolaSilenciosa();
        Juego juego = FabricaJuego.crear(consola, new ConfiguracionPartida(
                "Test", "marine", "default", Dificultad.NORMAL,
                new DimensionesMapa(8, 8), null));
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
        assertEquals(1, recargado.aliados.size());
        assertEquals(1, recargado.objetos.size());
        assertFalse(recargado.celda(1, 1).transitable);

        Juego juego = FabricaJuego.crear(new ConsolaSilenciosa(), new ConfiguracionPartida(
                "Json", "zapador", "ficheros", Dificultad.NORMAL, null, temporal));
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
                        new DimensionesMapa(8, 8), null),
                new ConfiguracionPartida("Gui", "francotirador", "grande", Dificultad.FACIL,
                        new DimensionesMapa(21, 21), null),
                new ConfiguracionPartida("Gui", "zapador", "ficheros", Dificultad.NORMAL,
                        null, temporal)
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
                PanelEditorMapa editor = new PanelEditorMapa(ruta -> { }, () -> { });
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
                new DimensionesMapa(8, 8), null)));

        SwingUtilities.invokeAndWait(() -> {
            PanelJuego panel = new PanelJuego(motor, consola, () -> { });
            for (String etiqueta : new String[] {
                    "Coger", "Usar", "Tirar", "Equipar", "Desequipar", "Atacar",
                    "Inventario", "Estado", "Ayuda", "Recorrido", "Salir"
            }) {
                assertTrue(contieneBoton(panel, etiqueta), "Falta el boton " + etiqueta);
            }
        });
    }

    private EscenarioDefinicion crearEscenarioCompleto() {
        EscenarioDefinicion escenario = EscenarioDefinicion.nuevo(6, 7);
        escenario.nombre = "Escenario de prueba";
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

        EscenarioDefinicion.PersonajeDef aliado = new EscenarioDefinicion.PersonajeDef();
        aliado.fila = 4;
        aliado.columna = 2;
        aliado.tipo = "aliado";
        aliado.nombre = "Apoyo";
        aliado.salud = 100;
        aliado.energia = 130;
        aliado.vision = 4;
        escenario.aliados.add(aliado);

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
