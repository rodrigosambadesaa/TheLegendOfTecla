package com.legendoftecla.persistence;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.legendoftecla.TestFixtures;
import com.legendoftecla.constants.CondicionVictoria;
import com.legendoftecla.constants.FormacionAliada;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenciaEscuadronV2Test {
    @TempDir
    Path temporal;

    @Test
    void preservaEvacuadosYPermiteCompletarVictoriaTrasCargar() throws Exception {
        Juego juego = partida();
        Aliado evacuado = aliado("Sanitaria");
        Aliado pendiente = aliado("Combatiente");
        juego.agregarAliado(evacuado);
        juego.agregarAliado(pendiente);
        juego.setCondicionVictoria(CondicionVictoria.JUGADOR_Y_ALIADOS);
        juego.setFormacionAliada(FormacionAliada.OFENSIVA);
        juego.getJugador().setPosicion(juego.getMapa().getObjetivo());

        assertTrue(juego.extraerAliado(evacuado));
        assertFalse(juego.jugadorGano());
        Path archivo = temporal.resolve("salida-parcial.json");
        PersistenciaPartida.guardar(juego, archivo, 99);

        Juego cargado = PersistenciaPartida.cargar(archivo, TestFixtures.consola());
        assertEquals(2, cargado.getAliadosIniciales());
        assertEquals(2, cargado.getAliadosRegistrados().size());
        assertEquals(1, cargado.getAliados().size());
        assertEquals(1, cargado.getAliadosExtraidos());
        assertEquals(1, cargado.getAliadosExtraidosDetalle().size());
        assertEquals(FormacionAliada.OFENSIVA, cargado.getFormacionAliada());
        assertEquals(CondicionVictoria.JUGADOR_Y_ALIADOS, cargado.getCondicionVictoria());
        assertSame(cargado.getAliadosRegistrados().get(0),
                cargado.getAliadosExtraidosDetalle().get(0));
        assertTrue(cargado.estaAliadoExtraido(cargado.getAliadosRegistrados().get(0)));
        assertFalse(cargado.jugadorGano());

        assertTrue(cargado.extraerAliado(cargado.getAliados().get(0)));
        assertEquals(2, cargado.getAliadosExtraidos());
        assertTrue(cargado.jugadorGano());
    }

    @Test
    void restituyeVictoriaSoloJugadorSinEvacuarAlResto() throws Exception {
        Juego juego = partida();
        juego.agregarAliado(aliado("Guardia"));
        juego.setCondicionVictoria(CondicionVictoria.SOLO_JUGADOR);
        juego.getJugador().setPosicion(juego.getMapa().getObjetivo());
        assertTrue(juego.jugadorGano());

        Path archivo = temporal.resolve("solo-jugador.json");
        PersistenciaPartida.guardar(juego, archivo, 101);
        Juego cargado = PersistenciaPartida.cargar(archivo, TestFixtures.consola());

        assertEquals(CondicionVictoria.SOLO_JUGADOR, cargado.getCondicionVictoria());
        assertEquals(0, cargado.getAliadosExtraidos());
        assertEquals(1, cargado.getAliadosRegistrados().size());
        assertTrue(cargado.jugadorGano());
    }

    @Test
    void permiteLeerGuardadosHistoricosV1SinNuevosCampos() throws Exception {
        Juego juego = partida();
        juego.agregarAliado(aliado("Veterano"));
        Path archivo = temporal.resolve("historico.json");
        PersistenciaPartida.guardar(juego, archivo, 102);
        JsonObject json = JsonParser.parseString(Files.readString(archivo)).getAsJsonObject();
        json.addProperty("version", 1);
        json.remove("aliadosRegistrados");
        json.remove("indicesExtraidos");
        json.remove("condicionVictoria");
        json.remove("formacionAliada");
        Files.writeString(archivo, json.toString());

        Juego restaurado = PersistenciaPartida.cargar(archivo, TestFixtures.consola());

        assertEquals(1, restaurado.getAliadosIniciales());
        assertEquals(1, restaurado.getAliadosRegistrados().size());
        assertEquals(1, restaurado.getAliados().size());
        assertEquals(0, restaurado.getAliadosExtraidos());
    }

    @Test
    void rechazaIndicesEvacuadosDuplicadosOFueraDeRango() throws Exception {
        Juego juego = partida();
        juego.agregarAliado(aliado("Solo"));
        Path archivo = temporal.resolve("manipulado.json");
        PersistenciaPartida.guardar(juego, archivo, 103);

        JsonObject json = JsonParser.parseString(Files.readString(archivo)).getAsJsonObject();
        JsonArray duplicados = new JsonArray();
        duplicados.add(0);
        duplicados.add(0);
        json.add("indicesExtraidos", duplicados);
        Files.writeString(archivo, json.toString());
        assertThrows(JuegoException.class,
                () -> PersistenciaPartida.cargar(archivo, TestFixtures.consola()));

        JsonArray fuera = new JsonArray();
        fuera.add(999);
        json.add("indicesExtraidos", fuera);
        Files.writeString(archivo, json.toString());
        assertThrows(JuegoException.class,
                () -> PersistenciaPartida.cargar(archivo, TestFixtures.consola()));
    }

    private static Juego partida() {
        return TestFixtures.juegoBasico(TestFixtures.consola());
    }

    private static Aliado aliado(String nombre) {
        return new Aliado(nombre, new Posicion(2, 2), new Mochila(2, 5), 2);
    }
}
