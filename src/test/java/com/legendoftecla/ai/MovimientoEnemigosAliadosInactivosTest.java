package com.legendoftecla.ai;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MovimientoEnemigosAliadosInactivosTest {
    private static final Posicion DESTINO = new Posicion(1, 2);

    @Test
    void enemigoPuedeAtravesarLaCeldaDeUnAliadoCaido() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Scout enemigo = scout(juego);
        Aliado caido = aliado(juego);
        caido.setSalud(0);

        assertTrue(acercar(juego, enemigo));

        assertEquals(DESTINO, enemigo.getPosicion());
        assertEquals(0, caido.getSalud());
    }

    @Test
    void aliadoVivoContinuaBloqueandoLaCasillaOcupada() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Scout enemigo = scout(juego);
        Aliado aliado = aliado(juego);

        assertTrue(acercar(juego, enemigo));

        assertFalse(enemigo.getPosicion().equals(DESTINO));
        assertEquals(DESTINO, aliado.getPosicion());
    }

    @Test
    void aliadoEvacuadoNoBloqueaElPasoAunquePermanezcaEnListaActiva() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Scout enemigo = scout(juego);
        Aliado evacuado = aliado(juego);
        juego.setAliadosExtraidosDetalle(List.of(evacuado));

        assertTrue(acercar(juego, enemigo));

        assertEquals(DESTINO, enemigo.getPosicion());
        assertTrue(juego.estaAliadoExtraido(evacuado));
    }

    private static Scout scout(Juego juego) {
        Scout enemigo = new Scout("Patrulla", new Posicion(1, 1),
                new Mochila(2, 10), 4);
        juego.agregarEnemigo(enemigo);
        return enemigo;
    }

    private static Aliado aliado(Juego juego) {
        Aliado aliado = new Aliado("A", DESTINO, new Mochila(2, 10), 4);
        juego.agregarAliado(aliado);
        return aliado;
    }

    private static boolean acercar(Juego juego, Scout enemigo) {
        return new EjecutorAccionIA().ejecutar(juego, enemigo,
                new AccionIA(TipoAccionIA.ACERCARSE, DESTINO, "Caminar"),
                new Random(9));
    }
}
