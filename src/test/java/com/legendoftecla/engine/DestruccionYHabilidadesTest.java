package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.effects.TipoEstado;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.elements.EstadoPuerta;
import com.legendoftecla.model.elements.ParedDebil;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.items.Armeria;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.progression.CatalogoHabilidades;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DestruccionYHabilidadesTest {
    @Test
    void explosionDestruyeParedYPuertaYActualizaPasoYVision() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion posicion = new Posicion(0, 1);
        ParedDebil pared = new ParedDebil("pared", 20);
        Puerta puerta = new Puerta("puerta", EstadoPuerta.BLINDADA,
                null, true, 30);
        juego.getMapa().getCelda(posicion).agregarElemento(pared);
        juego.getMapa().getCelda(posicion).agregarElemento(puerta);
        assertFalse(juego.getMapa().esTransitable(posicion));
        assertFalse(juego.getMapa().hayLineaAtaque(new Posicion(0, 0),
                new Posicion(0, 2)));

        SistemaDestruccion.ResultadoDestruccion resultado =
                SistemaDestruccion.danar(juego, posicion, 35);

        assertEquals(List.of("pared", "puerta"), resultado.destruidos());
        assertTrue(juego.getMapa().esTransitable(posicion));
        assertTrue(juego.getMapa().hayLineaAtaque(new Posicion(0, 0),
                new Posicion(0, 2)));
    }

    @Test
    void silenciadorReduceRuidoYSupresionAsustaAlObjetivo() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().getProgresion().ganarExperiencia(600);
        var arbol = CatalogoHabilidades.para(juego.getJugador());
        juego.getJugador().getProgresion().desbloquear(
                CatalogoHabilidades.RESISTENCIA, arbol, juego.getJugador());
        juego.getJugador().getProgresion().desbloquear(
                CatalogoHabilidades.FUEGO_SUPRESION, arbol, juego.getJugador());
        // El Marine no tiene silenciador en su arbol; se restaura como habilidad
        // adquirida en una campaña compatible para probar la regla transversal.
        java.util.Set<String> habilidades = new java.util.HashSet<>(
                juego.getJugador().getProgresion().getDesbloqueadas());
        habilidades.add(CatalogoHabilidades.SILENCIADOR);
        juego.getJugador().getProgresion().restaurar(4, 0, habilidades);
        juego.getJugador().equipar(Armeria.rifle("Rifle", 3, 3));
        Sectoid objetivo = new Sectoid("S", new Posicion(0, 2),
                new Mochila(2, 10), 4);
        juego.agregarEnemigo(objetivo);
        juego.getMapa().getCelda(objetivo.getPosicion()).agregarEnemigo(objetivo);
        List<RuidoGenerado> ruidos = new ArrayList<>();
        juego.getBusEventos().suscribir(RuidoGenerado.class, ruidos::add);

        SistemaCombate.atacar(juego, juego.getJugador(), objetivo, new Random(2));

        assertEquals(2, ruidos.get(0).intensidad());
        assertTrue(objetivo.getEstados().contiene(TipoEstado.ASUSTADO));
    }
}
