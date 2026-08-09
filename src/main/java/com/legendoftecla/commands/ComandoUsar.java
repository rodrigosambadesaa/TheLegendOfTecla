package com.legendoftecla.commands;

import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad ComandoUsar del juego.
 */
public class ComandoUsar implements Comando {
    private CommandContext context;
    private String nombreObjeto;

    /**
     * Ejecuta ComandoUsar.
      * @param context valor de {@code context}
      * @param nombreObjeto valor de {@code nombreObjeto}
     */
    public ComandoUsar(CommandContext context, String nombreObjeto) {
        setContext(context);
        setNombreObjeto(nombreObjeto);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) { this.context = Validaciones.noNulo(context, "Contexto"); }
    /** @return nombre del objeto */
    public String getNombreObjeto() { return nombreObjeto; }
    /** @param nombreObjeto nombre obligatorio y acotado */
    public void setNombreObjeto(String nombreObjeto) {
        this.nombreObjeto = Validaciones.textoObligatorio(
                nombreObjeto, "Nombre del objeto", Limites.TEXTO_CORTO);
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        Jugador jugador = context.getJuego().getJugador();
        Objeto obj = jugador.getMochila().quitarPorNombre(nombreObjeto);
        boolean estabaEquipado = false;
        Binocular equipado = jugador.getBinocularEquipado();
        if (obj == null && equipado != null && equipado.getNombre().equalsIgnoreCase(nombreObjeto)) {
            obj = equipado;
            jugador.setBinocularEquipado(null);
            estabaEquipado = true;
        }
        if (obj == null) {
            throw new ComandoException("No tienes ese objeto en la mochila.");
        }
        try {
            obj.usar(jugador);
            if (!obj.isConsumible()) {
                devolverObjeto(jugador, obj, estabaEquipado);
            }
            context.getJuego().getConsola().imprimir("Usas " + obj.getNombre() + ".");
        } catch (JuegoException e) {
            devolverObjeto(jugador, obj, estabaEquipado);
            throw new ComandoException(e.getMessage());
        }
    }

    private void devolverObjeto(Jugador jugador, Objeto objeto, boolean estabaEquipado) {
        if (estabaEquipado && objeto instanceof Binocular binocular) {
            jugador.setBinocularEquipado(binocular);
        } else {
            jugador.getMochila().guardar(objeto);
        }
    }
}

