package com.legendoftecla.engine;

import com.legendoftecla.TestFixtures;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Mochila;
import com.legendoftecla.model.items.Componente;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RegistroAliadosConsistenciaTest {
    @Test
    void evacuadoCaidoNoSeRestaDosVecesEnConteoDeActivos() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Aliado evacuado = aliado("Evacuado");
        Aliado activo = aliado("Activo");
        juego.agregarAliado(evacuado);
        juego.agregarAliado(activo);
        juego.setAliadosExtraidosDetalle(List.of(evacuado));
        evacuado.setSalud(0);
        RegistroEstadoAliados registro = new RegistroEstadoAliados();
        registro.inicializar(juego.getAliadosRegistrados());

        String resumen = registro.resumen(juego);

        assertTrue(resumen.contains("ALIADOS 2 | activos=1"), resumen);
        assertTrue(resumen.contains("evacuados=1 | caidos=0"), resumen);
        assertTrue(resumen.contains("Evacuado | Estado EVACUADO"), resumen);

        activo.setSalud(0);
        String sinActivos = registro.resumen(juego);
        assertTrue(sinActivos.contains("ALIADOS 2 | activos=0"), sinActivos);
        assertTrue(sinActivos.contains("evacuados=1 | caidos=1"), sinActivos);
    }

    @Test
    void inventarioExtensoSePresentaCompletoYConOrdenOriginal() {
        Juego juego = TestFixtures.juegoBasico(TestFixtures.consola());
        Mochila mochila = new Mochila(3_000, 3_000);
        List<Objeto> objetos = new ArrayList<>();
        StringBuilder esperado = new StringBuilder("Objetos: ");
        for (int i = 0; i < 3_000; i++) {
            String nombre = "Recurso" + i;
            objetos.add(new Componente(nombre, "Material", 0.1));
            if (i != 0) esperado.append(", ");
            esperado.append(nombre);
        }
        mochila.setObjetos(objetos);
        Aliado aliado = new Aliado("Inventario", new Posicion(1, 1), mochila, 2);
        juego.agregarAliado(aliado);
        RegistroEstadoAliados registro = new RegistroEstadoAliados();
        registro.inicializar(juego.getAliadosRegistrados());

        String informe = registro.resumen(juego);

        assertTrue(informe.contains(esperado.toString()));
        assertTrue(informe.contains("ALIADOS 1 | activos=1"));
    }

    private static Aliado aliado(String nombre) {
        return new Aliado(nombre, new Posicion(1, 1), new Mochila(2, 10), 2);
    }
}
