package com.legendoftecla.inventory;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoMunicion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioRecargaSeleccionTest {
    @Test
    void recargaAutomaticaIgnoraPrimeraArmaDeMunicionInfinita() throws Exception {
        Personaje personaje = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma infinita = new Arma("Arma infinita", "", 1, 8, false);
        Arma rifle = new Arma("Rifle", "", 2, 12, false, TipoMunicion.RIFLE, 6, 1);
        personaje.setArmasEquipadas(List.of(infinita, rifle));
        personaje.getMochila().guardar(new Municion("Balas", 1, TipoMunicion.RIFLE, 4));

        ResultadoRecarga recarga = new ServicioRecarga().recargar(personaje, null);

        assertSame(rifle, recarga.arma());
        assertEquals(4, recarga.cantidad());
        assertEquals(5, rifle.getMunicionActual());
        assertTrue(personaje.getMochila().getObjetos().isEmpty());
    }

    @Test
    void seleccionExplicitaNoOcultaLaIncompatibilidadDelArma() {
        Personaje personaje = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma infinita = new Arma("Arma infinita", "", 1, 8, false);
        Arma rifle = new Arma("Rifle", "", 2, 12, false, TipoMunicion.RIFLE, 6, 0);
        personaje.setArmasEquipadas(List.of(infinita, rifle));

        assertThrows(AccionInvalidaException.class,
                () -> new ServicioRecarga().recargar(personaje, "Arma infinita"));
        assertThrows(AccionInvalidaException.class,
                () -> new ServicioRecarga().recargar(personaje, "Rifle"));
        assertEquals(0, rifle.getMunicionActual());
    }
}
