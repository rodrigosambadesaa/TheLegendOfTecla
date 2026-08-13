package com.legendoftecla.inventory;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.engine.PrecisionTirador;
import com.legendoftecla.model.characters.Francotirador;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.CatalogoArmas;
import com.legendoftecla.model.items.ClaseArma;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogoArmasAvanzadoTest {
    @Test
    void catalogoIncluyeVariantesReproduciblesDeTodasLasFamiliasSolicitadas() {
        assertTrue(CatalogoArmas.nombres().size() >= 30);
        for (ClaseArma clase : ClaseArma.values()) {
            Arma primera = CatalogoArmas.crearDeClase(clase, new Random(17), "a");
            Arma segunda = CatalogoArmas.crearDeClase(clase, new Random(17), "b");
            assertEquals(primera.getClaseArma(), segunda.getClaseArma());
            assertEquals(primera.getDanio(), segunda.getDanio());
            assertEquals(primera.getCapacidadCargador(), segunda.getCapacidadCargador());
        }
        Arma unaMano = CatalogoArmas.crearDeClase(
                ClaseArma.ESPADA_UNA_MANO, new Random(1), "una");
        Arma dosManos = CatalogoArmas.crearDeClase(
                ClaseArma.ESPADA_DOS_MANOS, new Random(1), "dos");
        assertNotEquals(unaMano.isDosManos(), dosManos.isDosManos());
        assertNotEquals(unaMano.getDanio(), dosManos.getDanio());
    }

    @Test
    void penetracionReduceSoloLaDefensaEfectivaDeLaArmadura() {
        Sectoid objetivo = new Sectoid("Blindado", new Posicion(0, 0),
                new Mochila(2, 20), 2);
        objetivo.setArmaduraEquipada(new Armadura("Blindaje", "", 2, 10, 0, 0,
                com.legendoftecla.model.items.FaccionEquipo.ENEMIGA));
        int salud = objetivo.getSalud();

        objetivo.recibirDanio(20, 6);

        assertEquals(16, salud - objetivo.getSalud());
    }

    @Test
    void precisionPerteneceAlTiradorYElNivelLaMejoraLevemente() {
        Francotirador tirador = new Francotirador("Ada", new Posicion(0, 0),
                new Mochila(2, 20), 4);
        double nivelUno = PrecisionTirador.calcular(tirador);
        tirador.getProgresion().restaurar(50, 0, java.util.Set.of());

        assertTrue(PrecisionTirador.calcular(tirador) > nivelUno);
        assertTrue(PrecisionTirador.calcular(tirador) <= 0.97);
        assertTrue(TestFixtures.juegoBasico(TestFixtures.consola()).getJugador()
                .getProgresion().getNivel() == 1);
    }
}
