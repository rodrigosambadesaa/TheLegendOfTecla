package com.legendoftecla.missions;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.progression.ArbolHabilidades;
import com.legendoftecla.progression.CatalogoHabilidades;
import com.legendoftecla.progression.Habilidad;
import com.legendoftecla.progression.ProgresionPersonaje;
import com.legendoftecla.progression.RequisitoHabilidad;
import com.legendoftecla.persistence.PersistenciaCampana;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MisionYProgresionTest {
    @TempDir Path temporal;
    @Test
    void unaMisionSustituyeLaVictoriaSoloCuandoExiste() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.getJugador().setPosicion(juego.getMapa().getObjetivo());
        assertTrue(juego.jugadorGano());

        Mision mision = new Mision("recuperacion", "Recuperacion",
                new RecuperarObjeto("Nucleo"), List.of(new NoPerderAliados()),
                List.of("100 XP"));
        juego.setMision(mision);
        assertFalse(juego.jugadorGano());
        juego.getJugador().getMochila().guardar(new Botiquin("Nucleo", "", 1, 1));
        assertTrue(juego.jugadorGano());
        assertEquals(1, mision.secundariosCompletados(juego));
    }

    @Test
    void objetivosDeTurnosFuegoYDisparosSonComponibles() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.setPasos(5);
        assertTrue(new SobrevivirTurnos(5).completado(juego));
        Posicion fuego = new Posicion(1, 1);
        assertTrue(new ApagarIncendio(fuego).completado(juego));
        assertTrue(new CompletarSinDisparar(() -> false,
                new SobrevivirTurnos(5)).completado(juego));
        assertFalse(new CompletarSinDisparar(() -> true,
                new SobrevivirTurnos(5)).completado(juego));
        assertFalse(new EliminarEnemigo("ausente").completado(juego));
    }

    @Test
    void campanaAvanzaYProgresionRespetaNivelYPrerrequisitos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        ProgresionPersonaje progresion = juego.getJugador().getProgresion();
        assertEquals(2, progresion.ganarExperiencia(300));
        ArbolHabilidades arbol = new ArbolHabilidades();
        arbol.agregar(new Habilidad("resistencia", "Resistencia",
                new RequisitoHabilidad(2, null), jugador ->
                        jugador.setSaludMaxima(jugador.getSaludMaxima() + 10)));
        arbol.agregar(new Habilidad("supresion", "Fuego de supresion",
                new RequisitoHabilidad(3, "resistencia"), jugador -> { }));
        assertTrue(progresion.desbloquear("resistencia", arbol, juego.getJugador()));
        assertTrue(progresion.desbloquear("supresion", arbol, juego.getJugador()));
        assertFalse(progresion.desbloquear("supresion", arbol, juego.getJugador()));

        Mision m = new Mision("m", "M", new AlcanzarSalida(), List.of(), List.of());
        Campana campana = new Campana("c", List.of(m, m), progresion);
        assertFalse(campana.avanzar());
        assertTrue(campana.avanzar());
    }

    @Test
    void catalogoDeClaseAplicaEfectosYValidaDuplicados() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        ProgresionPersonaje progresion = juego.getJugador().getProgresion();
        progresion.ganarExperiencia(600);
        ArbolHabilidades arbol = CatalogoHabilidades.para(juego.getJugador());
        int salud = juego.getJugador().getSaludMaxima();

        assertTrue(progresion.desbloquear(CatalogoHabilidades.RESISTENCIA,
                arbol, juego.getJugador()));
        assertEquals(salud + 15, juego.getJugador().getSaludMaxima());
        assertTrue(progresion.desbloquear(CatalogoHabilidades.FUEGO_SUPRESION,
                arbol, juego.getJugador()));
        assertThrows(IllegalArgumentException.class,
                () -> arbol.agregar(arbol.buscar(CatalogoHabilidades.RESISTENCIA)));
    }

    @Test
    void campanaPersisteIndiceYProgresionSobreSuDefinicion() throws Exception {
        Mision m = new Mision("m", "M", new AlcanzarSalida(), List.of(), List.of());
        ProgresionPersonaje origen = new ProgresionPersonaje();
        origen.ganarExperiencia(250);
        Campana campana = new Campana("campana", List.of(m, m), origen);
        campana.avanzar();
        Path archivo = temporal.resolve("campana.json");
        PersistenciaCampana.guardar(campana, archivo);

        Campana restaurada = new Campana("campana", List.of(m, m),
                new ProgresionPersonaje());
        PersistenciaCampana.restaurar(restaurada, archivo);

        assertEquals(1, restaurada.getIndice());
        assertEquals(2, restaurada.getProgresion().getNivel());
        assertEquals(150, restaurada.getProgresion().getExperiencia());
    }
}
