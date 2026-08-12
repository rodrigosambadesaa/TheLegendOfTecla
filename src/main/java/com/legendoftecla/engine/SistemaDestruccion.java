package com.legendoftecla.engine;

import com.legendoftecla.model.elements.ElementoBase;
import com.legendoftecla.model.elements.ElementoMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.List;

/** Aplica daño estructural y deja que mapa, visión y rutas consulten el nuevo estado. */
public final class SistemaDestruccion {
    private SistemaDestruccion() { }

    /** Daña todos los elementos destructibles de una celda en orden determinista. */
    public static ResultadoDestruccion danar(Juego juego, Posicion posicion, int dano) {
        java.util.Objects.requireNonNull(juego, "Juego");
        if (!juego.getMapa().estaDentro(posicion) || dano < 0) {
            throw new IllegalArgumentException("Destruccion fuera de mapa o dano invalido");
        }
        List<String> danados = new ArrayList<>();
        List<String> destruidos = new ArrayList<>();
        for (ElementoMapa elemento : juego.getMapa().getCelda(posicion).getElementos()) {
            boolean destructible = elemento instanceof ElementoBase base
                    && base.isDestructible() && !elemento.estaDestruido();
            if (!destructible) continue;
            elemento.recibirDanio(dano);
            danados.add(elemento.getId());
            if (elemento.estaDestruido()) destruidos.add(elemento.getId());
        }
        return new ResultadoDestruccion(danados, destruidos);
    }

    /** Resultado estable útil para consola, GUI, replay y pruebas. */
    public record ResultadoDestruccion(List<String> danados, List<String> destruidos) {
        public ResultadoDestruccion {
            danados = List.copyOf(danados);
            destruidos = List.copyOf(destruidos);
        }
        public boolean modificoMapa() { return !danados.isEmpty(); }
    }
}
