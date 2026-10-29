package com.legendoftecla.events;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.ai.FuenteRuido;
import com.legendoftecla.ai.NivelAlerta;
import com.legendoftecla.ai.SistemaRuido;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RuidoGeneradoInstantaneaTest {
    @Test
    void laPosicionDelEmisorNoAlteraUnEventoPublicable() {
        Posicion origen = new Posicion(0, 0);
        RuidoGenerado evento = new RuidoGenerado(
                Instant.EPOCH, origen, 5, "pasos");
        int hashOriginal = evento.hashCode();

        origen.setFila(2);
        origen.setColumna(2);

        assertEquals(new Posicion(0, 0), evento.origen());
        assertEquals(hashOriginal, evento.hashCode());
        assertNotEquals(origen, evento.origen());
    }

    @Test
    void losListenersNoPuedenAlterarLasCoordenadasDeOtrosListeners() {
        BusEventos bus = new BusEventos();
        List<Posicion> posicionesRecibidas = new ArrayList<>();
        bus.suscribir(RuidoGenerado.class, ruido -> {
            Posicion posicion = ruido.origen();
            posicion.setFila(2);
            posicion.setColumna(2);
        });
        bus.suscribir(RuidoGenerado.class,
                ruido -> posicionesRecibidas.add(ruido.origen()));

        RuidoGenerado evento = new RuidoGenerado(
                Instant.EPOCH, new Posicion(0, 0), 3, "alerta");
        bus.publicar(evento);

        assertEquals(List.of(new Posicion(0, 0)), posicionesRecibidas);
        assertEquals(new Posicion(0, 0), evento.origen());
    }

    @Test
    void laUltimaSenalSonoraSigueSiendoLaOriginalDespuesDeConsultarla() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Scout scout = new Scout("Oidor", new Posicion(0, 1),
                new Mochila(2, 10), 4);
        juego.agregarEnemigo(scout);
        Posicion origen = new Posicion(0, 0);

        try (SistemaRuido sistema = new SistemaRuido(juego)) {
            sistema.generar(origen, FuenteRuido.CAMINAR);
            origen.setColumna(2);
            sistema.getUltimoRuido().orElseThrow().origen().setFila(2);

            assertEquals(new Posicion(0, 0),
                    sistema.getUltimoRuido().orElseThrow().origen());
            assertEquals(NivelAlerta.INVESTIGANDO, scout.getControladorIA().getEstado());
            assertEquals(new Posicion(0, 0),
                    scout.getControladorIA().getUltimaPosicionConocida());
        }
    }

    @Test
    void elEventoRechazaParametrosNulosSinPublicarse() {
        assertThrows(NullPointerException.class,
                () -> new RuidoGenerado(Instant.EPOCH, null, 1, "ruido"));
        assertThrows(NullPointerException.class,
                () -> new RuidoGenerado(null, new Posicion(0, 0), 1, "ruido"));
    }
}
