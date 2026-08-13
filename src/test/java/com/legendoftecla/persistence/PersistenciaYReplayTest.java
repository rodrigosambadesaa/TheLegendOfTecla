package com.legendoftecla.persistence;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.effects.Quemado;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.model.elements.EstadoPuerta;
import com.legendoftecla.model.elements.Mina;
import com.legendoftecla.model.elements.ParedDebil;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.elements.Terminal;
import com.legendoftecla.missions.ActivarTerminal;
import com.legendoftecla.missions.Mision;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.Credencial;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.RolAliado;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ArsenalEnemigo;
import com.legendoftecla.model.items.FaccionEquipo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersistenciaYReplayTest {
    @TempDir Path temporal;

    @Test
    void savegameVersionadoHaceRoundTripDeEstadoMutable() throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        juego.setPasos(4);
        juego.getJugador().setSalud(83);
        juego.getJugador().getEstados().aplicar(new Quemado());
        juego.getJugador().getMochila().guardar(new Municion(
                "Balas", 1, TipoMunicion.RIFLE, 8));
        juego.getJugador().getMochila().guardar(new Credencial(
                "Tarjeta roja", "Acceso", 0.1, "roja"));
        juego.getJugador().getMochila().guardar(new Componente(
                "Piezas", "Material", 0.2));
        juego.getJugador().getProgresion().ganarExperiencia(250);
        juego.publicarEvento(new PersonajeAtacado(Instant.EPOCH, "Tecla", "Sectoid",
                new Posicion(0, 0), new Posicion(0, 1)));
        juego.getMapa().getCelda(new Posicion(1, 1)).setNivelFuego(2);
        Puerta puerta = new Puerta("puerta-1", EstadoPuerta.BLOQUEADA,
                "roja", true, 10);
        puerta.recibirDanio(3);
        Mina mina = new Mina("mina-1", 9, 2, true);
        mina.detectar(juego.getJugador(), Integer.MAX_VALUE);
        Terminal terminal = new Terminal("terminal-1", 2, "puerta-1");
        terminal.hackear(2);
        ParedDebil pared = new ParedDebil("pared-1", 12);
        pared.recibirDanio(5);
        juego.getMapa().getCelda(new Posicion(1, 1)).setElementos(
                List.of(puerta, mina, terminal, pared));
        juego.setMision(new Mision("mision-1", "Hackeo", new ActivarTerminal(terminal),
                List.of(), List.of("100 XP")));
        juego.setPuntuacion(321);
        Aliado aliado = new Aliado("Veterano", juego.getMapa().getInicio(),
                new Mochila(4, 20), 3);
        aliado.setNivel(14);
        aliado.setRol(RolAliado.MEDICO);
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
        Sectoid enemigo = new Sectoid("Persistente", new Posicion(1, 0),
                new Mochila(4, 20), 4);
        ArsenalEnemigo.asignar(enemigo, Dificultad.NORMAL);
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
        Path archivo = temporal.resolve("partida.json");

        PersistenciaPartida.guardar(juego, archivo, 77);
        Juego cargado = PersistenciaPartida.cargar(archivo, TestFixtures.consola());

        assertTrue(Files.readString(archivo).contains("\"version\": 1"));
        assertEquals(4, cargado.getPasos());
        assertEquals(83, cargado.getJugador().getSalud());
        assertEquals(2, cargado.getMapa().getCelda(new Posicion(1, 1)).getNivelFuego());
        assertEquals(1, cargado.getJugador().getEstados().getActivos().size());
        assertEquals("Balas", cargado.getJugador().getMochila().getObjetos().get(0).getNombre());
        assertInstanceOf(Credencial.class,
                cargado.getJugador().getMochila().getObjetos().get(1));
        assertInstanceOf(Componente.class,
                cargado.getJugador().getMochila().getObjetos().get(2));
        assertEquals(2, cargado.getJugador().getProgresion().getNivel());
        assertEquals(150, cargado.getJugador().getProgresion().getExperiencia());
        Puerta puertaCargada = assertInstanceOf(Puerta.class,
                cargado.getMapa().getCelda(new Posicion(1, 1)).getElementos().get(0));
        assertEquals(EstadoPuerta.BLOQUEADA, puertaCargada.getEstado());
        assertEquals(7, puertaCargada.getResistencia());
        Mina minaCargada = assertInstanceOf(Mina.class,
                cargado.getMapa().getCelda(new Posicion(1, 1)).getElementos().get(1));
        assertTrue(minaCargada.isRemota());
        assertTrue(minaCargada.isDetectada());
        ParedDebil paredCargada = assertInstanceOf(ParedDebil.class,
                cargado.getMapa().getCelda(new Posicion(1, 1)).getElementos().get(3));
        assertEquals(7, paredCargada.getResistencia());
        assertTrue(paredCargada.bloqueaVision());
        assertEquals(1, cargado.getEstadisticas().getDisparos());
        assertTrue(cargado.getLogros().getDesbloqueados().contains("primer-contacto"));
        assertEquals(321, cargado.getPuntuacion());
        assertEquals(14, cargado.getAliados().get(0).getNivel());
        assertEquals(RolAliado.MEDICO, cargado.getAliados().get(0).getRol());
        assertEquals(FaccionEquipo.ENEMIGA,
                cargado.getEnemigos().get(0).getArmasEquipadas().get(0).getFaccion());
        assertEquals(FaccionEquipo.ENEMIGA,
                cargado.getEnemigos().get(0).getArmaduraEquipada().getFaccion());
        assertEquals("mision-1", cargado.getMision().getId());
        assertTrue(cargado.getMision().completada(cargado));
    }

    @Test
    void rechazaCorrupcionYVersionDesconocida() throws Exception {
        Path corrupta = temporal.resolve("corrupta.json");
        Files.writeString(corrupta, "no-json");
        assertThrows(Exception.class, () -> PersistenciaPartida.cargar(
                corrupta, TestFixtures.consola()));
        Files.writeString(corrupta, "{\"version\":99}");
        assertThrows(Exception.class, () -> PersistenciaPartida.cargar(
                corrupta, TestFixtures.consola()));
    }

    @Test
    void replayValidaLaHuellaFinal() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        String hash = ServicioReplay.hash(juego);
        ReplayPartida replay = new ReplayPartida(1, 1, List.of(), hash);
        assertTrue(ServicioReplay.reproducir(replay,
                () -> new com.legendoftecla.engine.MotorPartida(
                        TestFixtures.juegoBasico(TestFixtures.consola()))));
    }
}
