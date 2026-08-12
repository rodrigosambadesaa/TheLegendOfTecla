package com.legendoftecla.audio;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.IncendioExtinguido;
import com.legendoftecla.events.IncendioIniciado;
import com.legendoftecla.events.ObjetoTirado;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.events.PersonajeMovido;
import com.legendoftecla.events.PersonajeMuerto;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SuscriptorAudioEventosTest {
    @Test
    void traduceEventosEnOrdenYConAudioPosicional() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion origen = new Posicion(1, 1);
        List<LlamadaAudio> llamadas = new ArrayList<>();
        SuscriptorAudioEventos.registrar(juego,
                (sonido, posicion, oyente) -> llamadas.add(
                        new LlamadaAudio(sonido, posicion, oyente)));

        juego.publicarEvento(new PersonajeMovido(Instant.EPOCH, "Tecla",
                juego.getJugador().getPosicion(), origen));
        juego.publicarEvento(new PersonajeAtacado(Instant.EPOCH, "Tecla", "Alien",
                origen, origen));
        juego.publicarEvento(new PersonajeDanado(Instant.EPOCH, "Alien", 3, origen));
        juego.publicarEvento(new ObjetoTirado(Instant.EPOCH, "Tecla", "Piedra", origen));
        juego.publicarEvento(new IncendioIniciado(Instant.EPOCH, origen));
        juego.publicarEvento(new IncendioExtinguido(Instant.EPOCH, origen));
        juego.publicarEvento(new PersonajeMuerto(Instant.EPOCH, "Alien", origen));

        assertEquals(List.of(EventoSonido.MOVIMIENTO, EventoSonido.ATAQUE,
                EventoSonido.DANIO, EventoSonido.TIRAR, EventoSonido.INCENDIO,
                EventoSonido.APAGAR_FUEGO, EventoSonido.MUERTE_ENEMIGO),
                llamadas.stream().map(LlamadaAudio::sonido).toList());
        assertEquals(origen, llamadas.get(0).origen());
        assertEquals(juego.getJugador().getPosicion(), llamadas.get(0).oyente());
    }

    private record LlamadaAudio(EventoSonido sonido, Posicion origen, Posicion oyente) { }
}
