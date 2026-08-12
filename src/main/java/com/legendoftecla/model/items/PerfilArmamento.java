package com.legendoftecla.model.items;

import java.util.Set;

/** Competencias de una clase o rol sin acoplarlas al comando de equipar. */
public record PerfilArmamento(Set<CategoriaArma> categorias,
        Set<TipoMunicion> municiones, boolean permiteGranadas,
        boolean permiteDemolicion) {
    public PerfilArmamento {
        categorias = Set.copyOf(categorias);
        municiones = Set.copyOf(municiones);
    }

    /** @return si el perfil domina la familia y proyectil del arma */
    public boolean permite(Arma arma) {
        return categorias.contains(arma.getCategoria())
                && (arma.usaMunicionInfinita()
                        || municiones.contains(arma.getTipoMunicion()));
    }
}
