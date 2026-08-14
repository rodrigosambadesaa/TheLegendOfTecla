package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.commands.CommandParser;
import com.legendoftecla.commands.ComandoReagrupar;
import com.legendoftecla.constants.FormacionAliada;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormacionesAliadasTest {
    @Test
    void elAliadoReservaElBinocularHastaQueRevelaUnaAmenazaNueva() throws Exception {
        Escenario escenario = crearEscenario(new Posicion(2, 2));
        Binocular binocular = new Binocular("visor tactico", "Un uso", 1, 2);
        escenario.aliado().equipar(binocular);
        MotorPartida motor = new MotorPartida(escenario.juego());
        motor.setRandom(new java.util.Random(0));

        motor.ejecutarComando("mirar");
        assertEquals(binocular, escenario.aliado().getBinocularEquipado());

        Sectoid enemigo = new Sectoid("Explorador", new Posicion(1, 6), new Mochila(2, 10), 2);
        agregarEnemigo(escenario.juego(), enemigo);
        motor.ejecutarComando("mirar");

        assertEquals(null, escenario.aliado().getBinocularEquipado());
        assertEquals(2, escenario.aliado().getVisionTemporal());
        assertTrue(escenario.consola().salida().contains("permite detectar una amenaza nueva"));
    }

    @Test
    void ambasOrdenesSonComandosValidosYRechazanUnaFormacionDesconocida() throws Exception {
        Escenario escenario = crearEscenario(new Posicion(1, 0));
        CommandParser parser = new CommandParser(new CommandContext(escenario.juego()));

        assertInstanceOf(ComandoReagrupar.class, parser.parse("reagrupar defensiva"));
        assertInstanceOf(ComandoReagrupar.class, parser.parse("formacion ofensiva"));
        assertInstanceOf(ComandoReagrupar.class, parser.parse("romper formacion"));
        assertThrows(Exception.class, () -> parser.parse("reagrupar dispersa"));
    }

    @Test
    void romperFormacionDesactivaLaEstrategiaOfensivaODefensiva() {
        for (String orden : new String[] {"reagrupar defensiva", "reagrupar ofensiva"}) {
            Escenario escenario = crearEscenario(new Posicion(1, 0));
            MotorPartida motor = new MotorPartida(escenario.juego());

            motor.ejecutarComando(orden);
            assertTrue(escenario.juego().getFormacionAliada() != FormacionAliada.SIN_FORMACION);
            motor.ejecutarComando("romper formacion");

            assertEquals(FormacionAliada.SIN_FORMACION,
                    escenario.juego().getFormacionAliada());
            assertTrue(escenario.consola().salida().contains("Formacion rota"));
        }
    }

    @Test
    void laFormacionDefensivaHaceQueLosAliadosAcompanenAlJugador() throws Exception {
        Escenario escenario = crearEscenario(new Posicion(4, 0));
        escenario.juego().getJugador().getMochila().guardar(
                new Botiquin("botiquin reserva", "Reserva", 1, 10));
        escenario.juego().getJugador().getMochila().guardar(
                new ToritoRojo("torito reserva", "Reserva", 1, 10));
        MotorPartida motor = new MotorPartida(escenario.juego());
        int distanciaInicial = distanciaAlJugador(escenario);

        motor.ejecutarComando("reagrupar defensiva");
        motor.ejecutarComando("mirar");
        motor.ejecutarComando("mirar");

        assertEquals(FormacionAliada.DEFENSIVA, escenario.juego().getFormacionAliada());
        assertTrue(distanciaAlJugador(escenario) < distanciaInicial);
        assertTrue(distanciaAlJugador(escenario) <= 1);
    }

    @Test
    void elAliadoEnMejorEstadoBuscaSuministrosSinDispersarAlGrupo() throws Exception {
        Escenario escenario = crearEscenario(new Posicion(1, 0));
        Aliado fatigado = new Aliado("Fatigado", new Posicion(0, 1), new Mochila(8, 40), 3);
        fatigado.recibirDanio(45);
        fatigado.gastarEnergia(50);
        agregarAliado(escenario.juego(), fatigado);
        MotorPartida motor = new MotorPartida(escenario.juego());

        motor.ejecutarComando("reagrupar defensiva");

        assertTrue(escenario.consola().salida().contains(
                escenario.aliado().getNombre() + " explora suministros"));
        assertFalse(escenario.consola().salida().contains("Fatigado explora suministros"));
        assertTrue(distanciaAlJugador(escenario) <= 3);
        assertTrue(fatigado.getPosicion().distanciaManhattan(
                escenario.juego().getJugador().getPosicion()) <= 1);
    }

    @Test
    void losEnemigosDetectanLaFormacionOfensivaYContienenAlAliadoDeVanguardia() {
        Escenario escenario = crearEscenario(new Posicion(1, 0));
        Sectoid enemigo = new Sectoid("Centinela", new Posicion(1, 3), new Mochila(2, 10), 3);
        agregarEnemigo(escenario.juego(), enemigo);
        MotorPartida motor = new MotorPartida(escenario.juego());
        motor.setRandom(new java.util.Random(0));
        int saludAliado = escenario.aliado().getSalud();

        motor.ejecutarComando("reagrupar ofensiva");

        assertTrue(escenario.consola().salida().contains("detecta la formacion ofensiva"));
        assertTrue(escenario.aliado().getSalud() < saludAliado);
        assertTrue(escenario.aliado().getPosicion().distanciaManhattan(
                escenario.juego().getJugador().getPosicion()) <= 2);
    }

    @Test
    void ambasFormacionesMantienenElAtaqueContraAmenazasDelAliadoYDelJugador() {
        for (String orden : new String[] {"reagrupar defensiva", "reagrupar ofensiva"}) {
            Escenario escenario = crearEscenario(new Posicion(0, 1));
            escenario.juego().getJugador().getMochila().guardar(
                    new Botiquin("reserva", "Evita desvio por suministros", 1, 10));
            escenario.juego().getJugador().getMochila().guardar(
                    new ToritoRojo("energia", "Evita desvio por suministros", 1, 10));
            Sectoid enemigo = new Sectoid("Amenaza", new Posicion(0, 0), new Mochila(2, 10), 1);
            agregarEnemigo(escenario.juego(), enemigo);
            int saludInicial = enemigo.getSalud();
            MotorPartida motor = new MotorPartida(escenario.juego());
            motor.setRandom(new java.util.Random(4));

            motor.ejecutarComando(orden);

            assertTrue(enemigo.getSalud() < saludInicial, orden);
            assertTrue(escenario.consola().salida().contains(
                    "Vanguardia ataca a Amenaza: quita"), orden);
            assertTrue(escenario.consola().salida().contains("de vida; quedan"), orden);
        }
    }

    @Test
    void unAliadoAdyacenteSaleAunqueElJugadorDescanseEnLaEstrella() throws Exception {
        for (FormacionAliada formacion : FormacionAliada.values()) {
            Escenario escenario = crearEscenario(new Posicion(6, 5));
            escenario.juego().getJugador().setPosicion(escenario.juego().getMapa().getObjetivo());
            escenario.juego().getJugador().gastarEnergia(10);
            MotorPartida motor = new MotorPartida(escenario.juego());
            activarConductaAliada(motor, formacion);

            motor.ejecutarComando("descansar");

            assertTrue(escenario.juego().estaAliadoExtraido(escenario.aliado()), formacion.name());
            assertTrue(escenario.juego().jugadorGano(), formacion.name());
            assertTrue(escenario.consola().salida().contains(
                    "prioriza la salida y alcanza la casilla final"), formacion.name());
        }
    }

    @Test
    void unAliadoAdyacenteSaleAunqueElJugadorTodaviaNoHayaLlegado() {
        for (FormacionAliada formacion : FormacionAliada.values()) {
            Escenario escenario = crearEscenario(new Posicion(6, 5));
            MotorPartida motor = new MotorPartida(escenario.juego());
            activarConductaAliada(motor, formacion);

            motor.ejecutarComando("mirar");

            assertTrue(escenario.juego().estaAliadoExtraido(escenario.aliado()), formacion.name());
            assertFalse(escenario.juego().jugadorGano(), formacion.name());
        }
    }

    @Test
    void variosPersonajesPuedenCompartirCeldaDuranteElMovimiento() throws Exception {
        Escenario escenario = crearEscenario(new Posicion(0, 2));
        escenario.juego().getJugador().getMochila().guardar(
                new Botiquin("botiquin reserva", "Evita buscar suministros", 1, 10));
        escenario.juego().getJugador().getMochila().guardar(
                new ToritoRojo("energia reserva", "Evita buscar suministros", 1, 10));
        Aliado segundo = new Aliado("Retaguardia", new Posicion(1, 1), new Mochila(8, 40), 3);
        agregarAliado(escenario.juego(), segundo);
        MotorPartida motor = new MotorPartida(escenario.juego());

        motor.ejecutarComando("reagrupar defensiva");

        Posicion compartida = new Posicion(0, 1);
        assertEquals(compartida, escenario.aliado().getPosicion());
        assertEquals(compartida, segundo.getPosicion());
        escenario.juego().getJugador().setPosicion(compartida);
        agregarEnemigo(escenario.juego(),
                new Sectoid("Intruso", compartida, new Mochila(2, 10), 1));

        assertEquals(compartida, escenario.juego().getJugador().getPosicion());
        assertEquals(2, escenario.juego().getMapa().getCelda(compartida).getAliados().size());
        assertEquals(1, escenario.juego().getMapa().getCelda(compartida).getEnemigos().size());
    }

    private void activarConductaAliada(MotorPartida motor, FormacionAliada formacion) {
        if (formacion == FormacionAliada.SIN_FORMACION) {
            motor.setTurnosAyudaAliados(3);
        } else {
            motor.getJuego().setFormacionAliada(formacion);
        }
    }

    private Escenario crearEscenario(Posicion posicionAliado) {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Mapa mapa = new Mapa("Formaciones", "Prueba tactica", 7, 7,
                new Posicion(0, 0), new Posicion(6, 6));
        for (int fila = 0; fila < 7; fila++) {
            for (int columna = 0; columna < 7; columna++) {
                mapa.setCelda(fila, columna, new Celda("Celda", true));
            }
        }
        Marine jugador = new Marine("Jugador", new Posicion(0, 0), new Mochila(8, 40), 3);
        Juego juego = new Juego(consola, mapa, jugador, 100);
        Aliado aliado = new Aliado("Vanguardia", posicionAliado, new Mochila(8, 40), 3);
        agregarAliado(juego, aliado);
        return new Escenario(juego, aliado, consola);
    }

    private void agregarAliado(Juego juego, Aliado aliado) {
        juego.agregarAliado(aliado);
        juego.getMapa().getCelda(aliado.getPosicion()).agregarAliado(aliado);
    }

    private void agregarEnemigo(Juego juego, Sectoid enemigo) {
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(enemigo.getPosicion()).agregarEnemigo(enemigo);
    }

    private int distanciaAlJugador(Escenario escenario) {
        return escenario.aliado().getPosicion().distanciaManhattan(
                escenario.juego().getJugador().getPosicion());
    }

    private record Escenario(Juego juego, Aliado aliado, TestFixtures.CapturingConsole consola) {
    }
}
