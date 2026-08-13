package com.legendoftecla.model.items;

/** Subfamilias tacticas con alcance y uso claramente diferenciados. */
public enum ClaseArma {
    ESPADA_UNA_MANO(1), ESPADA_DOS_MANOS(1), CUCHILLO(1),
    CUCHILLO_ARROJADIZO(4), ARCO(6), BALLESTA(7),
    PISTOLA(7), SUBFUSIL(7), ESCOPETA(5), RIFLE_ASALTO(9),
    RIFLE_PRECISION(12), AMETRALLADORA(9), LANZACOHETES(10), ENERGIA(9);

    private final int alcance;

    ClaseArma(int alcance) { this.alcance = alcance; }

    /** @return alcance tactico base en celdas */
    public int getAlcance() { return alcance; }
}
