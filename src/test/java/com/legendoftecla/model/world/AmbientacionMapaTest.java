package com.legendoftecla.model.world;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.commands.CommandParser;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.loader.CargadorJuegoJson;
import com.legendoftecla.loader.CargadorJuegoPorDefecto;
import com.legendoftecla.loader.EscenarioDefinicion;
import com.legendoftecla.loader.SerializadorEscenarioJson;
import com.legendoftecla.model.elements.EstadoPuerta;
import com.legendoftecla.model.elements.Mina;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.procedural.ConfiguracionGeneracion;
import com.legendoftecla.procedural.GeneradorHabitaciones;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmbientacionMapaTest {
    @TempDir
    Path temporal;

    @Test
    void completaCadaCeldaDeUnMapaProceduralConContextoPropio() {
        Mapa mapa = new GeneradorHabitaciones().generar(
                9876L, ConfiguracionGeneracion.normal(12, 14));

        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                String descripcion = mapa.getCelda(new Posicion(fila, columna)).getDescripcion();
                assertTrue(descripcion.contains("Entorno del escenario:"));
                assertTrue(descripcion.contains("fila " + (fila + 1)
                        + ", columna " + (columna + 1)));
                assertTrue(descripcion.length() > 150);
            }
        }
    }

    @Test
    void conservaLaNarrativaImportadaSinDuplicarlaAlCompletarDeNuevo() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Mapa mapa = TestFixtures.juegoBasico(consola).getMapa();
        Posicion posicion = new Posicion(1, 1);
        mapa.getCelda(posicion).setDescripcion(
                "Una capilla derruida conserva mosaicos de la antigua guarnicion");

        AmbientacionMapa.completar(mapa);
        String primera = mapa.getCelda(posicion).getDescripcion();
        AmbientacionMapa.completar(mapa);

        assertTrue(primera.contains("Una capilla derruida"));
        assertTrue(primera.contains("sector central"));
        assertTrue(primera.equals(mapa.getCelda(posicion).getDescripcion()));
    }

    @Test
    void mirarDescribeEstadoRealAdemasDeObjetosSinRevelarTrampasOcultas() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Celda celda = juego.getMapa().getCelda(juego.getJugador().getPosicion());
        celda.setTipoSuelo(TipoSuelo.MADERA);
        celda.setAntorchaMural(true);
        celda.setFuenteAgua(true);
        celda.setNivelFuego(2);
        celda.agregarElemento(new Puerta(
                "acceso-norte", EstadoPuerta.CERRADA, null, true, 15));
        celda.agregarElemento(new Mina("mina-oculta", 8, 1, false));

        new CommandParser(new CommandContext(juego)).parse("mirar").ejecutar();
        String salida = consola.salida();

        assertTrue(salida.contains("Estado actual del lugar:"));
        assertTrue(salida.contains("tablones de madera"));
        assertTrue(salida.contains("antorcha mural"));
        assertTrue(salida.contains("fuente de agua"));
        assertTrue(salida.contains("Las llamas se han extendido"));
        assertTrue(salida.contains("puerta 'acceso-norte' cerrada, bloquea el paso"));
        assertTrue(salida.contains("No hay objetos en esta celda."));
        assertFalse(salida.contains("mina-oculta"));
    }

    @Test
    void cubreTodasLasCeldasDeMapasPredeterminadosEImportados() throws Exception {
        Juego predeterminado = new CargadorJuegoPorDefecto(TestFixtures.consola(),
                "Tecla", "marine", Dificultad.NORMAL, null, false).cargarJuego();
        assertAmbientado(predeterminado.getMapa());

        EscenarioDefinicion escenario = EscenarioDefinicion.nuevo(5, 6);
        escenario.setNombre("Observatorio Boreal");
        escenario.setDescripcion("Un observatorio cientifico aislado por la tormenta");
        escenario.celda(2, 3).setDescripcion(
                "La cupula principal conserva un telescopio orientado hacia el norte");
        SerializadorEscenarioJson.guardar(escenario, temporal);

        Juego importado = new CargadorJuegoJson(TestFixtures.consola(), "Tecla", "marine",
                temporal, Dificultad.NORMAL, null, false).cargarJuego();
        assertAmbientado(importado.getMapa());
        assertTrue(importado.getMapa().getCelda(new Posicion(2, 3)).getDescripcion()
                .contains("La cupula principal conserva un telescopio"));
    }

    private void assertAmbientado(Mapa mapa) {
        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                assertTrue(mapa.getCelda(new Posicion(fila, columna)).getDescripcion()
                        .contains("Entorno del escenario:"));
            }
        }
    }
}
