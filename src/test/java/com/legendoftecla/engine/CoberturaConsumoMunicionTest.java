package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.elements.Barricada;
import com.legendoftecla.model.elements.OrientacionCobertura;
import com.legendoftecla.model.elements.TipoCobertura;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoberturaConsumoMunicionTest {
    @Test
    void disparoFallidoPorCoberturaConsumeExactamenteUnaBala() {
        Juego juego = juegoConCobertura();
        Arma arma = rifle(3);
        juego.getJugador().setArmasEquipadas(List.of(arma));
        Sectoid objetivo = enemigo(juego);
        int salud = objetivo.getSalud();
        List<PersonajeDanado> danios = new ArrayList<>();
        List<PersonajeAtacado> ataques = new ArrayList<>();
        juego.getBusEventos().suscribir(PersonajeDanado.class, danios::add);
        juego.getBusEventos().suscribir(PersonajeAtacado.class, ataques::add);

        ResultadoAtaque resultado = SistemaCombate.atacar(juego, juego.getJugador(),
                objetivo, siempreFalla());

        assertEquals(2, arma.getMunicionActual());
        assertEquals(salud, objetivo.getSalud());
        assertEquals(0, resultado.vidaQuitada());
        assertEquals(1, ataques.size());
        assertEquals(0, danios.size());
    }

    @Test
    void impactoConCoberturaConsumeUnaBalaYAplicaDanio() {
        Juego juego = juegoConCobertura();
        Arma arma = rifle(3);
        juego.getJugador().setArmasEquipadas(List.of(arma));
        Sectoid objetivo = enemigo(juego);

        ResultadoAtaque resultado = SistemaCombate.atacar(juego, juego.getJugador(),
                objetivo, siempreAcierta());

        assertEquals(2, arma.getMunicionActual());
        assertTrue(resultado.vidaQuitada() > 0);
        assertTrue(objetivo.getSalud() < objetivo.getSaludMaxima());
    }

    @Test
    void ultimoDisparoFallidoVaciaElCargadorEImpideRepetir() {
        Juego juego = juegoConCobertura();
        Arma arma = rifle(1);
        juego.getJugador().setArmasEquipadas(List.of(arma));
        Sectoid objetivo = enemigo(juego);
        List<PersonajeAtacado> ataques = new ArrayList<>();
        juego.getBusEventos().suscribir(PersonajeAtacado.class, ataques::add);

        SistemaCombate.atacar(juego, juego.getJugador(), objetivo, siempreFalla());

        assertEquals(0, arma.getMunicionActual());
        assertThrows(IllegalStateException.class, () ->
                SistemaCombate.atacar(juego, juego.getJugador(), objetivo, siempreFalla()));
        assertEquals(1, ataques.size());
        assertEquals(objetivo.getSaludMaxima(), objetivo.getSalud());
    }

    private static Juego juegoConCobertura() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getMapa().getCelda(new Posicion(0, 1)).agregarElemento(
                new Barricada("Cobertura", 30, TipoCobertura.COMPLETA,
                        OrientacionCobertura.TODAS));
        return juego;
    }

    private static Sectoid enemigo(Juego juego) {
        Sectoid enemigo = new Sectoid("Cubierto", new Posicion(0, 2),
                new Mochila(3, 10), 3);
        juego.agregarEnemigo(enemigo);
        return enemigo;
    }

    private static Arma rifle(int balas) {
        return new Arma("Rifle", "Finita", 2, 12, true,
                TipoMunicion.RIFLE, 10, balas);
    }

    private static Random siempreFalla() {
        return new Random(27) {
            @Override public double nextDouble() { return 0.999; }
        };
    }

    private static Random siempreAcierta() {
        return new Random(27) {
            @Override public double nextDouble() { return 0.0; }
        };
    }
}
