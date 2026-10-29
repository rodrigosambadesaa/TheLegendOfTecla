package com.legendoftecla.engine;

import com.legendoftecla.missions.EliminarEnemigo;
import com.legendoftecla.missions.ObjetivoMision;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Jefe;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/** Aplica la invariante enemigos &lt;= aliados + jugador al terminar el despliegue. */
public final class EquilibradorBandos {
    private EquilibradorBandos() { }

    /**
     * Retira deterministamente los enemigos sobrantes sin borrar el objetivo
     * principal de la mision cuando se puede conservar dentro del limite.
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
            Set<Enemigo> elegidos = Collections.newSetFromMap(new IdentityHashMap<>());
            if (validado.getMision() != null) {
                incluir(actuales, elegidos, limite,
                        objetivo(validado.getMision().getPrincipal()));
                for (ObjetivoMision secundario : validado.getMision().getSecundarios()) {
                    incluir(actuales, elegidos, limite, objetivo(secundario));
                }
            }
            incluir(actuales, elegidos, limite, enemigo -> enemigo instanceof Jefe);
            incluir(actuales, elegidos, limite, enemigo -> true);

            // Se conserva el orden de turnos original de los enemigos elegidos.
            List<Enemigo> conservados = actuales.stream().filter(elegidos::contains).toList();
            actuales.stream().filter(enemigo -> !elegidos.contains(enemigo))
                    .forEach(enemigo -> validado.getMapa().getCelda(
                            enemigo.getPosicion()).quitarEnemigo(enemigo));
            validado.setEnemigos(conservados);
        }
        validado.sellarEquilibrioBandos();
        return retirados;
    }

    private static Predicate<Enemigo> objetivo(ObjetivoMision mision) {
        if (mision instanceof EliminarEnemigo eliminar) {
            return enemigo -> enemigo.getNombre().equalsIgnoreCase(eliminar.getNombre());
        }
        return enemigo -> false;
    }

    private static void incluir(List<Enemigo> enemigos, Set<Enemigo> elegidos,
            int limite, Predicate<Enemigo> criterio) {
        for (Enemigo enemigo : enemigos) {
            if (elegidos.size() >= limite) return;
            if (criterio.test(enemigo)) elegidos.add(enemigo);
        }
    }
}
