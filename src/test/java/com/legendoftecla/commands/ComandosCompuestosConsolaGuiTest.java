package com.legendoftecla.commands;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.gui.ConsolaGrafica;
import com.legendoftecla.gui.PanelJuego;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Sectoid;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ComandosCompuestosConsolaGuiTest {

    @Test
    void elParserConstruyeRepetidosCompuestosYRechazaRepeticionesInvalidas() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = juegoAbierto(consola);
        CommandParser parser = new CommandParser(new CommandContext(juego));

        assertInstanceOf(ComandoRepetido.class, parser.parse("mover norte 3"));
        assertInstanceOf(ComandoRepetido.class, parser.parse("atacar 2e 3"));
        assertInstanceOf(ComandoRepetido.class, parser.parse("atacar 2e alien azul 3"));
        assertThrows(Exception.class, () -> parser.parse("mover norte 0"));
        assertThrows(Exception.class, () -> parser.parse("mover norte 1001"));
        assertThrows(Exception.class, () -> parser.parse("mover norte 2 extra"));

        Arma anterior = new Arma("ametralladora", "Anterior", 2, 8, true);
        Arma nueva = new Arma("lanzacohetes", "Nueva", 2, 20, true);
        juego.getJugador().equipar(anterior);
        juego.getJugador().getMochila().guardar(nueva);

        Comando compuesto = parser.parse("equipar lanzacohetes ametralladora");

        assertInstanceOf(ComandoCompuesto.class, compuesto);
        assertEquals(2, ((ComandoCompuesto) compuesto).getComandos().size());
    }

    @Test
    void laConsolaRepiteMovimientosYAtaquesATodaLaCelda() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = juegoAbierto(consola);
        MotorPartida motor = new MotorPartida(juego);

        motor.ejecutarComando("mover norte 2");

        assertEquals(new Posicion(0, 2), juego.getJugador().getPosicion());
        assertEquals(2, juego.getPasos());

        juego.getJugador().setPosicion(new Posicion(2, 2));
        Sectoid primero = agregarEnemigo(juego, "alien azul", new Posicion(2, 4));
        Sectoid segundo = agregarEnemigo(juego, "alien rojo", new Posicion(2, 4));
        int saludPrimero = primero.getSalud();
        int saludSegundo = segundo.getSalud();

        motor.ejecutarComando("atacar 2e alien azul 2");

        assertTrue(primero.getSalud() < saludPrimero);
        assertTrue(segundo.getSalud() < saludSegundo,
                "La ampliacion actual ataca a todos aunque se nombre uno de la celda");
        assertEquals(saludPrimero - primero.getSalud(), saludSegundo - segundo.getSalud());
        assertEquals(2, contar(consola.salida(), "Atacas a todos los enemigos"));
    }

    @Test
    void elMovimientoRepetidoConservaLosPasosCompletadosAntesDeUnBloqueo() {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = juegoAbierto(consola);
        juego.getJugador().setPosicion(new Posicion(1, 1));
        MotorPartida motor = new MotorPartida(juego);

        motor.ejecutarComando("mover norte 3");

        assertEquals(new Posicion(0, 1), juego.getJugador().getPosicion());
        assertEquals(1, juego.getPasos());
        assertTrue(consola.salida().contains("No puedes moverte a NORTE"));
    }

    @Test
    void laConsolaEjecutaLaSustitucionCompuestaDeEquipo() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = juegoAbierto(consola);
        Arma anterior = new Arma("ametralladora pesada", "Anterior", 2, 8, true);
        Arma nueva = new Arma("lanzacohetes pesado", "Nueva", 2, 20, true);
        juego.getJugador().equipar(anterior);
        juego.getJugador().getMochila().guardar(nueva);
        MotorPartida motor = new MotorPartida(juego);

        motor.ejecutarComando("equipar lanzacohetes pesado ametralladora pesada");

        assertTrue(juego.getJugador().getArmasEquipadas().contains(nueva));
        assertFalse(juego.getJugador().getArmasEquipadas().contains(anterior));
        assertTrue(juego.getJugador().getMochila().getObjetos().contains(anterior));
        assertTrue(consola.salida().contains("Equipado: lanzacohetes pesado"));
        assertTrue(consola.salida().contains("Desequipado: ametralladora pesada"));
    }

    @Test
    void laSustitucionCompuestaSirveTambienParaArmaduraYBinocular() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = juegoAbierto(consola);
        Armadura armaduraVieja = new Armadura("chaleco viejo", "Viejo", 2, 1, 2, 2);
        Armadura armaduraNueva = new Armadura("chaleco nuevo", "Nuevo", 2, 4, 10, 8);
        Binocular binocularViejo = new Binocular("visor viejo", "Viejo", 1, 1);
        Binocular binocularNuevo = new Binocular("visor nuevo", "Nuevo", 1, 4);
        juego.getJugador().equipar(armaduraVieja);
        juego.getJugador().equipar(binocularViejo);
        juego.getJugador().getMochila().guardar(armaduraNueva);
        juego.getJugador().getMochila().guardar(binocularNuevo);
        MotorPartida motor = new MotorPartida(juego);

        motor.ejecutarComando("equipar chaleco nuevo chaleco viejo");
        motor.ejecutarComando("equipar visor nuevo visor viejo");

        assertEquals(armaduraNueva, juego.getJugador().getArmaduraEquipada());
        assertEquals(binocularNuevo, juego.getJugador().getBinocularEquipado());
        assertTrue(juego.getJugador().getMochila().getObjetos().contains(armaduraVieja));
        assertTrue(juego.getJugador().getMochila().getObjetos().contains(binocularViejo));
    }

    @Test
    void laEntradaGuiEjecutaMovimientoAtaqueYEquipoCompuestos() throws Exception {
        ConsolaGrafica consola = new ConsolaGrafica();
        Juego juego = juegoAbierto(consola);
        Arma anterior = new Arma("arma vieja", "Anterior", 2, 8, true);
        Arma nueva = new Arma("arma nueva", "Nueva", 2, 20, true);
        juego.getJugador().equipar(anterior);
        juego.getJugador().getMochila().guardar(nueva);
        MotorPartida motor = new MotorPartida(juego);
        PanelJuego[] panel = new PanelJuego[1];

        SwingUtilities.invokeAndWait(() -> panel[0] = new PanelJuego(motor, consola, () -> { }));
        ejecutarGui(panel[0], "mover norte 2");
        assertEquals(new Posicion(0, 2), juego.getJugador().getPosicion());

        juego.getJugador().setPosicion(new Posicion(2, 2));
        Sectoid primero = agregarEnemigo(juego, "gui uno", new Posicion(2, 4));
        Sectoid segundo = agregarEnemigo(juego, "gui dos", new Posicion(2, 4));
        int salud = primero.getSalud();
        ejecutarGui(panel[0], "atacar 2e todos 2");
        assertTrue(primero.getSalud() < salud);
        assertEquals(primero.getSalud(), segundo.getSalud());

        ejecutarGui(panel[0], "equipar arma nueva arma vieja");
        assertTrue(juego.getJugador().getArmasEquipadas().contains(nueva));
        assertFalse(juego.getJugador().getArmasEquipadas().contains(anterior));
    }

    @Test
    void mirarAdmiteObjetoAlcanceCompactoYDetalleDeEnemigo() throws Exception {
        TestFixtures.CapturingConsole consola = TestFixtures.consola();
        Juego juego = juegoAbierto(consola);
        juego.getMapa().getCelda(juego.getJugador().getPosicion()).agregarObjeto(
                new Arma("rifle de prueba", "Arma detallada", 2, 17, false));
        agregarEnemigo(juego, "objetivo remoto", new Posicion(2, 4));
        CommandParser parser = new CommandParser(new CommandContext(juego));

        parser.parse("mirar rifle de prueba").ejecutar();
        parser.parse("mirar 2e").ejecutar();
        parser.parse("mirar 2e objetivo remoto").ejecutar();

        assertTrue(consola.salida().contains("dano=17"));
        assertTrue(consola.salida().contains("Enemigos aqui: objetivo remoto"));
        assertTrue(consola.salida().contains("objetivo remoto"));
        assertThrows(Exception.class, () -> parser.parse("mirar 4e").ejecutar());
    }

    @Test
    void elMarinePagaLaPenalizacionDelPdfConDosArmasADosManos() throws Exception {
        Juego juego = juegoAbierto(TestFixtures.consola());
        Marine marine = (Marine) juego.getJugador();
        int costeBase = marine.estimarCosteMovimiento();
        marine.equipar(new Arma("pesada uno", "", 1, 10, true));
        marine.equipar(new Arma("pesada dos", "", 1, 10, true));

        assertEquals((int) Math.ceil(costeBase * 1.5), marine.estimarCosteMovimiento());
    }

    private Juego juegoAbierto(com.legendoftecla.console.Consola consola) {
        Mapa mapa = new Mapa("Compuestos", "Prueba", 6, 6,
                new Posicion(2, 2), new Posicion(5, 5));
        for (int fila = 0; fila < 6; fila++) {
            for (int columna = 0; columna < 6; columna++) {
                mapa.setCelda(fila, columna, new Celda("Celda " + fila + "," + columna, true));
            }
        }
        Marine jugador = new Marine("Tecla", new Posicion(2, 2), new Mochila(10, 40), 3);
        return new Juego(consola, mapa, jugador, 100);
    }

    private Sectoid agregarEnemigo(Juego juego, String nombre, Posicion posicion) {
        Sectoid enemigo = new Sectoid(nombre, posicion, new Mochila(2, 10), 1);
        juego.agregarEnemigo(enemigo);
        juego.getMapa().getCelda(posicion).agregarEnemigo(enemigo);
        return enemigo;
    }

    private void ejecutarGui(PanelJuego panel, String comando) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JTextField entrada = (JTextField) buscarPorNombre(panel, "comando.entrada");
            assertNotNull(entrada);
            entrada.setText(comando);
            entrada.postActionEvent();
        });
    }

    private Component buscarPorNombre(Container contenedor, String nombre) {
        for (Component componente : contenedor.getComponents()) {
            if (nombre.equals(componente.getName())) {
                return componente;
            }
            if (componente instanceof Container hijo) {
                Component encontrado = buscarPorNombre(hijo, nombre);
                if (encontrado != null) {
                    return encontrado;
                }
            }
        }
        return null;
    }

    private int contar(String texto, String fragmento) {
        return texto.split(java.util.regex.Pattern.quote(fragmento), -1).length - 1;
    }
}
