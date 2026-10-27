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

class ReemplazoMapaPersonajesTest {
    @Test
    void noRecortaUnEnemigoYConservaElMapaAnterior() {
        Juego juego = juegoGrande();
        Mapa original = juego.getMapa();
        Sectoid enemigo = new Sectoid("Enemigo", new Posicion(4, 4), new Mochila(1, 1), 2);
        juego.agregarEnemigo(enemigo);

        assertThrows(IllegalArgumentException.class, () -> juego.setMapa(mapa(3)));
        assertSame(original, juego.getMapa());
        assertEquals(new Posicion(4, 4), juego.getEnemigos().get(0).getPosicion());
    }

    @Test
    void noRecortaUnAliadoNiSuHistorialCuandoEstaEvacuado() {
        Juego juego = juegoGrande();
        Mapa original = juego.getMapa();
        Aliado aliado = new Aliado("Aliado", new Posicion(4, 3), new Mochila(1, 1), 2);
        juego.agregarAliado(aliado);

        assertThrows(IllegalArgumentException.class, () -> juego.setMapa(mapa(3)));
        assertSame(original, juego.getMapa());

        juego.setAliados(List.of());
        juego.setAliadosExtraidosDetalle(List.of(aliado));
        assertThrows(IllegalArgumentException.class, () -> juego.setMapa(mapa(3)));
        assertSame(original, juego.getMapa());
        assertEquals(List.of(aliado), juego.getAliadosExtraidosDetalle());
    }

    @Test
    void permiteSustituirPorMapaMenorCuandoTodosPermanecenDentro() {
        Juego juego = juegoGrande();
        Aliado aliado = new Aliado("Aliado", new Posicion(2, 1), new Mochila(1, 1), 2);
        Sectoid enemigo = new Sectoid("Enemigo", new Posicion(1, 2), new Mochila(1, 1), 2);
        juego.agregarAliado(aliado);
        juego.agregarEnemigo(enemigo);

        Mapa nuevo = mapa(3);
        juego.setMapa(nuevo);

        assertSame(nuevo, juego.getMapa());
        assertEquals(List.of(aliado), juego.getAliados());
        assertEquals(List.of(enemigo), juego.getEnemigos());
    }

    private static Juego juegoGrande() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.setMapa(mapa(5));
        return juego;
    }

    private static Mapa mapa(int lado) {
        Mapa resultado = new Mapa("Nuevo", "Prueba", lado, lado,
                new Posicion(0, 0), new Posicion(lado - 1, lado - 1));
        for (int fila = 0; fila < lado; fila++) {
            for (int columna = 0; columna < lado; columna++) {
                resultado.setCelda(fila, columna,
                        new Celda("Libre", true));
            }
        }
        return resultado;
    }
}
