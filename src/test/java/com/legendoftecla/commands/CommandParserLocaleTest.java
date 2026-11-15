package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** El parser de comandos es independiente de la configuracion regional. */
class CommandParserLocaleTest {
    @Test
    void reconoceMayusculasYFormacionesConLocaleTurco() throws Exception {
        Locale original = Locale.getDefault();
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            CommandParser parser = new CommandParser(
                    new CommandContext(TestFixtures.juegoBasico(TestFixtures.consola())));
            assertInstanceOf(ComandoInventario.class, parser.parse("INVENTARIO"));
            assertInstanceOf(ComandoReagrupar.class, parser.parse("REAGRUPAR NINGUNA"));
        } finally {
            Locale.setDefault(original);
        }
    }
}
