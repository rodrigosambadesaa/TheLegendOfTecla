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

/** Seleccion determinista de arma al usar el comando recargar sin nombre. */
class RecargaAutomaticaSeleccionTest {
    @Test
    void prefiereSegundaArmaSiLaPrimeraTieneCargadorLleno() throws Exception {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma llena = arma("Pistola", TipoMunicion.PISTOLA, 6, 6);
        Arma incompleta = arma("Rifle", TipoMunicion.RIFLE, 10, 1);
        jugador.setArmasEquipadas(List.of(llena, incompleta));
        jugador.getMochila().guardar(new Municion("Balas rifle", 1, TipoMunicion.RIFLE, 5));

        ResultadoRecarga resultado = new ServicioRecarga().recargar(jugador, null);

        assertSame(incompleta, resultado.arma());
        assertEquals(5, resultado.cantidad());
        assertEquals(6, incompleta.getMunicionActual());
        assertEquals(6, llena.getMunicionActual());
        assertTrue(jugador.getMochila().getObjetos().isEmpty());
    }

    @Test
    void priorizaLaMunicionCompatibleDisponible() throws Exception {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma primera = arma("Pistola", TipoMunicion.PISTOLA, 6, 0);
        Arma segunda = arma("Rifle", TipoMunicion.RIFLE, 10, 0);
        jugador.setArmasEquipadas(List.of(primera, segunda));
        jugador.getMochila().guardar(new Municion("Balas rifle", 1, TipoMunicion.RIFLE, 2));
        jugador.getMochila().guardar(new Municion("Balas pistola agotadas",
                1, TipoMunicion.PISTOLA, 0));

        ResultadoRecarga resultado = new ServicioRecarga().recargar(jugador, null);

        assertSame(segunda, resultado.arma());
        assertEquals(2, resultado.cantidad());
        assertEquals(0, primera.getMunicionActual());
    }

    @Test
    void nombreExplicitoNoSeRedirigeAUnArmaDiferente() {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma llena = arma("Pistola", TipoMunicion.PISTOLA, 6, 6);
        Arma vacia = arma("Rifle", TipoMunicion.RIFLE, 10, 0);
        jugador.setArmasEquipadas(List.of(llena, vacia));
        jugador.getMochila().guardar(new Municion("Balas rifle", 1, TipoMunicion.RIFLE, 5));

        AccionInvalidaException error = assertThrows(AccionInvalidaException.class,
                () -> new ServicioRecarga().recargar(jugador, "Pistola"));

        assertTrue(error.getMessage().contains("lleno"));
        assertEquals(0, vacia.getMunicionActual());
    }

    @Test
    void conservaDiagnosticoDeFaltaDeMunicion() {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma vacia = arma("Rifle", TipoMunicion.RIFLE, 10, 0);
        jugador.setArmasEquipadas(List.of(vacia));

        AccionInvalidaException error = assertThrows(AccionInvalidaException.class,
                () -> new ServicioRecarga().recargar(jugador, null));

        assertTrue(error.getMessage().contains("No hay municion"));
        assertEquals(0, vacia.getMunicionActual());
    }

    @Test
    void noIntentaRecargarArmasDeMunicionInfinita() throws Exception {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma infinita = new Arma("Energia", "Sin cargador", 1, 5, false);
        Arma rifle = arma("Rifle", TipoMunicion.RIFLE, 10, 0);
        jugador.setArmasEquipadas(List.of(infinita, rifle));
        jugador.getMochila().guardar(new Municion("Balas rifle", 1, TipoMunicion.RIFLE, 3));

        ResultadoRecarga resultado = new ServicioRecarga().recargar(jugador, null);

        assertSame(rifle, resultado.arma());
        assertEquals(3, resultado.cantidad());
        assertTrue(infinita.usaMunicionInfinita());
    }

    private static Arma arma(String nombre, TipoMunicion tipo, int capacidad, int carga) {
        return new Arma(nombre, "Prueba", 1, 10, false, tipo, capacidad, carga);
    }
}
