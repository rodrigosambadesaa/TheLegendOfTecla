package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Granada;
import com.legendoftecla.model.items.TipoGranada;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExplosivosObjetivosVivosTest {
    @Test
    void unaCeldaConSoloCadaveresNoConsumeLaGranadaNiPublicaAtaques() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Granada granada = new Granada("Granada", "Ensayo", 1, TipoGranada.FRAGMENTACION);
        assertTrue(juego.getJugador().getMochila().guardar(granada));
        Sectoid muerto = enemigo(juego, "Caido", 0);
        List<PersonajeAtacado> ataques = new ArrayList<>();
        juego.getBusEventos().suscribir(PersonajeAtacado.class, ataques::add);

        assertThrows(ComandoException.class,
                () -> new ComandoLanzarExplosivo(
                        new CommandContext(juego), "1e", "Granada").ejecutar());

        assertSame(granada, juego.getJugador().getMochila().getObjetos().get(0));
        assertEquals(1, juego.getJugador().getMochila().getObjetos().size());
        assertEquals(0, muerto.getSalud());
        assertTrue(ataques.isEmpty());
    }

    @Test
    void losCadaveresNoSeContabilizanComoObjetivosDeUnaExplosion() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Granada granada = new Granada("Granada", "Ensayo", 1, TipoGranada.FRAGMENTACION);
        juego.getJugador().getMochila().guardar(granada);
        Sectoid muerto = enemigo(juego, "Caido", 0);
        Sectoid vivo = enemigo(juego, "Vivo", 70);
        List<PersonajeAtacado> ataques = new ArrayList<>();
        juego.getBusEventos().suscribir(PersonajeAtacado.class, ataques::add);

        new ComandoLanzarExplosivo(new CommandContext(juego), "1e", "Granada").ejecutar();

        assertEquals(0, juego.getJugador().getMochila().getObjetos().size());
        assertEquals(0, muerto.getSalud());
        assertTrue(vivo.getSalud() < 70);
        assertEquals(1, ataques.size());
        assertEquals("Vivo", ataques.get(0).objetivo());
    }

    private static Sectoid enemigo(Juego juego, String nombre, int salud) {
        Posicion destino = new Posicion(0, 1);
        Sectoid enemigo = new Sectoid(nombre, destino, new Mochila(3, 10), 2);
        enemigo.setSalud(salud);
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(destino).agregarEnemigo(enemigo);
        return enemigo;
    }
}
