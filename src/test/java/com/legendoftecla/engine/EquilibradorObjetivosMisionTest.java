package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.missions.EliminarEnemigo;
import com.legendoftecla.missions.EliminarJefe;
import com.legendoftecla.missions.Mision;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.PyroOverlord;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EquilibradorObjetivosMisionTest {
    @Test
    void noEliminaElUnicoObjetivoNecesarioParaGanarLaMision() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Enemigo primero = enemigo(juego, "RellenoUno");
        Enemigo segundo = enemigo(juego, "RellenoDos");
        Enemigo objetivo = enemigo(juego, "Objetivo");
        juego.setMision(new Mision("principal", "Eliminar objetivo",
                new EliminarEnemigo("Objetivo"), List.of(), List.of()));

        assertEquals(2, EquilibradorBandos.aplicar(juego));

        assertEquals(List.of(objetivo), juego.getEnemigos());
        assertFalse(juego.getMision().completada(juego));
        assertFalse(juego.getMapa().getCelda(primero.getPosicion()).getEnemigos()
                .contains(primero));
        assertFalse(juego.getMapa().getCelda(segundo.getPosicion()).getEnemigos()
                .contains(segundo));
        assertTrue(juego.getMapa().getCelda(objetivo.getPosicion()).getEnemigos()
                .contains(objetivo));
        objetivo.setSalud(0);
        assertTrue(juego.getMision().completada(juego));
    }

    @Test
    void sinMisionLosJefesTienenPrioridadSobreEnemigosDeRelleno() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        enemigo(juego, "Soldado1");
        enemigo(juego, "Soldado2");
        PyroOverlord jefe = new PyroOverlord("Jefe",
                new Posicion(1, 1), new Mochila(2, 5), 3);
        registrar(juego, jefe);

        assertEquals(2, EquilibradorBandos.aplicar(juego));
        assertEquals(1, juego.getEnemigos().size());
        assertSame(jefe, juego.getEnemigos().get(0));
    }

    @Test
    void priorizaObjetivoPrincipalSecundarioYJefeConservandoOrdenOriginal() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.agregarAliado(aliado("A"));
        juego.agregarAliado(aliado("B"));
        Enemigo relleno = enemigo(juego, "Relleno");
        PyroOverlord jefe = new PyroOverlord("Jefe",
                new Posicion(1, 1), new Mochila(2, 5), 3);
        registrar(juego, jefe);
        Enemigo secundario = enemigo(juego, "Secundario");
        Enemigo principal = enemigo(juego, "Principal");
        Enemigo sobrante = enemigo(juego, "Sobran");
        juego.setMision(new Mision("dos-objetivos", "Mision compuesta",
                new EliminarEnemigo("Principal"),
                List.of(new EliminarEnemigo("Secundario")), List.of()));

        assertEquals(2, EquilibradorBandos.aplicar(juego));
        assertEquals(List.of(jefe, secundario, principal), juego.getEnemigos());
        assertFalse(juego.getEnemigos().contains(relleno));
        assertFalse(juego.getEnemigos().contains(sobrante));
        assertEquals(3, juego.getEnemigos().size());
    }

    @Test
    void misionDeJefePriorizaSuNombreSinConfundirloConOtroJefe() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        PyroOverlord primero = new PyroOverlord("JefeNoObjetivo",
                new Posicion(1, 1), new Mochila(2, 5), 3);
        PyroOverlord objetivo = new PyroOverlord("JefeObjetivo",
                new Posicion(1, 2), new Mochila(2, 5), 3);
        registrar(juego, primero);
        registrar(juego, objetivo);
        juego.setMision(new Mision("jefe", "Eliminar jefe",
                new EliminarJefe("JefeObjetivo"), List.of(), List.of()));

        EquilibradorBandos.aplicar(juego);

        assertEquals(List.of(objetivo), juego.getEnemigos());
        assertFalse(juego.getMision().completada(juego));
    }

    @Test
    void sinObjetivosNiJefesMantieneElOrdenHistorico() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Enemigo primero = enemigo(juego, "Primero");
        enemigo(juego, "Segundo");

        assertEquals(1, EquilibradorBandos.aplicar(juego));
        assertEquals(List.of(primero), juego.getEnemigos());
    }

    private static Enemigo enemigo(Juego juego, String nombre) {
        Enemigo enemigo = new Sectoid(nombre, new Posicion(1, 1),
                new Mochila(2, 5), 3);
        registrar(juego, enemigo);
        return enemigo;
    }

    private static void registrar(Juego juego, Enemigo enemigo) {
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
    }

    private static Aliado aliado(String nombre) {
        return new Aliado(nombre, new Posicion(2, 1), new Mochila(2, 5), 3);
    }
}
