package com.legendoftecla.engine;

import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.events.PersonajeMuerto;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.elements.SistemaCobertura;
import com.legendoftecla.model.elements.TipoCobertura;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.TipoMunicion;
import com.legendoftecla.model.world.Juego;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.Random;

/** Unifica daño, registro, sonido y efectos ambientales de todos los combatientes. */
public final class SistemaCombate {
    private SistemaCombate() { }

    public static ResultadoAtaque atacar(Juego juego, Personaje atacante, Personaje objetivo, Random random) {
        int distancia = atacante.getPosicion().distanciaManhattan(objetivo.getPosicion());
        Arma arma = atacante.armaDisponiblePara(distancia).orElse(null);
        exigirMunicion(atacante, objetivo);
        int vidaAntes = objetivo.getSalud();
        publicarAtaque(juego, atacante, objetivo);
        SistemaCobertura cobertura = new SistemaCobertura(random);
        SistemaCobertura.Proteccion proteccion = cobertura.proteccion(
                juego.getMapa(), atacante.getPosicion(), objetivo.getPosicion());
        if (proteccion.tipo() != TipoCobertura.NINGUNA) {
            double probabilidad = cobertura.probabilidadImpacto(
                    PrecisionTirador.calcular(atacante), proteccion.tipo(),
                    proteccion.flanqueada(), atacante.getEstados().multiplicadorPrecision());
            if (!cobertura.impacta(probabilidad)) {
                // El disparo se ha efectuado, aunque la cobertura lo detenga.
                // Al acertar, Personaje.atacar consume la municion habitual.
                if (arma != null) arma.consumirDisparo();
                ResultadoAtaque fallo = resultado(atacante, objetivo, vidaAntes);
                juego.getConsola().imprimir(atacante.getNombre() + " falla contra "
                        + objetivo.getNombre() + " por la cobertura.", TipoMensaje.INFO);
                return fallo;
            }
        }
        atacante.atacar(objetivo);
        aplicarEfectosDeArma(juego, atacante, objetivo, arma);
        ResultadoAtaque resultado = resultado(atacante, objetivo, vidaAntes);
        informar(juego, atacante, resultado, objetivo);
        SistemaIncendios.intentarDerribarAntorcha(juego, objetivo.getPosicion(), random);
        return resultado;
    }

    public static List<ResultadoAtaque> atacarTodos(Juego juego, Personaje atacante,
            List<? extends Personaje> objetivos, Random random) {
        // Validar toda la lista antes de publicar ataques o gastar cartuchos.
        java.util.Objects.requireNonNull(objetivos, "Objetivos");
        if (objetivos.isEmpty()) return List.of();
        validarObjetivosColectivos(objetivos);
        Arma arma = atacante.armaDisponiblePara(
                atacante.getPosicion().distanciaManhattan(
                        objetivos.get(0).getPosicion())).orElse(null);
        if (!objetivos.isEmpty()) {
            exigirMunicion(atacante, objetivos.get(0));
        }
        List<Integer> vidas = objetivos.stream().map(Personaje::getSalud).toList();
        objetivos.forEach(objetivo -> juego.publicarEvento(new PersonajeAtacado(
                juego.getBusEventos().ahora(), atacante.getNombre(), objetivo.getNombre(),
                atacante.getPosicion(), objetivo.getPosicion())));
        juego.publicarEvento(new RuidoGenerado(juego.getBusEventos().ahora(),
                atacante.getPosicion(), intensidadAtaque(atacante,
                        objetivos.isEmpty() ? null : objetivos.get(0)), "ataque"));
        atacante.atacar(objetivos);
        if (!objetivos.isEmpty()) {
            // El efecto estructural se aplica una sola vez a la casilla comun.
            aplicarEfectosDeArma(juego, atacante, objetivos.get(0), arma);
            // La supresion, en cambio, afecta a cada enemigo alcanzado.
            for (int i = 1; i < objetivos.size(); i++) {
                aplicarSupresion(atacante, objetivos.get(i));
            }
        }
        List<ResultadoAtaque> resultados = new ArrayList<>();
        for (int i = 0; i < objetivos.size(); i++) {
            Personaje objetivo = objetivos.get(i);
            ResultadoAtaque resultado = resultado(atacante, objetivo, vidas.get(i));
            resultados.add(resultado);
            informar(juego, atacante, resultado, objetivo);
        }
        if (!objetivos.isEmpty()) {
            SistemaIncendios.intentarDerribarAntorcha(juego, objetivos.get(0).getPosicion(), random);
        }
        return List.copyOf(resultados);
    }

    /**
     * Un ataque colectivo corresponde a personajes distintos en una misma casilla.
     * Las listas mal formadas no deben provocar danos/eventos duplicados.
     */
    private static void validarObjetivosColectivos(List<? extends Personaje> objetivos) {
        Set<Personaje> vistos = Collections.newSetFromMap(new IdentityHashMap<>());
        var posicion = java.util.Objects.requireNonNull(
                objetivos.get(0), "Objetivo").getPosicion();
        for (Personaje objetivo : objetivos) {
            java.util.Objects.requireNonNull(objetivo, "Objetivo");
            if (!vistos.add(objetivo)) {
                throw new IllegalArgumentException("Un objetivo colectivo aparece repetido.");
            }
            if (!objetivo.getPosicion().equals(posicion)) {
                throw new IllegalArgumentException(
                        "Los objetivos colectivos deben ocupar la misma casilla.");
            }
        }
    }

    private static ResultadoAtaque resultado(Personaje atacante, Personaje objetivo, int vidaAntes) {
        return new ResultadoAtaque(atacante.getNombre(), objetivo.getNombre(),
                Math.max(0, vidaAntes - objetivo.getSalud()), objetivo.getSalud(),
                objetivo.getSaludMaxima(), vidaAntes > 0 && objetivo.getSalud() <= 0);
    }

    private static void informar(Juego juego, Personaje atacante,
            ResultadoAtaque resultado, Personaje objetivo) {
        String texto = resultado.atacante() + " ataca a " + resultado.objetivo()
                + ": quita " + resultado.vidaQuitada() + " de vida; quedan "
                + resultado.vidaRestante() + "/" + resultado.vidaMaxima() + ".";
        juego.getConsola().imprimir(texto, resultado.mortal() ? TipoMensaje.EXITO : TipoMensaje.INFO);
        if (resultado.vidaQuitada() > 0) {
            juego.publicarEvento(new PersonajeDanado(juego.getBusEventos().ahora(),
                    objetivo.getNombre(), resultado.vidaQuitada(), objetivo.getPosicion()));
        }
        if (resultado.mortal()) {
            juego.publicarEvento(new PersonajeMuerto(juego.getBusEventos().ahora(),
                    objetivo.getNombre(), objetivo.getPosicion()));
            ServicioExperiencia.recompensarBaja(juego, atacante, objetivo);
            juego.getConsola().imprimir(resultado.objetivo() + " muere.", TipoMensaje.ADVERTENCIA);
        }
    }

    private static void publicarAtaque(Juego juego, Personaje atacante, Personaje objetivo) {
        juego.publicarEvento(new PersonajeAtacado(juego.getBusEventos().ahora(),
                atacante.getNombre(), objetivo.getNombre(), atacante.getPosicion(),
                objetivo.getPosicion()));
        juego.publicarEvento(new RuidoGenerado(juego.getBusEventos().ahora(),
                atacante.getPosicion(), intensidadAtaque(atacante, objetivo), "ataque"));
    }

    private static int intensidadAtaque(Personaje atacante, Personaje objetivo) {
        if (objetivo == null) return 2;
        int distancia = atacante.getPosicion().distanciaManhattan(objetivo.getPosicion());
        Arma arma = atacante.armaDisponiblePara(distancia).orElse(null);
        if (arma == null) return 2;
        if (atacante instanceof Jugador jugador
                && jugador.getProgresion().tiene(
                        com.legendoftecla.progression.CatalogoHabilidades.SILENCIADOR)) {
            return 2;
        }
        return switch (arma.getCategoria()) {
            case MELE -> 1;
            case ARROJADIZA, ARCO, BALLESTA -> 2;
            case FUEGO -> 9;
        };
    }

    private static void aplicarEfectosDeArma(Juego juego, Personaje atacante,
            Personaje objetivo, Arma arma) {
        if (arma != null && (arma.getTipoMunicion() == TipoMunicion.PESADA
                || arma.getTipoMunicion() == TipoMunicion.COHETE)) {
            SistemaDestruccion.danar(juego, objetivo.getPosicion(), arma.getDanio());
        }
        aplicarSupresion(atacante, objetivo);
    }

    /** Cada combatiente vivo recibe la misma supresion, no solo el primero. */
    private static void aplicarSupresion(Personaje atacante, Personaje objetivo) {
        if (atacante instanceof Jugador jugador
                && jugador.getProgresion().tiene(
                        com.legendoftecla.progression.CatalogoHabilidades.FUEGO_SUPRESION)
                && objetivo.getSalud() > 0) {
            objetivo.getEstados().aplicar(new com.legendoftecla.effects.Asustado());
        }
    }

    private static void exigirMunicion(Personaje atacante, Personaje objetivo) {
        int distancia = atacante.getPosicion().distanciaManhattan(objetivo.getPosicion());
        if (!atacante.puedeAtacarA(distancia)) {
            throw new IllegalStateException(
                    "No hay un arma cargada con alcance suficiente.");
        }
    }
}
