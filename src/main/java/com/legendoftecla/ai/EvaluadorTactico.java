package com.legendoftecla.ai;

import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Commander;
import com.legendoftecla.model.characters.Medic;

/** Combina la maquina de alerta con la estrategia especifica de cada rol. */
public final class EvaluadorTactico {
    /** @return decision explicable sin modificar mapa ni personajes */
    public AccionIA decidir(ContextoIA contexto) {
        Enemigo enemigo = contexto.enemigo();
        AccionIA estado = enemigo.getControladorIA().decidir(contexto);
        if (enemigo instanceof EnemigoTactico tactico
                && ((enemigo instanceof Medic && contexto.aliadoHerido())
                || enemigo instanceof Commander
                || (contexto.veJugador() && !contexto.armaVacia()))) {
            return tactico.decidirTactica(contexto);
        }
        return estado;
    }
}
