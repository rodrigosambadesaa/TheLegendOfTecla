package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.validation.Validaciones;

/**
 * Representa la entidad ComandoAyuda del juego.
 */
public class ComandoAyuda implements Comando {
    private CommandContext context;

    /**
     * Ejecuta ComandoAyuda.
      * @param context valor de {@code context}
     */
    public ComandoAyuda(CommandContext context) {
        setContext(context);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) {
        this.context = Validaciones.noNulo(context, "Contexto");
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
                "2) mirar [objeto|<distancia><direccion> [enemigo]]",
                "   Ejemplos: mirar | mirar rifle | mirar 3e | mirar 3e Sectoid_A",
                "3) coger <objeto>",
                "   Ejemplo: coger rifle",
                "4) tirar <objeto>",
                "   Ejemplo: tirar botiquin",
                "5) inventario (alias: mochila)",
                "   Ejemplo: inventario",
                "6) usar <objeto>",
                "   Ejemplo: usar botiquin",
                "7) equipar <arma|armadura|binocular> [equipado]",
                "   Ejemplo compuesto: equipar lanzacohetes ametralladora",
                "8) desequipar <arma|armadura>",
                "   Ejemplo: desequipar rifle | desequipar chaleco",
                "9) atacar [<distancia><direccion>] [objetivo|todos] [repeticiones]",
                "   Ejemplos: atacar | atacar todos | atacar 2e Sectoid_A 2",
                "10) lanzar <distancia><direccion> <explosivo>",
                "    Ejemplo: lanzar 3e c4_1 (solo zapador, alcance maximo 5)",
                "11) recorrido",
                "    Ejemplo: recorrido",
                "12) pedir ayuda (alias: socorro o asistir)",
                "    Los aliados seguros buscan suministros, acuden y combaten",
                "13) reagrupar <defensiva|ofensiva> (alias: formacion)",
                "    Los aliados acompañan al jugador; los enemigos reaccionan si la detectan",
                "    Si escasean suministros, el aliado en mejor estado explora cerca del grupo",
                "14) descansar (alias: reposar)",
                "    Recupera un 10 % de salud y energia sin moverse; los enemigos se acercan",
                "15) ayuda (alias: comandos)",
                "    Ejemplo: ayuda",
                "16) cargar <directorio>",
                "    Carga escenario.json o mapa.txt, objetos.txt y enemigos.txt",
                "17) recargar [arma] | estado arma",
                "18) dar <objeto> <aliado> | pedir <objeto> <aliado>",
                "    intercambiar <objeto1> <objeto2> <aliado>",
                "19) abrir puerta | cerrar puerta | hackear terminal | activar interruptor",
                "20) inspeccionar trampa | desactivar trampa",
                "21) recetas | fabricar <objeto>",
                "22) guardar partida [archivo] | cargar partida [archivo]",
                "23) estadisticas (alias: logros)",
                "24) salir",
                "    Ejemplo: salir");
        context.getJuego().getConsola().imprimirInfo(ayuda);
    }
}
