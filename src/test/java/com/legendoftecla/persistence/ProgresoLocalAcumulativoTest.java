package com.legendoftecla.persistence;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** El perfil voluntario conserva el progreso de escuadras diferentes. */
class ProgresoLocalAcumulativoTest {
    @TempDir Path temporal;

    @Test
    void conservaAliadosAusentesCuandoLaSiguientePartidaCambiaElEquipo()
            throws Exception {
        Path archivo = temporal.resolve("progreso.json");
        Juego primera = juegoConAliado("Alfa");
        primera.getJugador().getProgresion().restaurar(3, 75, Set.of());
        primera.getAliadosRegistrados().get(0).getProgresion()
                .restaurar(4, 120, Set.of());
        PersistenciaProgresoLocal.guardar(primera, archivo);

        Juego segunda = juegoConAliado("Beta");
        segunda.getJugador().getProgresion().restaurar(2, 20, Set.of());
        segunda.getAliadosRegistrados().get(0).getProgresion()
                .restaurar(2, 25, Set.of());
        PersistenciaProgresoLocal.guardar(segunda, archivo);

        Juego tercera = TestFixtures.juegoBasico(TestFixtures.consola());
        tercera.agregarAliado(aliado("Alfa"));
        tercera.agregarAliado(aliado("Beta"));
        PersistenciaProgresoLocal.aplicar(tercera, archivo);

        assertEquals(3, tercera.getJugador().getProgresion().getNivel());
        assertEquals(75, tercera.getJugador().getProgresion().getExperiencia());
        assertEquals(4, tercera.getAliadosRegistrados().get(0).getNivel());
        assertEquals(120, tercera.getAliadosRegistrados().get(0)
                .getProgresion().getExperiencia());
        assertEquals(2, tercera.getAliadosRegistrados().get(1).getNivel());
        assertEquals(25, tercera.getAliadosRegistrados().get(1)
                .getProgresion().getExperiencia());
    }

    @Test
    void partidasAntiguasNoReducenNivelNiExperienciaPreviamenteGuardados()
            throws Exception {
        Path archivo = temporal.resolve("progreso.json");
        Juego nueva = juegoConAliado("Alfa");
        nueva.getJugador().getProgresion().restaurar(6, 80, Set.of());
        nueva.getAliadosRegistrados().get(0).getProgresion()
                .restaurar(5, 95, Set.of());
        PersistenciaProgresoLocal.guardar(nueva, archivo);

        Juego antigua = juegoConAliado("Alfa");
        antigua.getJugador().getProgresion().restaurar(6, 10, Set.of());
        antigua.getAliadosRegistrados().get(0).getProgresion()
                .restaurar(4, 390, Set.of());
        PersistenciaProgresoLocal.guardar(antigua, archivo);

        Juego reanudada = juegoConAliado("Alfa");
        PersistenciaProgresoLocal.aplicar(reanudada, archivo);
        assertEquals(6, reanudada.getJugador().getProgresion().getNivel());
        assertEquals(80, reanudada.getJugador().getProgresion().getExperiencia());
        assertEquals(5, reanudada.getAliadosRegistrados().get(0).getNivel());
        assertEquals(95, reanudada.getAliadosRegistrados().get(0)
                .getProgresion().getExperiencia());

        assertEquals(10, antigua.getJugador().getProgresion().getExperiencia());
    }

    @Test
    void perfilDeOtroPersonajeNoSeMezclaConElJugadorActual() throws Exception {
        Path archivo = temporal.resolve("progreso.json");
        Juego primero = juegoConAliado("Alfa");
        primero.getJugador().getProgresion().restaurar(8, 10, Set.of());
        PersistenciaProgresoLocal.guardar(primero, archivo);

        Juego segundo = juegoConAliado("Beta");
        segundo.getJugador().setNombre("OtroJugador");
        segundo.getJugador().getProgresion().restaurar(2, 20, Set.of());
        PersistenciaProgresoLocal.guardar(segundo, archivo);

        Juego nuevo = juegoConAliado("Beta");
        nuevo.getJugador().setNombre("OtroJugador");
        PersistenciaProgresoLocal.aplicar(nuevo, archivo);
        assertEquals(2, nuevo.getJugador().getProgresion().getNivel());
        assertEquals(20, nuevo.getJugador().getProgresion().getExperiencia());

        Juego antiguo = juegoConAliado("Alfa");
        PersistenciaProgresoLocal.aplicar(antiguo, archivo);
        assertEquals(1, antiguo.getJugador().getProgresion().getNivel());
    }

    @Test
    void perfilAnteriorCorruptoPuedeSustituirsePorUnoValido() throws Exception {
        Path archivo = temporal.resolve("progreso.json");
        Files.writeString(archivo, "{invalid:");
        Juego juego = juegoConAliado("Alfa");
        juego.getJugador().getProgresion().restaurar(2, 25, Set.of());

        PersistenciaProgresoLocal.guardar(juego, archivo);

        assertTrue(Files.size(archivo) > 0);
        assertFalse(Files.readString(archivo).contains("{invalid:"));
        Juego reanudada = juegoConAliado("Alfa");
        PersistenciaProgresoLocal.aplicar(reanudada, archivo);
        assertEquals(2, reanudada.getJugador().getProgresion().getNivel());
        assertEquals(25, reanudada.getJugador().getProgresion().getExperiencia());
    }

    private static Juego juegoConAliado(String nombre) {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.agregarAliado(aliado(nombre));
        return juego;
    }

    private static Aliado aliado(String nombre) {
        return new Aliado(nombre, new Posicion(1, 1), new Mochila(2, 5), 3);
    }
}
