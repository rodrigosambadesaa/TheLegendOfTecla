package com.legendoftecla.inventory;

import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoMunicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Contratos de consistencia entre municion, tipo y capacidad del cargador. */
class EstadoMunicionArmaTest {
    @Test
    void elArmaInfinitaNoPuedeConvertirseEnFinitaSinCargador() {
        Arma arma = new Arma("Energia", "Arma infinita", 1, 8, false);
        assertTrue(arma.usaMunicionInfinita());
        assertEquals(0, arma.getCapacidadCargador());

        assertThrows(IllegalArgumentException.class,
                () -> arma.setTipoMunicion(TipoMunicion.PISTOLA));

        assertTrue(arma.usaMunicionInfinita());
        assertEquals(0, arma.getCapacidadCargador());
        assertEquals(0, arma.getMunicionActual());
        assertTrue(arma.puedeDisparar());
    }

    @Test
    void noTransformaProyectilesExistentesEnOtraFamilia() {
        Arma arma = new Arma("Rifle", "Con cargador", 2, 12, true,
                TipoMunicion.RIFLE, 20, 7);
        assertThrows(IllegalArgumentException.class,
                () -> arma.setTipoMunicion(TipoMunicion.PISTOLA));
        assertEquals(TipoMunicion.RIFLE, arma.getTipoMunicion());
        assertEquals(20, arma.getCapacidadCargador());
        assertEquals(7, arma.getMunicionActual());
    }

    @Test
    void cambiaMunicionCompatibleDeCargadorVacioSinCrearCartuchos() {
        Arma arma = new Arma("Rifle", "Vacio", 2, 12, true,
                TipoMunicion.RIFLE, 20, 0);
        arma.setTipoMunicion(TipoMunicion.PISTOLA);
        assertEquals(TipoMunicion.PISTOLA, arma.getTipoMunicion());
        assertEquals(20, arma.getCapacidadCargador());
        assertEquals(0, arma.getMunicionActual());
        assertFalse(arma.puedeDisparar());

        Municion balasAntiguas = new Municion("Rifle", 1, TipoMunicion.RIFLE, 5);
        Municion balasNuevas = new Municion("Pistola", 1, TipoMunicion.PISTOLA, 4);
        assertEquals(0, arma.recargar(balasAntiguas));
        assertEquals(5, balasAntiguas.getCantidad());
        assertEquals(4, arma.recargar(balasNuevas));
        assertEquals(4, arma.getMunicionActual());
        assertTrue(arma.puedeDisparar());
    }

    @Test
    void conversionAInfinitaInvalidaElCargadorYRechazaReconversionInvalida() {
        Arma arma = new Arma("Rifle", "Convertida", 2, 12, true,
                TipoMunicion.RIFLE, 20, 7);
        arma.setTipoMunicion(TipoMunicion.INFINITA);
        assertTrue(arma.usaMunicionInfinita());
        assertEquals(0, arma.getCapacidadCargador());
        assertEquals(0, arma.getMunicionActual());
        assertThrows(IllegalArgumentException.class,
                () -> arma.setTipoMunicion(TipoMunicion.RIFLE));
        assertTrue(arma.usaMunicionInfinita());
    }

    @Test
    void cambioAlMismoTipoEsIdempotente() {
        Arma arma = new Arma("Rifle", "Cargada", 2, 12, true,
                TipoMunicion.RIFLE, 20, 7);
        arma.setTipoMunicion(TipoMunicion.RIFLE);
        assertEquals(7, arma.getMunicionActual());
        assertEquals(20, arma.getCapacidadCargador());
    }
}
