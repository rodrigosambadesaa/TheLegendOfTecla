package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.commands.ComandoAtacar;
import com.legendoftecla.commands.CommandContext;
import com.legendoftecla.constants.CondicionVictoria;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReglasCombateYVictoriaTest {
    @Test
    void atacarUnNombreGolpeaATodosLosEnemigosDeSuCelda() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        Posicion posicion = juego.getJugador().getPosicion();
        Enemigo primero = agregarEnemigo(juego, "Primero", posicion);
        Enemigo segundo = agregarEnemigo(juego, "Segundo", posicion);
        int saludPrimero = primero.getSalud();
        int saludSegundo = segundo.getSalud();

        new ComandoAtacar(new CommandContext(juego), null, "Primero").ejecutar();

        assertTrue(primero.getSalud() < saludPrimero);
        assertTrue(segundo.getSalud() < saludSegundo);
        assertEquals(saludPrimero - primero.getSalud(), saludSegundo - segundo.getSalud());
        assertTrue(consola.salida().contains("Atacas a todos los enemigos"));
    }

    @Test
    void laVictoriaSoloJugadorNoEsperaALosAliados() {
        Juego juego = crearJuegoConAliado(false);
        juego.setCondicionVictoria(CondicionVictoria.SOLO_JUGADOR);
        juego.getJugador().setPosicion(juego.getMapa().getObjetivo());

        assertTrue(juego.jugadorGano());
        assertEquals(0, juego.getAliadosExtraidos());
    }

    @Test
    void laVictoriaConTodosFuncionaSinImportarElOrdenDeLlegada() {
        Juego aliadoPrimero = crearJuegoConAliado(true);
        aliadoPrimero.setCondicionVictoria(CondicionVictoria.JUGADOR_Y_ALIADOS);
        Aliado aliadoExtraido = aliadoPrimero.getAliados().get(0);
        assertTrue(aliadoPrimero.extraerAliado(aliadoExtraido));
        assertFalse(aliadoPrimero.jugadorGano());
        aliadoPrimero.getJugador().setPosicion(aliadoPrimero.getMapa().getObjetivo());
        assertTrue(aliadoPrimero.jugadorGano());

        Juego jugadorPrimero = crearJuegoConAliado(false);
        jugadorPrimero.setCondicionVictoria(CondicionVictoria.JUGADOR_Y_ALIADOS);
        jugadorPrimero.getJugador().setPosicion(jugadorPrimero.getMapa().getObjetivo());
        assertFalse(jugadorPrimero.jugadorGano());
        Aliado ultimo = jugadorPrimero.getAliados().get(0);
        ultimo.setPosicion(jugadorPrimero.getMapa().getObjetivo());
        assertTrue(jugadorPrimero.extraerAliado(ultimo));
        assertTrue(jugadorPrimero.jugadorGano());
    }

    @Test
    void losAliadosRecogenYEquipanArmasYArmadurasConEfectoReal() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = crearJuegoConAliado(false);
        Aliado aliado = juego.getAliados().get(0);
        Posicion posicion = aliado.getPosicion();
        Arma rifle = new Arma("rifle aliado", "Rifle", 2.0, 20, false);
        Armadura chaleco = new Armadura("chaleco aliado", "Chaleco", 4.0, 4, 15, 10);
        juego.getMapa().getCelda(posicion).agregarObjeto(rifle);
        juego.getMapa().getCelda(posicion).agregarObjeto(chaleco);
        Enemigo enemigo = agregarEnemigo(juego, "Objetivo", new Posicion(2, 3));
        int saludEnemigo = enemigo.getSalud();

        MotorPartida motor = new MotorPartida(juego);
        motor.setRandom(new java.util.Random(0));
        motor.ejecutarComando("mirar");

        assertTrue(aliado.getArmasEquipadas().contains(rifle));
        assertEquals(chaleco, aliado.getArmaduraEquipada());
        assertEquals(28, saludEnemigo - enemigo.getSalud(),
                "El rifle debe aumentar el ataque cuerpo a cuerpo del aliado");
        int saludAntesDelGolpe = aliado.getSalud();
        aliado.recibirDanio(10);
        assertEquals(6, saludAntesDelGolpe - aliado.getSalud(),
                "La armadura debe mitigar el dano recibido");
        assertTrue(motor.getEstadoAliados().contains("arma rifle aliado"));
        assertTrue(motor.getEstadoAliados().contains("armadura chaleco aliado"));
    }

    @Test
    void losAliadosInspeccionanCadaCeldaYSustituyenYTiranEquipoEnEsaCelda() throws Exception {
        Juego juego = crearJuegoConAliado(false);
        TestFixtures.CapturingConsole consola = (TestFixtures.CapturingConsole) juego.getConsola();
        Aliado aliado = juego.getAliados().get(0);
        Posicion celdaInspeccionada = aliado.getPosicion();
        Arma armaAntigua = new Arma("arma antigua", "Debil", 2, 5, true);
        Armadura armaduraAntigua = new Armadura("armadura antigua", "Debil", 3, 1, 2, 2);
        Arma armaNueva = new Arma("arma nueva", "Potente", 2, 18, true);
        Armadura armaduraNueva = new Armadura("armadura nueva", "Resistente", 3, 5, 15, 10);
        Binocular binocularAntiguo = new Binocular("binocular antiguo", "Corto", 1, 1);
        Binocular binocular = new Binocular("binocular aliado", "Reutilizable", 1, 2);
        aliado.equipar(armaAntigua);
        aliado.equipar(armaduraAntigua);
        aliado.equipar(binocularAntiguo);
        Celda celda = juego.getMapa().getCelda(celdaInspeccionada);
        celda.agregarObjeto(armaNueva);
        celda.agregarObjeto(armaduraNueva);
        celda.agregarObjeto(binocular);
        ToritoRojo oculto = new ToritoRojo("torito oculto", "Sin descubrir", 1, 20);
        Posicion celdaOculta = new Posicion(4, 0);
        juego.getMapa().getCelda(celdaOculta).agregarObjeto(oculto);
        juego.getJugador().gastarEnergia(20);

        assertFalse(juego.isCeldaInspeccionada(aliado, celdaInspeccionada));
        MotorPartida motor = new MotorPartida(juego);
        motor.ejecutarComando("mirar");

        assertTrue(juego.isCeldaInspeccionada(aliado, celdaInspeccionada));
        assertFalse(juego.isCeldaInspeccionada(aliado, celdaOculta));
        assertTrue(aliado.getArmasEquipadas().contains(armaNueva));
        assertEquals(armaduraNueva, aliado.getArmaduraEquipada());
        assertEquals(binocular, aliado.getBinocularEquipado());
        motor.ejecutarComando("mirar");
        assertEquals(binocular, aliado.getBinocularEquipado());
        assertEquals(binocular.getRango(), aliado.getVisionTemporal());
        assertTrue(celda.getObjetos().contains(armaAntigua));
        assertTrue(celda.getObjetos().contains(armaduraAntigua));
        assertTrue(celda.getObjetos().contains(binocularAntiguo));
        assertTrue(juego.getMapa().getCelda(celdaOculta).getObjetos().contains(oculto));
        assertTrue(consola.salida().contains("inspecciona la celda"));
        assertTrue(consola.salida().contains("tira arma antigua en la celda " + celdaInspeccionada));
    }

    @Test
    void descansarRecuperaRecursosSinMoverAlJugadorYAtraeEnemigos() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = TestFixtures.juegoBasico(consola);
        juego.getJugador().recibirDanio(30);
        juego.getJugador().gastarEnergia(20);
        Enemigo enemigo = agregarEnemigo(juego, "Acechante", new Posicion(2, 2));
        Posicion posicionJugador = juego.getJugador().getPosicion();
        int saludAnterior = juego.getJugador().getSalud();
        int energiaAnterior = juego.getJugador().getEnergia();
        int distanciaAnterior = enemigo.getPosicion().distanciaManhattan(posicionJugador);
        MotorPartida motor = new MotorPartida(juego);
        motor.setRandom(new java.util.Random(0));

        motor.ejecutarComando("descansar");

        assertEquals(posicionJugador, juego.getJugador().getPosicion());
        assertTrue(juego.getJugador().getSalud() > saludAnterior);
        assertTrue(juego.getJugador().getEnergia() > energiaAnterior);
        assertTrue(enemigo.getPosicion().distanciaManhattan(posicionJugador) < distanciaAnterior);
        assertTrue(consola.salida().contains("Descansas sin moverte"));
        assertTrue(consola.salida().contains("se acerca mientras descansas"));
    }

    private Juego crearJuegoConAliado(boolean aliadoEnObjetivo) {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Mapa mapa = new Mapa("Aliados", "Prueba", 5, 5,
                new Posicion(0, 0), new Posicion(4, 4));
        for (int fila = 0; fila < 5; fila++) {
            for (int columna = 0; columna < 5; columna++) {
                mapa.setCelda(fila, columna, new Celda("Celda", true));
            }
        }
        Marine jugador = new Marine("Jugador", new Posicion(0, 0), new Mochila(5, 30), 3);
        Juego juego = new Juego(consola, mapa, jugador, 100);
        Posicion posicionAliado = aliadoEnObjetivo ? mapa.getObjetivo() : new Posicion(2, 2);
        Aliado aliado = new Aliado("Aliado", posicionAliado, new Mochila(8, 40), 3);
        mapa.getCelda(posicionAliado).agregarAliado(aliado);
        juego.agregarAliado(aliado);
        return juego;
    }

    private Enemigo agregarEnemigo(Juego juego, String nombre, Posicion posicion) {
        Enemigo enemigo = new Sectoid(nombre, posicion, new Mochila(2, 10), 3);
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(posicion).agregarEnemigo(enemigo);
        return enemigo;
    }
}
