package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.ComandoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandParserTest {
    private final CommandParser parser = new CommandParser(
            new CommandContext(TestFixtures.juegoBasico(TestFixtures.consola())));

    @Test
    void rechazaComandoNuloVacioOConSoloEspacios() {
        assertThrows(ComandoException.class, () -> parser.parse(null));
        assertThrows(ComandoException.class, () -> parser.parse(""));
        assertThrows(ComandoException.class, () -> parser.parse("   \t  "));
    }

    @Test
    void informaComandoDesconocido() {
        ComandoException error = assertThrows(ComandoException.class,
                () -> parser.parse("teleportar norte"));

        assertTrue(error.getMessage().contains("Comando desconocido: teleportar"));
    }

    @Test
    void validaArgumentosDeMovimientoMiradaLanzamientoPeticionDeAyudaYDescanso()
            throws ComandoException {
        assertThrows(ComandoException.class, () -> parser.parse("mover arriba"));
        assertThrows(ComandoException.class, () -> parser.parse("mover norte 0"));
        assertThrows(ComandoException.class, () -> parser.parse("mirar norte cero"));
        assertThrows(ComandoException.class, () -> parser.parse("lanzar norte granada"));
        assertThrows(ComandoException.class, () -> parser.parse("pedir auxilio"));
        assertThrows(ComandoException.class, () -> parser.parse("descansar ahora"));
        assertInstanceOf(ComandoDescansar.class, parser.parse("reposar"));
        assertInstanceOf(ComandoReagrupar.class, parser.parse("reagrupar defensiva"));
        assertInstanceOf(ComandoReagrupar.class, parser.parse("formacion ofensiva"));
        assertThrows(ComandoException.class, () -> parser.parse("reagrupar dispersa"));
    }

    @Test
    void validaArgumentoObligatorioEnComandosDeInventario() {
        ComandoException error = assertThrows(ComandoException.class,
                () -> parser.parse("coger"));

        assertTrue(error.getMessage().contains("Falta argumento"));
        assertThrows(ComandoException.class, () -> parser.parse("tirar"));
        assertThrows(ComandoException.class, () -> parser.parse("usar"));
        assertThrows(ComandoException.class, () -> parser.parse("equipar"));
        assertThrows(ComandoException.class, () -> parser.parse("desequipar"));
        assertThrows(ComandoException.class, () -> parser.parse("cargar"));
    }

    @Test
    void reconoceComandosTacticosNuevosYValidaSusFormas() throws ComandoException {
        assertInstanceOf(ComandoRecargar.class, parser.parse("recargar"));
        assertInstanceOf(ComandoEstadoArma.class, parser.parse("estado arma"));
        assertInstanceOf(ComandoPuerta.class, parser.parse("abrir puerta"));
        assertInstanceOf(ComandoTrampa.class, parser.parse("desactivar trampa"));
        assertInstanceOf(ComandoFabricar.class, parser.parse("fabricar botiquin"));
        assertInstanceOf(ComandoGuardarPartida.class, parser.parse("guardar partida"));
        assertInstanceOf(ComandoCargarPartida.class, parser.parse("cargar partida"));
        assertInstanceOf(ComandoEstadisticas.class, parser.parse("estadisticas"));
        assertInstanceOf(ComandoEstadisticas.class, parser.parse("logros"));
        assertInstanceOf(ComandoTransferir.class, parser.parse("dar botiquin Ana"));
        assertInstanceOf(ComandoTransferir.class, parser.parse("pedir balas Ana"));
        assertInstanceOf(ComandoTransferir.class,
                parser.parse("intercambiar rifle botiquin Ana"));
        assertThrows(ComandoException.class, () -> parser.parse("estado jugador"));
        assertThrows(ComandoException.class, () -> parser.parse("guardar escenario"));
        assertThrows(ComandoException.class, () -> parser.parse("dar botiquin"));
    }
}
