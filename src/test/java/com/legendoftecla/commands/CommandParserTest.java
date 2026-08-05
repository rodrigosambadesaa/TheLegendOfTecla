package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.ComandoException;
import org.junit.jupiter.api.Test;

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
        ComandoException error = assertThrows(ComandoException.class, () -> parser.parse("teleportar norte"));

        assertTrue(error.getMessage().contains("Comando desconocido: teleportar"));
    }

    @Test
    void validaArgumentosDeMovimientoMiradaLanzamientoYPeticionDeAyuda() {
        assertThrows(ComandoException.class, () -> parser.parse("mover arriba"));
        assertThrows(ComandoException.class, () -> parser.parse("mover norte 0"));
        assertThrows(ComandoException.class, () -> parser.parse("mirar norte 1 extra"));
        assertThrows(ComandoException.class, () -> parser.parse("lanzar norte granada"));
        assertThrows(ComandoException.class, () -> parser.parse("pedir auxilio"));
    }

    @Test
    void validaArgumentoObligatorioEnComandosDeInventario() {
        ComandoException error = assertThrows(ComandoException.class, () -> parser.parse("coger"));

        assertTrue(error.getMessage().contains("Falta argumento"));
        assertThrows(ComandoException.class, () -> parser.parse("tirar"));
        assertThrows(ComandoException.class, () -> parser.parse("usar"));
        assertThrows(ComandoException.class, () -> parser.parse("equipar"));
        assertThrows(ComandoException.class, () -> parser.parse("desequipar"));
    }
}
