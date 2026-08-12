package com.legendoftecla.inventory;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.validation.Validaciones;

import java.util.List;

/** Recarga armas finitas de jugadores, aliados o enemigos sin duplicar recursos. */
public final class ServicioRecarga {
    /**
     * Recarga un arma equipada con todos los paquetes compatibles necesarios.
     *
     * @param personaje propietario del arma y la municion
     * @param nombreArma nombre opcional; {@code null} selecciona la primera arma finita
     * @return arma y cantidad transferida
     * @throws AccionInvalidaException si no existe arma, no necesita o no hay municion
     */
    public ResultadoRecarga recargar(Personaje personaje, String nombreArma)
            throws AccionInvalidaException {
        Personaje propietario = Validaciones.noNulo(personaje, "Personaje");
        Arma arma = propietario.getArmasEquipadas().stream()
                .filter(candidata -> nombreArma == null
                        || candidata.getNombre().equalsIgnoreCase(nombreArma))
                .findFirst()
                .orElseThrow(() -> new AccionInvalidaException(
                        "Arma equipada no encontrada."));
        if (arma.usaMunicionInfinita()) {
            throw new AccionInvalidaException("El arma no necesita recarga.");
        }
        if (arma.getMunicionActual() == arma.getCapacidadCargador()) {
            throw new AccionInvalidaException("El cargador ya esta lleno.");
        }
        int total = 0;
        List<Municion> paquetes = propietario.getMochila().getObjetos().stream()
                .filter(Municion.class::isInstance)
                .map(Municion.class::cast)
                .filter(municion -> municion.getTipo() == arma.getTipoMunicion())
                .toList();
        for (Municion municion : paquetes) {
            total += arma.recargar(municion);
            if (municion.getCantidad() == 0) {
                propietario.getMochila().quitar(municion);
            }
            if (arma.getMunicionActual() == arma.getCapacidadCargador()) {
                break;
            }
        }
        if (total == 0) {
            throw new AccionInvalidaException("No hay municion compatible.");
        }
        return new ResultadoRecarga(arma, total);
    }
}
