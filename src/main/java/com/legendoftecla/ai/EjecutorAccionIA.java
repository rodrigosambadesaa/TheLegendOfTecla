package com.legendoftecla.ai;

import com.legendoftecla.effects.Inspirado;
import com.legendoftecla.engine.SistemaCombate;
import com.legendoftecla.engine.SistemaIncendios;
import com.legendoftecla.events.PersonajeCurado;
import com.legendoftecla.events.PersonajeAtacado;
import com.legendoftecla.events.PersonajeDanado;
import com.legendoftecla.events.ObjetoUsado;
import com.legendoftecla.events.PersonajeMovido;
import com.legendoftecla.events.RuidoGenerado;
import com.legendoftecla.inventory.ServicioRecarga;
import com.legendoftecla.model.characters.Commander;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.characters.Jefe;
import com.legendoftecla.model.characters.Medic;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.model.items.Granada;
import com.legendoftecla.model.items.TipoGranada;
import com.legendoftecla.model.elements.SistemaCobertura;
import com.legendoftecla.model.elements.TipoCobertura;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;

import java.util.Comparator;
import java.util.List;
import java.util.Random;

/** Ejecuta decisiones abstractas y concentra las mutaciones del turno enemigo. */
public final class EjecutorAccionIA {
    /** @return si la decision produjo una accion efectiva */
    public boolean ejecutar(Juego juego, Enemigo enemigo, AccionIA accion, Random random) {
        if (enemigo instanceof Jefe jefe) {
            SistemaJefes.activarFase(juego, jefe, random);
        }
        return switch (accion.tipo()) {
            case ATACAR -> atacar(juego, enemigo, accion.objetivo(), random);
            case RECARGAR -> recargar(enemigo);
            case CURAR -> curar(juego, enemigo);
            case INCENDIAR -> incendiar(juego, enemigo, accion.objetivo());
            case ALERTAR -> alertar(juego, enemigo, accion.objetivo());
            case PROTEGER -> proteger(juego, enemigo);
            case BUSCAR_COBERTURA -> buscarCobertura(
                    juego, enemigo, accion.objetivo(), random);
            case ALEJARSE -> mover(juego, enemigo, accion.objetivo(), false, random);
            case ACERCARSE, INVESTIGAR, BUSCAR ->
                    mover(juego, enemigo, accion.objetivo(), true, random);
            case PATRULLAR -> mover(juego, enemigo, null, true, random);
            case ESPERAR -> false;
        };
    }

    private boolean atacar(Juego juego, Enemigo enemigo,
            Posicion posicionObjetivo, Random random) {
        Personaje objetivo = resolverObjetivo(juego, posicionObjetivo);
        if (objetivo == null || enemigo.getPosicion().distanciaManhattan(
                objetivo.getPosicion()) > enemigo.getRangoVision()
                || !juego.getMapa().hayLineaAtaque(
                        enemigo.getPosicion(), objetivo.getPosicion())) {
            return false;
        }
        int distancia = enemigo.getPosicion().distanciaManhattan(objetivo.getPosicion());
        if (!enemigo.puedeAtacarA(distancia)) {
            return enemigo.puedeAtacar()
                    ? mover(juego, enemigo, objetivo.getPosicion(), true, random)
                    : recargar(enemigo);
        }
        SistemaCombate.atacar(juego, enemigo, objetivo, random);
        return true;
    }

    private boolean recargar(Enemigo enemigo) {
        try {
            new ServicioRecarga().recargar(enemigo, null);
            return true;
        } catch (com.legendoftecla.exceptions.AccionInvalidaException error) {
            return false;
        }
    }

    private boolean curar(Juego juego, Enemigo enemigo) {
        if (!(enemigo instanceof Medic medic) || !coordinacionActiva(juego)) {
            return false;
        }
        Enemigo objetivo = juego.getEnemigos().stream()
                .filter(otro -> otro != enemigo && otro.getSalud() > 0
                        && otro.getSalud() < otro.getSaludMaxima())
                .filter(otro -> enemigo.getPosicion().distanciaManhattan(
                        otro.getPosicion()) <= medic.getAlcanceCuracion())
                .min(Comparator.comparingDouble(otro ->
                        (double) otro.getSalud() / otro.getSaludMaxima()))
                .orElse(null);
        if (objetivo == null) {
            return false;
        }
        int antes = objetivo.getSalud();
        objetivo.recuperarSalud(medic.getPotenciaCuracion());
        int curada = objetivo.getSalud() - antes;
        juego.publicarEvento(new PersonajeCurado(juego.getBusEventos().ahora(),
                objetivo.getNombre(), curada, objetivo.getPosicion()));
        return curada > 0;
    }

    private boolean incendiar(Juego juego, Enemigo enemigo, Posicion objetivo) {
        if (objetivo == null || !juego.getMapa().estaDentro(objetivo)
                || enemigo.getPosicion().distanciaManhattan(objetivo)
                        > enemigo.getRangoVision()) {
            return false;
        }
        Granada granada = enemigo.getMochila().getObjetos().stream()
                .filter(Granada.class::isInstance)
                .map(Granada.class::cast)
                .filter(candidata -> candidata.getTipo() == TipoGranada.INCENDIARIA)
                .findFirst().orElse(null);
        if (granada != null) {
            enemigo.getMochila().quitar(granada);
            juego.publicarEvento(new ObjetoUsado(juego.getBusEventos().ahora(),
                    enemigo.getNombre(), granada.getNombre(), enemigo.getPosicion()));
            aplicarGranadaEnemiga(juego, enemigo, granada, objetivo);
        } else if (!consumirCargaIncendiaria(enemigo, objetivo)) {
            return false;
        }
        int antes = juego.getMapa().getCelda(objetivo).getNivelFuego();
        SistemaIncendios.iniciar(juego, objetivo, enemigo instanceof Jefe ? 3 : 2);
        return granada != null
                || juego.getMapa().getCelda(objetivo).getNivelFuego() > antes;
    }

    private boolean consumirCargaIncendiaria(Enemigo enemigo, Posicion objetivo) {
        int distancia = enemigo.getPosicion().distanciaManhattan(objetivo);
        return enemigo.getArmasEquipadas().stream()
                .filter(arma -> arma.alcanza(distancia))
                .anyMatch(com.legendoftecla.model.items.Arma::consumirDisparo);
    }

    private void aplicarGranadaEnemiga(Juego juego, Enemigo enemigo,
            Granada granada, Posicion objetivo) {
        java.util.List<com.legendoftecla.model.characters.Personaje> afectados =
                new java.util.ArrayList<>();
        if (juego.getJugador().getPosicion().equals(objetivo)
                && juego.getJugador().getSalud() > 0) {
            afectados.add(juego.getJugador());
        }
        juego.getAliados().stream()
                .filter(aliado -> aliado.getSalud() > 0
                        && aliado.getPosicion().equals(objetivo))
                .forEach(afectados::add);
        juego.publicarEvento(new RuidoGenerado(juego.getBusEventos().ahora(),
                objetivo, FuenteRuido.EXPLOSION.intensidad(), "granada-enemiga"));
        for (var afectado : afectados) {
            int antes = afectado.getSalud();
            juego.publicarEvento(new PersonajeAtacado(juego.getBusEventos().ahora(),
                    enemigo.getNombre(), afectado.getNombre(),
                    enemigo.getPosicion(), objetivo));
            afectado.recibirDanio(granada.getDanio());
            int danio = antes - afectado.getSalud();
            if (danio > 0) {
                juego.publicarEvento(new PersonajeDanado(juego.getBusEventos().ahora(),
                        afectado.getNombre(), danio, objetivo));
            }
        }
    }

    private boolean alertar(Juego juego, Enemigo origen, Posicion objetivo) {
        if (!coordinacionActiva(juego)) return false;
        Posicion comunicada = objetivo == null
                ? juego.getJugador().getPosicion() : objetivo;
        boolean comunicado = false;
        for (Enemigo enemigo : juego.getEnemigos()) {
            if (enemigo != origen && enemigo.getSalud() > 0
                    && origen.getPosicion().distanciaManhattan(enemigo.getPosicion()) <= 5) {
                enemigo.getControladorIA().alertar(comunicada, 3);
                comunicado = true;
            }
        }
        juego.publicarEvento(new RuidoGenerado(juego.getBusEventos().ahora(),
                origen.getPosicion(), FuenteRuido.DISPARO.intensidad(), "alerta"));
        return comunicado;
    }

    private boolean proteger(Juego juego, Enemigo origen) {
        if (!(origen instanceof Commander commander) || !coordinacionActiva(juego)) {
            return false;
        }
        boolean aplicado = false;
        for (Enemigo aliado : juego.getEnemigos()) {
            if (aliado != origen && aliado.getSalud() > 0
                    && origen.getPosicion().distanciaManhattan(aliado.getPosicion()) <= 3) {
                aliado.getEstados().aplicar(new Inspirado());
                aplicado = true;
            }
        }
        return aplicado && commander.bonificacionAliados() > 1.0;
    }

    private boolean buscarCobertura(Juego juego, Enemigo enemigo,
            Posicion objetivo, Random random) {
        SistemaCobertura cobertura = new SistemaCobertura(random);
        Posicion amenaza = objetivo == null
                ? juego.getJugador().getPosicion() : objetivo;
        List<Posicion> candidatas = candidatas(juego, enemigo).stream()
                .filter(posicion -> cobertura.proteccion(
                        juego.getMapa(), amenaza, posicion).tipo() != TipoCobertura.NINGUNA)
                .toList();
        if (candidatas.isEmpty()) {
            return mover(juego, enemigo, amenaza, false, random);
        }
        return desplazar(juego, enemigo, candidatas.get(random.nextInt(candidatas.size())));
    }

    private boolean mover(Juego juego, Enemigo enemigo, Posicion objetivo,
            boolean acercar, Random random) {
        List<Posicion> candidatas = candidatas(juego, enemigo);
        if (candidatas.isEmpty()) {
            return false;
        }
        Posicion destino;
        if (objetivo == null) {
            destino = candidatas.get(random.nextInt(candidatas.size()));
        } else {
            Comparator<Posicion> comparador = Comparator.comparingInt(
                    posicion -> posicion.distanciaManhattan(objetivo));
            destino = (acercar ? candidatas.stream().min(comparador)
                    : candidatas.stream().max(comparador)).orElse(null);
        }
        return destino != null && desplazar(juego, enemigo, destino);
    }

    private List<Posicion> candidatas(Juego juego, Enemigo enemigo) {
        return java.util.Arrays.stream(Direccion.values())
                .map(enemigo.getPosicion()::mover)
                .filter(juego.getMapa()::esTransitable)
                .filter(posicion -> !posicion.equals(juego.getJugador().getPosicion()))
                .filter(posicion -> juego.getAliados().stream()
                        .noneMatch(aliado -> aliado.getPosicion().equals(posicion)))
                .filter(posicion -> juego.getEnemigos().stream()
                        .noneMatch(otro -> otro != enemigo
                                && otro.getSalud() > 0
                                && otro.getPosicion().equals(posicion)))
                .toList();
    }

    private boolean desplazar(Juego juego, Enemigo enemigo, Posicion destino) {
        Posicion origen = enemigo.getPosicion();
        Direccion direccion = java.util.Arrays.stream(Direccion.values())
                .filter(candidata -> origen.mover(candidata).equals(destino))
                .findFirst().orElse(null);
        if (direccion == null) {
            return false;
        }
        try {
            juego.getMapa().getCelda(origen).quitarEnemigo(enemigo);
            enemigo.mover(direccion, juego);
            juego.getMapa().getCelda(destino).agregarEnemigo(enemigo);
            juego.getConsola().imprimirInfo(enemigo.getNombre()
                    + " se mueve de " + origen + " a " + destino + ".");
            juego.publicarEvento(new PersonajeMovido(juego.getBusEventos().ahora(),
                    enemigo.getNombre(), origen, destino));
            juego.publicarEvento(new RuidoGenerado(juego.getBusEventos().ahora(),
                    destino, FuenteRuido.CAMINAR.intensidad(), "movimiento-enemigo"));
            return true;
        } catch (com.legendoftecla.exceptions.AccionInvalidaException error) {
            juego.getMapa().getCelda(origen).agregarEnemigo(enemigo);
            return false;
        }
    }

    private Personaje resolverObjetivo(Juego juego, Posicion posicion) {
        Posicion buscada = posicion == null
                ? juego.getJugador().getPosicion() : posicion;
        if (juego.getJugador().getSalud() > 0
                && juego.getJugador().getPosicion().equals(buscada)) {
            return juego.getJugador();
        }
        return juego.getAliados().stream()
                .filter(aliado -> aliado.getSalud() > 0
                        && aliado.getPosicion().equals(buscada))
                .findFirst().orElse(null);
    }

    private boolean coordinacionActiva(Juego juego) {
        return juego.getAliados().stream().anyMatch(aliado -> aliado.getSalud() > 0);
    }
}
