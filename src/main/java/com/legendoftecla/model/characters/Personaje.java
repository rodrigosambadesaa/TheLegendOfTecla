package com.legendoftecla.model.characters;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;

import java.util.ArrayList;
import java.util.List;

/**
 * Representa la entidad Personaje del juego.
 */
public abstract class Personaje {
    protected final String nombre;
    protected int salud;
    protected int saludMaxima;
    protected int energia;
    protected int energiaMaxima;
    protected Posicion posicion;
    protected final Mochila mochila;
    protected final List<Arma> armasEquipadas;
    protected Armadura armaduraEquipada;
    protected int visionBase;
    protected int visionTemporal;
    protected double penalizacionEnergiaSiguienteTurno;

    /**
     * Ejecuta Personaje.
     */
    protected Personaje(String nombre, int salud, int energia, Posicion posicion, Mochila mochila, int visionBase) {
        this.nombre = nombre;
        this.salud = salud;
        this.saludMaxima = salud;
        this.energia = energia;
        this.energiaMaxima = energia;
        this.posicion = posicion;
        this.mochila = mochila;
        this.visionBase = visionBase;
        this.visionTemporal = 0;
        this.penalizacionEnergiaSiguienteTurno = 0.0;
        this.armasEquipadas = new ArrayList<>();
    }

    /**
     * Ejecuta getNombre.
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Ejecuta getSalud.
     */
    public int getSalud() {
        return salud;
    }

    /**
     * Ejecuta getSaludMaxima.
     */
    public int getSaludMaxima() {
        return saludMaxima;
    }

    /**
     * Ejecuta getEnergia.
     */
    public int getEnergia() {
        return energia;
    }

    /**
     * Ejecuta getEnergiaMaxima.
     */
    public int getEnergiaMaxima() {
        return energiaMaxima;
    }

    /**
     * Ejecuta getPosicion.
     */
    public Posicion getPosicion() {
        return posicion;
    }

    /**
     * Ejecuta getMochila.
     */
    public Mochila getMochila() {
        return mochila;
    }

    /**
     * Ejecuta getRangoVision.
     */
    public int getRangoVision() {
        return visionBase + visionTemporal;
    }

    /** Configura las estadisticas base al cargar un escenario creado por el editor. */
    public void configurarEstadisticas(int nuevaSalud, int nuevaEnergia, int nuevaVision) {
        if (nuevaSalud <= 0 || nuevaEnergia <= 0 || nuevaVision <= 0) {
            throw new IllegalArgumentException("Salud, energia y vision deben ser mayores que cero.");
        }
        saludMaxima = nuevaSalud;
        salud = nuevaSalud;
        energiaMaxima = nuevaEnergia;
        energia = nuevaEnergia;
        visionBase = nuevaVision;
        visionTemporal = 0;
    }

    /**
     * Ejecuta getArmasEquipadas.
     */
    public List<Arma> getArmasEquipadas() {
        return armasEquipadas;
    }

    /**
     * Ejecuta getArmaduraEquipada.
     */
    public Armadura getArmaduraEquipada() {
        return armaduraEquipada;
    }

    /**
     * Ejecuta mover.
     */
    public void mover(Direccion direccion, Juego juego) throws AccionInvalidaException {
        Posicion destino = posicion.mover(direccion);
        if (!juego.getMapa().esTransitable(destino)) {
            throw new AccionInvalidaException("No puedes moverte a " + direccion + ".");
        }
        int coste = calcularCosteMovimiento();
        gastarEnergia(coste);
        posicion = destino;
    }

    /**
     * Ejecuta coger.
     */
    public void coger(Objeto objeto) throws AccionInvalidaException {
        if (!mochila.guardar(objeto)) {
            throw new AccionInvalidaException("La mochila no tiene capacidad o peso disponible.");
        }
    }

    /**
     * Ejecuta tirar.
     */
    public Objeto tirar(String nombreObjeto) throws AccionInvalidaException {
        Objeto obj = mochila.quitarPorNombre(nombreObjeto);
        if (obj == null) {
            throw new AccionInvalidaException("No tienes ese objeto en la mochila.");
        }
        return obj;
    }

    /**
     * Ejecuta equipar.
     */
    public void equipar(Objeto objeto) throws AccionInvalidaException {
        if (objeto instanceof Arma arma) {
            equiparArma(arma);
            return;
        }
        if (objeto instanceof Armadura armadura) {
            equiparArmadura(armadura);
            return;
        }
        throw new AccionInvalidaException("Solo puedes equipar armas o armaduras.");
    }

    /**
     * Ejecuta desequipar.
     */
    public void desequipar(String nombreObjeto) throws AccionInvalidaException {
        for (int i = 0; i < armasEquipadas.size(); i++) {
            Arma arma = armasEquipadas.get(i);
            if (arma.getNombre().equalsIgnoreCase(nombreObjeto)) {
                armasEquipadas.remove(i);
                mochila.guardar(arma);
                return;
            }
        }
        if (armaduraEquipada != null && armaduraEquipada.getNombre().equalsIgnoreCase(nombreObjeto)) {
            mochila.guardar(armaduraEquipada);
            armaduraEquipada = null;
            return;
        }
        throw new AccionInvalidaException("No tienes ese objeto equipado.");
    }

    /**
     * Ejecuta atacar.
     */
    public void atacar(Personaje objetivo) {
        int danio = calcularDanio(objetivo);
        objetivo.recibirDanio(danio);
    }

    /**
     * Ejecuta atacar.
     */
    public void atacar(List<? extends Personaje> objetivos) {
        if (objetivos.isEmpty()) {
            return;
        }
        int danio = Math.max(1, calcularDanio(objetivos.get(0)) / objetivos.size());
        for (Personaje personaje : objetivos) {
            personaje.recibirDanio(danio);
        }
    }

    /**
     * Ejecuta calcularDanio.
     */
    protected int calcularDanio(Personaje objetivo) {
        int base = armasEquipadas.stream().mapToInt(Arma::getDanio).sum();
        if (base <= 0) {
            base = 4;
        }
        return Math.max(1, aplicarModificadorDanio(base, objetivo));
    }

    /**
     * Ejecuta aplicarModificadorDanio.
     */
    protected abstract int aplicarModificadorDanio(int base, Personaje objetivo);

    /**
     * Ejecuta calcularCosteMovimiento.
     */
    protected int calcularCosteMovimiento() {
        int pesoExtra = (int) (mochila.getPesoActual() / 5.0);
        int coste = 5 + pesoExtra;
        if (penalizacionEnergiaSiguienteTurno > 0) {
            coste += (int) Math.ceil(coste * penalizacionEnergiaSiguienteTurno);
            penalizacionEnergiaSiguienteTurno = 0.0;
        }
        return coste;
    }

    /**
     * Ejecuta equiparArma.
     */
    protected void equiparArma(Arma arma) throws AccionInvalidaException {
        int usadas = 0;
        for (Arma equipada : armasEquipadas) {
            usadas += equipada.isDosManos() ? 2 : 1;
        }
        int nuevas = usadas + (arma.isDosManos() ? 2 : 1);
        if (nuevas > 2) {
            throw new AccionInvalidaException("No tienes manos suficientes para equipar esa arma.");
        }
        armasEquipadas.add(arma);
    }

    /**
     * Ejecuta equiparArmadura.
     */
    protected void equiparArmadura(Armadura armadura) {
        this.armaduraEquipada = armadura;
        this.saludMaxima += armadura.getBonusSalud();
        this.energiaMaxima += armadura.getBonusEnergia();
        this.salud = Math.min(salud + armadura.getBonusSalud(), saludMaxima);
        this.energia = Math.min(energia + armadura.getBonusEnergia(), energiaMaxima);
    }

    /**
     * Ejecuta recibirDanio.
     */
    public void recibirDanio(int danio) {
        int mitigado = danio;
        if (armaduraEquipada != null) {
            mitigado = Math.max(0, danio - armaduraEquipada.getDefensa());
        }
        salud = Math.max(0, salud - mitigado);
    }

    /**
     * Ejecuta recuperarSalud.
     */
    public void recuperarSalud(int cantidad) {
        salud = Math.min(saludMaxima, salud + cantidad);
    }

    /**
     * Ejecuta recuperarEnergia.
     */
    public void recuperarEnergia(int cantidad) {
        energia = Math.min(energiaMaxima, energia + cantidad);
    }

    /**
     * Ejecuta escalarSalud.
     */
    public void escalarSalud(double factor) {
        if (factor <= 0) {
            return;
        }
        int nuevaSaludMaxima = Math.max(1, (int) Math.round(saludMaxima * factor));
        double proporcionActual = saludMaxima <= 0 ? 1.0 : (double) salud / saludMaxima;
        saludMaxima = nuevaSaludMaxima;
        salud = Math.max(1, Math.min(saludMaxima, (int) Math.round(saludMaxima * proporcionActual)));
    }

    /**
     * Ejecuta gastarEnergia.
     */
    public void gastarEnergia(int cantidad) throws AccionInvalidaException {
        if (energia < cantidad) {
            throw new AccionInvalidaException("No tienes energia suficiente.");
        }
        energia -= cantidad;
    }

    /**
     * Ejecuta aumentarVisionTemporal.
     */
    public void aumentarVisionTemporal(int incremento) {
        visionTemporal = Math.max(visionTemporal, incremento);
    }

    /**
     * Ejecuta aplicarPenalizacionEnergiaSiguienteTurno.
     */
    public void aplicarPenalizacionEnergiaSiguienteTurno(double porcentaje) {
        penalizacionEnergiaSiguienteTurno = Math.max(penalizacionEnergiaSiguienteTurno, porcentaje);
    }

    /**
     * Ejecuta resetTurno.
     */
    public void resetTurno() {
        visionTemporal = 0;
    }
}
