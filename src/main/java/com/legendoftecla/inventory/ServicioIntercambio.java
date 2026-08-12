package com.legendoftecla.inventory;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Objeto;

import java.util.Objects;

/** Transferencias finitas, cercanas y atomicas entre personajes. */
public final class ServicioIntercambio {
    private final int distanciaMaxima;
    public ServicioIntercambio(int distanciaMaxima) {
        if (distanciaMaxima < 0) throw new IllegalArgumentException("Distancia invalida");
        this.distanciaMaxima = distanciaMaxima;
    }

    public Objeto dar(Personaje origen, Personaje destino, String objeto,
            boolean enCombate) throws AccionInvalidaException {
        validar(origen, destino, enCombate);
        Objeto candidato = origen.getMochila().buscarPorNombre(objeto);
        if (candidato == null) {
            throw new AccionInvalidaException("No tienes ese objeto en la mochila.");
        }
        if (!destino.puedeCoger(candidato)) {
            throw new AccionInvalidaException(
                    "El destino no admite el objeto por capacidad, peso o clase.");
        }
        Objeto retirado = origen.tirar(objeto);
        try {
            destino.coger(retirado);
            return retirado;
        } catch (AccionInvalidaException error) {
            origen.getMochila().guardar(retirado);
            throw error;
        }
    }

    public Objeto pedir(Personaje solicitante, Personaje aliado, String objeto,
            boolean enCombate) throws AccionInvalidaException {
        return dar(aliado, solicitante, objeto, enCombate);
    }

    public void intercambiar(Personaje primero, String objetoPrimero,
            Personaje segundo, String objetoSegundo, boolean enCombate)
            throws AccionInvalidaException {
        validar(primero, segundo, enCombate);
        Objeto uno = primero.tirar(objetoPrimero);
        Objeto dos;
        try {
            dos = segundo.tirar(objetoSegundo);
        } catch (AccionInvalidaException error) {
            primero.getMochila().guardar(uno);
            throw error;
        }
        if (!primero.getMochila().puedeGuardar(dos)
                || !segundo.getMochila().puedeGuardar(uno)
                || !primero.puedeCoger(dos)
                || !segundo.puedeCoger(uno)) {
            primero.getMochila().guardar(uno);
            segundo.getMochila().guardar(dos);
            throw new AccionInvalidaException("El intercambio supera capacidad o peso.");
        }
        primero.coger(dos);
        segundo.coger(uno);
    }

    private void validar(Personaje primero, Personaje segundo, boolean enCombate)
            throws AccionInvalidaException {
        Objects.requireNonNull(primero, "Primer personaje");
        Objects.requireNonNull(segundo, "Segundo personaje");
        if (primero == segundo) {
            throw new AccionInvalidaException("No se puede intercambiar con uno mismo.");
        }
        if (primero.getSalud() <= 0 || segundo.getSalud() <= 0) {
            throw new AccionInvalidaException("No se puede comerciar con personajes muertos.");
        }
        if (enCombate) throw new AccionInvalidaException("No se puede intercambiar en combate.");
        if (primero.getPosicion().distanciaManhattan(segundo.getPosicion()) > distanciaMaxima) {
            throw new AccionInvalidaException("El personaje esta demasiado lejos.");
        }
    }
}
