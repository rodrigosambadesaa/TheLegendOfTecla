package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;

/**
 * Representa la entidad ComandoAyuda del juego.
 */
public class ComandoAyuda implements Comando {
    private final CommandContext context;

    /**
     * Ejecuta ComandoAyuda.
      * @param context valor de {@code context}
     */
    public ComandoAyuda(CommandContext context) {
        this.context = context;
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        String ayuda = String.join("\n",
                "COMANDOS DISPONIBLES",
                "",
                "1) mover <norte|sur|este|oeste> [repeticiones]",
                "   Ejemplos: mover norte | mover este 3",
                "2) mirar [norte|sur|este|oeste] [pasos]",
                "   Ejemplos: mirar | mirar este 3",
                "3) coger <objeto>",
                "   Ejemplo: coger rifle",
                "4) tirar <objeto>",
                "   Ejemplo: tirar botiquin",
                "5) inventario (alias: mochila)",
                "   Ejemplo: inventario",
                "6) usar <objeto>",
                "   Ejemplo: usar botiquin",
                "7) equipar <arma>",
                "   Ejemplo: equipar rifle",
                "8) desequipar <arma>",
                "   Ejemplo: desequipar rifle",
                "9) atacar [<distancia><direccion>] [objetivo|todos] [repeticiones]",
                "   Ejemplos: atacar | atacar todos | atacar 2e Sectoid_A 2",
                "10) lanzar <distancia><direccion> <explosivo>",
                "    Ejemplo: lanzar 3e c4_1 (solo zapador, alcance maximo 5)",
                "11) recorrido",
                "    Ejemplo: recorrido",
                "12) pedir ayuda (alias: socorro o asistir)",
                "    Los aliados seguros acuden, combaten y entregan suministros",
                "13) ayuda (alias: comandos)",
                "    Ejemplo: ayuda",
                "14) salir",
                "    Ejemplo: salir");
        context.getJuego().getConsola().imprimirInfo(ayuda);
    }
}
