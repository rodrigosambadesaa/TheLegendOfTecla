package com.legendoftecla.model.characters;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.commands.ComandoDescansar;
import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CuracionSinResurreccionTest {
    @Test
    void laCuracionNoResucitaANingunPersonajeFallecido() {
        Sectoid enemigo = new Sectoid("Muerto", new Posicion(1, 1),
                new Mochila(3, 10), 2);
        enemigo.setSalud(0);

        enemigo.recuperarSalud(20);
        enemigo.recuperarSalud(1_000);

        assertEquals(0, enemigo.getSalud());
        assertEquals(70, enemigo.getSaludMaxima());
    }

    @Test
    void elBotiquinNoRevivePeroCuraAlPersonajeVivo() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Botiquin botiquin = new Botiquin("Medico", "Tratamiento", 1, 20);
        juego.getJugador().setSalud(0);

        botiquin.usar(juego.getJugador());
        assertEquals(0, juego.getJugador().getSalud());

        juego.getJugador().setSalud(40);
        botiquin.usar(juego.getJugador());
        assertEquals(60, juego.getJugador().getSalud());
    }

    @Test
    void descansoNoCambiaLaCondicionDeFallecido() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().setSalud(0);

        new ComandoDescansar(new CommandContext(juego)).ejecutar();

        assertEquals(0, juego.getJugador().getSalud());
    }

    @Test
    void escaladoDeDificultadPreservaCeroYRespetaLaProporcionDeLosVivos() {
        Sectoid muerto = new Sectoid("Muerto", new Posicion(1, 1),
                new Mochila(3, 10), 2);
        muerto.setSalud(0);
        muerto.escalarSalud(1.5);

        assertEquals(105, muerto.getSaludMaxima());
        assertEquals(0, muerto.getSalud());

        Sectoid vivo = new Sectoid("Vivo", new Posicion(1, 1),
                new Mochila(3, 10), 2);
        vivo.setSalud(35);
        vivo.escalarSalud(1.5);

        assertEquals(105, vivo.getSaludMaxima());
        assertEquals(53, vivo.getSalud());
        assertTrue(vivo.getSalud() > 0);
    }
}
