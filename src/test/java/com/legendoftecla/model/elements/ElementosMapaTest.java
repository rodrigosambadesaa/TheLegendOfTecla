package com.legendoftecla.model.elements;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.effects.TipoEstado;
import com.legendoftecla.commands.ComandoPuerta;
import com.legendoftecla.commands.ComandoTerminal;
import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.engine.SistemaCombate;
import com.legendoftecla.engine.SistemaTrampas;
import com.legendoftecla.events.EventoJuego;
import com.legendoftecla.events.PuertaAbierta;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.events.TrampaActivada;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.characters.Zapador;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElementosMapaTest {
    @Test
    void puertaActualizaPasoVisionLlavesYDestruccion() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion posicion = new Posicion(0, 1);
        Puerta puerta = new Puerta("p1", EstadoPuerta.BLOQUEADA,
                "tarjeta-roja", true, 10);
        juego.getMapa().getCelda(posicion).agregarElemento(puerta);

        assertFalse(juego.getMapa().esTransitable(posicion));
        assertFalse(juego.getMapa().hayLineaAtaque(new Posicion(0, 0), posicion));
        assertFalse(puerta.abrir("llave-azul"));
        assertTrue(puerta.abrir("tarjeta-roja"));
        assertTrue(juego.getMapa().esTransitable(posicion));
        assertTrue(puerta.cerrar());
        puerta.recibirDanio(10);
        assertTrue(juego.getMapa().esTransitable(posicion));
    }

    @Test
    void trampaOcultaDetectaPremiaZapadorYSeActivaAlEntrar() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        TrampaFuego trampa = new TrampaFuego("fuego-1");
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(trampa);
        Marine marine = (Marine) juego.getJugador();
        Zapador zapador = new Zapador("Z", new Posicion(0, 0), new Mochila(4, 20), 2);

        assertFalse(trampa.detectar(marine, 1));
        assertTrue(trampa.detectar(zapador, 2));
        assertTrue(trampa.desactivar(zapador, 2));

        TrampaFuego activa = new TrampaFuego("fuego-2");
        juego.getMapa().getCelda(new Posicion(0, 1)).setElementos(List.of(activa));
        marine.mover(Direccion.ESTE, juego);
        assertEquals(116, marine.getSalud());
        assertTrue(marine.getEstados().contiene(TipoEstado.QUEMADO));
        assertFalse(activa.isActiva());
    }

    @Test
    void coberturaUsaRngInyectadoYFlanqueoAnulaPenalizacion() {
        SistemaCobertura cobertura = new SistemaCobertura(new Random(7));
        double protegida = cobertura.probabilidadImpacto(
                0.8, TipoCobertura.COMPLETA, false, 1.0);
        double flanqueada = cobertura.probabilidadImpacto(
                0.8, TipoCobertura.COMPLETA, true, 1.0);

        assertEquals(0.25, protegida, 0.0001);
        assertEquals(0.8, flanqueada, 0.0001);
        assertFalse(cobertura.impacta(protegida));
        assertTrue(new Barricada("b", 5, TipoCobertura.COMPLETA,
                OrientacionCobertura.NORTE).bloqueaVision());
    }

    @Test
    void minaRemotaAfectaATodosLosBandosEnRadioYPublicaEventos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion centro = new Posicion(1, 1);
        Mina mina = new Mina("remota", 8, 2, true);
        juego.getMapa().getCelda(centro).agregarElemento(mina);
        Aliado aliado = new Aliado("Aliado", new Posicion(1, 2), new Mochila(3, 12), 2);
        Sectoid enemigo = new Sectoid("Enemigo", new Posicion(2, 1), new Mochila(2, 10), 2);
        juego.agregarAliado(aliado);
        juego.agregarEnemigo(enemigo);
        List<EventoJuego> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(EventoJuego.class, eventos::add);

        assertTrue(SistemaTrampas.detonar(juego, centro, mina));

        assertEquals(112, juego.getJugador().getSalud());
        assertEquals(82, aliado.getSalud());
        assertEquals(62, enemigo.getSalud());
        assertEquals(3, eventos.stream().filter(TrampaActivada.class::isInstance).count());
        assertTrue(eventos.stream().anyMatch(RuidoGenerado.class::isInstance));
        assertFalse(SistemaTrampas.detonar(juego, centro, mina));
    }

    @Test
    void terminalDesbloqueaPuertaBlindadaYLaInteraccionGeneraRuido() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Puerta puerta = new Puerta("bunker", EstadoPuerta.BLINDADA,
                null, false, 20);
        Terminal terminal = new Terminal("terminal", 5, "bunker");
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(puerta);
        juego.getMapa().getCelda(juego.getJugador().getPosicion()).agregarElemento(terminal);
        List<EventoJuego> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(EventoJuego.class, eventos::add);

        new ComandoTerminal(new CommandContext(juego), true).ejecutar();
        new ComandoPuerta(new CommandContext(juego), true).ejecutar();

        assertEquals(EstadoPuerta.ABIERTA, puerta.getEstado());
        assertTrue(eventos.stream().anyMatch(PuertaAbierta.class::isInstance));
        assertEquals(2, eventos.stream().filter(RuidoGenerado.class::isInstance).count());
    }

    @Test
    void combateConsultaCoberturaOrientadaConRngDeterminista() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid objetivo = new Sectoid("Objetivo", new Posicion(0, 2),
                new Mochila(2, 10), 2);
        juego.agregarEnemigo(objetivo);
        juego.getMapa().getCelda(objetivo.getPosicion()).agregarEnemigo(objetivo);
        Barricada barricada = new Barricada("media", 10, TipoCobertura.MEDIA,
                OrientacionCobertura.OESTE);
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(barricada);
        Random falla = new Random(1) {
            @Override public double nextDouble() { return 0.99; }
        };

        var resultado = SistemaCombate.atacar(
                juego, juego.getJugador(), objetivo, falla);

        assertEquals(0, resultado.vidaQuitada());
        SistemaCobertura.Proteccion flanqueada = new SistemaCobertura(new Random(1))
                .proteccion(juego.getMapa(), juego.getJugador().getPosicion(),
                        objetivo.getPosicion());
        assertFalse(flanqueada.flanqueada());
        barricada.recibirDanio(10);
        assertEquals(TipoCobertura.NINGUNA, barricada.getCobertura());
    }
}
