package com.legendoftecla.engine;

import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.characters.Berserker;
import com.legendoftecla.model.characters.Commander;
import com.legendoftecla.model.characters.CommanderPrime;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.HeavyFloater;
import com.legendoftecla.model.characters.Medic;
import com.legendoftecla.model.characters.Pyro;
import com.legendoftecla.model.characters.PyroOverlord;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.characters.Sniper;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armeria;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoMunicion;

/** Genera equipamiento finito y coherente con el rol de cada enemigo. */
public final class ArsenalEnemigo {
    private ArsenalEnemigo() { }

    /** Equipa una carga inicial y guarda una reserva escalada por dificultad. */
    public static void asignar(Enemigo enemigo, Dificultad dificultad) {
        if (!enemigo.getArmasEquipadas().isEmpty()) {
            return;
        }
        Arma arma = crearArma(enemigo);
        try {
            enemigo.equipar(arma);
        } catch (AccionInvalidaException error) {
            throw new IllegalStateException("Perfil de enemigo incompatible", error);
        }
        if (!arma.usaMunicionInfinita()) {
            int reserva = Math.max(1, (int) Math.round(
                    arma.getCapacidadCargador()
                            * dificultad.getMultiplicadorEnemigos()));
            enemigo.getMochila().guardar(new Municion(
                    "reserva-" + enemigo.getNombre(), peso(arma.getTipoMunicion()),
                    arma.getTipoMunicion(), reserva));
        }
        if (enemigo instanceof Pyro || enemigo instanceof PyroOverlord) {
            enemigo.getMochila().guardar(new com.legendoftecla.model.items.Granada(
                    "incendiaria-" + enemigo.getNombre(), "Granada incendiaria", 0.6,
                    com.legendoftecla.model.items.TipoGranada.INCENDIARIA));
        }
    }

    private static Arma crearArma(Enemigo enemigo) {
        if (enemigo instanceof Berserker) {
            return Armeria.espada("mandoble-" + enemigo.getNombre());
        }
        if (enemigo instanceof Sniper) {
            return Armeria.rifle("rifle-sniper-" + enemigo.getNombre(), 5, 5);
        }
        if (enemigo instanceof Medic) {
            return Armeria.pistola("pistola-medica-" + enemigo.getNombre(), 6, 6);
        }
        if (enemigo instanceof Scout) {
            return Armeria.cuchillosArrojadizos(
                    "cuchillos-scout-" + enemigo.getNombre(), 4);
        }
        if (enemigo instanceof CommanderPrime || enemigo instanceof HeavyFloater) {
            return Armeria.pesada("pesada-" + enemigo.getNombre(), 6, 6);
        }
        if (enemigo instanceof Pyro || enemigo instanceof PyroOverlord) {
            return Armeria.energia("proyector-pyro-" + enemigo.getNombre(), 6, 6);
        }
        if (enemigo instanceof Commander) {
            return Armeria.rifle("rifle-mando-" + enemigo.getNombre(), 8, 8);
        }
        return Armeria.energia("arma-alien-" + enemigo.getNombre(), 6, 6);
    }

    private static double peso(TipoMunicion tipo) {
        return switch (tipo) {
            case PESADA, COHETE -> 1.5;
            case ENERGIA -> 0.8;
            default -> 0.5;
        };
    }
}
