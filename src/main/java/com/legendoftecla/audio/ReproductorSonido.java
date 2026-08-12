package com.legendoftecla.audio;

import com.legendoftecla.model.world.Posicion;

/** Puerto de salida para reproducir sonidos sin acoplar listeners al dispositivo. */
@FunctionalInterface
public interface ReproductorSonido {
    /**
     * Reproduce un efecto desde un origen respecto de la posicion del oyente.
     *
     * @param sonido efecto solicitado
     * @param origen origen espacial opcional
     * @param oyente posicion del oyente opcional
     */
    void reproducir(EventoSonido sonido, Posicion origen, Posicion oyente);
}
