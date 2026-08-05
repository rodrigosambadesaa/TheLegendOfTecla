package com.legendoftecla.model;

import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.ToritoRojo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MochilaTest {
    @Test
    void rechazaGuardarCuandoNoQuedaCapacidadOPesoDisponible() {
        Mochila mochilaCapacidad = new Mochila(1, 10);
        assertTrue(mochilaCapacidad.guardar(new Botiquin("botiquin", "Cura", 1, 10)));
        assertFalse(mochilaCapacidad.guardar(new ToritoRojo("torito", "Energia", 1, 10)));

        Mochila mochilaPeso = new Mochila(2, 1.5);
        assertTrue(mochilaPeso.guardar(new Botiquin("ligero", "Cura", 1, 10)));
        assertFalse(mochilaPeso.guardar(new ToritoRojo("pesado", "Energia", 1, 10)));
    }

    @Test
    void noPermiteMutarLaColeccionInternaNiRetirarObjetosInexistentes() {
        Mochila mochila = new Mochila(2, 10);
        mochila.guardar(new Botiquin("botiquin", "Cura", 1, 10));

        assertThrows(UnsupportedOperationException.class,
                () -> mochila.getObjetos().clear());
        assertNull(mochila.quitarPorNombre("no existe"));
    }
}
