package com.legendoftecla.inventory;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.commands.ComandoLanzarExplosivo;
import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.effects.TipoEstado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armeria;
import com.legendoftecla.model.items.CategoriaArma;
import com.legendoftecla.model.items.Granada;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoGranada;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmamentoTest {
    @Test
    void familiasMantienenAlcanceYProyectilesCompatibles() {
        Arma espada = Armeria.espada("Espada");
        Arma cuchillos = Armeria.cuchillosArrojadizos("Cuchillos", 3);
        Arma arco = Armeria.arco("Arco", 2);
        Arma ballesta = Armeria.ballesta("Ballesta", 1);
        Arma rifle = Armeria.rifle("Rifle", 6, 4);

        assertEquals(CategoriaArma.MELE, espada.getCategoria());
        assertEquals(1, espada.getAlcance());
        assertTrue(espada.usaMunicionInfinita());
        assertEquals(TipoMunicion.CUCHILLO_ARROJADIZO,
                cuchillos.getTipoMunicion());
        assertEquals(4, cuchillos.getAlcance());
        assertEquals(TipoMunicion.FLECHA, arco.getTipoMunicion());
        assertEquals(6, arco.getAlcance());
        assertEquals(TipoMunicion.VIROTE, ballesta.getTipoMunicion());
        assertEquals(7, ballesta.getAlcance());
        assertEquals(CategoriaArma.FUEGO, rifle.getCategoria());
        assertThrows(IllegalArgumentException.class, () -> new Arma(
                "Arco roto", "", 1, 10, true, CategoriaArma.ARCO,
                TipoMunicion.RIFLE, 3, 3));
    }

    @Test
    void meleNoConsumeMunicionPeroNoAtacaADistancia() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().setArmasEquipadas(List.of(Armeria.espada("Espada")));
        Sectoid cercano = new Sectoid("Cercano", new Posicion(0, 1),
                new Mochila(2, 10), 2);
        Sectoid lejano = new Sectoid("Lejano", new Posicion(0, 2),
                new Mochila(2, 10), 2);

        juego.getJugador().atacar(cercano);
        assertTrue(cercano.getSalud() < cercano.getSaludMaxima());
        assertThrows(IllegalStateException.class,
                () -> juego.getJugador().atacar(lejano));
    }

    @Test
    void flechasYVirotesSeRecarganComoRecursosFinitos() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Arma arco = Armeria.arco("Arco", 0);
        juego.getJugador().setArmasEquipadas(List.of(arco));
        juego.getJugador().getMochila().guardar(new Municion(
                "Flechas", 0.5, TipoMunicion.FLECHA, 3));

        ResultadoRecarga resultado = new ServicioRecarga().recargar(
                juego.getJugador(), "Arco");

        assertEquals(1, resultado.cantidad());
        assertEquals(1, arco.getMunicionActual());
        assertFalse(juego.getJugador().getMochila().getObjetos().isEmpty());
    }

    @Test
    void granadasSonAccesiblesSinRomperExclusividadDeDemolicion() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Sectoid enemigo = new Sectoid("Objetivo", new Posicion(0, 1),
                new Mochila(2, 10), 2);
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
        Granada granada = new Granada("Flash", "", 0.5, TipoGranada.ATURDIDORA);
        juego.getJugador().coger(granada);

        new ComandoLanzarExplosivo(new CommandContext(juego), "1e", "Flash").ejecutar();

        assertTrue(enemigo.getEstados().contiene(TipoEstado.ATURDIDO));
        assertTrue(juego.getJugador().getMochila().getObjetos().isEmpty());
    }
}
