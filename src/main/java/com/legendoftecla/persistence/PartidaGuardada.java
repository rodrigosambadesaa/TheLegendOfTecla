package com.legendoftecla.persistence;

import com.legendoftecla.effects.EstadoActivo;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.stats.EstadisticasPartida;

import java.util.List;
import java.util.Set;

/** DTO versionado de una partida completa, distinto de escenario.json. */
public record PartidaGuardada(int version, long seed, int turnos, int pasosMaximos,
        MapaEstado mapa, PersonajeEstado jugador, List<PersonajeEstado> aliados,
        List<PersonajeEstado> enemigos, Set<Posicion> inspeccionadas,
        String misionId, int puntuacion, EstadisticasPartida.Snapshot estadisticas,
        Set<String> logros, MisionEstado mision, Boolean mejorasEquipoAliado,
        Boolean municionAliadaAutomatica) {
    public static final int VERSION_ACTUAL = 1;

    public record MapaEstado(String nombre, String descripcion, int filas, int columnas,
            Posicion inicio, Posicion objetivo, List<CeldaEstado> celdas) { }
    public record CeldaEstado(int fila, int columna, String descripcion,
            boolean transitable, boolean oscura, boolean oscuridadPermanente,
            boolean madera, boolean antorcha, boolean fuente, int fuego,
            List<ObjetoEstado> objetos, List<ElementoEstado> elementos) { }
    public record PersonajeEstado(String tipo, String nombre, int salud,
            int saludMaxima, int energia, int energiaMaxima, int vision,
            Posicion posicion, int capacidad, double pesoMax,
            List<ObjetoEstado> inventario, List<ObjetoEstado> armas,
            ObjetoEstado armadura, List<EstadoActivo> estados,
            int nivel, int experiencia, Set<String> habilidades, String rolAliado) { }
    public record ObjetoEstado(String tipo, String nombre, String descripcion,
            double peso, int valor, int valor2, int valor3, boolean bandera,
            String subtipo, String faccion, String categoria, String claseArma,
            int penetracionArmadura) { }
    /** Estado generico y versionable de elementos interactivos del mapa. */
    public record ElementoEstado(String tipo, String id, String estado,
            String referencia, int resistencia, boolean destructible,
            int valor1, int valor2, int valor3, int valor4,
            boolean bandera1, boolean bandera2, List<ObjetoEstado> objetos) { }
    /** Definicion completa de mision y objetivos de una partida en curso. */
    public record MisionEstado(String id, String nombre, ObjetivoEstado principal,
            List<ObjetivoEstado> secundarios, List<String> recompensas) { }
    public record ObjetivoEstado(String tipo, String argumento, int valor,
            Posicion posicion, ObjetivoEstado anidado) { }
}
