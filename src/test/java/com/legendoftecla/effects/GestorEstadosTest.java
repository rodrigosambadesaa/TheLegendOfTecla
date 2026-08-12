package com.legendoftecla.effects;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.events.BusEventos;
import com.legendoftecla.events.EstadoAplicado;
import com.legendoftecla.events.EstadoEliminado;
import com.legendoftecla.events.EventoJuego;
import com.legendoftecla.events.PersonajeDanado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GestorEstadosTest {
    @Test
    void fuegoDuraSeAcumulaYAguaLoEliminaEImpideReaplicarlo() {
        Personaje personaje = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        GestorEstados estados = personaje.getEstados();
        estados.aplicar(new Quemado(2, 3));
        estados.aplicar(new Quemado(2, 3));

        estados.inicioTurno();
        assertEquals(114, personaje.getSalud());
        assertEquals(2, estados.getActivos().get(0).acumulaciones());

        estados.mojar(2);
        assertFalse(estados.contiene(TipoEstado.QUEMADO));
        assertFalse(estados.aplicar(new Quemado()));
        estados.finTurno();
        estados.finTurno();
        assertFalse(estados.contiene(TipoEstado.MOJADO));
    }

    @Test
    void sangradoDaniaAlMoverYAturdidoConsumeUnaAccion() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Personaje personaje = juego.getJugador();
        personaje.getEstados().aplicar(new Sangrado());

        personaje.mover(Direccion.ESTE, juego);
        assertEquals(117, personaje.getSalud());
        personaje.getEstados().aplicar(new Aturdido());
        assertThrows(Exception.class, () -> personaje.mover(Direccion.SUR, juego));
        personaje.mover(Direccion.SUR, juego);
        assertEquals(114, personaje.getSalud());
    }

    @Test
    void venenoCegueraMiedoInspiracionYDescansoSonDeterministas() {
        Personaje personaje = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        GestorEstados estados = personaje.getEstados();
        estados.aplicar(new Envenenado());
        estados.aplicar(new Cegado());
        estados.aplicar(new Asustado());
        estados.aplicar(new Inspirado());
        estados.aplicar(new Exhausto());

        assertEquals(0.55 * 0.75 * 1.2, estados.multiplicadorPrecision(), 0.0001);
        estados.finTurno();
        assertEquals(118, personaje.getSalud());
        assertTrue(estados.contiene(TipoEstado.EXHAUSTO));
        estados.descansar();
        assertFalse(estados.contiene(TipoEstado.EXHAUSTO));
    }

    @ParameterizedTest
    @MethodSource("efectosConDuracion")
    void todosLosEfectosCaducanEnLaDuracionDeclarada(EfectoEstado efecto) {
        Personaje personaje = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        GestorEstados estados = personaje.getEstados();
        estados.aplicar(efecto);

        for (int turno = 1; turno < efecto.duracionInicial(); turno++) {
            estados.finTurno();
            assertTrue(estados.contiene(efecto.tipo()));
        }
        estados.finTurno();

        assertFalse(estados.contiene(efecto.tipo()));
    }

    @Test
    void publicaAplicacionDanioYEliminacionConElRelojDeLaPartida() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Instant instante = Instant.parse("2042-03-04T05:06:07Z");
        juego.setBusEventos(new BusEventos(Clock.fixed(instante, ZoneOffset.UTC)));
        List<EventoJuego> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(EventoJuego.class, eventos::add);

        juego.getJugador().getEstados().aplicar(new Quemado(1, 3));
        juego.getJugador().getEstados().inicioTurno();
        juego.getJugador().getEstados().finTurno();

        assertEquals(List.of(EstadoAplicado.class, PersonajeDanado.class,
                EstadoEliminado.class), eventos.stream().map(Object::getClass).toList());
        assertTrue(eventos.stream().allMatch(evento -> evento.instante().equals(instante)));
    }

    @Test
    void restauraEstadoPersistibleYBotiquinDetieneSangrado() {
        Personaje personaje = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        personaje.getEstados().restaurar(List.of(
                new EstadoActivo(TipoEstado.SANGRADO, 4, 1),
                new EstadoActivo(TipoEstado.INSPIRADO, 2, 1)));

        new Botiquin("Trauma", "Cura", 1, 5).usar(personaje);

        assertFalse(personaje.getEstados().contiene(TipoEstado.SANGRADO));
        assertTrue(personaje.getEstados().contiene(TipoEstado.INSPIRADO));
        assertThrows(IllegalArgumentException.class,
                () -> new EstadoActivo(TipoEstado.QUEMADO, 0, 1));
    }

    @Test
    void cegueraReduceVisionYElResumenSirveAConsolaYGui() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Personaje personaje = juego.getJugador();
        int vision = personaje.getRangoVision();

        personaje.getEstados().aplicar(new Cegado());

        assertTrue(personaje.getRangoVision() < vision);
        assertTrue(com.legendoftecla.engine.SistemaEstados.resumen(personaje)
                .contains("cegado(2)"));
    }

    private static Stream<Arguments> efectosConDuracion() {
        return Stream.of(new Quemado(), new Envenenado(), new Sangrado(),
                        new Aturdido(), new Cegado(), new Mojado(), new Exhausto(),
                        new Asustado(), new Inspirado())
                .map(Arguments::of);
    }
}
