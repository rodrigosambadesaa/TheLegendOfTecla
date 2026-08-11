package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.commands.ComandoAtacar;
import com.legendoftecla.commands.ComandoUsar;
import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.CuboAgua;
import com.legendoftecla.model.items.Linterna;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.TipoSuelo;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AmbienteCombateYAguaTest {
    @Test
    void elAtaqueMultipleRegistraAtacanteObjetivoDanioYVidaIndividual() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Posicion posicion = juego.getJugador().getPosicion();
        Sectoid uno = enemigo(juego, "Uno", posicion);
        Sectoid dos = enemigo(juego, "Dos", posicion);

        new ComandoAtacar(new CommandContext(juego), null, "todos").ejecutar();

        assertTrue(uno.getSalud() < uno.getSaludMaxima());
        assertTrue(dos.getSalud() < dos.getSaludMaxima());
        assertTrue(consola.salida().contains("Tecla ataca a Uno: quita"));
        assertTrue(consola.salida().contains("Tecla ataca a Dos: quita"));
        assertTrue(consola.salida().contains("de vida; quedan"));
    }

    @Test
    void antorchaEnMaderaIniciaFuegoQueDaniaATodosYSePropaga() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Posicion origen = juego.getJugador().getPosicion();
        Celda celda = juego.getMapa().getCelda(origen);
        celda.setTipoSuelo(TipoSuelo.MADERA);
        celda.setAntorchaMural(true);
        Posicion vecina = new Posicion(0, 1);
        juego.getMapa().getCelda(vecina).setTipoSuelo(TipoSuelo.MADERA);
        Aliado aliado = new Aliado("Aliado", origen, new Mochila(4, 20), 2);
        juego.agregarAliado(aliado);
        celda.agregarAliado(aliado);
        Sectoid enemigo = enemigo(juego, "Alien", origen);
        int vidaJugador = juego.getJugador().getSalud();
        int vidaAliado = aliado.getSalud();
        int vidaEnemigo = enemigo.getSalud();
        Random siempre = new Random(0) {
            @Override public double nextDouble() { return 0.0; }
        };

        assertTrue(SistemaIncendios.intentarDerribarAntorcha(juego, origen, siempre));
        SistemaIncendios.avanzarTurno(juego, siempre);

        assertFalse(celda.hasAntorchaMural());
        assertTrue(juego.getJugador().getSalud() < vidaJugador);
        assertTrue(aliado.getSalud() < vidaAliado);
        assertTrue(enemigo.getSalud() < vidaEnemigo);
        assertTrue(juego.getMapa().getCelda(vecina).estaArdiendo());
        assertTrue(consola.salida().contains("INCENDIO iniciado"));
        assertTrue(consola.salida().contains("El fuego daña a Tecla"));
    }

    @Test
    void cuboApagaElFuegoYDespuesPuedeRellenarseEnUnaFuente() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Celda celda = juego.getMapa().getCelda(juego.getJugador().getPosicion());
        CuboAgua cubo = new CuboAgua("Cubo", "Agua", 2, true);
        juego.getJugador().getMochila().guardar(cubo);
        celda.setNivelFuego(3);

        new ComandoUsar(new CommandContext(juego), "Cubo").ejecutar();
        assertFalse(celda.estaArdiendo());
        assertFalse(cubo.isLleno());

        celda.setFuenteAgua(true);
        new ComandoUsar(new CommandContext(juego), "Cubo").ejecutar();
        assertTrue(cubo.isLleno());
        assertTrue(consola.salida().contains("Llenas el cubo en la fuente"));
    }

    @Test
    void linternaReutilizableRevelaUnaZonaOscuraYElAsciiDistingueElEntorno() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Posicion inicio = juego.getJugador().getPosicion();
        juego.getMapa().getCelda(inicio).setOscuridadPermanente(true);
        Linterna linterna = new Linterna("Linterna", "Luz", 1, 3);
        juego.getJugador().getMochila().guardar(linterna);
        MotorPartida motor = new MotorPartida(juego);
        assertFalse(motor.hayLuzEn(inicio));

        new ComandoUsar(new CommandContext(juego), "Linterna").ejecutar();
        assertTrue(motor.hayLuzEn(inicio));
        assertTrue(juego.getJugador().getMochila().getObjetos().contains(linterna));

        Celda fuego = juego.getMapa().getCelda(new Posicion(0, 1));
        fuego.setNivelFuego(2);
        Celda fuente = juego.getMapa().getCelda(new Posicion(1, 0));
        fuente.setFuenteAgua(true);
        Celda madera = juego.getMapa().getCelda(new Posicion(1, 1));
        madera.setTipoSuelo(TipoSuelo.MADERA);
        String ascii = juego.getMapa().renderAscii(inicio, Set.of(), Set.of(), Set.of(),
                Set.of(inicio, new Posicion(0, 1), new Posicion(1, 0), new Posicion(1, 1)));
        assertTrue(ascii.contains("F"));
        assertTrue(ascii.contains("U"));
        assertTrue(ascii.contains("="));
    }

    private Sectoid enemigo(Juego juego, String nombre, Posicion posicion) {
        Sectoid enemigo = new Sectoid(nombre, posicion, new Mochila(2, 10), 2);
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(posicion).agregarEnemigo(enemigo);
        return enemigo;
    }
}
