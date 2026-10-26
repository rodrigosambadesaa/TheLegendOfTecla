package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IndiceEspacialPersonajesTest {
    @Test
    void comparaMilesDeConsultasConUnOraculoIndependiente() {
        Random random = new Random(0x7EC1A);
        int[] radios = {0, 1, 2, 5, 20, 200, 400, Integer.MAX_VALUE};
        for (int escenario = 0; escenario < 80; escenario++) {
            List<Aliado> personajes = new ArrayList<>();
            for (int i = 0; i < 35; i++) {
                int fila = random.nextInt(230) - 15;
                int columna = random.nextInt(230) - 15;
                Aliado aliado = aliado("A" + i, fila, columna);
                aliado.setNivel(random.nextInt(100) + 1);
                personajes.add(aliado);
            }
            // Inserta duplicados de posicion: su orden original debe preservarse.
            personajes.add(aliado("Duplicado", 10, 10));
            personajes.add(aliado("Duplicado2", 10, 10));
            IndiceEspacialPersonajes<Aliado> indice = new IndiceEspacialPersonajes<>(personajes);

            for (int consulta = 0; consulta < 30; consulta++) {
                Posicion origen = new Posicion(
                        random.nextInt(330) - 65, random.nextInt(330) - 65);
                int radio = radios[random.nextInt(radios.length)];
                List<Aliado> esperado = personajes.stream()
                        .filter(personaje -> estaIndexado(personaje.getPosicion()))
                        .filter(personaje -> personaje.getNivel() % 2 == 0)
                        .filter(personaje -> distancia(origen, personaje.getPosicion()) <= radio)
                        .sorted(Comparator.comparingInt(
                                (Aliado personaje) -> distancia(origen, personaje.getPosicion()))
                                .thenComparingInt(personaje -> personaje.getPosicion().getFila())
                                .thenComparingInt(personaje -> personaje.getPosicion().getColumna()))
                        .toList();

                List<Aliado> cercanos = indice.cercanos(
                        origen, radio, personaje -> personaje.getNivel() % 2 == 0);
                assertEquals(esperado, cercanos,
                        "Escenario " + escenario + ", consulta " + consulta);
                assertEquals(esperado.isEmpty() ? null : esperado.get(0),
                        indice.masCercano(origen, radio, personaje -> personaje.getNivel() % 2 == 0));
                assertThrows(UnsupportedOperationException.class,
                        () -> cercanos.add(aliado("Invalido", 0, 0)));
            }
        }
    }

    @Test
    void soportaRadiosMaximosYOrigenesRemotosEnTiempoAcotado() {
        List<Aliado> personajes = new ArrayList<>();
        for (int i = 0; i < 5_000; i++) {
            personajes.add(aliado("A" + i, i % 200, (i * 31) % 200));
        }
        IndiceEspacialPersonajes<Aliado> indice = new IndiceEspacialPersonajes<>(personajes);
        Posicion origen = new Posicion(-1_000_000, -1_000_000);

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            assertEquals(5_000, indice.cercanos(
                    origen, Integer.MAX_VALUE, personaje -> true).size());
            assertSame(personajes.get(0), indice.masCercano(
                    origen, Integer.MAX_VALUE, personaje -> true));
            assertNull(indice.masCercano(origen, 1, personaje -> true));
        });
    }

    @Test
    void conservaInstantaneaCuandoCambianLaListaYLasPosiciones() {
        Aliado primero = aliado("Primero", 2, 2);
        Aliado segundo = aliado("Segundo", 2, 4);
        List<Aliado> mutables = new ArrayList<>(List.of(primero, segundo));
        IndiceEspacialPersonajes<Aliado> indice = new IndiceEspacialPersonajes<>(mutables);

        mutables.clear();
        primero.setPosicion(new Posicion(150, 150));

        assertSame(primero, indice.masCercano(new Posicion(2, 2), 0, aliado -> true));
        assertEquals(List.of(primero, segundo),
                indice.cercanos(new Posicion(2, 3), 1, aliado -> true));
        assertTrue(indice.alguno(aliado -> aliado == primero));
        assertFalse(indice.alguno(aliado -> aliado.getNombre().equals("Inexistente")));
    }

    @Test
    void respetaEmpatesYValidaEntradas() {
        Aliado ultimo = aliado("Ultimo", 1, 2);
        Aliado primero = aliado("Primero", 1, 0);
        Aliado arriba = aliado("Arriba", 0, 1);
        Aliado repetido = aliado("Repetido", 1, 0);
        IndiceEspacialPersonajes<Aliado> indice = new IndiceEspacialPersonajes<>(
                List.of(ultimo, primero, arriba, repetido));

        assertEquals(List.of(arriba, primero, repetido, ultimo),
                indice.cercanos(new Posicion(1, 1), 1, aliado -> true));
        assertSame(arriba, indice.masCercano(new Posicion(1, 1), aliado -> true));
        assertThrows(IllegalArgumentException.class,
                () -> indice.masCercano(new Posicion(0, 0), -1, aliado -> true));
        assertThrows(NullPointerException.class,
                () -> indice.cercanos(null, 1, aliado -> true));
        assertThrows(NullPointerException.class,
                () -> indice.cercanos(new Posicion(0, 0), 1, null));
        assertThrows(NullPointerException.class, () -> new IndiceEspacialPersonajes<Aliado>(null));
        assertThrows(NullPointerException.class,
                () -> new IndiceEspacialPersonajes<>(java.util.Arrays.asList(primero, null)));
    }

    private static Aliado aliado(String nombre, int fila, int columna) {
        return new Aliado(nombre, new Posicion(fila, columna), new Mochila(1, 1), 2);
    }

    private static boolean estaIndexado(Posicion posicion) {
        return posicion.getFila() >= 0 && posicion.getFila() < 200
                && posicion.getColumna() >= 0 && posicion.getColumna() < 200;
    }

    private static int distancia(Posicion origen, Posicion destino) {
        return Math.abs(origen.getFila() - destino.getFila())
                + Math.abs(origen.getColumna() - destino.getColumna());
    }
}
