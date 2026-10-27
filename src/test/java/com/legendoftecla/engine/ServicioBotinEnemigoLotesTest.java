package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioBotinEnemigoLotesTest {
    @Test
    void preservaObjetosEnElSueloYSoloCuentaLosRealmenteTransferidos() {
        Celda celda = new Celda("Botin", true);
        Objeto anterior = pieza("Anterior");
        Objeto compartido = pieza("Compartido");
        Objeto nuevo = pieza("Nuevo");
        celda.setObjetos(List.of(anterior, compartido));
        Sectoid enemigo = enemigo(5);
        enemigo.getMochila().setObjetos(List.of(compartido, nuevo));

        assertEquals(1, ServicioBotinEnemigo.soltar(celda, enemigo));
        assertEquals(3, celda.getObjetos().size());
        assertSame(anterior, celda.getObjetos().get(0));
        assertSame(compartido, celda.getObjetos().get(1));
        assertSame(nuevo, celda.getObjetos().get(2));
        assertTrue(enemigo.getMochila().getObjetos().isEmpty());
        assertEquals(0, ServicioBotinEnemigo.soltar(celda, enemigo));
    }

    @Test
    void eliminaInstanciasDuplicadasEnLaMochilaSinClonarElBotin() {
        Celda celda = new Celda("Botin", true);
        Objeto repetido = pieza("Repetido");
        Objeto distinto = pieza("Repetido");
        Sectoid enemigo = enemigo(5);
        enemigo.getMochila().setObjetos(List.of(repetido, repetido, distinto));

        assertEquals(2, ServicioBotinEnemigo.soltar(celda, enemigo));
        assertEquals(2, celda.getObjetos().size());
        assertSame(repetido, celda.getObjetos().get(0));
        assertSame(distinto, celda.getObjetos().get(1));
    }

    @Test
    void milesDeObjetosSeTransfierenSinCopiaPorObjeto() {
        Celda celda = new Celda("Botin", true);
        List<Objeto> previos = new ArrayList<>();
        for (int i = 0; i < 2_000; i++) previos.add(pieza("Suelo" + i));
        celda.setObjetos(previos);

        Sectoid enemigo = enemigo(6_000);
        List<Objeto> reserva = new ArrayList<>();
        for (int i = 0; i < 5_000; i++) reserva.add(pieza("Reserva" + i));
        enemigo.getMochila().setObjetos(reserva);

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            assertEquals(5_000, ServicioBotinEnemigo.soltar(celda, enemigo));
            assertEquals(7_000, celda.getObjetos().size());
            assertSame(previos.get(0), celda.getObjetos().get(0));
            assertSame(previos.get(1_999), celda.getObjetos().get(1_999));
            assertSame(reserva.get(0), celda.getObjetos().get(2_000));
            assertSame(reserva.get(4_999), celda.getObjetos().get(6_999));
            assertTrue(enemigo.getMochila().getObjetos().isEmpty());
            assertEquals(0, ServicioBotinEnemigo.soltar(celda, enemigo));
        });
    }

    private static Sectoid enemigo(int capacidad) {
        return new Sectoid("Botin", new Posicion(1, 1), new Mochila(capacidad, 1_000), 2);
    }

    private static Componente pieza(String nombre) {
        return new Componente(nombre, "Botin", 0.1);
    }
}
