package com.legendoftecla.model.elements;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.loader.CargadorJuegoJson;
import com.legendoftecla.loader.EscenarioDefinicion;
import com.legendoftecla.loader.SerializadorEscenarioJson;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElementosPersistenciaTest {
    @TempDir
    Path temporal;

    @Test
    void jsonConservaPuertaTerminalYReferenciasSinRomperCamposHistoricos() throws Exception {
        EscenarioDefinicion escenario = EscenarioDefinicion.nuevo(5, 5);
        EscenarioDefinicion.CeldaDef puerta = escenario.celda(0, 1);
        puerta.setElementoTipo("puerta");
        puerta.setElementoId("acceso");
        puerta.setElementoEstado("BLINDADA");
        puerta.setResistencia(25);
        EscenarioDefinicion.CeldaDef terminal = escenario.celda(1, 0);
        terminal.setElementoTipo("terminal");
        terminal.setElementoId("consola");
        terminal.setReferencia("acceso");
        terminal.setDificultad(4);

        SerializadorEscenarioJson.guardar(escenario, temporal);
        Juego juego = new CargadorJuegoJson(TestFixtures.consola(), "Tecla", "marine",
                temporal, Dificultad.NORMAL, null, false).cargarJuego();

        Puerta cargada = assertInstanceOf(Puerta.class, juego.getMapa()
                .getCelda(new Posicion(0, 1)).getElementos().get(0));
        Terminal terminalCargado = assertInstanceOf(Terminal.class, juego.getMapa()
                .getCelda(new Posicion(1, 0)).getElementos().get(0));
        assertEquals(EstadoPuerta.BLINDADA, cargada.getEstado());
        assertEquals(25, cargada.getResistencia());
        assertEquals("acceso", terminalCargado.getObjetivoId());
    }

    @Test
    void validadorRechazaReferenciaRotaYTipoDesconocido() {
        EscenarioDefinicion escenario = EscenarioDefinicion.nuevo(4, 4);
        EscenarioDefinicion.CeldaDef terminal = escenario.celda(1, 1);
        terminal.setElementoTipo("terminal");
        terminal.setElementoId("t1");
        terminal.setReferencia("no-existe");

        assertThrows(JuegoException.class, () -> SerializadorEscenarioJson.validar(escenario));

        terminal.setReferencia(null);
        terminal.setElementoTipo("teletransportador");
        assertThrows(JuegoException.class, () -> SerializadorEscenarioJson.validar(escenario));
    }

    @Test
    void jsonConservaAmbienteInicialYComponentesDelEditor() throws Exception {
        EscenarioDefinicion escenario = EscenarioDefinicion.nuevo(5, 5);
        EscenarioDefinicion.CeldaDef celda = escenario.celda(2, 2);
        celda.setOscura(true);
        celda.setSueloMadera(true);
        celda.setFuenteAgua(true);
        celda.setNivelFuego(2);
        EscenarioDefinicion.ObjetoDef componente = new EscenarioDefinicion.ObjetoDef();
        componente.setTipo("componente");
        componente.setNombre("Piezas");
        componente.setDescripcion("Material de crafting");
        componente.setFila(2);
        componente.setColumna(2);
        escenario.agregarObjeto(componente);

        SerializadorEscenarioJson.guardar(escenario, temporal);
        Juego juego = new CargadorJuegoJson(TestFixtures.consola(), "Tecla", "marine",
                temporal, Dificultad.NORMAL, null, false).cargarJuego();

        var cargada = juego.getMapa().getCelda(new Posicion(2, 2));
        assertEquals(2, cargada.getNivelFuego());
        assertEquals(com.legendoftecla.model.world.TipoSuelo.MADERA, cargada.getTipoSuelo());
        assertTrue(cargada.hasFuenteAgua());
        assertInstanceOf(Componente.class, cargada.getObjetos().get(0));
    }

    @Test
    void jsonDelEditorCargaMisionPrincipalYSecundaria() throws Exception {
        EscenarioDefinicion escenario = EscenarioDefinicion.nuevo(5, 5);
        EscenarioDefinicion.ObjetivoDef principal = new EscenarioDefinicion.ObjetivoDef();
        principal.setTipo("sobrevivir_turnos");
        principal.setValor(4);
        EscenarioDefinicion.ObjetivoDef secundario = new EscenarioDefinicion.ObjetivoDef();
        secundario.setTipo("no_perder_aliados");
        EscenarioDefinicion.MisionDef mision = new EscenarioDefinicion.MisionDef();
        mision.setId("resistencia");
        mision.setNombre("Resistir");
        mision.setPrincipal(principal);
        mision.setSecundarios(java.util.List.of(secundario));
        mision.setRecompensas(java.util.List.of("100 XP"));
        escenario.setMision(mision);

        SerializadorEscenarioJson.guardar(escenario, temporal);
        Juego juego = new CargadorJuegoJson(TestFixtures.consola(), "Tecla", "marine",
                temporal, Dificultad.NORMAL, null, false).cargarJuego();

        assertEquals("resistencia", juego.getMision().getId());
        assertEquals("Sobrevivir 4 turnos", juego.getMision().getPrincipal().descripcion());
        assertEquals(1, juego.getMision().getSecundarios().size());
        assertEquals(java.util.List.of("100 XP"), juego.getMision().getRecompensas());
    }
}
