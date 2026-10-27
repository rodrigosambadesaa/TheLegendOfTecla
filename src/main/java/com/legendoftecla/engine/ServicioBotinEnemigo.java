package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Celda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Deposita equipo enemigo y reservas sin repetir instancias ni copias cuadráticas. */
public final class ServicioBotinEnemigo {
    private ServicioBotinEnemigo() { }

    /**
     * Transfiere el equipo una sola vez y conserva el orden del suelo.
     *
     * @return cantidad de instancias realmente añadidas a la celda
     */
    public static int soltar(Celda celda, Enemigo enemigo) {
        java.util.Objects.requireNonNull(celda, "Celda");
        java.util.Objects.requireNonNull(enemigo, "Enemigo");

        List<Objeto> nuevos = new ArrayList<>(celda.getObjetos());
        Set<Objeto> presentes = Collections.newSetFromMap(new IdentityHashMap<>());
        presentes.addAll(nuevos);
        int iniciales = nuevos.size();

        for (var arma : enemigo.getArmasEquipadas()) {
            if (presentes.add(arma)) nuevos.add(arma);
        }
        if (enemigo.getArmaduraEquipada() != null
                && presentes.add(enemigo.getArmaduraEquipada())) {
            nuevos.add(enemigo.getArmaduraEquipada());
        }
        for (Objeto objeto : enemigo.getMochila().getObjetos()) {
            if (presentes.add(objeto)) nuevos.add(objeto);
        }

        // Una sola validacion y sustitucion en cada inventario, sin iterar
        // repetidamente sobre toda la lista por cada item transferido.
        celda.setObjetos(nuevos);
        enemigo.setArmasEquipadas(List.of());
        enemigo.setArmaduraEquipada(null);
        enemigo.getMochila().setObjetos(List.of());
        return nuevos.size() - iniciales;
    }
}
