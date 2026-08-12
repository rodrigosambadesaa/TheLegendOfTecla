package com.legendoftecla.inventory;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Municion;

/** Cooperacion automatica opcional para compartir recursos tacticamente utiles. */
public final class CooperacionInventario {
    private final ServicioIntercambio intercambio;

    /** @param distanciaMaxima distancia permitida entre participantes */
    public CooperacionInventario(int distanciaMaxima) {
        intercambio = new ServicioIntercambio(distanciaMaxima);
    }

    /**
     * Entrega un paquete compatible si el receptor tiene un cargador incompleto.
     *
     * @return si se transfirio un paquete finito
     */
    public boolean compartirMunicion(Personaje donante, Personaje receptor) {
        Arma arma = receptor.getArmasEquipadas().stream()
                .filter(candidata -> !candidata.usaMunicionInfinita())
                .filter(candidata -> candidata.getMunicionActual()
                        < candidata.getCapacidadCargador())
                .findFirst()
                .orElse(null);
        if (arma == null) {
            return false;
        }
        Municion paquete = donante.getMochila().getObjetos().stream()
                .filter(Municion.class::isInstance)
                .map(Municion.class::cast)
                .filter(municion -> municion.getCantidad() > 0
                        && municion.getTipo() == arma.getTipoMunicion())
                .findFirst()
                .orElse(null);
        if (paquete == null) {
            return false;
        }
        try {
            intercambio.dar(donante, receptor, paquete.getNombre(), false);
            return true;
        } catch (AccionInvalidaException error) {
            return false;
        }
    }

    /**
     * Entrega un arma guardada solo si mejora el mejor dano equipado del receptor.
     *
     * @return si se transfirio el arma, sin equiparla automaticamente
     */
    public boolean transferirMejorArma(Personaje donante, Personaje receptor) {
        int mejorActual = receptor.getArmasEquipadas().stream()
                .mapToInt(Arma::getDanio)
                .max()
                .orElse(0);
        Arma mejora = donante.getMochila().getObjetos().stream()
                .filter(Arma.class::isInstance)
                .map(Arma.class::cast)
                .filter(arma -> arma.getDanio() > mejorActual)
                .max(java.util.Comparator.comparingInt(Arma::getDanio))
                .orElse(null);
        if (mejora == null) {
            return false;
        }
        try {
            intercambio.dar(donante, receptor, mejora.getNombre(), false);
            return true;
        } catch (AccionInvalidaException error) {
            return false;
        }
    }
}
