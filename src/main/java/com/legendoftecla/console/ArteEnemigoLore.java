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
                "Arma propia: " + enemigo.getArmasEquipadas().stream()
                        .map(com.legendoftecla.model.items.Arma::getNombre)
                        .findFirst().orElse("ataque biologico"),
                "Armadura propia: " + (enemigo.getArmaduraEquipada() == null
                        ? "proteccion natural" : enemigo.getArmaduraEquipada().getNombre()),
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
        return switch (enemigo.getClass().getSimpleName()) {
            case "Berserker" -> "Berserker";
            case "Medic" -> "Sanitario xeno";
            case "Sniper" -> "Francotirador xeno";
            case "Pyro" -> "Incendiario xeno";
            case "Scout" -> "Explorador xeno";
            case "Commander" -> "Comandante xeno";
            case "CommanderPrime" -> "Commander Prime";
            case "PyroOverlord" -> "Pyro Overlord";
            default -> "Hostil desconocido";
        };
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
        return switch (enemigo.getClass().getSimpleName()) {
            case "Berserker" ->
                    "Unidad de asalto con dermoplacas y garras oseas para romper la linea.";
            case "Medic" ->
                    "Simbionte sanitario que repara a su escuadra durante combates coordinados.";
            case "Sniper" ->
                    "Tirador de aguja de vacio que busca cobertura y objetivos vulnerables.";
            case "Pyro" ->
                    "Especialista termico que separa al escuadron rival mediante fuego.";
            case "Scout" ->
                    "Infiltrador sensorial que transmite contactos a la red enemiga.";
            case "Commander" ->
                    "Nodo de mando que sincroniza, protege y concentra el fuego de su grupo.";
            case "CommanderPrime" ->
                    "Centro de una red tactica capaz de fortificar e invocar refuerzos.";
            case "PyroOverlord" ->
                    "Jefe termico acorazado que convierte el terreno en un arma.";
            default -> "Entidad hostil sin clasificar en base de datos tactica.";
        };
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
