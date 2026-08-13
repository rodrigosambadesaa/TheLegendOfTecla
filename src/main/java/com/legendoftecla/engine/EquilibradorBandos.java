package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.List;

/** Aplica la invariante enemigos &lt;= aliados + jugador al terminar el despliegue. */
public final class EquilibradorBandos {
    private EquilibradorBandos() { }

    /**
     * Retira deterministamente los enemigos sobrantes del juego y de sus celdas.
     *
     * @return numero de enemigos retirados
     */
    public static int aplicar(Juego juego) {
        Juego validado = Validaciones.noNulo(juego, "Juego");
        int limite = Math.min(com.legendoftecla.validation.Limites.COMBATIENTES_POR_BANDO,
                validado.getAliadosRegistrados().size() + 1);
        List<Enemigo> actuales = new ArrayList<>(validado.getEnemigos());
        int retirados = Math.max(0, actuales.size() - limite);
        if (retirados > 0) {
            actuales.subList(limite, actuales.size()).forEach(enemigo ->
                    validado.getMapa().getCelda(enemigo.getPosicion()).quitarEnemigo(enemigo));
            validado.setEnemigos(new ArrayList<>(actuales.subList(0, limite)));
        }
        validado.sellarEquilibrioBandos();
        return retirados;
    }
}
