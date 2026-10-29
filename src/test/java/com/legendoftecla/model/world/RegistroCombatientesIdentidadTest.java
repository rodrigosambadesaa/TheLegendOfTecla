package com.legendoftecla.model.world;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RegistroCombatientesIdentidadTest {
    @Test
    void unEnemigoNoPuedeParticiparDosVecesEnElMismoTurnoPorDuplicado() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid enemigo = enemigo("Uno");
        juego.agregarEnemigo(enemigo);

        assertThrows(IllegalArgumentException.class,
                () -> juego.setEnemigos(List.of(enemigo, enemigo)));

        assertEquals(1, juego.getEnemigos().size());
        assertSame(enemigo, juego.getEnemigos().get(0));
    }

    @Test
    void noAdmiteDuplicadosEnAliadosActivosNiEnSuHistorial() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = aliado("Apoyo");
        juego.agregarAliado(aliado);

        assertThrows(IllegalArgumentException.class,
                () -> juego.setAliados(List.of(aliado, aliado)));
        assertThrows(IllegalArgumentException.class,
                () -> juego.setAliadosRegistrados(List.of(aliado, aliado)));

        assertEquals(List.of(aliado), juego.getAliados());
        assertEquals(List.of(aliado), juego.getAliadosRegistrados());
    }

    @Test
    void evacuadosDuplicadosNoAlteranElHistorialNiElIndice() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = aliado("Rescatado");
        juego.agregarAliado(aliado);
        juego.setAliadosExtraidosDetalle(List.of(aliado));

        assertThrows(IllegalArgumentException.class,
                () -> juego.setAliadosExtraidosDetalle(List.of(aliado, aliado)));

        assertEquals(List.of(aliado), juego.getAliadosExtraidosDetalle());
        assertEquals(1, juego.getAliadosExtraidosIndice().size());
    }

    @Test
    void dosEnemigosDistintosConElMismoNombreSiguenSiendoValidos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid uno = enemigo("Mismo nombre");
        Sectoid dos = enemigo("Mismo nombre");

        juego.setEnemigos(List.of(uno, dos));

        assertEquals(2, juego.getEnemigos().size());
        assertSame(uno, juego.getEnemigos().get(0));
        assertSame(dos, juego.getEnemigos().get(1));
    }

    private static Sectoid enemigo(String nombre) {
        return new Sectoid(nombre, new Posicion(1, 1), new Mochila(3, 10), 2);
    }

    private static Aliado aliado(String nombre) {
        return new Aliado(nombre, new Posicion(1, 1), new Mochila(3, 10), 2);
    }
}
