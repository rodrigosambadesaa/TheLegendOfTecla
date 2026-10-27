package com.legendoftecla.ai;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.EventoJuego;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.events.PersonajeMuerto;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Granada;
import com.legendoftecla.model.items.TipoGranada;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GranadaEnemigaEventosMuerteTest {
    @Test
    void unaGranadaLetalNotificaLaMuerteDeJugadorYAliadoYActualizaEstadisticas() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion objetivo = juego.getJugador().getPosicion();
        juego.getJugador().setSalud(12);
        Aliado aliado = new Aliado("Rescate", objetivo, new Mochila(3, 10), 2);
        aliado.setSalud(17);
        juego.agregarAliado(aliado);
        Sectoid atacante = atacanteConGranada(juego, new Posicion(0, 1));
        List<EventoJuego> eventos = escuchar(juego);

        assertTrue(lanzar(juego, atacante, objetivo));

        assertEquals(0, juego.getJugador().getSalud());
        assertEquals(0, aliado.getSalud());
        assertEquals(2, eventos.stream().filter(PersonajeMuerto.class::isInstance).count());
        assertEquals(2, eventos.stream().filter(PersonajeDanado.class::isInstance).count());
        assertEquals(2, eventos.stream().filter(PersonajeAtacado.class::isInstance).count());
        assertEquals(2, juego.getEstadisticas().getMuertes());
        assertEquals(12, juego.getEstadisticas().getDanoRecibido());
        assertTrue(atacante.getMochila().getObjetos().isEmpty());
    }

    @Test
    void unaGranadaNoLetalNotificaDanoPeroNoMuerte() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion objetivo = juego.getJugador().getPosicion();
        juego.getJugador().setSalud(70);
        Sectoid atacante = atacanteConGranada(juego, new Posicion(0, 1));
        List<EventoJuego> eventos = escuchar(juego);

        assertTrue(lanzar(juego, atacante, objetivo));

        assertEquals(50, juego.getJugador().getSalud());
        assertEquals(0, eventos.stream().filter(PersonajeMuerto.class::isInstance).count());
        assertEquals(1, eventos.stream().filter(PersonajeDanado.class::isInstance).count());
        assertEquals(0, juego.getEstadisticas().getMuertes());
        assertEquals(20, juego.getEstadisticas().getDanoRecibido());
    }

    @Test
    void noEmiteMuertesDuplicadasSobrePersonajesYaFallecidos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion objetivo = juego.getJugador().getPosicion();
        juego.getJugador().setSalud(5);
        Sectoid atacante = atacanteConGranada(juego, new Posicion(0, 1));
        List<EventoJuego> eventos = escuchar(juego);

        assertTrue(lanzar(juego, atacante, objetivo));
        atacante.getMochila().guardar(granada());
        assertTrue(lanzar(juego, atacante, objetivo));

        assertEquals(0, juego.getJugador().getSalud());
        assertEquals(1, eventos.stream().filter(PersonajeMuerto.class::isInstance).count());
        assertEquals(1, eventos.stream().filter(PersonajeDanado.class::isInstance).count());
        assertEquals(1, juego.getEstadisticas().getMuertes());
        assertTrue(juego.getMapa().getCelda(objetivo).estaArdiendo());
    }

    private static Sectoid atacanteConGranada(Juego juego, Posicion posicion) {
        Sectoid atacante = new Sectoid("Lanzador", posicion, new Mochila(4, 10), 5);
        atacante.getMochila().guardar(granada());
        juego.agregarEnemigo(atacante);
        return atacante;
    }

    private static Granada granada() {
        return new Granada("Incendiaria", "Explosion", 1, TipoGranada.INCENDIARIA);
    }

    private static boolean lanzar(Juego juego, Sectoid atacante, Posicion objetivo) {
        return new EjecutorAccionIA().ejecutar(juego, atacante,
                new AccionIA(TipoAccionIA.INCENDIAR, objetivo, "prueba"), new Random(27));
    }

    private static List<EventoJuego> escuchar(Juego juego) {
        List<EventoJuego> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(EventoJuego.class, eventos::add);
        return eventos;
    }
}
