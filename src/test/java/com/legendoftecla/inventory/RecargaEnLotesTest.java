package com.legendoftecla.inventory;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.TipoMunicion;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class RecargaEnLotesTest {
    @Test
    void paquetesVaciosNoDesaparecenSiLaRecargaEsRechazada() {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma rifle = arma(10, 0);
        jugador.setArmasEquipadas(List.of(rifle));
        Municion primera = paquete(0);
        Municion segunda = paquete(0);
        Objeto otro = new Componente("Herramienta", "No municion", 1);
        jugador.getMochila().setObjetos(List.of(primera, otro, segunda));

        assertThrows(AccionInvalidaException.class,
                () -> new ServicioRecarga().recargar(jugador, null));

        assertEquals(0, rifle.getMunicionActual());
        assertEquals(3, jugador.getMochila().getObjetos().size());
        assertSame(primera, jugador.getMochila().getObjetos().get(0));
        assertSame(otro, jugador.getMochila().getObjetos().get(1));
        assertSame(segunda, jugador.getMochila().getObjetos().get(2));
    }

    @Test
    void laRecargaParcialConservaPaquetesRestantesYOrden() throws Exception {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Arma rifle = arma(10, 2);
        jugador.setArmasEquipadas(List.of(rifle));
        Municion primera = paquete(5);
        Municion segunda = paquete(7);
        Municion tercera = paquete(9);
        Objeto otro = new Componente("Herramienta", "No municion", 1);
        jugador.getMochila().setObjetos(List.of(primera, otro, segunda, tercera));

        ResultadoRecarga resultado = new ServicioRecarga().recargar(jugador, "Rifle");

        assertEquals(8, resultado.cantidad());
        assertEquals(10, rifle.getMunicionActual());
        assertEquals(0, primera.getCantidad());
        assertEquals(4, segunda.getCantidad());
        assertEquals(9, tercera.getCantidad());
        assertEquals(3, jugador.getMochila().getObjetos().size());
        assertSame(otro, jugador.getMochila().getObjetos().get(0));
        assertSame(segunda, jugador.getMochila().getObjetos().get(1));
        assertSame(tercera, jugador.getMochila().getObjetos().get(2));
    }

    @Test
    void milesDePaquetesSeProcesanSinRecopiarLaMochilaPorCadaUno() {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        jugador.setMochila(new Mochila(5_000, 10_000));
        Arma rifle = arma(3_000, 0);
        jugador.setArmasEquipadas(List.of(rifle));
        Objeto primero = new Componente("Primero", "Prueba", 1);
        Objeto ultimo = new Componente("Ultimo", "Prueba", 1);
        List<Objeto> objetos = new ArrayList<>();
        objetos.add(primero);
        for (int i = 0; i < 3_000; i++) objetos.add(paquete(1));
        objetos.add(ultimo);
        jugador.getMochila().setObjetos(objetos);

        assertTimeoutPreemptively(Duration.ofSeconds(10), () -> {
            ResultadoRecarga resultado = new ServicioRecarga().recargar(jugador, null);
            assertEquals(3_000, resultado.cantidad());
            assertEquals(3_000, rifle.getMunicionActual());
            assertEquals(2, jugador.getMochila().getObjetos().size());
            assertSame(primero, jugador.getMochila().getObjetos().get(0));
            assertSame(ultimo, jugador.getMochila().getObjetos().get(1));
        });
    }

    private static Arma arma(int capacidad, int actual) {
        return new Arma("Rifle", "Prueba", 2, 10, false,
                TipoMunicion.RIFLE, capacidad, actual);
    }

    private static Municion paquete(int cantidad) {
        return new Municion("Cartuchos", 1, TipoMunicion.RIFLE, cantidad);
    }
}
