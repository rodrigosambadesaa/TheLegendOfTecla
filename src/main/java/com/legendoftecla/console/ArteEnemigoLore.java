package com.legendoftecla.console;

import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.HeavyFloater;
import com.legendoftecla.model.characters.LightFloater;
import com.legendoftecla.model.characters.Sectoid;

/**
 * Renderiza fichas visuales ASCII de enemigos segun su perfil de lore.
 */
public final class ArteEnemigoLore {
    private ArteEnemigoLore() {
    }

    /**
     * Ejecuta la operacion publica {@code renderizarFicha}.
      * @param enemigo valor de {@code enemigo}
      * @return resultado de la operacion
     */
    public static String renderizarFicha(Enemigo enemigo) {
        return String.join("\n",
                "------------------------------",
                "Contacto hostil: " + enemigo.getNombre(),
                "Tipo: " + tipoLore(enemigo),
                arte(enemigo),
                "Lore: " + descripcionLore(enemigo),
                "------------------------------");
    }

    private static String tipoLore(Enemigo enemigo) {
        if (enemigo instanceof Sectoid) {
            return "Sectoid";
        }
        if (enemigo instanceof LightFloater) {
            return "Light Floater";
        }
        if (enemigo instanceof HeavyFloater) {
            return "Heavy Floater";
        }
        return "Hostil desconocido";
    }

    private static String descripcionLore(Enemigo enemigo) {
        if (enemigo instanceof Sectoid) {
            return "Explorador psi de cabeza amplia y tacticas de hostigamiento a media distancia.";
        }
        if (enemigo instanceof LightFloater) {
            return "Unidad flotante agil con movilidad vertical y ataques rapidos de acoso.";
        }
        if (enemigo instanceof HeavyFloater) {
            return "Variante blindada de floater con armadura reforzada y mayor resistencia.";
        }
        return "Entidad hostil sin clasificar en base de datos tactica.";
    }

    private static String arte(Enemigo enemigo) {
        if (enemigo instanceof Sectoid) {
            return String.join("\n",
                    "      .-''''-.",
                    "    .' 0  0  '.",
                    "   /    --     \\",
                    "  |   .____.    |",
                    "  |  /|____|\\   |",
                    "   \\_\\____/_/ _/",
                    "     /_/  \\_\\");
        }
        if (enemigo instanceof LightFloater) {
            return String.join("\n",
                    "      .-==-.",
                    "    .'  __  '.",
                    "   /   /  \\   \\",
                    "  |   | () |   |",
                    "  |   |____|   |",
                    "   \\  / || \\  /",
                    "    \\/  ||  \\/",
                    "        /  \\");
        }
        if (enemigo instanceof HeavyFloater) {
            return String.join("\n",
                    "       .-====-.",
                    "     .'  ____  '.",
                    "    /   / __ \\   \\",
                    "   |   | |  | |   |",
                    "   |   | |__| |   |",
                    "   |   |  __  |   |",
                    "    \\   \\__/   //",
                    "     '._/||||\\_.'",
                    "        /_||_\\");
        }
        return String.join("\n",
                "     [ ??? ]",
                "      /|||\\",
                "       / \\");
    }
}
