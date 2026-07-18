package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

/** Ajusta la energia inicial a la longitud real de los mapas extensos. */
final class EscaladorEnergiaMapa {
    private static final int PASOS_MINIMOS_PARA_ESCALAR = 24;
    private static final int PASOS_DE_RESERVA = 8;
    private static final double FRACCION_RUTA_JUGADOR = 0.70;
    private static final double FRACCION_RUTA_ALIADO = 0.70;

    private EscaladorEnergiaMapa() {
    }

    static void aplicar(Juego juego) {
        Mapa mapa = juego.getMapa();
        int pasosRutaPrincipal = distancia(mapa, mapa.getInicio(), mapa.getObjetivo());
        if (pasosRutaPrincipal < PASOS_MINIMOS_PARA_ESCALAR) {
            return;
        }

        Personaje jugador = juego.getJugador();
        int energiaJugador = energiaNecesaria(
                jugador, pasosRutaPrincipal, FRACCION_RUTA_JUGADOR);
        jugador.asegurarEnergiaMaxima(energiaJugador);

        int energiaAliadaMinima = Integer.MAX_VALUE;
        for (Aliado aliado : juego.getAliados()) {
            int hastaJugador = distancia(mapa, aliado.getPosicion(), jugador.getPosicion());
            int hastaSalida = distancia(mapa, aliado.getPosicion(), mapa.getObjetivo());
            int recorridoExigente = Math.max(pasosRutaPrincipal, Math.max(hastaJugador, hastaSalida));
            int energiaAliado = energiaNecesaria(aliado, recorridoExigente, FRACCION_RUTA_ALIADO);
            aliado.asegurarEnergiaMaxima(energiaAliado);
            energiaAliadaMinima = Math.min(energiaAliadaMinima, aliado.getEnergiaMaxima());
        }

        String mensaje = "Energia inicial adaptada al mapa: ruta minima=" + pasosRutaPrincipal
                + " pasos | jugador=" + jugador.getEnergiaMaxima();
        if (energiaAliadaMinima != Integer.MAX_VALUE) {
            mensaje += " | aliados desde=" + energiaAliadaMinima;
        }
        juego.getConsola().imprimirInfo(mensaje + ".");
    }

    private static int energiaNecesaria(Personaje personaje, int pasosRuta, double fraccion) {
        int pasosCubiertos = (int) Math.ceil(pasosRuta * fraccion) + PASOS_DE_RESERVA;
        return pasosCubiertos * personaje.estimarCosteMovimiento();
    }

    private static int distancia(Mapa mapa, Posicion origen, Posicion destino) {
        if (origen.equals(destino)) {
            return 0;
        }
        ArrayDeque<Posicion> pendientes = new ArrayDeque<>();
        Map<Posicion, Integer> distancias = new HashMap<>();
        pendientes.add(origen);
        distancias.put(origen, 0);
        while (!pendientes.isEmpty()) {
            Posicion actual = pendientes.removeFirst();
            int distanciaActual = distancias.get(actual);
            for (Direccion direccion : Direccion.values()) {
                Posicion siguiente = actual.mover(direccion);
                if (!mapa.esTransitable(siguiente) || distancias.containsKey(siguiente)) {
                    continue;
                }
                if (siguiente.equals(destino)) {
                    return distanciaActual + 1;
                }
                distancias.put(siguiente, distanciaActual + 1);
                pendientes.addLast(siguiente);
            }
        }
        return -1;
    }
}
