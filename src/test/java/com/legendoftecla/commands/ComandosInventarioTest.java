package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.engine.MotorPartida;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandosInventarioTest {
    private TestFixtures.CapturingConsole consola;
    private Juego juego;
    private CommandParser parser;

    @BeforeEach
    void prepararPartida() {
        consola = TestFixtures.consola();
        juego = TestFixtures.juegoBasico(consola);
        parser = new CommandParser(new CommandContext(juego));
    }

    @Test
    void completaCicloDeRecogerUsarYTirarObjeto() throws ComandoException {
        Botiquin botiquin = new Botiquin("botiquin", "Cura", 1, 20);
        juego.getMapa().getCelda(juego.getJugador().getPosicion()).agregarObjeto(botiquin);

        assertThrows(ComandoException.class, () -> parser.parse("coger botiquin").ejecutar());
        parser.parse("mirar").ejecutar();
        parser.parse("coger botiquin").ejecutar();
        juego.getJugador().recibirDanio(30);
        parser.parse("usar botiquin").ejecutar();

        assertEquals(110, juego.getJugador().getSalud());
        assertTrue(juego.getJugador().getMochila().getObjetos().isEmpty());
        assertThrows(ComandoException.class, () -> parser.parse("usar botiquin").ejecutar());

        juego.getJugador().getMochila().guardar(botiquin);
        parser.parse("tirar botiquin").ejecutar();
        assertTrue(juego.getMapa().getCelda(juego.getJugador().getPosicion())
                .getObjetos().contains(botiquin));
        assertThrows(ComandoException.class, () -> parser.parse("tirar inexistente").ejecutar());
    }

    @Test
    void equipaYDesequipaConservandoElObjeto() throws ComandoException {
        Arma rifle = new Arma("rifle", "Arma de prueba", 2, 15, true);
        juego.getJugador().getMochila().guardar(rifle);

        parser.parse("equipar rifle").ejecutar();
        assertTrue(juego.getJugador().getArmasEquipadas().contains(rifle));
        assertFalse(juego.getJugador().getMochila().getObjetos().contains(rifle));

        parser.parse("desequipar rifle").ejecutar();
        assertTrue(juego.getJugador().getArmasEquipadas().isEmpty());
        assertTrue(juego.getJugador().getMochila().getObjetos().contains(rifle));
        assertThrows(ComandoException.class, () -> parser.parse("desequipar laser").ejecutar());
    }

    @Test
    void elBinocularNoSeConsumeYPuedeUsarseEnTurnosDistintos() {
        Binocular binocular = new Binocular("binocular", "Vision reutilizable", 1, 3);
        juego.getJugador().getMochila().guardar(binocular);
        MotorPartida motor = new MotorPartida(juego);

        motor.ejecutarComando("usar binocular");
        assertTrue(juego.getJugador().getMochila().getObjetos().contains(binocular));
        assertEquals(3, juego.getJugador().getVisionTemporal());

        motor.ejecutarComando("mirar");
        assertEquals(0, juego.getJugador().getVisionTemporal());
        motor.ejecutarComando("usar binocular");
        assertTrue(juego.getJugador().getMochila().getObjetos().contains(binocular));
        assertEquals(3, juego.getJugador().getVisionTemporal());
    }

    @Test
    void informaInventarioRecorridoYFaltaDeAliados() throws ComandoException {
        parser.parse("inventario").ejecutar();
        parser.parse("recorrido").ejecutar();

        assertTrue(consola.salida().contains("Mochila:"));
        assertTrue(consola.salida().contains("(vacia)"));
        assertTrue(consola.salida().contains("Recorrido:"));
        assertThrows(ComandoException.class, () -> parser.parse("pedir ayuda").ejecutar());
    }

    @Test
    void compuestoYRepetidoEjecutanEnOrdenYValidanLimites() throws ComandoException {
        AtomicInteger contador = new AtomicInteger();
        Comando primero = () -> contador.addAndGet(1);
        Comando segundo = () -> contador.addAndGet(10);
        ComandoCompuesto compuesto = new ComandoCompuesto();
        compuesto.agregar(primero);
        compuesto.agregar(new ComandoRepetido(segundo, 2));

        compuesto.ejecutar();

        assertEquals(21, contador.get());
        assertEquals(2, compuesto.getComandos().size());
        assertThrows(UnsupportedOperationException.class,
                () -> compuesto.getComandos().add(primero));
        assertThrows(IllegalArgumentException.class,
                () -> compuesto.setComandos(Arrays.asList(primero, null)));
        assertThrows(IllegalArgumentException.class, () -> new ComandoRepetido(primero, 0));
    }
}
