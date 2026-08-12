package com.legendoftecla.ai;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.BusEventos;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.characters.Berserker;
import com.legendoftecla.model.characters.Commander;
import com.legendoftecla.model.characters.CommanderPrime;
import com.legendoftecla.model.characters.FaseJefe;
import com.legendoftecla.model.characters.Medic;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Pyro;
import com.legendoftecla.model.characters.PyroOverlord;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.characters.Sniper;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InteligenciaTacticaTest {
    @Test
    void ruidoTransicionaAInvestigacionYLaVisionACombateYBusqueda() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Scout scout = new Scout("S", new Posicion(1, 1), new Mochila(2, 10), 4);
        BusEventos bus = new BusEventos();
        try (SistemaRuido ruido = new SistemaRuido(bus, List.of(scout))) {
            ruido.generar(new Posicion(0, 0), FuenteRuido.CAMINAR);
            assertEquals(NivelAlerta.INVESTIGANDO, scout.getControladorIA().getEstado());
            AccionIA investigar = scout.getControladorIA().decidir(
                    new ContextoIA(scout, juego, false, null, false, false));
            assertEquals(TipoAccionIA.INVESTIGAR, investigar.tipo());

            AccionIA combatir = scout.getControladorIA().decidir(
                    new ContextoIA(scout, juego, true, null, false, false));
            assertEquals(TipoAccionIA.ATACAR, combatir.tipo());
            scout.getControladorIA().decidir(
                    new ContextoIA(scout, juego, false, null, false, false));
            assertEquals(NivelAlerta.BUSQUEDA, scout.getControladorIA().getEstado());
        }
    }

    @Test
    void cadaRolTieneUnaDecisionDistinguible() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Posicion p = new Posicion(1, 1);
        Mochila mochila = new Mochila(6, 40);
        Berserker berserker = new Berserker("B", p, mochila, 3);
        Medic medic = new Medic("M", p, new Mochila(2, 10), 3);
        Sniper sniper = new Sniper("N", p, new Mochila(2, 10), 6);
        Pyro pyro = new Pyro("P", p, new Mochila(2, 10), 4);
        Scout scout = new Scout("S", p, new Mochila(2, 10), 6);
        Commander commander = new Commander("C", p, new Mochila(2, 10), 5);

        assertEquals(TipoAccionIA.ACERCARSE, berserker.decidirTactica(
                new ContextoIA(berserker, juego, true, null, false, false)).tipo());
        assertEquals(TipoAccionIA.CURAR, medic.decidirTactica(
                new ContextoIA(medic, juego, true, null, true, false)).tipo());
        assertEquals(TipoAccionIA.ALEJARSE, sniper.decidirTactica(
                new ContextoIA(sniper, juego, true, null, false, false)).tipo());
        assertEquals(TipoAccionIA.INCENDIAR, pyro.decidirTactica(
                new ContextoIA(pyro, juego, true, null, false, false)).tipo());
        assertEquals(TipoAccionIA.ALERTAR, scout.decidirTactica(
                new ContextoIA(scout, juego, true, null, false, false)).tipo());
        assertEquals(TipoAccionIA.PROTEGER, commander.decidirTactica(
                new ContextoIA(commander, juego, true, null, false, false)).tipo());
        assertTrue(commander.bonificacionAliados() > 1);
    }

    @Test
    void jefesCambianDeFaseYHabilidadSegunSalud() {
        CommanderPrime prime = new CommanderPrime("Prime", new Posicion(1, 1),
                new Mochila(2, 10), 5);
        assertEquals(FaseJefe.UNO, prime.getFase());
        String inicial = prime.habilidadActual();
        prime.setSalud(200);
        assertEquals(FaseJefe.DOS, prime.getFase());
        prime.setSalud(100);
        assertEquals(FaseJefe.TRES, prime.getFase());
        prime.setSalud(20);
        assertEquals(FaseJefe.FINAL, prime.getFase());
        assertNotEquals(inicial, prime.habilidadActual());

        PyroOverlord pyro = new PyroOverlord("Overlord", new Posicion(1, 1),
                new Mochila(2, 10), 5);
        pyro.setSalud(30);
        assertEquals("infierno final", pyro.habilidadActual());
    }

    @Test
    void motorDelegaLosNuevosEnemigosEnLaIaFormal() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Berserker berserker = new Berserker("B", new Posicion(0, 2),
                new Mochila(2, 10), 4);
        juego.agregarEnemigo(berserker);
        juego.getMapa().getCelda(berserker.getPosicion()).agregarEnemigo(berserker);
        MotorPartida motor = new MotorPartida(juego);
        motor.setRandom(new Random(1));

        motor.ejecutarComando("mirar");

        assertEquals(new Posicion(0, 1), berserker.getPosicion());
    }
}
