package com.legendoftecla.commands;

import com.legendoftecla.console.ArteEnemigoLore;
import com.legendoftecla.exceptions.ComandoException;
import com.legendoftecla.model.characters.Enemigo;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Botiquin;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.ToritoRojo;
import com.legendoftecla.model.world.Celda;
import com.legendoftecla.model.world.AmbientacionMapa;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Mapa;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;
import com.legendoftecla.engine.SistemaIluminacion;

import java.util.stream.Collectors;

/**
 * Representa la entidad ComandoMirar del juego.
 */
public class ComandoMirar implements Comando {
    private CommandContext context;
    private Direccion direccion;
    private int pasos;
    private String detalle;

    /**
     * Ejecuta ComandoMirar.
      * @param context valor de {@code context}
     */
    public ComandoMirar(CommandContext context) {
        this(context, null, 0, null);
    }

    /**
     * Ejecuta ComandoMirar.
      * @param context valor de {@code context}
      * @param direccion valor de {@code direccion}
      * @param pasos valor de {@code pasos}
     */
    public ComandoMirar(CommandContext context, Direccion direccion, int pasos) {
        this(context, direccion, pasos, null);
    }

    /**
     * Crea una mirada local o remota con detalle opcional.
     *
     * @param context contexto
     * @param direccion direccion opcional
     * @param pasos distancia, cero para la celda actual
     * @param detalle nombre opcional de objeto o enemigo
     */
    public ComandoMirar(CommandContext context, Direccion direccion, int pasos, String detalle) {
        setContext(context);
        setDireccion(direccion);
        setPasos(pasos);
        setDetalle(detalle);
    }

    /** @return contexto de ejecucion */
    public CommandContext getContext() { return context; }
    /** @param context contexto no nulo */
    public void setContext(CommandContext context) { this.context = Validaciones.noNulo(context, "Contexto"); }
    /** @return direccion observada o {@code null} */
    public Direccion getDireccion() { return direccion; }
    /** @param direccion direccion opcional coherente con los pasos actuales */
    public void setDireccion(Direccion direccion) {
        if (direccion == null && pasos != 0) {
            throw new IllegalArgumentException("Mirar sin direccion no admite pasos.");
        }
        this.direccion = direccion;
    }
    /** @return distancia de observacion */
    public int getPasos() { return pasos; }
    /** @param pasos cero sin direccion o entre 1 y el limite del mapa */
    public void setPasos(int pasos) {
        if (direccion == null) {
            if (pasos != 0) {
                throw new IllegalArgumentException("Mirar sin direccion no admite pasos.");
            }
            this.pasos = 0;
            return;
        }
        this.pasos = Validaciones.enteroEntre(pasos, 1, Limites.MAPA_MAXIMO, "Pasos de vision");
    }

    /** @return nombre detallado opcional */
    public String getDetalle() { return detalle; }
    /** @param detalle nombre opcional y acotado */
    public void setDetalle(String detalle) {
        this.detalle = Validaciones.textoOpcional(detalle, "Detalle", Limites.TEXTO_CORTO);
    }

    @Override
    /**
     * Ejecuta ejecutar.
     */
    public void ejecutar() throws ComandoException {
        Celda celda = resolverCeldaAMirar();
        Posicion observada = direccion == null
                ? context.getJuego().getJugador().getPosicion()
                : resolverPosicion(context.getJuego().getJugador().getPosicion());
        if (!SistemaIluminacion.hayLuz(context.getJuego(), observada)) {
            throw new ComandoException("La zona esta oscura; necesitas una linterna o una fuente de luz.");
        }
        context.getJuego().getConsola().imprimir(
                AmbientacionMapa.describir(context.getJuego().getMapa(), observada));
        if (direccion == null) {
            context.getJuego().inspeccionarCeldaActual();
            if (detalle != null) {
                Objeto objeto = celda.getObjetos().stream()
                        .filter(candidato -> candidato.getNombre().equalsIgnoreCase(detalle))
                        .findFirst()
                        .orElseThrow(() -> new ComandoException("No existe ese objeto en la celda actual."));
                context.getJuego().getConsola().imprimir("Objeto: " + describirObjeto(objeto));
            } else if (celda.getObjetos().isEmpty()) {
                context.getJuego().getConsola().imprimir("No hay objetos en esta celda.");
            } else {
                String lista = celda.getObjetos().stream()
                        .map(o -> o.getNombre()).collect(Collectors.joining(", "));
                context.getJuego().getConsola().imprimir("Objetos: " + lista);
            }
        } else {
            context.getJuego().getConsola().imprimir(
                    "Los objetos solo pueden inspeccionarse al llegar a la celda y mirar alli.");
        }
        if (detalle != null && direccion != null) {
            Enemigo enemigo = celda.getEnemigos().stream()
                    .filter(candidato -> candidato.getNombre().equalsIgnoreCase(detalle))
                    .findFirst()
                    .orElseThrow(() -> new ComandoException("No existe ese enemigo en la celda observada."));
            context.getJuego().getConsola().imprimirInfo(ArteEnemigoLore.renderizarFicha(enemigo));
        } else if (!celda.getEnemigos().isEmpty()) {
            String enemigos = celda.getEnemigos().stream().map(e -> e.getNombre()).collect(Collectors.joining(", "));
            context.getJuego().getConsola().imprimir("Enemigos aqui: " + enemigos);
            for (var enemigo : celda.getEnemigos()) {
                context.getJuego().getConsola().imprimirInfo(ArteEnemigoLore.renderizarFicha(enemigo));
            }
        }
    }

    private String describirObjeto(Objeto objeto) {
        String atributos = "sin atributos adicionales";
        if (objeto instanceof Arma arma) {
            atributos = "dano=" + arma.getDanio() + ", dosManos=" + arma.isDosManos();
        } else if (objeto instanceof Armadura armadura) {
            atributos = "defensa=" + armadura.getDefensa()
                    + ", salud=" + armadura.getBonusSalud() + ", energia=" + armadura.getBonusEnergia();
        } else if (objeto instanceof Binocular binocular) {
            atributos = "rango=" + binocular.getRango();
        } else if (objeto instanceof Botiquin botiquin) {
            atributos = "curacion=" + botiquin.getCuracion();
        } else if (objeto instanceof ToritoRojo torito) {
            atributos = "energia=" + torito.getEnergiaTurno();
        } else if (objeto instanceof Explosivo explosivo) {
            atributos = "dano=" + explosivo.getDanio() + ", alcance=" + explosivo.getAlcanceMaximo();
        }
        return objeto.getNombre() + " - " + objeto.getDescripcion() + " ("
                + objeto.getPeso() + " kg, " + atributos + ")";
    }

    private Celda resolverCeldaAMirar() throws ComandoException {
        Mapa mapa = context.getJuego().getMapa();
        Posicion origen = context.getJuego().getJugador().getPosicion();
        if (direccion == null) {
            return mapa.getCelda(origen);
        }
        if (pasos > context.getJuego().getJugador().getRangoVision()) {
            throw new ComandoException("La celda queda fuera del rango de vision.");
        }

        Posicion destino = origen;
        for (int i = 0; i < pasos; i++) {
            destino = destino.mover(direccion);
            if (!mapa.estaDentro(destino)) {
                throw new ComandoException("No puedes mirar fuera del mapa.");
            }
        }

        if (!mapa.esTransitable(destino)) {
            throw new ComandoException("No puedes mirar esa celda: destino no transitable.");
        }

        if (!SistemaIluminacion.hayLuz(context.getJuego(), destino)) {
            throw new ComandoException("La zona esta oscura; necesitas una linterna o una fuente de luz.");
        }
        return mapa.getCelda(destino);
    }

    private Posicion resolverPosicion(Posicion origen) {
        Posicion destino = origen;
        for (int i = 0; i < pasos; i++) {
            destino = destino.mover(direccion);
        }
        return destino;
    }
}
