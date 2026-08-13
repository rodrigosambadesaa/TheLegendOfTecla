package com.legendoftecla.config;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.constants.CondicionVictoria;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpcionesInicioTest {
    @Test
    void aplicaValoresPredeterminadosEnInicioRapido() {
        OpcionesInicio opciones = OpcionesInicio.desdeArgumentos(new String[] { "--rapido" });

        assertEquals("Tecla", opciones.nombre());
        assertEquals("marine", opciones.clase());
        assertEquals("default", opciones.modo());
        assertEquals(Dificultad.NORMAL, opciones.dificultad());
        assertEquals(Boolean.FALSE, opciones.conAliados());
        assertEquals(0, opciones.cantidadAliados());
        assertEquals(1, opciones.nivelJugador());
        assertEquals(CondicionVictoria.JUGADOR_Y_ALIADOS, opciones.condicionVictoria());
        assertEquals(1, opciones.varianteMapa());
        assertTrue(opciones.rapido());
        assertNull(opciones.dimensiones());
    }

    @Test
    void interpretaTodasLasOpcionesYNormalizaAlias() {
        OpcionesInicio opciones = OpcionesInicio.desdeArgumentos(new String[] {
            "--rapido", "--nombre", "Ada", "--clase", "ZAPADOR",
            "--modo", "3", "--dificultad", "muy_dificil",
            "--dimensiones", "12x20", "--datos", "data/../data/escenario_json",
            "--aliados", "sí", "--nivel-aliados", "12", "--nivel-jugador", "27",
            "--sin-mejoras-aliados", "--sin-municion-aliada",
            "--victoria", "solo_jugador", "--variante", "50", "--editor"
        });

        assertEquals("Ada", opciones.getNombre());
        assertEquals("zapador", opciones.getClase());
        assertEquals("ficheros", opciones.getModo());
        assertEquals(Dificultad.MUY_DIFICIL, opciones.getDificultad());
        assertEquals(12, opciones.getDimensiones().getFilas());
        assertEquals(20, opciones.getDimensiones().getColumnas());
        assertEquals(Path.of("data", "escenario_json"), opciones.getDirectorioDatos());
        assertEquals(Boolean.TRUE, opciones.getConAliados());
        assertEquals(-1, opciones.getCantidadAliados());
        assertEquals(12, opciones.getNivelAliados());
        assertEquals(27, opciones.getNivelJugador());
        assertFalse(opciones.isMejorasEquipoAliado());
        assertFalse(opciones.isMunicionAliadaAutomatica());
        assertEquals(CondicionVictoria.SOLO_JUGADOR, opciones.getCondicionVictoria());
        assertEquals(50, opciones.getVarianteMapa());
        assertTrue(opciones.isEditor());
        assertTrue(opciones.isGui());
    }

    @Test
    void aceptaCantidadExactaDeAliadosYConservaElModoAutomatico() {
        OpcionesInicio exactos = OpcionesInicio.desdeArgumentos(
                new String[] { "--aliados", "37" });
        OpcionesInicio automaticos = OpcionesInicio.desdeArgumentos(
                new String[] { "--aliados", "auto" });

        assertEquals(37, exactos.cantidadAliados());
        assertEquals(Boolean.TRUE, exactos.conAliados());
        assertEquals(-1, automaticos.cantidadAliados());
        assertEquals(0, OpcionesInicio.desdeArgumentos(
                new String[] { "--nivel-aliados", "auto" }).nivelAliados());
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--aliados", "5000" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--nivel-aliados", "101" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--nivel-jugador", "0" }));
    }

    @Test
    void permiteQueInteractivoDesactiveRapidoYReconoceAyuda() {
        OpcionesInicio opciones = OpcionesInicio.desdeArgumentos(new String[] {
            "--rapido", "--interactivo", "-h"
        });

        assertFalse(opciones.isRapido());
        assertTrue(opciones.isMostrarAyuda());
        assertNull(opciones.getNombre());
        assertTrue(OpcionesInicio.ayuda().contains("--dimensiones"));
    }

    @Test
    void completaDirectorioDeEscenarioTxtEnModoRapido() {
        OpcionesInicio opciones = OpcionesInicio.desdeArgumentos(new String[] {
            "--rapido", "--modo", "ficheros"
        });

        assertEquals(Path.of("data", "escenario_basico"), opciones.directorioDatos());
    }

    @Test
    void rechazaOpcionesYValoresInvalidosConMensajesUtiles() {
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--desconocida" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--nombre" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--clase", "mago" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--modo", "red" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--dificultad", "letal" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--dimensiones", "grande" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--dimensiones", "axb" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--aliados", "quizas" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--victoria", "el_primero" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--variante", "cincuenta" }));
        assertThrows(IllegalArgumentException.class,
                () -> OpcionesInicio.desdeArgumentos(new String[] { "--variante", "51" }));
    }
}
