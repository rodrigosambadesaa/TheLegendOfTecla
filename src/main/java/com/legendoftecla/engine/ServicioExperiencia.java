package com.legendoftecla.engine;

import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.persistence.PersistenciaProgresoLocal;
import com.legendoftecla.progression.ProgresionPersonaje;
import com.legendoftecla.validation.Limites;

/** Experiencia obtenida por acciones reales; no por intentos fallidos ni bajas repetidas. */
public final class ServicioExperiencia {
    private static final int NIVEL_MAXIMO = Limites.NIVEL_ALIADO_MAXIMO;
    private ServicioExperiencia() { }

    /** Una baja atribuye XP al autor y una pequena bonificacion al equipo cercano. */
    public static void recompensarBaja(Juego juego, Personaje atacante, Personaje objetivo) {
        if (!(objetivo instanceof Enemigo) || objetivo.getSalud() > 0
                || !(atacante instanceof Jugador || atacante instanceof Aliado)) return;
        int experiencia = 40 + Math.min(40, objetivo.getSaludMaxima() / 8);
        conceder(juego, atacante, experiencia);
        if (juego.getJugador() != atacante && juego.getJugador().getSalud() > 0
                && cerca(atacante, juego.getJugador())) {
            conceder(juego, juego.getJugador(), Math.max(8, experiencia / 4));
        }
        for (Aliado aliado : juego.getAliados()) {
            if (aliado != atacante && aliado.getSalud() > 0 && cerca(atacante, aliado)) {
                conceder(juego, aliado, Math.max(8, experiencia / 4));
            }
        }
        PersistenciaProgresoLocal.guardarSiActivado(juego);
    }

    /** Premio de objetivo completado, una sola vez al finalizar el motor. */
    public static void recompensarVictoria(Juego juego) {
        if (juego.getJugador().getSalud() > 0) conceder(juego, juego.getJugador(), 100);
        for (Aliado aliado : juego.getAliadosRegistrados()) {
            if (aliado.getSalud() > 0) conceder(juego, aliado, 60);
        }
        PersistenciaProgresoLocal.guardarSiActivado(juego);
    }

    private static boolean cerca(Personaje origen, Personaje otro) {
        return origen.getPosicion().distanciaManhattan(otro.getPosicion()) <= 6;
    }

    private static void conceder(Juego juego, Personaje personaje, int cantidad) {
        int subidas = ganarExperiencia(personaje, cantidad);
        if (subidas > 0) {
            juego.getConsola().imprimirExito(personaje.getNombre() + " sube al nivel "
                    + nivel(personaje) + " (" + subidas + " niveles).");
        }
    }

    /** Sube de nivel sin curar muertos ni superar el limite jugable de 100. */
    public static int ganarExperiencia(Personaje personaje, int cantidad) {
        if (cantidad < 0) throw new IllegalArgumentException("XP negativa");
        ProgresionPersonaje progreso = progreso(personaje);
        if (progreso == null || progreso.getNivel() >= NIVEL_MAXIMO) return 0;
        int anterior = progreso.getNivel();
        progreso.ganarExperiencia(cantidad);
        int nuevo = Math.min(NIVEL_MAXIMO, progreso.getNivel());
        if (progreso.getNivel() > NIVEL_MAXIMO || nuevo == NIVEL_MAXIMO) {
            progreso.restaurar(NIVEL_MAXIMO, 0, progreso.getDesbloqueadas());
        }
        int subidas = nuevo - anterior;
        mejorarEstadisticas(personaje, subidas);
        return subidas;
    }

    private static ProgresionPersonaje progreso(Personaje personaje) {
        if (personaje instanceof Jugador jugador) return jugador.getProgresion();
        if (personaje instanceof Aliado aliado) return aliado.getProgresion();
        return null;
    }

    private static int nivel(Personaje personaje) {
        return progreso(personaje).getNivel();
    }

    private static void mejorarEstadisticas(Personaje personaje, int subidas) {
        if (subidas <= 0) return;
        boolean vivo = personaje.getSalud() > 0;
        int bonoVida = subidas * (personaje instanceof Jugador ? 3 : 8);
        int bonoEnergia = subidas * (personaje instanceof Jugador ? 4 : 10);
        int saludActual = personaje.getSalud();
        int energiaActual = personaje.getEnergia();
        personaje.setSaludMaxima((int) Math.min(Limites.ESTADISTICA,
                (long) personaje.getSaludMaxima() + bonoVida));
        personaje.setEnergiaMaxima((int) Math.min(Limites.ESTADISTICA,
                (long) personaje.getEnergiaMaxima() + bonoEnergia));
        if (vivo) personaje.setSalud(saludActual + bonoVida);
        personaje.setEnergia(energiaActual + bonoEnergia);
    }

    /** Reequilibra el nivel inicial sin modificar los combates ya comenzados. */
    public static void ajustarNivelInicial(Aliado aliado, int nivel) {
        int objetivo = Math.max(1, Math.min(NIVEL_MAXIMO, nivel));
        int diferencia = objetivo - aliado.getNivel();
        if (diferencia == 0) return;
        int saludActual = aliado.getSalud();
        int energiaActual = aliado.getEnergia();
        int saludMaxima = (int) Math.max(1, Math.min(Limites.ESTADISTICA,
                (long) aliado.getSaludMaxima() + diferencia * 8L));
        int energiaMaxima = (int) Math.max(1, Math.min(Limites.ESTADISTICA,
                (long) aliado.getEnergiaMaxima() + diferencia * 10L));
        aliado.setSaludMaxima(saludMaxima);
        aliado.setEnergiaMaxima(energiaMaxima);
        aliado.setSalud(Math.max(1, Math.min(saludMaxima, saludActual + diferencia * 8)));
        aliado.setEnergia(Math.max(1, Math.min(energiaMaxima, energiaActual + diferencia * 10)));
        aliado.setNivel(objetivo);
    }
}
