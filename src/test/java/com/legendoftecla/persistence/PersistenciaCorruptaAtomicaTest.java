package com.legendoftecla.persistence;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.legendoftecla.missions.AlcanzarSalida;
import com.legendoftecla.missions.Campana;
import com.legendoftecla.missions.Mision;
import com.legendoftecla.progression.ProgresionPersonaje;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Los archivos corruptos no alteran la campaña ni filtran excepciones JSON. */
class PersistenciaCorruptaAtomicaTest {
    @TempDir Path temporal;

    @Test
    void nivelInvalidoNoModificaElIndiceNiLaProgresion() throws Exception {
        Campana campana = campana();
        Path ruta = temporal.resolve("campana.json");
        PersistenciaCampana.guardar(campana, ruta);
        JsonObject json = JsonParser.parseString(Files.readString(ruta)).getAsJsonObject();
        json.addProperty("indice", 2);
        json.addProperty("nivel", -1);
        Files.writeString(ruta, json.toString());

        assertThrows(IOException.class, () -> PersistenciaCampana.restaurar(campana, ruta));

        assertEquals(1, campana.getIndice());
        assertEquals(2, campana.getProgresion().getNivel());
        assertEquals(11, campana.getProgresion().getExperiencia());
        assertEquals(Set.of("habilidad"), campana.getProgresion().getDesbloqueadas());
    }

    @Test
    void indiceInvalidoNoModificaElEstado() throws Exception {
        Campana campana = campana();
        Path ruta = temporal.resolve("indice.json");
        PersistenciaCampana.guardar(campana, ruta);
        JsonObject json = JsonParser.parseString(Files.readString(ruta)).getAsJsonObject();
        json.addProperty("indice", 999);
        json.addProperty("nivel", 3);
        Files.writeString(ruta, json.toString());

        assertThrows(IOException.class, () -> PersistenciaCampana.restaurar(campana, ruta));
        assertEquals(1, campana.getIndice());
        assertEquals(2, campana.getProgresion().getNivel());
        assertEquals(11, campana.getProgresion().getExperiencia());
    }

    @Test
    void archivosJsonMalformadosSeTraducenAIOException() throws Exception {
        Campana campana = campana();
        Path ruta = temporal.resolve("malformado.json");
        Files.writeString(ruta, "{esto no es json");
        assertThrows(IOException.class, () -> PersistenciaCampana.restaurar(campana, ruta));
        assertThrows(IOException.class, () -> PersistenciaEstadisticas.cargar(ruta));

        assertEquals(1, campana.getIndice());
        assertEquals(2, campana.getProgresion().getNivel());
    }

    @Test
    void unaCampanaValidaSigueRestaurandoseCorrectamente() throws Exception {
        Campana origen = campana();
        Path ruta = temporal.resolve("campana/validada.json");
        PersistenciaCampana.guardar(origen, ruta);
        Campana destino = new Campana("campana", misiones(), new ProgresionPersonaje());

        PersistenciaCampana.restaurar(destino, ruta);

        assertEquals(origen.getIndice(), destino.getIndice());
        assertEquals(origen.getProgresion().getNivel(), destino.getProgresion().getNivel());
        assertEquals(origen.getProgresion().getExperiencia(),
                destino.getProgresion().getExperiencia());
        assertEquals(origen.getProgresion().getDesbloqueadas(),
                destino.getProgresion().getDesbloqueadas());
    }

    private static Campana campana() {
        ProgresionPersonaje progreso = new ProgresionPersonaje();
        progreso.restaurar(2, 11, Set.of("habilidad"));
        Campana campana = new Campana("campana", misiones(), progreso);
        campana.avanzar();
        return campana;
    }

    private static List<Mision> misiones() {
        Mision mision = new Mision("prueba", "Prueba", new AlcanzarSalida(),
                List.of(), List.of());
        return List.of(mision, mision);
    }
}
