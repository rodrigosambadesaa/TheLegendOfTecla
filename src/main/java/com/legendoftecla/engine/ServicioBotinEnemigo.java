package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.world.Celda;

/** Deposita equipo xeno inutilizable y reservas exactamente una vez al morir. */
public final class ServicioBotinEnemigo {
    private ServicioBotinEnemigo() { }

    /** @return cantidad de objetos depositados en la celda */
    public static int soltar(Celda celda, Enemigo enemigo) {
        int total = 0;
        for (var arma : enemigo.getArmasEquipadas()) {
            celda.agregarObjeto(arma);
            total++;
        }
        enemigo.setArmasEquipadas(java.util.List.of());
        if (enemigo.getArmaduraEquipada() != null) {
            celda.agregarObjeto(enemigo.getArmaduraEquipada());
            enemigo.setArmaduraEquipada(null);
            total++;
        }
        for (var objeto : java.util.List.copyOf(enemigo.getMochila().getObjetos())) {
            celda.agregarObjeto(objeto);
            enemigo.getMochila().quitar(objeto);
            total++;
        }
        return total;
    }
}
