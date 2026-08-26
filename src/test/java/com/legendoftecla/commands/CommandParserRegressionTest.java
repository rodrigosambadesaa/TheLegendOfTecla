package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.ComandoException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandParserRegressionTest {
    private final CommandParser parser = new CommandParser(
            new CommandContext(TestFixtures.juegoBasico(TestFixtures.consola())));

    @Test
    void aceptaPedirAuxilioComoSolicitudDeAyuda() throws ComandoException {
        assertInstanceOf(ComandoPedirAyuda.class, parser.parse("pedir auxilio"));
    }

    @Test
    void rechazaRepeticionesNumericasFueraDeRangoEnAtaque() {
        ComandoException cero = assertThrows(ComandoException.class,
                () -> parser.parse("atacar Sectoid_A 0"));
        ComandoException negativo = assertThrows(ComandoException.class,
                () -> parser.parse("atacar Sectoid_A -1"));
        ComandoException excesivo = assertThrows(ComandoException.class,
                () -> parser.parse("atacar Sectoid_A 1001"));

        assertTrue(cero.getMessage().contains("entre 1 y 1000"));
        assertTrue(negativo.getMessage().contains("entre 1 y 1000"));
        assertTrue(excesivo.getMessage().contains("entre 1 y 1000"));
    }
}
