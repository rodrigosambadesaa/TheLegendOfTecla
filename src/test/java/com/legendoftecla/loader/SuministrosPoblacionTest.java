package com.legendoftecla.loader;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.engine.FabricaJuego;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.CuboAgua;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Granada;
import com.legendoftecla.model.items.Linterna;
import com.legendoftecla.model.items.Municion;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SuministrosPoblacionTest {
    @Test
    void cadaFamiliaDeObjetosCreceConLaCantidadDeEntidades() throws Exception {
        Juego pequeno = crear(4);
        Juego numeroso = crear(40);
        List<Class<? extends Objeto>> familias = List.of(
                Botiquin.class, ToritoRojo.class, Municion.class, Arma.class,
                Armadura.class, Explosivo.class, Granada.class, Binocular.class,
                CuboAgua.class, Linterna.class, Componente.class);

        for (Class<? extends Objeto> familia : familias) {
            assertTrue(contar(numeroso, familia) > contar(pequeno, familia),
                    () -> "No aumento la familia " + familia.getSimpleName());
        }
    }

    private Juego crear(int aliados) throws Exception {
        ConfiguracionPartida configuracion = new ConfiguracionPartida(
                "Tecla", "marine", "procedural", Dificultad.NORMAL,
                new DimensionesMapa(25, 25), null, false, 1);
        configuracion.setCantidadAliados(aliados);
        configuracion.setSeed(741);
        return FabricaJuego.crear(TestFixtures.consola(), configuracion);
    }

    private long contar(Juego juego, Class<? extends Objeto> familia) {
        return objetos(juego).stream().filter(objeto -> objeto.getClass().equals(familia)).count();
    }

    private List<Objeto> objetos(Juego juego) {
        List<Objeto> objetos = new ArrayList<>();
        for (int fila = 0; fila < juego.getMapa().getFilas(); fila++) {
            for (int columna = 0; columna < juego.getMapa().getColumnas(); columna++) {
                objetos.addAll(juego.getMapa().getCelda(new Posicion(fila, columna)).getObjetos());
            }
        }
        return objetos;
    }
}
