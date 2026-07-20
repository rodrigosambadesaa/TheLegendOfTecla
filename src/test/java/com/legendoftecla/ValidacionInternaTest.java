package com.legendoftecla;

import com.legendoftecla.config.OpcionesInicio;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.loader.EscenarioDefinicion;
import com.legendoftecla.loader.CargadorJuegoBase;
import com.legendoftecla.loader.CargadorJuegoDeFicheros;
import com.legendoftecla.loader.CargadorJuegoGrandeConAliados;
import com.legendoftecla.loader.CargadorJuegoJson;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.characters.Marine;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.model.world.SistemaPuntuacion;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidacionInternaTest {
    @Test
    void elModeloExponeGetterYSetterParaCadaAtributoDeEstado() {
        List<Class<?>> clases = List.of(
                Personaje.class, Jugador.class, Mochila.class,
                Objeto.class, Arma.class, Armadura.class, Binocular.class,
                Botiquin.class, ToritoRojo.class, Posicion.class, Celda.class,
                Mapa.class, Juego.class, DimensionesMapa.class,
                SistemaPuntuacion.ResultadoPuntuacion.class,
                ConfiguracionPartida.class, OpcionesInicio.class,
                com.legendoftecla.engine.MotorPartida.class,
                CargadorJuegoBase.class, CargadorJuegoDeFicheros.class,
                CargadorJuegoGrandeConAliados.class, CargadorJuegoJson.class,
                EscenarioDefinicion.class, EscenarioDefinicion.Punto.class,
                EscenarioDefinicion.CeldaDef.class,
                EscenarioDefinicion.PersonajeDef.class,
                EscenarioDefinicion.ObjetoDef.class,
                com.legendoftecla.commands.CommandContext.class,
                com.legendoftecla.commands.CommandParser.class,
                com.legendoftecla.commands.ComandoAtacar.class,
                com.legendoftecla.commands.ComandoAyuda.class,
                com.legendoftecla.commands.ComandoCoger.class,
                com.legendoftecla.commands.ComandoCompuesto.class,
                com.legendoftecla.commands.ComandoDesequipar.class,
                com.legendoftecla.commands.ComandoEquipar.class,
                com.legendoftecla.commands.ComandoInventario.class,
                com.legendoftecla.commands.ComandoLanzarExplosivo.class,
                com.legendoftecla.commands.ComandoMirar.class,
                com.legendoftecla.commands.ComandoMover.class,
                com.legendoftecla.commands.ComandoPedirAyuda.class,
                com.legendoftecla.commands.ComandoRecorrido.class,
                com.legendoftecla.commands.ComandoRepetido.class,
                com.legendoftecla.commands.ComandoSalir.class,
                com.legendoftecla.commands.ComandoTirar.class,
                com.legendoftecla.commands.ComandoUsar.class,
                com.legendoftecla.console.ConsolaNormal.class,
                com.legendoftecla.gui.ConsolaGrafica.class,
                com.legendoftecla.gui.ConsolaGrafica.Mensaje.class);

        for (Class<?> clase : clases) {
            for (Field atributo : clase.getDeclaredFields()) {
                if (Modifier.isStatic(atributo.getModifiers()) || atributo.isSynthetic()) {
                    continue;
                }
                String sufijo = Character.toUpperCase(atributo.getName().charAt(0))
                        + atributo.getName().substring(1);
                String getter = atributo.getType() == boolean.class ? "is" + sufijo : "get" + sufijo;
                assertTrue(tieneMetodo(clase, getter),
                        () -> clase.getSimpleName() + "." + atributo.getName() + " no tiene getter");
                assertTrue(tieneMetodo(clase, "set" + sufijo, atributo.getType()),
                        () -> clase.getSimpleName() + "." + atributo.getName() + " no tiene setter");
            }
        }
    }

    @Test
    void losSettersDelPersonajeAcotanEstadoSinValidacionesExternas() {
        Marine marine = new Marine("Tecla", new Posicion(0, 0), new Mochila(4, 20), 2);

        marine.setSalud(-500);
        assertEquals(0, marine.getSalud());
        marine.setSalud(Integer.MAX_VALUE);
        assertEquals(marine.getSaludMaxima(), marine.getSalud());
        marine.setEnergia(-500);
        assertEquals(0, marine.getEnergia());
        marine.setEnergia(Integer.MAX_VALUE);
        assertEquals(marine.getEnergiaMaxima(), marine.getEnergia());

        assertThrows(IllegalArgumentException.class, () -> marine.setSaludMaxima(0));
        assertThrows(IllegalArgumentException.class, () -> marine.setEnergiaMaxima(0));
        assertThrows(IllegalArgumentException.class, () -> marine.setVisionBase(0));
        assertThrows(IllegalArgumentException.class, () -> marine.recibirDanio(-1));
        assertThrows(IllegalArgumentException.class, () -> marine.recuperarEnergia(-1));
    }

    @Test
    void objetosMochilaMapaConfiguracionYJsonSeProtegenPorSiMismos() {
        assertThrows(IllegalArgumentException.class,
                () -> new Botiquin("", "cura", 1, 20));
        assertThrows(IllegalArgumentException.class,
                () -> new Arma("arma", "", -1, 10, false));
        assertThrows(IllegalArgumentException.class,
                () -> new DimensionesMapa(2, 10));

        Mochila mochila = new Mochila(2, 5);
        assertThrows(IllegalArgumentException.class, () -> mochila.setCapacidadMax(0));
        assertThrows(IllegalArgumentException.class, () -> mochila.setPesoMax(Double.NaN));
        assertThrows(UnsupportedOperationException.class,
                () -> mochila.getObjetos().add(new ToritoRojo("t", "e", 1, 10)));

        Mapa mapa = new Mapa("M", "D", 3, 3, new Posicion(0, 0), new Posicion(2, 2));
        assertThrows(IllegalArgumentException.class, () -> mapa.setCelda(3, 0, new Celda("C", true)));
        assertThrows(NullPointerException.class, () -> mapa.setCelda(0, 0, null));

        ConfiguracionPartida configuracion = new ConfiguracionPartida(
                "Tecla", "marine", "default", Dificultad.NORMAL,
                new DimensionesMapa(10, 10), null, false, 1);
        assertThrows(IllegalArgumentException.class, () -> configuracion.setVarianteMapa(51));
        assertThrows(IllegalArgumentException.class, () -> configuracion.setClase("mago"));

        EscenarioDefinicion escenario = new EscenarioDefinicion();
        assertThrows(IllegalArgumentException.class, () -> escenario.setFilas(500));
        assertThrows(UnsupportedOperationException.class,
                () -> escenario.getCeldas().add(new EscenarioDefinicion.CeldaDef()));
        assertDoesNotThrow(escenario::normalizar);
    }

    private static boolean tieneMetodo(Class<?> clase, String nombre, Class<?>... parametros) {
        try {
            Method metodo = clase.getMethod(nombre, parametros);
            return Modifier.isPublic(metodo.getModifiers());
        } catch (NoSuchMethodException e) {
            return false;
        }
    }
}
