package com.legendoftecla.engine;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Juego;

/** Empareja el nivel de la escuadra y la amenaza enemiga al crear una partida. */
public final class ServicioEquilibrioInicial {
    private ServicioEquilibrioInicial() { }

    public static void aplicar(Juego juego, Dificultad dificultad) {
        int nivelJugador = juego.getJugador().getProgresion().getNivel();
        int minimo = Math.max(1, nivelJugador - 2);
        int maximo = Math.min(100, nivelJugador + 2);
        for (Aliado aliado : juego.getAliadosRegistrados()) {
            int actual = aliado.getNivel();
            int ajustado = Math.max(minimo, Math.min(maximo, actual));
            ServicioExperiencia.ajustarNivelInicial(aliado, ajustado);
        }
        // Las criaturas enemigas no tienen niveles numericos: su salud y dano
        // representan la amenaza. Compensar los grupos de alto nivel, sin
        // alterar el nivel uno ni eliminar jefes/objetivos de escenario.
        double salud = Math.min(3.0, 1.0 + (nivelJugador - 1) * 0.02);
        double dano = Math.min(2.5, 1.0 + (nivelJugador - 1) * 0.015);
        if (salud > 1) {
            for (Enemigo enemigo : juego.getEnemigos()) enemigo.escalarSalud(salud);
        }
        double multiplicador = dificultad.getMultiplicadorDanioEnemigo() * dano;
        // Mantener el valor global de referencia para consumidores historicos,
        // pero fijar el dano de los enemigos de ESTA partida al inicializarla.
        Enemigo.setMultiplicadorDanioGlobal(multiplicador);
        for (Enemigo enemigo : juego.getEnemigos()) {
            enemigo.setMultiplicadorDanioPartida(multiplicador);
        }
    }
}
