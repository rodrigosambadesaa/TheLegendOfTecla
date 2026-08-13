package com.legendoftecla.ai;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.effects.TipoEstado;
import com.legendoftecla.engine.ArsenalEnemigo;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Berserker;
import com.legendoftecla.model.characters.Commander;
import com.legendoftecla.model.characters.CommanderPrime;
import com.legendoftecla.model.characters.Medic;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Pyro;
import com.legendoftecla.model.characters.PyroOverlord;
import com.legendoftecla.model.characters.Scout;
import com.legendoftecla.model.characters.Sniper;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SistemaTurnosIATest {
    @Test
    void enemigoConsumeMunicionRecargaYVuelveADisparar() {
        Juego juego = juegoConJugadorEn(new Posicion(0, 2));
        Medic enemigo = agregar(juego, new Medic("M", new Posicion(0, 0),
                new Mochila(4, 20), 8));
        ArsenalEnemigo.asignar(enemigo, Dificultad.NORMAL);
        Arma rifle = enemigo.getArmasEquipadas().get(0);
        while (rifle.consumirDisparo()) {
            // vacia el cargador para forzar la estrategia de recarga
        }
        int reservaAntes = reserva(enemigo);
        SistemaTurnosIA sistema = new SistemaTurnosIA();

        ResultadoTurnoIA recarga = sistema.ejecutar(juego, enemigo, new Random(4));
        int saludAntes = juego.getJugador().getSalud();
        ResultadoTurnoIA ataque = sistema.ejecutar(juego, enemigo, new Random(4));

        assertEquals(TipoAccionIA.RECARGAR, recarga.accion().tipo());
        assertTrue(rifle.getMunicionActual() > 0);
        assertTrue(reserva(enemigo) < reservaAntes);
        assertEquals(TipoAccionIA.ATACAR, ataque.accion().tipo());
        assertTrue(juego.getJugador().getSalud() < saludAntes);
    }

    @Test
    void cadaRolEjecutaSuComportamientoYConservaRecursosFinitos() {
        Juego juego = juegoConJugadorEn(new Posicion(1, 2));
        agregarAliado(juego, "Escuadra");
        Medic medic = agregar(juego, new Medic("M", new Posicion(0, 0),
                new Mochila(5, 30), 8));
        Pyro pyro = agregar(juego, new Pyro("P", new Posicion(1, 0),
                new Mochila(5, 30), 8));
        Scout scout = agregar(juego, new Scout("S", new Posicion(2, 2),
                new Mochila(5, 30), 8));
        Commander commander = agregar(juego, new Commander("C", new Posicion(2, 1),
                new Mochila(5, 30), 8));
        Berserker herido = agregar(juego, new Berserker("B", new Posicion(0, 1),
                new Mochila(5, 30), 8));
        java.util.List.of(medic, pyro, scout, commander, herido)
                .forEach(enemigo -> ArsenalEnemigo.asignar(enemigo, Dificultad.NORMAL));
        herido.setSalud(40);
        SistemaTurnosIA sistema = new SistemaTurnosIA();

        ResultadoTurnoIA curacion = sistema.ejecutar(juego, medic, new Random(1));
        int objetosPyro = pyro.getMochila().getObjetos().size();
        ResultadoTurnoIA fuego = sistema.ejecutar(juego, pyro, new Random(1));
        ResultadoTurnoIA alerta = sistema.ejecutar(juego, scout, new Random(1));
        ResultadoTurnoIA orden = sistema.ejecutar(juego, commander, new Random(1));

        assertEquals(TipoAccionIA.CURAR, curacion.accion().tipo());
        assertTrue(herido.getSalud() > 40);
        assertEquals(TipoAccionIA.INCENDIAR, fuego.accion().tipo());
        assertTrue(juego.getMapa().getCelda(juego.getJugador().getPosicion()).estaArdiendo());
        assertEquals(objetosPyro - 1, pyro.getMochila().getObjetos().size());
        assertEquals(TipoAccionIA.ALERTAR, alerta.accion().tipo());
        assertEquals(NivelAlerta.ALERTA, medic.getControladorIA().getEstado());
        assertEquals(TipoAccionIA.PROTEGER, orden.accion().tipo());
        assertTrue(medic.getEstados().contiene(TipoEstado.INSPIRADO));
    }

    @Test
    void sniperBuscaCoberturaYLaOscuridadReduceDeteccion() {
        Juego juego = juegoConJugadorEn(new Posicion(0, 2));
        Sniper sniper = agregar(juego, new Sniper("N", new Posicion(0, 0),
                new Mochila(4, 20), 3));
        ArsenalEnemigo.asignar(sniper, Dificultad.NORMAL);
        juego.getMapa().getCelda(juego.getJugador().getPosicion()).setOscura(true);
        juego.getMapa().getCelda(juego.getJugador().getPosicion())
                .setOscuridadPermanente(true);

        ContextoIA oscuro = new PercepcionIA().percibir(juego, sniper);
        juego.getMapa().getCelda(juego.getJugador().getPosicion())
                .setOscuridadPermanente(false);
        juego.getMapa().getCelda(juego.getJugador().getPosicion()).setOscura(false);
        ContextoIA iluminado = new PercepcionIA().percibir(juego, sniper);

        assertFalse(oscuro.veJugador());
        assertTrue(iluminado.veJugador());
        assertEquals(TipoAccionIA.ALEJARSE,
                sniper.decidirTactica(iluminado).tipo());
    }

    @Test
    void jefesActivanCadaFaseUnaSolaVezYLosRefuerzosLleganArmados() {
        Juego juego = juegoConJugadorEn(new Posicion(0, 0));
        agregarAliado(juego, "Apoyo");
        CommanderPrime prime = agregar(juego, new CommanderPrime("Prime",
                new Posicion(1, 1), new Mochila(5, 40), 8));
        ArsenalEnemigo.asignar(prime, Dificultad.NORMAL);
        prime.setSalud(200);

        assertTrue(SistemaJefes.activarFase(juego, prime, new Random(2)));
        assertFalse(SistemaJefes.activarFase(juego, prime, new Random(2)));
        Scout refuerzo = juego.getEnemigos().stream()
                .filter(Scout.class::isInstance).map(Scout.class::cast)
                .findFirst().orElseThrow();
        assertFalse(refuerzo.getArmasEquipadas().isEmpty());

        PyroOverlord pyro = agregar(juego, new PyroOverlord("Overlord",
                new Posicion(2, 2), new Mochila(5, 40), 8));
        ArsenalEnemigo.asignar(pyro, Dificultad.NORMAL);
        pyro.setSalud(30);
        assertTrue(SistemaJefes.activarFase(juego, pyro, new Random(2)));
        assertFalse(SistemaJefes.activarFase(juego, pyro, new Random(2)));
        assertNotEquals(0, juego.getMapa().getCelda(new Posicion(0, 0)).getNivelFuego());
    }

    @Test
    void granadaEnemigaDanaJugadorYAliadoSinCrearCopias() {
        Juego juego = juegoConJugadorEn(new Posicion(1, 1));
        Aliado aliado = new Aliado("A", new Posicion(1, 1), new Mochila(3, 10), 4);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
        Pyro pyro = agregar(juego, new Pyro("P", new Posicion(1, 0),
                new Mochila(5, 30), 8));
        ArsenalEnemigo.asignar(pyro, Dificultad.NORMAL);
        int objetosAntes = pyro.getMochila().getObjetos().size();
        int saludJugador = juego.getJugador().getSalud();
        int saludAliado = aliado.getSalud();

        new SistemaTurnosIA().ejecutar(juego, pyro, new Random(0));

        assertTrue(juego.getJugador().getSalud() < saludJugador);
        assertTrue(aliado.getSalud() < saludAliado);
        assertEquals(objetosAntes - 1, pyro.getMochila().getObjetos().size());
    }

    @Test
    void elEscuadronEnemigoConcentraFuegoEnElAliadoMasVulnerable() {
        Juego juego = juegoConJugadorEn(new Posicion(0, 2));
        Aliado vulnerable = new Aliado("Vulnerable", new Posicion(0, 1),
                new Mochila(3, 10), 4);
        vulnerable.setSalud(20);
        juego.agregarAliado(vulnerable);
        juego.getMapa().getCelda(vulnerable.getPosicion()).agregarAliado(vulnerable);
        Sectoid enemigo = agregar(juego, new Sectoid("S", new Posicion(0, 0),
                new Mochila(4, 20), 8));
        ArsenalEnemigo.asignar(enemigo, Dificultad.NORMAL);
        int saludJugador = juego.getJugador().getSalud();

        ResultadoTurnoIA resultado = new SistemaTurnosIA().ejecutar(
                juego, enemigo, new Random(0));

        assertEquals(TipoAccionIA.ATACAR, resultado.accion().tipo());
        assertEquals(vulnerable.getPosicion(), resultado.accion().objetivo());
        assertTrue(vulnerable.getSalud() < 20);
        assertEquals(saludJugador, juego.getJugador().getSalud());
    }

    private Juego juegoConJugadorEn(Posicion posicion) {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().setPosicion(posicion);
        return juego;
    }

    private <T extends com.legendoftecla.model.characters.Enemigo> T agregar(
            Juego juego, T enemigo) {
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
        return enemigo;
    }

    private int reserva(com.legendoftecla.model.characters.Enemigo enemigo) {
        return enemigo.getMochila().getObjetos().stream()
                .filter(Municion.class::isInstance).map(Municion.class::cast)
                .mapToInt(Municion::getCantidad).sum();
    }

    private void agregarAliado(Juego juego, String nombre) {
        Aliado aliado = new Aliado(nombre, juego.getJugador().getPosicion(),
                new Mochila(3, 10), 4);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
    }
}
