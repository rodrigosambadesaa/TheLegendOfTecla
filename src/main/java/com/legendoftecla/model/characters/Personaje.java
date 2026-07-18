package com.legendoftecla.model.characters;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Explosivo;
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
    /**
     * Valor publico {@code nombre} utilizado por el modelo del juego.
     */
    protected final String nombre;
    /**
     * Valor publico {@code salud} utilizado por el modelo del juego.
     */
    protected int salud;
    /**
     * Valor publico {@code saludMaxima} utilizado por el modelo del juego.
     */
    protected int saludMaxima;
    /**
     * Valor publico {@code energia} utilizado por el modelo del juego.
     */
    protected int energia;
    /**
     * Valor publico {@code energiaMaxima} utilizado por el modelo del juego.
     */
    protected int energiaMaxima;
    /**
     * Valor publico {@code posicion} utilizado por el modelo del juego.
     */
    protected Posicion posicion;
    /**
     * Valor publico {@code mochila} utilizado por el modelo del juego.
     */
    protected final Mochila mochila;
    /**
     * Valor publico {@code armasEquipadas} utilizado por el modelo del juego.
     */
    protected final List<Arma> armasEquipadas;
    /**
     * Valor publico {@code armaduraEquipada} utilizado por el modelo del juego.
     */
    protected Armadura armaduraEquipada;
    /**
     * Valor publico {@code visionBase} utilizado por el modelo del juego.
     */
    protected int visionBase;
    /**
     * Valor publico {@code visionTemporal} utilizado por el modelo del juego.
     */
    protected int visionTemporal;
    /**
     * Valor publico {@code penalizacionEnergiaSiguienteTurno} utilizado por el modelo del juego.
     */
    protected double penalizacionEnergiaSiguienteTurno;

    /**
     * Ejecuta Personaje.
      * @param energia valor de {@code energia}
      * @param mochila valor de {@code mochila}
      * @param nombre valor de {@code nombre}
      * @param posicion valor de {@code posicion}
      * @param salud valor de {@code salud}
      * @param visionBase valor de {@code visionBase}
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
      * @return resultado de la operacion
     */
    public String getNombre() {
        return nombre;
    }

    /**
     * Ejecuta getSalud.
      * @return resultado de la operacion
     */
    public int getSalud() {
        return salud;
    }

    /**
     * Ejecuta getSaludMaxima.
      * @return resultado de la operacion
     */
    public int getSaludMaxima() {
        return saludMaxima;
    }

    /**
     * Ejecuta getEnergia.
      * @return resultado de la operacion
     */
    public int getEnergia() {
        return energia;
    }

    /**
     * Ejecuta getEnergiaMaxima.
      * @return resultado de la operacion
     */
    public int getEnergiaMaxima() {
        return energiaMaxima;
    }

    /**
     * Ejecuta getPosicion.
      * @return resultado de la operacion
     */
    public Posicion getPosicion() {
        return posicion;
    }

    /**
     * Ejecuta getMochila.
      * @return resultado de la operacion
     */
    public Mochila getMochila() {
        return mochila;
    }

    /**
     * Ejecuta getRangoVision.
      * @return resultado de la operacion
     */
    public int getRangoVision() {
        return visionBase + visionTemporal;
    }

    /**
     * Configura las estadisticas base al cargar un escenario creado por el editor.
     *
     * @param nuevaSalud salud maxima que tendra el personaje
     * @param nuevaEnergia energia maxima que tendra el personaje
     * @param nuevaVision alcance visual base del personaje
     */
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
      * @return resultado de la operacion
     */
    public List<Arma> getArmasEquipadas() {
        return armasEquipadas;
    }

    /**
     * Ejecuta getArmaduraEquipada.
      * @return resultado de la operacion
     */
    public Armadura getArmaduraEquipada() {
        return armaduraEquipada;
    }

    /**
     * Ejecuta mover.
      * @param direccion valor de {@code direccion}
      * @param juego valor de {@code juego}
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
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
      * @param objeto valor de {@code objeto}
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
     */
    public void coger(Objeto objeto) throws AccionInvalidaException {
        if (objeto instanceof Explosivo && !(this instanceof Zapador)) {
            throw new AccionInvalidaException("Solo el zapador puede cargar explosivos.");
        }
        if (!mochila.guardar(objeto)) {
            throw new AccionInvalidaException("La mochila no tiene capacidad o peso disponible.");
        }
    }

    /**
     * Ejecuta tirar.
      * @param nombreObjeto valor de {@code nombreObjeto}
      * @return resultado de la operacion
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
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
      * @param objeto valor de {@code objeto}
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
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
      * @param nombreObjeto valor de {@code nombreObjeto}
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
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
      * @param objetivo valor de {@code objetivo}
     */
    public void atacar(Personaje objetivo) {
        int danio = calcularDanio(objetivo);
        objetivo.recibirDanio(danio);
    }

    /**
     * Ejecuta atacar.
      * @param objetivos valor de {@code objetivos}
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
      * @param objetivo valor de {@code objetivo}
      * @return resultado de la operacion
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
      * @param base valor de {@code base}
      * @param objetivo valor de {@code objetivo}
      * @return resultado de la operacion
     */
    protected abstract int aplicarModificadorDanio(int base, Personaje objetivo);

    /**
     * Ejecuta calcularCosteMovimiento.
      * @return resultado de la operacion
     */
    protected int calcularCosteMovimiento() {
        int coste = estimarCosteMovimiento();
        penalizacionEnergiaSiguienteTurno = 0.0;
        return coste;
    }

    /**
     * Calcula la energia que consumiria el siguiente movimiento sin realizarlo.
     *
     * @return coste estimado teniendo en cuenta peso y penalizaciones temporales
     */
    public int estimarCosteMovimiento() {
        int pesoExtra = (int) (mochila.getPesoActual() / 5.0);
        int coste = 5 + pesoExtra;
        if (penalizacionEnergiaSiguienteTurno > 0) {
            coste += (int) Math.ceil(coste * penalizacionEnergiaSiguienteTurno);
        }
        return coste;
    }

    /**
     * Ejecuta equiparArma.
      * @param arma valor de {@code arma}
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
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
      * @param armadura valor de {@code armadura}
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
      * @param danio valor de {@code danio}
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
      * @param cantidad valor de {@code cantidad}
     */
    public void recuperarSalud(int cantidad) {
        salud = Math.min(saludMaxima, salud + cantidad);
    }

    /**
     * Ejecuta recuperarEnergia.
      * @param cantidad valor de {@code cantidad}
     */
    public void recuperarEnergia(int cantidad) {
        energia = Math.min(energiaMaxima, energia + cantidad);
    }

    /**
     * Ejecuta escalarSalud.
      * @param factor valor de {@code factor}
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
      * @param cantidad valor de {@code cantidad}
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
     */
    public void gastarEnergia(int cantidad) throws AccionInvalidaException {
        if (energia < cantidad) {
            throw new AccionInvalidaException("No tienes energia suficiente.");
        }
        energia -= cantidad;
    }

    /**
     * Ejecuta aumentarVisionTemporal.
      * @param incremento valor de {@code incremento}
     */
    public void aumentarVisionTemporal(int incremento) {
        visionTemporal = Math.max(visionTemporal, incremento);
    }

    /**
     * Ejecuta aplicarPenalizacionEnergiaSiguienteTurno.
      * @param porcentaje valor de {@code porcentaje}
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
