package com.legendoftecla.commands;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.Set;

/** Evita referencias circulares entre comandos compuestos y repetidos. */
final class ValidadorJerarquiaComandos {
    private ValidadorJerarquiaComandos() { }

    /**
     * Comprueba que la nueva dependencia no llegue de vuelta al contenedor.
     * La busqueda es iterativa y utiliza identidad, no igualdad por contenido.
     */
    static void exigirAciclico(Comando contenedor, Comando dependencia) {
        Deque<Comando> pendientes = new ArrayDeque<>();
        Set<Comando> visitados = Collections.newSetFromMap(new IdentityHashMap<>());
        pendientes.add(dependencia);
        while (!pendientes.isEmpty()) {
            Comando actual = pendientes.removeLast();
            if (actual == contenedor) {
                throw new IllegalArgumentException(
                        "Una composicion no puede contener referencias circulares.");
            }
            if (!visitados.add(actual)) continue;
            if (actual instanceof ComandoCompuesto compuesto) {
                compuesto.getComandos().forEach(pendientes::addLast);
            } else if (actual instanceof ComandoRepetido repetido) {
                pendientes.addLast(repetido.getComando());
            }
        }
    }
}
