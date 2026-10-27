package com.legendoftecla.stats;

import com.legendoftecla.events.AliadoEvacuado;
import com.legendoftecla.events.BusEventos;
import com.legendoftecla.events.CeldaInspeccionada;
import com.legendoftecla.events.IncendioExtinguido;
import com.legendoftecla.events.ObjetoUsado;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.events.PersonajeMovido;
import com.legendoftecla.events.PersonajeMuerto;
import com.legendoftecla.events.TrampaDesactivada;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstadisticasPartidaIntegridadTest {
    @Test
    void todosLosContadoresSeSaturanSinDesbordarseAnteEventos() {
        BusEventos bus = new BusEventos();
        try (EstadisticasPartida estadisticas = new EstadisticasPartida(bus)) {
            int casiMaximo = Integer.MAX_VALUE - 1;
            estadisticas.restaurar(new EstadisticasPartida.Snapshot(
                    casiMaximo, casiMaximo, casiMaximo, casiMaximo,
                    casiMaximo, casiMaximo, casiMaximo, casiMaximo,
                    casiMaximo, casiMaximo, casiMaximo));
            Posicion posicion = new Posicion(1, 1);
            Instant ahora = Instant.EPOCH;

            for (int i = 0; i < 3; i++) {
                bus.publicar(new PersonajeMovido(ahora, "Tecla", posicion, posicion));
                bus.publicar(new PersonajeAtacado(ahora, "Tecla", "Enemigo",
                        posicion, posicion));
                bus.publicar(new PersonajeDanado(ahora, "Enemigo",
                        Integer.MAX_VALUE, posicion));
                bus.publicar(new PersonajeMuerto(ahora, "Enemigo", posicion));
                bus.publicar(new AliadoEvacuado(ahora, "Aliado", posicion));
                bus.publicar(new IncendioExtinguido(ahora, posicion));
                bus.publicar(new ObjetoUsado(ahora, "Tecla", "Cubo", posicion));
                bus.publicar(new TrampaDesactivada(ahora, "Mina", "Tecla"));
                bus.publicar(new CeldaInspeccionada(ahora, "Tecla", posicion));
                estadisticas.registrarDanoRecibido(Integer.MAX_VALUE);
            }
            EstadisticasPartida.Snapshot todos = estadisticas.snapshot();
            assertEquals(Integer.MAX_VALUE, todos.danoCausado());
            assertEquals(Integer.MAX_VALUE, todos.danoRecibido());
            assertEquals(Integer.MAX_VALUE, todos.muertes());
            assertEquals(Integer.MAX_VALUE - 1, todos.enemigosEliminados());
            assertEquals(Integer.MAX_VALUE, todos.aliadosEvacuados());
            assertEquals(Integer.MAX_VALUE, todos.incendiosApagados());
            assertEquals(Integer.MAX_VALUE, todos.objetosUsados());
            assertEquals(Integer.MAX_VALUE, todos.pasos());
            assertEquals(Integer.MAX_VALUE, todos.disparos());
            assertEquals(Integer.MAX_VALUE, todos.trampasDesactivadas());
            assertEquals(Integer.MAX_VALUE, todos.celdasInspeccionadas());
        }
    }

    @Test
    void eventosConDanoNegativoNoReducenUnTotalAcumulado() {
        BusEventos bus = new BusEventos();
        try (EstadisticasPartida estadisticas = new EstadisticasPartida(bus)) {
            Posicion posicion = new Posicion(0, 0);
            bus.publicar(new PersonajeDanado(Instant.EPOCH, "Enemigo", 12, posicion));
            bus.publicar(new PersonajeDanado(Instant.EPOCH, "Enemigo", -100, posicion));
            estadisticas.registrarDanoRecibido(17);
            estadisticas.registrarDanoRecibido(-200);

            assertEquals(12, estadisticas.getDanoCausado());
            assertEquals(17, estadisticas.getDanoRecibido());
        }
    }

    @Test
    void snapshotCorruptoNoDejaLosCamposRestauradosAmedias() {
        BusEventos bus = new BusEventos();
        try (EstadisticasPartida estadisticas = new EstadisticasPartida(bus)) {
            EstadisticasPartida.Snapshot anterior = new EstadisticasPartida.Snapshot(
                    11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21);
            estadisticas.restaurar(anterior);
            EstadisticasPartida.Snapshot corrupto = new EstadisticasPartida.Snapshot(
                    40, 41, 42, 43, 44, 45, 46, 47, 48, 49, -1);

            assertThrows(IllegalArgumentException.class,
                    () -> estadisticas.restaurar(corrupto));
            assertEquals(anterior, estadisticas.snapshot());
        }
    }
}
