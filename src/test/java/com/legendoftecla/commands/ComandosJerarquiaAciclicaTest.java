package com.legendoftecla.commands;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandosJerarquiaAciclicaTest {
    @Test
    void unCompuestoNoPuedeAgregarseASiMismo() {
        ComandoCompuesto compuesto = new ComandoCompuesto();

        assertThrows(IllegalArgumentException.class, () -> compuesto.agregar(compuesto));

        assertTrue(compuesto.getComandos().isEmpty());
    }

    @Test
    void rechazaCirculoEntreDosComposicionesSinCorromperLaSegunda() {
        ComandoCompuesto primero = new ComandoCompuesto();
        ComandoCompuesto segundo = new ComandoCompuesto();
        primero.agregar(segundo);
        Comando vacio = () -> { };
        segundo.agregar(vacio);

        assertThrows(IllegalArgumentException.class,
                () -> segundo.setComandos(List.of(vacio, primero)));

        assertEquals(List.of(vacio), segundo.getComandos());
        assertEquals(List.of(segundo), primero.getComandos());
    }

    @Test
    void unRepetidoNoPuedeReferirseASiMismoNiAlContenedorPadre() {
        Comando inicial = () -> { };
        ComandoRepetido repetido = new ComandoRepetido(inicial, 2);
        ComandoCompuesto contenedor = new ComandoCompuesto();
        contenedor.agregar(repetido);

        assertThrows(IllegalArgumentException.class,
                () -> repetido.setComando(repetido));
        assertThrows(IllegalArgumentException.class,
                () -> repetido.setComando(contenedor));

        assertSame(inicial, repetido.getComando());
        assertEquals(2, repetido.getRepeticiones());
        assertEquals(List.of(repetido), contenedor.getComandos());
    }

    @Test
    void detectaCicloProfundoSinRecursion() {
        ComandoCompuesto compuesto = new ComandoCompuesto();
        Comando anidado = compuesto;
        for (int i = 0; i < 300; i++) {
            anidado = new ComandoRepetido(anidado, 1);
        }
        Comando candidato = anidado;

        assertThrows(IllegalArgumentException.class,
                () -> compuesto.setComandos(List.of(candidato)));

        assertTrue(compuesto.getComandos().isEmpty());
    }

    @Test
    void compartirSubcomandosValidosNoSeConfundeConUnCiclo() throws Exception {
        AtomicInteger contador = new AtomicInteger();
        Comando hoja = contador::incrementAndGet;
        ComandoRepetido repetido = new ComandoRepetido(hoja, 2);
        ComandoCompuesto compuesto = new ComandoCompuesto();
        compuesto.setComandos(List.of(repetido, repetido));

        compuesto.ejecutar();

        assertEquals(4, contador.get());
    }
}
