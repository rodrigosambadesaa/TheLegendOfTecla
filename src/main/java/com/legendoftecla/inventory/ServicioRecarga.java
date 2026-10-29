package com.legendoftecla.inventory;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.validation.Validaciones;

import com.legendoftecla.model.items.Objeto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Recarga armas finitas de jugadores, aliados o enemigos sin duplicar recursos. */
public final class ServicioRecarga {
    /**
     * Recarga un arma equipada con todos los paquetes compatibles necesarios.
     *
     * @param personaje propietario del arma y la municion
     * @param nombreArma nombre opcional; {@code null} prioriza la primera arma
     *                   finita incompleta con municion compatible disponible
     * @return arma y cantidad transferida
     * @throws AccionInvalidaException si no existe arma, no necesita o no hay municion
     */
    public ResultadoRecarga recargar(Personaje personaje, String nombreArma)
            throws AccionInvalidaException {
        Personaje propietario = Validaciones.noNulo(personaje, "Personaje");
        List<Municion> disponibles = propietario.getMochila().getObjetos().stream()
                .filter(Municion.class::isInstance)
                .map(Municion.class::cast)
                .toList();
        Arma primera = propietario.getArmasEquipadas().stream()
                .filter(candidata -> nombreArma == null
                        ? !candidata.usaMunicionInfinita()
                        : candidata.getNombre().equalsIgnoreCase(nombreArma))
                .findFirst()
                .orElseThrow(() -> new AccionInvalidaException(
                        "Arma equipada no encontrada."));

        // Sin nombre se prioriza un arma que realmente se pueda recargar.
        // Si no existe, conservar el diagnostico habitual (llena o sin municion).
        Arma arma = nombreArma != null ? primera : propietario.getArmasEquipadas().stream()
                .filter(candidata -> !candidata.usaMunicionInfinita()
                        && candidata.getMunicionActual() < candidata.getCapacidadCargador())
                .filter(candidata -> disponibles.stream().anyMatch(municion ->
                        municion.getTipo() == candidata.getTipoMunicion()
                                && municion.getCantidad() > 0))
                .findFirst().orElse(primera);
        if (arma.usaMunicionInfinita()) {
            throw new AccionInvalidaException("El arma no necesita recarga.");
        }
        if (arma.getMunicionActual() == arma.getCapacidadCargador()) {
            throw new AccionInvalidaException("El cargador ya esta lleno.");
        }
        List<Municion> paquetes = disponibles.stream()
                .filter(municion -> municion.getTipo() == arma.getTipoMunicion())
                .toList();
        // No modificar el inventario si ninguna caja ofrece cartuchos utiles.
        if (paquetes.stream().noneMatch(municion -> municion.getCantidad() > 0)) {
            throw new AccionInvalidaException("No hay municion compatible.");
        }

        int total = 0;
        Set<Objeto> agotados = Collections.newSetFromMap(new IdentityHashMap<>());
        for (Municion municion : paquetes) {
            total += arma.recargar(municion);
            if (municion.getCantidad() == 0) {
                agotados.add(municion);
            }
            if (arma.getMunicionActual() == arma.getCapacidadCargador()) {
                break;
            }
        }
        // Una unica copia/validacion de mochila, incluso para miles de cajas.
        if (!agotados.isEmpty()) {
            List<Objeto> restantes = new ArrayList<>();
            for (Objeto objeto : propietario.getMochila().getObjetos()) {
                if (!agotados.contains(objeto)) restantes.add(objeto);
            }
            propietario.getMochila().setObjetos(restantes);
        }
        return new ResultadoRecarga(arma, total);
    }
}
