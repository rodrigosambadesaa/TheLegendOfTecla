package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Commander;
import com.legendoftecla.model.characters.CommanderPrime;
import com.legendoftecla.model.characters.Francotirador;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.characters.Sniper;

/** Calcula la punteria del personaje; el arma no posee precision propia. */
public final class PrecisionTirador {
    private PrecisionTirador() { }

    /** @return probabilidad base antes de estados y cobertura */
    public static double calcular(Personaje tirador) {
        double base = 0.80;
        if (tirador instanceof Francotirador) base = 0.94;
        else if (tirador instanceof Sniper) base = 0.92;
        else if (tirador instanceof CommanderPrime) base = 0.90;
        else if (tirador instanceof Commander) base = 0.88;
        else if (tirador instanceof Marine) base = 0.86;
        else if (tirador instanceof Aliado aliado) {
            base = Math.min(0.92, 0.78 + aliado.getNivel() * 0.002);
        }
        if (tirador instanceof Jugador jugador) {
            base = Math.min(0.97, base + (jugador.getProgresion().getNivel() - 1) * 0.001);
        }
        return base;
    }
}
