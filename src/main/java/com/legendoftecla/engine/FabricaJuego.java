package com.legendoftecla.engine;

import com.legendoftecla.console.Consola;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.loader.CargadorJuego;
import com.legendoftecla.loader.CargadorJuegoDeFicheros;
import com.legendoftecla.loader.CargadorJuegoGrandeConAliados;
import com.legendoftecla.loader.CargadorJuegoPorDefecto;
import com.legendoftecla.loader.CargadorJuegoProcedural;
import com.legendoftecla.loader.CargadorJuegoBase;
import com.legendoftecla.model.world.Juego;

/** Construye el mismo juego para la interfaz de consola y para la grafica. */
public final class FabricaJuego {
    private FabricaJuego() {
    }

    /**
     * Ejecuta la operacion publica {@code crear}.
      * @param configuracion valor de {@code configuracion}
      * @param consola valor de {@code consola}
      * @return resultado de la operacion
      * @throws com.legendoftecla.exceptions.JuegoException si la operacion no puede completarse
     */
    public static Juego crear(Consola consola, ConfiguracionPartida configuracion) throws JuegoException {
        CargadorJuego cargador = switch (configuracion.modo()) {
            case "procedural" -> new CargadorJuegoProcedural(
                    consola, configuracion.nombreJugador(), configuracion.clase(),
                    configuracion.dificultad(), configuracion.dimensiones(),
                    configuracion.cantidadAliados(), configuracion.seed());
            case "ficheros" -> new CargadorJuegoDeFicheros(
                    consola,
                    configuracion.nombreJugador(),
                    configuracion.clase(),
                    configuracion.directorioDatos(),
                    configuracion.dificultad(),
                    configuracion.dimensiones(),
                    configuracion.cantidadAliados());
            case "grande" -> new CargadorJuegoGrandeConAliados(
                    consola,
                    configuracion.nombreJugador(),
                    configuracion.clase(),
                    configuracion.dificultad(),
                    configuracion.dimensiones(),
                    configuracion.cantidadAliados(),
                    configuracion.varianteMapa());
            default -> new CargadorJuegoPorDefecto(
                    consola,
                    configuracion.nombreJugador(),
                    configuracion.clase(),
                    configuracion.dificultad(),
                    configuracion.dimensiones(),
                    configuracion.cantidadAliados());
        };
        if (cargador instanceof CargadorJuegoBase base) {
            base.setNivelAliados(configuracion.nivelAliados());
        }
        Juego juego = cargador.cargarJuego();
        juego.setCondicionVictoria(configuracion.condicionVictoria());
        EscaladorEnergiaMapa.aplicar(juego);
        return juego;
    }
}
