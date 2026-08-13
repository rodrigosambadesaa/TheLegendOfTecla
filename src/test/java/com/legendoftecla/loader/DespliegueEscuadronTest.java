package com.legendoftecla.loader;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.engine.FabricaJuego;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.ToritoRojo;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        assertTrue(juego.getEnemigos().stream()
                .allMatch(enemigo -> !enemigo.getArmasEquipadas().isEmpty()
                        && enemigo.getArmaduraEquipada() != null));
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
    void unEscuadronRecibeTiempoDePreparacionYEnemigosDispersos() throws Exception {
        Juego solitario = procedural(false);
        Juego escuadron = procedural(true);

        assertEquals(solitario.getEnemigos().size(), escuadron.getEnemigos().size());
        Posicion despliegue = escuadron.getMapa().getInicio();
        assertTrue(escuadron.getEnemigos().stream().allMatch(enemigo ->
                enemigo.getPosicion().distanciaManhattan(despliegue) >= 4));
        assertTrue(escuadron.getEnemigos().stream().map(enemigo -> enemigo.getPosicion())
                .distinct().count() >= Math.min(4, escuadron.getEnemigos().size()));
        assertTrue(escuadron.getEnemigos().stream()
                .allMatch(enemigo -> escuadron.getMapa().esTransitable(enemigo.getPosicion())));
    }

    @Test
    void respetaLaCantidadExactaIndicadaPorElJugador() throws Exception {
        ConfiguracionPartida configuracion = new ConfiguracionPartida("Tecla", "marine",
                "procedural", Dificultad.NORMAL, new DimensionesMapa(15, 21), null, false, 1);
        configuracion.setCantidadAliados(23);
        configuracion.setNivelAliados(12);
        configuracion.setSeed(77);
        Juego juego = FabricaJuego.crear(TestFixtures.consola(), configuracion);

        assertEquals(23, juego.getAliados().size());
        assertEquals(23, juego.getEnemigos().size());
        assertTrue(juego.getAliados().stream().allMatch(aliado -> aliado.getNivel() == 12));
        assertTrue(juego.getAliados().stream().allMatch(aliado -> aliado.getSaludMaxima() > 90));
        assertEquals(6, juego.getAliados().stream().filter(aliado -> aliado.esMedico()).count());
        assertTrue(juego.getAliados().stream().filter(aliado -> aliado.esMedico())
                .allMatch(aliado -> aliado.getMochila().getObjetos().stream()
                        .filter(Botiquin.class::isInstance).count() >= 2));
        assertTrue(juego.getAliados().stream().filter(aliado -> aliado.esMedico())
                .allMatch(aliado -> aliado.getMochila().getObjetos().stream()
                        .filter(ToritoRojo.class::isInstance).count() >= 2));
        assertTrue(juego.getAliados().stream().allMatch(aliado ->
                aliado.getPosicion().equals(juego.getMapa().getInicio())));
    }

    @Test
    void cincuentaAliadosRecibenUnaAmenazaNumericaJustaYValida() throws Exception {
        Juego juego = proceduralConAliados(50, Dificultad.NORMAL, 91);

        assertEquals(50, juego.getAliados().size());
        assertEquals(50, juego.getEnemigos().size());
        assertTrue(juego.getEnemigos().stream().allMatch(enemigo ->
                juego.getMapa().esTransitable(enemigo.getPosicion())
                        && !enemigo.getPosicion().equals(juego.getMapa().getInicio())
                        && !enemigo.getPosicion().equals(juego.getMapa().getObjetivo())));
        assertTrue(juego.getEnemigos().stream().allMatch(enemigo ->
                !enemigo.getArmasEquipadas().isEmpty()
                        && enemigo.getArmaduraEquipada() != null));
    }

    @Test
    void laProporcionDeEnemigosRespetaLaDificultadElegida() throws Exception {
        assertEquals(10, proceduralConAliados(20, Dificultad.MUY_FACIL, 92)
                .getEnemigos().size());
        assertEquals(20, proceduralConAliados(20, Dificultad.NORMAL, 92)
                .getEnemigos().size());
        assertEquals(21, proceduralConAliados(20, Dificultad.DEMENTE, 92)
                .getEnemigos().size());
    }

    private Juego procedural(boolean aliados) throws Exception {
        return new CargadorJuegoProcedural(TestFixtures.consola(), "Tecla", "marine",
                Dificultad.NORMAL, new DimensionesMapa(15, 21), aliados, 77).cargarJuego();
    }

    private Juego proceduralConAliados(int cantidad, Dificultad dificultad,
            long seed) throws Exception {
        ConfiguracionPartida configuracion = new ConfiguracionPartida(
                "Tecla", "marine", "procedural", dificultad,
                new DimensionesMapa(15, 21), null, false, 1);
        configuracion.setCantidadAliados(cantidad);
        configuracion.setSeed(seed);
        return FabricaJuego.crear(TestFixtures.consola(), configuracion);
    }

}
