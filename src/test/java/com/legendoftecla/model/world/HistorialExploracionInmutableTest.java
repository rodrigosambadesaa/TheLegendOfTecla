package com.legendoftecla.model.world;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.CeldaInspeccionada;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HistorialExploracionInmutableTest {
    @Test
    void noPierdeInspeccionCuandoSeMutaLaPosicionDelJugador() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion posicionJugador = juego.getJugador().getPosicion();

        juego.inspeccionarCeldaActual();
        posicionJugador.setColumna(1);

        assertTrue(juego.isCeldaInspeccionada(new Posicion(0, 0)));
        assertFalse(juego.isCeldaInspeccionada(new Posicion(0, 1)));
    }

    @Test
    void lasPosicionesExternasNoMutanElRegistroNiPorSetterNiPorGetter() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion fuente = new Posicion(1, 1);
        Set<Posicion> inspeccionadas = new HashSet<>();
        inspeccionadas.add(fuente);

        juego.setCeldasInspeccionadas(inspeccionadas);
        fuente.setFila(2);
        assertTrue(juego.isCeldaInspeccionada(new Posicion(1, 1)));
        assertFalse(juego.isCeldaInspeccionada(new Posicion(2, 1)));

        Set<Posicion> vista = juego.getCeldasInspeccionadas();
        assertThrows(UnsupportedOperationException.class, () -> vista.clear());
        vista.iterator().next().setColumna(2);
        assertTrue(juego.isCeldaInspeccionada(new Posicion(1, 1)));
        assertFalse(juego.isCeldaInspeccionada(new Posicion(1, 2)));
    }

    @Test
    void noPierdeInspeccionAliadaTrasMoverseYNoFiltraClavesExternas() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("Explorador", new Posicion(1, 1), new Mochila(2, 5), 2);
        juego.agregarAliado(aliado);
        Posicion original = aliado.getPosicion();

        assertTrue(juego.inspeccionarCeldaAliado(aliado));
        original.setColumna(2);
        assertTrue(juego.isCeldaInspeccionada(aliado, new Posicion(1, 1)));
        assertFalse(juego.isCeldaInspeccionada(aliado, new Posicion(1, 2)));

        Posicion copiaExpuesta = juego.getCeldasInspeccionadasAliados()
                .get(aliado).iterator().next();
        copiaExpuesta.setFila(2);
        assertTrue(juego.isCeldaInspeccionada(aliado, new Posicion(1, 1)));
    }

    @Test
    void cambiosDelEventoNoAlteranElHistorialDeExploracion() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("Explorador", new Posicion(1, 1), new Mochila(2, 5), 2);
        juego.agregarAliado(aliado);
        juego.getBusEventos().suscribir(CeldaInspeccionada.class,
                evento -> evento.posicion().setColumna(2));

        juego.inspeccionarCeldaActual();
        juego.inspeccionarCeldaAliado(aliado);

        assertTrue(juego.isCeldaInspeccionada(new Posicion(0, 0)));
        assertTrue(juego.isCeldaInspeccionada(aliado, new Posicion(1, 1)));
        assertFalse(juego.isCeldaInspeccionada(aliado, new Posicion(1, 2)));
    }

    @Test
    void alRestaurarInspeccionesAliadasSeCopianLasCoordenadas() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("Explorador", new Posicion(1, 1), new Mochila(2, 5), 2);
        juego.agregarAliado(aliado);

        Posicion externa = new Posicion(2, 0);
        Set<Posicion> fuente = new HashSet<>();
        fuente.add(externa);
        juego.setCeldasInspeccionadasAliados(Map.of(aliado, fuente));

        externa.setColumna(2);
        assertTrue(juego.isCeldaInspeccionada(aliado, new Posicion(2, 0)));
        assertFalse(juego.isCeldaInspeccionada(aliado, new Posicion(2, 2)));
    }
}
