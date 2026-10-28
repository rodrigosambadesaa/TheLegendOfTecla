package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.PuertaAbierta;
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

class ComandoPuertaUbicacionTest {
    @Test
    void ruidoProvieneDePuertaCercanaNoDeOtraConElMismoId() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().setPosicion(new Posicion(1, 1));
        Puerta remota = puerta("repetida");
        Puerta cercana = puerta("repetida");
        juego.getMapa().getCelda(new Posicion(0, 0)).agregarElemento(remota);
        juego.getMapa().getCelda(new Posicion(1, 2)).agregarElemento(cercana);
        List<RuidoGenerado> ruidos = observarRuidos(juego);
        List<PuertaAbierta> aperturas = new ArrayList<>();
        juego.getBusEventos().suscribir(PuertaAbierta.class, aperturas::add);

        new ComandoPuerta(new CommandContext(juego), true).ejecutar();

        assertEquals(EstadoPuerta.CERRADA, remota.getEstado());
        assertEquals(EstadoPuerta.ABIERTA, cercana.getEstado());
        assertEquals(1, aperturas.size());
        assertEquals(1, ruidos.size());
        assertEquals(new Posicion(1, 2), ruidos.get(0).origen());
    }

    @Test
    void ruidoSeOriginaEnCeldaActualOAdyacenteSegunLaPuertaEncontrada()
            throws Exception {
        for (Posicion destino : List.of(
                new Posicion(1, 1), new Posicion(0, 1),
                new Posicion(2, 1), new Posicion(1, 0), new Posicion(1, 2))) {
            Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
            juego.getJugador().setPosicion(new Posicion(1, 1));
            Puerta puerta = puerta("encontrada");
            juego.getMapa().getCelda(destino).agregarElemento(puerta);
            List<RuidoGenerado> ruidos = observarRuidos(juego);

            new ComandoPuerta(new CommandContext(juego), true).ejecutar();
            new ComandoPuerta(new CommandContext(juego), false).ejecutar();

            assertEquals(2, ruidos.size(), "Ubicacion " + destino);
            assertEquals(destino, ruidos.get(0).origen());
            assertEquals(destino, ruidos.get(1).origen());
            assertEquals(EstadoPuerta.CERRADA, puerta.getEstado());
        }
    }

    @Test
    void puertaLejanaNoSePuedeAccionarNiGeneraRuido() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getMapa().getCelda(new Posicion(2, 2)).agregarElemento(puerta("lejana"));
        List<RuidoGenerado> ruidos = observarRuidos(juego);

        assertThrows(ComandoException.class,
                () -> new ComandoPuerta(new CommandContext(juego), true).ejecutar());

        assertTrue(ruidos.isEmpty());
    }

    private static Puerta puerta(String id) {
        return new Puerta(id, EstadoPuerta.CERRADA, null, true, 10);
    }

    private static List<RuidoGenerado> observarRuidos(Juego juego) {
        List<RuidoGenerado> ruidos = new ArrayList<>();
        juego.getBusEventos().suscribir(RuidoGenerado.class, ruidos::add);
        return ruidos;
    }
}
