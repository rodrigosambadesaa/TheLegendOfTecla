package com.legendoftecla.inventory;

import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.Objeto;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricacionAtomicaTest {
    @Test
    void ingredientesDeNombreRepetidoSeConsumenUnaVezYEnOrden() {
        Mochila mochila = new Mochila(6, 30);
        Objeto sinUsar = pieza("Otro", 2);
        Objeto primero = pieza("PIEZA", 1);
        Objeto segundo = pieza("pieza", 1);
        Objeto tercero = pieza("Pieza", 1);
        Objeto restante = pieza("Otro", 3);
        mochila.setObjetos(List.of(sinUsar, primero, segundo, tercero, restante));
        SistemaFabricacion sistema = new SistemaFabricacion();
        sistema.registrar(new Receta("kit", List.of(
                new Ingrediente("pieza", 1), new Ingrediente("PIEZA", 1)),
                () -> pieza("Fabricado", 1)));

        ResultadoFabricacion resultado = sistema.fabricar("kit", mochila);

        assertTrue(resultado.exito());
        assertEquals(4, mochila.getObjetos().size());
        assertSame(sinUsar, mochila.getObjetos().get(0));
        assertSame(tercero, mochila.getObjetos().get(1));
        assertSame(restante, mochila.getObjetos().get(2));
        assertSame(resultado.objeto(), mochila.getObjetos().get(3));
    }

    @Test
    void faltaUnIngredienteNoLlamaALaFabricaNiModificaMochila() {
        Mochila mochila = new Mochila(2, 5);
        Objeto unico = pieza("Pieza", 1);
        mochila.guardar(unico);
        AtomicInteger creados = new AtomicInteger();
        SistemaFabricacion sistema = new SistemaFabricacion();
        sistema.registrar(new Receta("doble", List.of(new Ingrediente("Pieza", 2)), () -> {
            creados.incrementAndGet();
            return pieza("Resultado", 1);
        }));

        assertFalse(sistema.fabricar("doble", mochila).exito());
        assertEquals(0, creados.get());
        assertEquals(1, mochila.getObjetos().size());
        assertSame(unico, mochila.getObjetos().get(0));
    }

    @Test
    void resultadoDemasiadoPesadoDejaElInventarioIntacto() {
        Mochila mochila = new Mochila(2, 2);
        Objeto materia = pieza("Pieza", 1);
        Objeto sinUsar = pieza("Otro", 1);
        mochila.setObjetos(List.of(materia, sinUsar));
        SistemaFabricacion sistema = new SistemaFabricacion();
        sistema.registrar(new Receta("grande", List.of(new Ingrediente("Pieza", 1)),
                () -> pieza("Resultado", 3)));

        assertFalse(sistema.fabricar("grande", mochila).exito());
        assertEquals(2, mochila.getObjetos().size());
        assertSame(materia, mochila.getObjetos().get(0));
        assertSame(sinUsar, mochila.getObjetos().get(1));
        assertEquals(2, mochila.getPesoActual());
    }

    @Test
    void ingredienteConCantidadDesbordadaNoProduceObjetosGratis() {
        Mochila mochila = new Mochila(2, 10);
        Objeto materia = pieza("Pieza", 1);
        mochila.guardar(materia);
        AtomicInteger creados = new AtomicInteger();
        SistemaFabricacion sistema = new SistemaFabricacion();
        sistema.registrar(new Receta("desbordada", List.of(
                new Ingrediente("Pieza", Integer.MAX_VALUE),
                new Ingrediente("pieza", 1)), () -> {
                    creados.incrementAndGet();
                    return pieza("Regalo", 1);
                }));

        ResultadoFabricacion resultado = sistema.fabricar("desbordada", mochila);

        assertFalse(resultado.exito());
        assertEquals(0, creados.get());
        assertEquals(1, mochila.getObjetos().size());
        assertSame(materia, mochila.getObjetos().get(0));
    }

    @Test
    void falloDeLaFabricaNoConsumeLosIngredientes() {
        Mochila mochila = new Mochila(2, 10);
        Objeto materia = pieza("Pieza", 1);
        mochila.guardar(materia);
        SistemaFabricacion sistema = new SistemaFabricacion();
        sistema.registrar(new Receta("rota", List.of(new Ingrediente("Pieza", 1)),
                () -> { throw new IllegalStateException("Fabrica no disponible"); }));

        assertThrows(IllegalStateException.class, () -> sistema.fabricar("rota", mochila));
        assertSame(materia, mochila.getObjetos().get(0));
    }

    @Test
    void fabricacionMasivaSeCompletaSinRetiradasCuadraticas() {
        List<Objeto> objetos = new ArrayList<>();
        for (int i = 0; i < 6_000; i++) {
            objetos.add(pieza(i % 3 == 0 ? "Otro" : "Pieza", 0.01));
        }
        Mochila mochila = new Mochila(10_000, 10_000);
        mochila.setObjetos(objetos);
        SistemaFabricacion sistema = new SistemaFabricacion();
        sistema.registrar(new Receta("masiva", List.of(
                new Ingrediente("Pieza", 4_000)), () -> pieza("Resultado", 1)));

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            assertTrue(sistema.fabricar("masiva", mochila).exito());
            assertEquals(2_001, mochila.getObjetos().size());
            assertEquals("Resultado", mochila.getObjetos().get(2_000).getNombre());
        });
    }

    private static Componente pieza(String nombre, double peso) {
        return new Componente(nombre, "Componente de prueba", peso);
    }
}
