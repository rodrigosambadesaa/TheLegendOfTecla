package com.legendoftecla.inventory;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.commands.ComandoRecargar;
import com.legendoftecla.commands.ComandoTransferir;
import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.commands.CommandParser;
import com.legendoftecla.events.ArmaRecargada;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MunicionEIntercambioTest {
    @Test
    void cargadorVacioParcialCompletoEIncompatible() {
        Arma rifle = new Arma("Rifle", "Servicio", 3, 12, true,
                TipoMunicion.RIFLE, 5, 0);
        Municion balas = new Municion("Balas", 1, TipoMunicion.RIFLE, 3);
        Municion pistola = new Municion("9mm", 1, TipoMunicion.PISTOLA, 8);

        assertFalse(rifle.puedeDisparar());
        assertEquals(0, rifle.recargar(pistola));
        assertEquals(3, rifle.recargar(balas));
        assertTrue(rifle.consumirDisparo());
        Municion reserva = new Municion("Reserva", 1, TipoMunicion.RIFLE, 10);
        assertEquals(3, rifle.recargar(reserva));
        assertEquals(5, rifle.getMunicionActual());
        assertEquals(7, reserva.getCantidad());
        assertEquals(0, rifle.recargar(reserva));
    }

    @Test
    void servicioYComandoRecarganSinDejarPaquetesVaciosYPublicanEvento()
            throws Exception {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Arma rifle = new Arma("Rifle tactico", "", 3, 12, true,
                TipoMunicion.RIFLE, 5, 1);
        juego.getJugador().setArmasEquipadas(List.of(rifle));
        juego.getJugador().getMochila().guardar(
                new Municion("Cargador", 1, TipoMunicion.RIFLE, 4));
        List<ArmaRecargada> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(ArmaRecargada.class, eventos::add);

        new ComandoRecargar(new CommandContext(juego), "Rifle tactico").ejecutar();

        assertEquals(5, rifle.getMunicionActual());
        assertTrue(juego.getJugador().getMochila().getObjetos().isEmpty());
        assertEquals(1, eventos.size());
        assertEquals(4, eventos.get(0).cantidad());
        assertThrows(ComandoException.class,
                () -> new ComandoRecargar(new CommandContext(juego), null).ejecutar());
    }

    @Test
    void cargadorVacioImpideAtaqueAntesDePublicarEventos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Personaje objetivo = new Aliado("A", new Posicion(0, 1),
                new Mochila(3, 10), 2);
        juego.getJugador().setArmasEquipadas(List.of(new Arma(
                "Pistola", "", 1, 10, false, TipoMunicion.PISTOLA, 6, 0)));
        List<PersonajeAtacado> eventos = new ArrayList<>();
        juego.getBusEventos().suscribir(PersonajeAtacado.class, eventos::add);

        assertThrows(IllegalStateException.class, () ->
                com.legendoftecla.engine.SistemaCombate.atacar(
                        juego, juego.getJugador(), objetivo, new Random(1)));
        assertTrue(eventos.isEmpty());
        assertEquals(objetivo.getSaludMaxima(), objetivo.getSalud());
    }

    @Test
    void intercambioEsAtomicoYRespetaDistanciaClaseCapacidadYEquipo()
            throws Exception {
        Personaje jugador = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        Aliado aliado = new Aliado("A", new Posicion(0, 1), new Mochila(1, 10), 2);
        Botiquin botiquin = new Botiquin("Botiquin", "", 1, 5);
        jugador.getMochila().guardar(botiquin);
        aliado.getMochila().guardar(new Botiquin("Vendas", "", 1, 2));
        ServicioIntercambio servicio = new ServicioIntercambio(1);

        servicio.intercambiar(jugador, "Botiquin", aliado, "Vendas", false);
        assertEquals("Vendas", jugador.getMochila().getObjetos().get(0).getNombre());
        assertEquals("Botiquin", aliado.getMochila().getObjetos().get(0).getNombre());

        Explosivo explosivo = new Explosivo("C4", "", 1);
        aliado.getMochila().setObjetos(List.of(explosivo));
        assertThrows(AccionInvalidaException.class,
                () -> servicio.dar(aliado, jugador, "C4", false));
        assertEquals(explosivo, aliado.getMochila().getObjetos().get(0));

        Arma equipada = new Arma("Equipada", "", 1, 5, false);
        jugador.setArmasEquipadas(List.of(equipada));
        assertThrows(AccionInvalidaException.class,
                () -> servicio.dar(jugador, aliado, "Equipada", false));
        aliado.setPosicion(new Posicion(2, 2));
        assertThrows(AccionInvalidaException.class,
                () -> servicio.pedir(jugador, aliado, "C4", false));
    }

    @Test
    void comandoBloqueaIntercambioDuranteCombateVisible() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado aliado = new Aliado("Ana", new Posicion(0, 1), new Mochila(3, 10), 2);
        aliado.getMochila().guardar(new Botiquin("Botiquin", "", 1, 5));
        juego.agregarAliado(aliado);
        Sectoid enemigo = new Sectoid("Amenaza", new Posicion(0, 2),
                new Mochila(3, 10), 3);
        juego.agregarEnemigo(enemigo);

        ComandoTransferir comando = new ComandoTransferir(new CommandContext(juego),
                ComandoTransferir.Operacion.PEDIR, "Botiquin", null, "Ana");

        assertThrows(ComandoException.class, comando::ejecutar);
        assertEquals(1, aliado.getMochila().getObjetos().size());
    }

    @Test
    void cooperacionComparteMunicionFinitaSinClonarla() {
        Personaje receptor = TestFixtures.juegoBasico(TestFixtures.consola()).getJugador();
        receptor.setArmasEquipadas(List.of(new Arma(
                "Rifle", "", 2, 10, false, TipoMunicion.RIFLE, 6, 0)));
        Aliado donante = new Aliado("A", new Posicion(0, 1), new Mochila(3, 10), 2);
        Municion paquete = new Municion("Balas", 1, TipoMunicion.RIFLE, 6);
        donante.getMochila().guardar(paquete);

        assertTrue(new CooperacionInventario(1).compartirMunicion(donante, receptor));
        assertTrue(donante.getMochila().getObjetos().isEmpty());
        assertEquals(List.of(paquete), receptor.getMochila().getObjetos());
    }

    @Test
    void parserReconoceComandosDeRecargaEIntercambio() throws Exception {
        CommandParser parser = new CommandParser(new CommandContext(
                TestFixtures.juegoBasico(TestFixtures.consola())));

        assertInstanceOf(ComandoRecargar.class, parser.parse("recargar rifle tactico"));
        assertInstanceOf(ComandoTransferir.class, parser.parse("dar botiquin Ana"));
        assertInstanceOf(ComandoTransferir.class, parser.parse("pedir balas Ana"));
        assertInstanceOf(ComandoTransferir.class,
                parser.parse("intercambiar rifle botiquin Ana"));
        assertThrows(ComandoException.class, () -> parser.parse("dar botiquin"));
    }
}
