package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.PuertaAbierta;
import com.legendoftecla.events.PuertaCerrada;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.elements.EstadoPuerta;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandoPuertaIdempotenciaTest {
    @Test
    void abrirCienVecesLaMismaPuertaGeneraUnaSolaAperturaYRuido() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Puerta puerta = nuevaPuerta("repetida", EstadoPuerta.CERRADA);
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(puerta);
        List<PuertaAbierta> aperturas = new ArrayList<>();
        List<RuidoGenerado> ruidos = new ArrayList<>();
        juego.getBusEventos().suscribir(PuertaAbierta.class, aperturas::add);
        juego.getBusEventos().suscribir(RuidoGenerado.class, ruidos::add);

        for (int intento = 0; intento < 100; intento++) {
            new ComandoPuerta(new CommandContext(juego), true).ejecutar();
        }

        assertEquals(EstadoPuerta.ABIERTA, puerta.getEstado());
        assertEquals(1, aperturas.size());
        assertEquals(1, ruidos.size());
        assertEquals(new Posicion(0, 1), ruidos.get(0).origen());
    }

    @Test
    void abrirPuertaDestruidaNoPublicaFalsasAperturas() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Puerta puerta = nuevaPuerta("rota", EstadoPuerta.CERRADA);
        puerta.recibirDanio(10);
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(puerta);
        List<PuertaAbierta> aperturas = new ArrayList<>();
        List<RuidoGenerado> ruidos = new ArrayList<>();
        juego.getBusEventos().suscribir(PuertaAbierta.class, aperturas::add);
        juego.getBusEventos().suscribir(RuidoGenerado.class, ruidos::add);

        new ComandoPuerta(new CommandContext(juego), true).ejecutar();

        assertTrue(puerta.estaDestruido());
        assertEquals(EstadoPuerta.CERRADA, puerta.getEstado());
        assertTrue(aperturas.isEmpty());
        assertTrue(ruidos.isEmpty());
    }

    @Test
    void abrirCerrarYReabrirProduceSoloTresTransicionesReales() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Puerta puerta = nuevaPuerta("funcional", EstadoPuerta.CERRADA);
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(puerta);
        List<PuertaAbierta> aperturas = new ArrayList<>();
        List<PuertaCerrada> cierres = new ArrayList<>();
        List<RuidoGenerado> ruidos = new ArrayList<>();
        juego.getBusEventos().suscribir(PuertaAbierta.class, aperturas::add);
        juego.getBusEventos().suscribir(PuertaCerrada.class, cierres::add);
        juego.getBusEventos().suscribir(RuidoGenerado.class, ruidos::add);

        new ComandoPuerta(new CommandContext(juego), true).ejecutar();
        new ComandoPuerta(new CommandContext(juego), true).ejecutar();
        new ComandoPuerta(new CommandContext(juego), false).ejecutar();
        new ComandoPuerta(new CommandContext(juego), true).ejecutar();

        assertEquals(EstadoPuerta.ABIERTA, puerta.getEstado());
        assertEquals(2, aperturas.size());
        assertEquals(1, cierres.size());
        assertEquals(3, ruidos.size());
    }

    @Test
    void puertaBloqueadaSinCredencialNoPublicaRuido() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Puerta puerta = new Puerta("privada", EstadoPuerta.BLOQUEADA,
                "llave", true, 10);
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(puerta);
        List<RuidoGenerado> ruidos = new ArrayList<>();
        juego.getBusEventos().suscribir(RuidoGenerado.class, ruidos::add);

        assertThrows(ComandoException.class, () ->
                new ComandoPuerta(new CommandContext(juego), true).ejecutar());

        assertEquals(EstadoPuerta.BLOQUEADA, puerta.getEstado());
        assertTrue(ruidos.isEmpty());
    }

    private static Puerta nuevaPuerta(String id, EstadoPuerta estado) {
        return new Puerta(id, estado, null, true, 10);
    }
}
