package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.elements.EstadoPuerta;
import com.legendoftecla.model.elements.Interruptor;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.elements.Terminal;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandoTerminalObjetivosAmbiguosTest {
    @Test
    void dosPuertasConMismoIdNoPermitenHackearUnObjetivoIndeterminado() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Terminal terminal = new Terminal("control", 5, "repetida");
        Puerta primera = puerta("repetida");
        Puerta segunda = puerta("repetida");
        juego.getMapa().getCelda(new Posicion(0, 0)).agregarElemento(terminal);
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(primera);
        juego.getMapa().getCelda(new Posicion(1, 0)).agregarElemento(segunda);
        List<RuidoGenerado> ruidos = escuchar(juego);

        ComandoException error = assertThrows(ComandoException.class,
                () -> new ComandoTerminal(new CommandContext(juego), true).ejecutar());

        assertTrue(error.getMessage().contains("ambiguo"));
        assertFalse(terminal.isHackeado());
        assertEquals(EstadoPuerta.BLINDADA, primera.getEstado());
        assertEquals(EstadoPuerta.BLINDADA, segunda.getEstado());
        assertTrue(ruidos.isEmpty());
    }

    @Test
    void interruptorAmbiguoNoSeActivaNiAfectaPuertas() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Interruptor interruptor = new Interruptor("control", false, "repetida");
        Puerta primera = puerta("repetida");
        Puerta segunda = puerta("repetida");
        juego.getMapa().getCelda(new Posicion(0, 0)).agregarElemento(interruptor);
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(primera);
        juego.getMapa().getCelda(new Posicion(1, 0)).agregarElemento(segunda);
        List<RuidoGenerado> ruidos = escuchar(juego);

        assertThrows(ComandoException.class,
                () -> new ComandoTerminal(new CommandContext(juego), false).ejecutar());

        assertFalse(interruptor.isActivo());
        assertEquals(EstadoPuerta.BLINDADA, primera.getEstado());
        assertEquals(EstadoPuerta.BLINDADA, segunda.getEstado());
        assertTrue(ruidos.isEmpty());
    }

    @Test
    void objetivoUnicoSeDesbloqueaConRuidoDelTerminal() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Terminal terminal = new Terminal("control", 5, "unica");
        Puerta puerta = puerta("unica");
        juego.getMapa().getCelda(new Posicion(0, 0)).agregarElemento(terminal);
        juego.getMapa().getCelda(new Posicion(2, 2)).agregarElemento(puerta);
        List<RuidoGenerado> ruidos = escuchar(juego);

        new ComandoTerminal(new CommandContext(juego), true).ejecutar();

        assertTrue(terminal.isHackeado());
        assertEquals(EstadoPuerta.CERRADA, puerta.getEstado());
        assertEquals(1, ruidos.size());
        assertEquals(new Posicion(0, 0), ruidos.get(0).origen());
    }

    @Test
    void terminalSinObjetivoConservaElComportamientoAutonomo() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Terminal terminal = new Terminal("control", 1, null);
        juego.getMapa().getCelda(new Posicion(0, 0)).agregarElemento(terminal);
        List<RuidoGenerado> ruidos = escuchar(juego);

        new ComandoTerminal(new CommandContext(juego), true).ejecutar();

        assertTrue(terminal.isHackeado());
        assertEquals(1, ruidos.size());
    }

    private static Puerta puerta(String id) {
        return new Puerta(id, EstadoPuerta.BLINDADA, null, true, 20);
    }

    private static List<RuidoGenerado> escuchar(Juego juego) {
        List<RuidoGenerado> ruidos = new ArrayList<>();
        juego.getBusEventos().suscribir(RuidoGenerado.class, ruidos::add);
        return ruidos;
    }
}
