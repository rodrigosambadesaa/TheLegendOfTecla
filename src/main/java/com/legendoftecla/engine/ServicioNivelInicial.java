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
        int anterior = validado.getProgresion().getNivel();
        int diferencia = elegido - anterior;
        // Al cargar un perfil local el jugador ya puede tener bonificaciones
        // del nivel seleccionado en la pantalla inicial: no sumarlas de nuevo.
        int salud = (int) Math.max(1, Math.min(Limites.ESTADISTICA,
                (long) validado.getSaludMaxima() + (long) diferencia * 3));
        int energia = (int) Math.max(1, Math.min(Limites.ESTADISTICA,
                (long) validado.getEnergiaMaxima() + (long) diferencia * 4));
        int visionAdicional = (elegido - 1) / 10 - (anterior - 1) / 10;
        int vision = (int) Math.max(1, Math.min(Limites.ESTADISTICA,
                (long) validado.getVisionBase() + visionAdicional));
        validado.setSaludMaxima(salud);
        validado.setSalud(salud);
        validado.setEnergiaMaxima(energia);
        validado.setEnergia(energia);
        validado.setVisionBase(vision);
        validado.getProgresion().restaurar(elegido, 0, Set.of());
    }
}
