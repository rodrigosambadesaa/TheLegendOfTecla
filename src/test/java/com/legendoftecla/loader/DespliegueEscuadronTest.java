package com.legendoftecla.loader;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DespliegueEscuadronTest {
    @Test
    void elProceduralPorSemillaIncluyeAliadosEnLaSalidaTransitable() throws Exception {
        Juego juego = procedural(true);
        Posicion inicio = juego.getMapa().getInicio();

        assertFalse(juego.getAliados().isEmpty());
        assertTrue(juego.getMapa().esTransitable(inicio));
        assertTrue(juego.getAliados().stream()
                .allMatch(aliado -> aliado.getPosicion().equals(inicio)));
        assertEquals(juego.getAliados().size(),
                juego.getMapa().getCelda(inicio).getAliados().size());
    }

    @Test
    void todosLosGeneradoresDesplieganElGrupoConElJugador() throws Exception {
        List<Juego> juegos = List.of(
                new CargadorJuegoPorDefecto(TestFixtures.consola(), "Base", "marine",
                        Dificultad.NORMAL, new DimensionesMapa(10, 10), true).cargarJuego(),
                new CargadorJuegoGrandeConAliados(TestFixtures.consola(), "Atlas", "marine",
                        Dificultad.NORMAL, new DimensionesMapa(15, 15), true, 7).cargarJuego(),
                procedural(true));

        for (Juego juego : juegos) {
            Posicion despliegue = juego.getJugador().getPosicion();
            assertEquals(juego.getMapa().getInicio(), despliegue);
            assertTrue(juego.getMapa().esTransitable(despliegue));
            assertTrue(juego.getAliados().stream()
                    .allMatch(aliado -> aliado.getPosicion().equals(despliegue)));
        }
    }

    @Test
    void unEscuadronRecibeUnaDistribucionEnemigaMasCercana() throws Exception {
        Juego solitario = procedural(false);
        Juego escuadron = procedural(true);

        assertEquals(solitario.getEnemigos().size(), escuadron.getEnemigos().size());
        List<Posicion> sinPresion = solitario.getEnemigos().stream()
                .map(enemigo -> enemigo.getPosicion()).toList();
        List<Posicion> conPresion = escuadron.getEnemigos().stream()
                .map(enemigo -> enemigo.getPosicion()).toList();
        assertNotEquals(sinPresion, conPresion);
        assertTrue(distanciaTotal(escuadron) < distanciaTotal(solitario));
        assertTrue(escuadron.getEnemigos().stream()
                .allMatch(enemigo -> escuadron.getMapa().esTransitable(enemigo.getPosicion())));
    }

    private Juego procedural(boolean aliados) throws Exception {
        return new CargadorJuegoProcedural(TestFixtures.consola(), "Tecla", "marine",
                Dificultad.NORMAL, new DimensionesMapa(15, 21), aliados, 77).cargarJuego();
    }

    private int distanciaTotal(Juego juego) {
        Posicion inicio = juego.getMapa().getInicio();
        return juego.getEnemigos().stream()
                .mapToInt(enemigo -> enemigo.getPosicion().distanciaManhattan(inicio)).sum();
    }
}
