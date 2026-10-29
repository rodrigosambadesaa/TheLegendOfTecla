package com.legendoftecla.model.characters;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.TipoMunicion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ArmasEquipadasIdentidadTest {
    @Test
    void unaMismaArmaNoPuedeOcuparDosRanuras() {
        Marine marine = (Marine) TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma fisica = arma("Pistola");

        assertThrows(IllegalArgumentException.class,
                () -> marine.setArmasEquipadas(List.of(fisica, fisica)));
        assertEquals(0, marine.getArmasEquipadas().size());
    }

    @Test
    void unCambioRechazadoConservaLasArmasEquipadasAnteriores() {
        Marine marine = (Marine) TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma anterior = arma("Inicial");
        Arma duplicada = arma("Repetida");
        marine.setArmasEquipadas(List.of(anterior));

        assertThrows(IllegalArgumentException.class,
                () -> marine.setArmasEquipadas(List.of(duplicada, duplicada)));
        assertEquals(1, marine.getArmasEquipadas().size());
        assertSame(anterior, marine.getArmasEquipadas().get(0));
    }

    @Test
    void permiteDosArmasFisicamenteDistintasAunqueSusNombresCoincidan() {
        Marine marine = (Marine) TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma primera = arma("Rifle");
        Arma segunda = arma("Rifle");

        marine.setArmasEquipadas(List.of(primera, segunda));

        assertEquals(2, marine.getArmasEquipadas().size());
        assertSame(primera, marine.getArmasEquipadas().get(0));
        assertSame(segunda, marine.getArmasEquipadas().get(1));
    }

    private static Arma arma(String nombre) {
        return new Arma(nombre, "Equipo individual", 1, 9,
                true, TipoMunicion.RIFLE, 6, 3);
    }
}
