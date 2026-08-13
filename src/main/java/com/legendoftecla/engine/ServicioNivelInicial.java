package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.Set;

/** Aplica una progresion inicial reproducible sin depender del modo de partida. */
public final class ServicioNivelInicial {
    private ServicioNivelInicial() { }

    /**
     * Fija el nivel y escala modestamente los recursos base. El nivel uno conserva
     * exactamente las estadisticas historicas de cada clase.
     *
     * @param jugador personaje recien creado
     * @param nivel nivel inicial entre uno y cien
     */
    public static void aplicar(Jugador jugador, int nivel) {
        Jugador validado = Validaciones.noNulo(jugador, "Jugador");
        int elegido = Validaciones.enteroEntre(nivel, 1,
                Limites.NIVEL_ALIADO_MAXIMO, "Nivel del jugador");
        if (elegido == validado.getProgresion().getNivel()) {
            return;
        }
        validado.getProgresion().restaurar(elegido, 0, Set.of());
        int saltos = elegido - 1;
        int salud = Math.min(Limites.ESTADISTICA, validado.getSaludMaxima() + saltos * 3);
        int energia = Math.min(Limites.ESTADISTICA, validado.getEnergiaMaxima() + saltos * 4);
        validado.setSaludMaxima(salud);
        validado.setSalud(salud);
        validado.setEnergiaMaxima(energia);
        validado.setEnergia(energia);
        validado.setVisionBase(Math.min(Limites.ESTADISTICA,
                validado.getVisionBase() + saltos / 10));
    }
}
