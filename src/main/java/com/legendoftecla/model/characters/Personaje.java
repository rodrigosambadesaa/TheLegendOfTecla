package com.legendoftecla.model.characters;

import com.legendoftecla.exceptions.AccionInvalidaException;
import com.legendoftecla.effects.GestorEstados;
import com.legendoftecla.engine.SistemaTrampas;
import com.legendoftecla.model.items.Arma;
import com.legendoftecla.model.items.Armadura;
import com.legendoftecla.model.items.Binocular;
import com.legendoftecla.model.items.Explosivo;
import com.legendoftecla.model.items.Granada;
import com.legendoftecla.model.items.PerfilArmamento;
import com.legendoftecla.model.items.ReglasArmamento;
import com.legendoftecla.model.items.Objeto;
import com.legendoftecla.model.items.Linterna;
import com.legendoftecla.model.world.Direccion;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa la entidad Personaje del juego.
 */
public abstract class Personaje {
    /**
     * Valor publico {@code nombre} utilizado por el modelo del juego.
     */
    private String nombre;
    /**
     * Valor publico {@code salud} utilizado por el modelo del juego.
     */
    private int salud;
    /**
     * Valor publico {@code saludMaxima} utilizado por el modelo del juego.
     */
    private int saludMaxima;
    /**
     * Valor publico {@code energia} utilizado por el modelo del juego.
     */
    private int energia;
    /**
     * Valor publico {@code energiaMaxima} utilizado por el modelo del juego.
     */
    private int energiaMaxima;
    /**
     * Valor publico {@code posicion} utilizado por el modelo del juego.
     */
    private Posicion posicion;
    /**
     * Valor publico {@code mochila} utilizado por el modelo del juego.
     */
    private Mochila mochila;
    /**
     * Valor publico {@code armasEquipadas} utilizado por el modelo del juego.
     */
    private List<Arma> armasEquipadas;
    /**
     * Valor publico {@code armaduraEquipada} utilizado por el modelo del juego.
     */
    private Armadura armaduraEquipada;
    /** Binocular equipado, como maximo uno. */
    private Binocular binocularEquipado;
    /**
     * Valor publico {@code visionBase} utilizado por el modelo del juego.
     */
    private int visionBase;
    /**
     * Valor publico {@code visionTemporal} utilizado por el modelo del juego.
     */
    private int visionTemporal;
    /**
     * Valor publico {@code penalizacionEnergiaSiguienteTurno} utilizado por el modelo del juego.
     */
    private double penalizacionEnergiaSiguienteTurno;
    private boolean linternaActiva;
    private int alcanceLinterna;
    private GestorEstados estados;

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
        setEstados(new GestorEstados(this));
        setNombre(nombre);
        setMochila(mochila);
        setArmasEquipadas(List.of());
        setBinocularEquipado(null);
        setSaludMaxima(salud);
        setSalud(salud);
        setEnergiaMaxima(energia);
        setEnergia(energia);
        setPosicion(posicion);
        setVisionBase(visionBase);
        setVisionTemporal(0);
        setPenalizacionEnergiaSiguienteTurno(0.0);
        setLinternaActiva(false);
        setAlcanceLinterna(0);
    }

    /**
     * Ejecuta getNombre.
      * @return resultado de la operacion
     */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre nombre obligatorio y acotado */
    public void setNombre(String nombre) {
        this.nombre = Validaciones.textoObligatorio(
                nombre, "Nombre del personaje", Limites.TEXTO_CORTO);
    }

    /**
     * Ejecuta getSalud.
      * @return resultado de la operacion
     */
    public int getSalud() {
        return salud;
    }

    /**
     * Limita la salud actual al intervalo entre cero y la salud maxima.
     *
     * @param nuevaSalud valor solicitado
     */
    public void setSalud(int nuevaSalud) {
        salud = Math.max(0, Math.min(saludMaxima, nuevaSalud));
    }

    /**
     * Ejecuta getSaludMaxima.
      * @return resultado de la operacion
     */
    public int getSaludMaxima() {
        return saludMaxima;
    }

    /**
     * Establece una salud maxima positiva y ajusta la salud actual si es necesario.
     *
     * @param nuevaSaludMaxima limite solicitado
     */
    public void setSaludMaxima(int nuevaSaludMaxima) {
        this.saludMaxima = Validaciones.enteroEntre(
                nuevaSaludMaxima, 1, Limites.ESTADISTICA, "Salud maxima");
        setSalud(salud);
    }

    /**
     * Ejecuta getEnergia.
      * @return resultado de la operacion
     */
    public int getEnergia() {
        return energia;
    }

    /**
     * Limita la energia actual al intervalo entre cero y la energia maxima.
     *
     * @param nuevaEnergia valor solicitado
     */
    public void setEnergia(int nuevaEnergia) {
        energia = Math.max(0, Math.min(energiaMaxima, nuevaEnergia));
    }

    /**
     * Ejecuta getEnergiaMaxima.
      * @return resultado de la operacion
     */
    public int getEnergiaMaxima() {
        return energiaMaxima;
    }

    /**
     * Establece una energia maxima positiva y ajusta la actual si es necesario.
     *
     * @param nuevaEnergiaMaxima limite solicitado
     */
    public void setEnergiaMaxima(int nuevaEnergiaMaxima) {
        this.energiaMaxima = Validaciones.enteroEntre(
                nuevaEnergiaMaxima, 1, Limites.ESTADISTICA, "Energia maxima");
        setEnergia(energia);
    }

    /**
     * Amplia la energia maxima hasta un minimo sin reducir valores ya superiores.
     *
     * @param minimoEnergia nueva energia maxima minima
     */
    public void asegurarEnergiaMaxima(int minimoEnergia) {
        Validaciones.enteroEntre(minimoEnergia, 1, Limites.ESTADISTICA, "Energia minima");
        if (minimoEnergia <= energiaMaxima) {
            return;
        }
        int incremento = minimoEnergia - energiaMaxima;
        setEnergiaMaxima(minimoEnergia);
        setEnergia(energia + incremento);
    }

    /**
     * Ejecuta getPosicion.
      * @return resultado de la operacion
     */
    public Posicion getPosicion() {
        return new Posicion(posicion.getFila(), posicion.getColumna());
    }

    /**
     * Establece una posicion valida no nula.
     *
     * @param nuevaPosicion posicion solicitada
     */
    public void setPosicion(Posicion nuevaPosicion) {
        Posicion validada = Validaciones.noNulo(nuevaPosicion, "Posicion");
        posicion = new Posicion(validada.getFila(), validada.getColumna());
    }

    /**
     * Ejecuta getMochila.
      * @return resultado de la operacion
     */
    public Mochila getMochila() {
        return mochila;
    }

    /** @param mochila mochila no nula */
    public void setMochila(Mochila mochila) {
        this.mochila = Validaciones.noNulo(mochila, "Mochila");
    }

    /**
     * Ejecuta getRangoVision.
      * @return resultado de la operacion
     */
    public int getRangoVision() {
        return Math.max(1, (int) Math.floor((visionBase + visionTemporal)
                * estados.multiplicadorVision()));
    }

    /** @return alcance visual base sin mejoras temporales */
    public int getVisionBase() {
        return visionBase;
    }

    /** @return vision temporal activa */
    public int getVisionTemporal() {
        return visionTemporal;
    }

    /** @param visionTemporal vision temporal no negativa y acotada */
    public void setVisionTemporal(int visionTemporal) {
        this.visionTemporal = Validaciones.enteroEntre(
                visionTemporal, 0, Limites.ESTADISTICA, "Vision temporal");
    }

    /** @return penalizacion del siguiente movimiento */
    public double getPenalizacionEnergiaSiguienteTurno() {
        return penalizacionEnergiaSiguienteTurno;
    }

    /** @param penalizacion penalizacion entre cero y uno */
    public void setPenalizacionEnergiaSiguienteTurno(double penalizacion) {
        this.penalizacionEnergiaSiguienteTurno = Validaciones.decimalEntre(
                penalizacion, 0.0, 1.0, "Penalizacion de energia");
    }

    /**
     * Establece el alcance visual base dentro de los limites del dominio.
     *
     * @param nuevaVision vision base solicitada
     */
    public void setVisionBase(int nuevaVision) {
        visionBase = Validaciones.enteroEntre(nuevaVision, 1, Limites.ESTADISTICA, "Vision base");
    }

    /**
     * Configura las estadisticas base al cargar un escenario creado por el editor.
     *
     * @param nuevaSalud salud maxima que tendra el personaje
     * @param nuevaEnergia energia maxima que tendra el personaje
     * @param nuevaVision alcance visual base del personaje
     */
    public void configurarEstadisticas(int nuevaSalud, int nuevaEnergia, int nuevaVision) {
        setSaludMaxima(nuevaSalud);
        setSalud(nuevaSalud);
        setEnergiaMaxima(nuevaEnergia);
        setEnergia(nuevaEnergia);
        setVisionBase(nuevaVision);
        setVisionTemporal(0);
    }

    /**
     * Ejecuta getArmasEquipadas.
      * @return resultado de la operacion
     */
    public List<Arma> getArmasEquipadas() {
        return Collections.unmodifiableList(armasEquipadas);
    }

    /**
     * Sustituye las armas equipadas por una copia validada.
     *
     * @param armas nuevas armas, como maximo dos
     */
    public void setArmasEquipadas(List<Arma> armas) {
        Validaciones.noNulo(armas, "Armas equipadas");
        if (armas.size() > 2 || armas.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException("Solo se admiten dos armas equipadas y ninguna puede ser nula.");
        }
        if (armas.stream().anyMatch(arma -> !faccionCompatible(arma.getFaccion()))) {
            throw new IllegalArgumentException(
                    "El armamento equipado no es compatible con la faccion o el rol.");
        }
        this.armasEquipadas = new ArrayList<>(armas);
    }

    /**
     * Ejecuta getArmaduraEquipada.
      * @return resultado de la operacion
     */
    public Armadura getArmaduraEquipada() {
        return armaduraEquipada;
    }

    /**
     * Establece la referencia de armadura equipada; {@code null} la elimina.
     *
     * @param armadura nueva armadura
     */
    public void setArmaduraEquipada(Armadura armadura) {
        if (armadura != null && !puedeUsar(armadura)) {
            throw new IllegalArgumentException(
                    "La armadura equipada no es compatible con la faccion.");
        }
        this.armaduraEquipada = armadura;
    }

    /** @return binocular equipado o {@code null} */
    public Binocular getBinocularEquipado() { return binocularEquipado; }
    /** @param binocular binocular equipado opcional */
    public void setBinocularEquipado(Binocular binocular) { this.binocularEquipado = binocular; }

    public boolean isLinternaActiva() { return linternaActiva; }
    public void setLinternaActiva(boolean linternaActiva) { this.linternaActiva = linternaActiva; }
    public int getAlcanceLinterna() { return alcanceLinterna; }
    public void setAlcanceLinterna(int alcanceLinterna) {
        this.alcanceLinterna = Validaciones.enteroEntre(
                alcanceLinterna, 0, Limites.ESTADISTICA, "Alcance de linterna");
    }

    public boolean tieneLinterna() {
        return mochila.getObjetos().stream().anyMatch(Linterna.class::isInstance);
    }

    /** @return gestor de efectos temporales de este personaje */
    public GestorEstados getEstados() {
        return estados;
    }

    /** @param estados gestor no nulo perteneciente a este personaje */
    public void setEstados(GestorEstados estados) {
        GestorEstados validado = Validaciones.noNulo(estados, "Gestor de estados");
        if (validado.getPersonaje() != this) {
            throw new IllegalArgumentException("El gestor debe pertenecer al personaje.");
        }
        this.estados = validado;
    }

    /**
     * Ejecuta mover.
      * @param direccion valor de {@code direccion}
      * @param juego valor de {@code juego}
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
     */
    public void mover(Direccion direccion, Juego juego) throws AccionInvalidaException {
        if (estados.consumirBloqueoAccion()) {
            throw new AccionInvalidaException("Estas aturdido y pierdes la accion.");
        }
        Validaciones.noNulo(direccion, "Direccion");
        Validaciones.noNulo(juego, "Juego");
        Posicion destino = posicion.mover(direccion);
        if (!juego.getMapa().esTransitable(destino)) {
            throw new AccionInvalidaException("No puedes moverte a " + direccion + ".");
        }
        int coste = calcularCosteMovimiento();
        gastarEnergia(coste);
        setPosicion(destino);
        SistemaTrampas.activarAlEntrar(juego, destino, this);
        estados.alMover();
    }

    /**
     * Ejecuta coger.
      * @param objeto valor de {@code objeto}
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
     */
    public void coger(Objeto objeto) throws AccionInvalidaException {
        Validaciones.noNulo(objeto, "Objeto");
        if (!admitePorClase(objeto)) {
            throw new AccionInvalidaException("Solo el zapador puede cargar explosivos.");
        }
        if (!mochila.guardar(objeto)) {
            throw new AccionInvalidaException("La mochila no tiene capacidad o peso disponible.");
        }
    }

    /**
     * Comprueba de antemano peso, capacidad y restricciones de clase.
     *
     * @param objeto candidato a recoger
     * @return si {@link #coger(Objeto)} puede completarse en el estado actual
     */
    public boolean puedeCoger(Objeto objeto) {
        Validaciones.noNulo(objeto, "Objeto");
        return admitePorClase(objeto) && mochila.puedeGuardar(objeto);
    }

    private boolean admitePorClase(Objeto objeto) {
        if (objeto instanceof Arma arma && !puedeUsar(arma)) {
            return false;
        }
        if (objeto instanceof Armadura armadura && !puedeUsar(armadura)) {
            return false;
        }
        if (objeto instanceof Granada) {
            return getPerfilArmamento().permiteGranadas();
        }
        return !(objeto instanceof Explosivo)
                || getPerfilArmamento().permiteDemolicion();
    }

    /**
     * Ejecuta tirar.
      * @param nombreObjeto valor de {@code nombreObjeto}
      * @return resultado de la operacion
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
     */
    public Objeto tirar(String nombreObjeto) throws AccionInvalidaException {
        Validaciones.textoObligatorio(nombreObjeto, "Nombre del objeto", Limites.TEXTO_CORTO);
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
        Validaciones.noNulo(objeto, "Objeto");
        if (objeto instanceof Arma arma) {
            equiparArma(arma);
            return;
        }
        if (objeto instanceof Armadura armadura) {
            equiparArmadura(armadura);
            return;
        }
        if (objeto instanceof Binocular binocular) {
            equiparBinocular(binocular);
            return;
        }
        throw new AccionInvalidaException("Solo puedes equipar armas, armaduras o binoculares.");
    }

    /**
     * Ejecuta desequipar.
      * @param nombreObjeto valor de {@code nombreObjeto}
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
     */
    public void desequipar(String nombreObjeto) throws AccionInvalidaException {
        String nombreValidado = Validaciones.textoObligatorio(
                nombreObjeto, "Nombre del objeto", Limites.TEXTO_CORTO);
        for (int i = 0; i < armasEquipadas.size(); i++) {
            Arma arma = armasEquipadas.get(i);
            if (arma.getNombre().equalsIgnoreCase(nombreValidado)) {
                if (!mochila.puedeGuardar(arma)) {
                    throw new AccionInvalidaException("La mochila no tiene espacio para desequipar el arma.");
                }
                List<Arma> restantes = new ArrayList<>(armasEquipadas);
                restantes.remove(i);
                setArmasEquipadas(restantes);
                mochila.guardar(arma);
                return;
            }
        }
        if (armaduraEquipada != null && armaduraEquipada.getNombre().equalsIgnoreCase(nombreValidado)) {
            Armadura retirada = armaduraEquipada;
            if (!mochila.guardar(retirada)) {
                throw new AccionInvalidaException("La mochila no tiene espacio para desequipar la armadura.");
            }
            setArmaduraEquipada(null);
            setSaludMaxima(Math.max(1, saludMaxima - retirada.getBonusSalud()));
            setEnergiaMaxima(Math.max(1, energiaMaxima - retirada.getBonusEnergia()));
            return;
        }
        if (binocularEquipado != null && binocularEquipado.getNombre().equalsIgnoreCase(nombreValidado)) {
            Binocular retirado = binocularEquipado;
            if (!mochila.guardar(retirado)) {
                throw new AccionInvalidaException("La mochila no tiene espacio para desequipar el binocular.");
            }
            setBinocularEquipado(null);
            return;
        }
        throw new AccionInvalidaException("No tienes ese objeto equipado.");
    }

    /**
     * Ejecuta atacar.
      * @param objetivo valor de {@code objetivo}
     */
    public void atacar(Personaje objetivo) {
        Validaciones.noNulo(objetivo, "Objetivo");
        int distancia = posicion.distanciaManhattan(objetivo.getPosicion());
        Arma arma = prepararArmaAtaque(distancia);
        int danio = calcularDanio(objetivo, arma);
        objetivo.recibirDanio(danio, arma == null ? 0 : arma.getPenetracionArmadura());
    }

    /**
     * Ejecuta atacar.
      * @param objetivos valor de {@code objetivos}
     */
    public void atacar(List<? extends Personaje> objetivos) {
        Validaciones.noNulo(objetivos, "Objetivos");
        if (objetivos.isEmpty()) {
            return;
        }
        int distancia = posicion.distanciaManhattan(objetivos.get(0).getPosicion());
        Arma arma = prepararArmaAtaque(distancia);
        int danio = Math.max(1, calcularDanio(objetivos.get(0), arma) / objetivos.size());
        for (Personaje personaje : objetivos) {
            Validaciones.noNulo(personaje, "Objetivo");
            personaje.recibirDanio(danio, arma == null ? 0 : arma.getPenetracionArmadura());
        }
    }

    /**
     * Ejecuta calcularDanio.
      * @param objetivo valor de {@code objetivo}
      * @return resultado de la operacion
     */
    protected int calcularDanio(Personaje objetivo) {
        int distancia = posicion.distanciaManhattan(objetivo.getPosicion());
        return calcularDanio(objetivo, armaDisponiblePara(distancia).orElse(null));
    }

    private int calcularDanio(Personaje objetivo, Arma arma) {
        int base = arma == null ? 4 : arma.getDanio();
        return Math.max(1, aplicarModificadorDanio(base, objetivo));
    }

    /** @return si al menos un arma equipada puede disparar, o se puede combatir desarmado */
    public boolean puedeAtacar() {
        return armasEquipadas.isEmpty() || armasEquipadas.stream().anyMatch(Arma::puedeDisparar);
    }

    /** @return si existe un arma cargada que cubre la distancia indicada */
    public boolean puedeAtacarA(int distancia) {
        return armasEquipadas.isEmpty() ? distancia <= getRangoVision()
                : armasEquipadas.stream().anyMatch(arma ->
                        arma.puedeDisparar() && arma.alcanza(distancia));
    }

    /** @return primera arma cargada que el ataque consumiría a esa distancia */
    public java.util.Optional<Arma> armaDisponiblePara(int distancia) {
        return armasEquipadas.stream().filter(arma ->
                arma.puedeDisparar() && arma.alcanza(distancia)).findFirst();
    }

    private Arma prepararArmaAtaque(int distancia) {
        // Los personajes historicos sin equipo conservan su ataque natural/implicito.
        // Solo las armas explicitas quedan sujetas a cargador y tipo de municion.
        if (armasEquipadas.isEmpty() && distancia <= getRangoVision()) return null;
        for (Arma arma : armasEquipadas) {
            if (arma.alcanza(distancia) && arma.consumirDisparo()) return arma;
        }
        throw new IllegalStateException("No hay un arma cargada con alcance suficiente.");
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
        setPenalizacionEnergiaSiguienteTurno(0.0);
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
        Validaciones.noNulo(arma, "Arma");
        if (!puedeUsar(arma)) {
            throw new AccionInvalidaException(
                    "La clase no domina esta categoria de arma o su municion.");
        }
        int usadas = 0;
        for (Arma equipada : armasEquipadas) {
            usadas += equipada.isDosManos() ? 2 : 1;
        }
        int nuevas = usadas + (arma.isDosManos() ? 2 : 1);
        if (nuevas > 2) {
            throw new AccionInvalidaException("No tienes manos suficientes para equipar esa arma.");
        }
        List<Arma> nuevasArmas = new ArrayList<>(armasEquipadas);
        nuevasArmas.add(arma);
        setArmasEquipadas(nuevasArmas);
    }

    /** @return competencias de armamento del rol concreto */
    public PerfilArmamento getPerfilArmamento() {
        return ReglasArmamento.perfil(this);
    }

    /** @param arma arma que se desea equipar */
    public boolean puedeUsar(Arma arma) {
        Arma validada = Validaciones.noNulo(arma, "Arma");
        return faccionCompatible(validada.getFaccion())
                && getPerfilArmamento().permite(validada);
    }

    /** @param armadura proteccion que se desea equipar */
    public boolean puedeUsar(Armadura armadura) {
        Armadura validada = Validaciones.noNulo(armadura, "Armadura");
        return faccionCompatible(validada.getFaccion());
    }

    private boolean faccionCompatible(
            com.legendoftecla.model.items.FaccionEquipo faccion) {
        return this instanceof Enemigo
                ? faccion == com.legendoftecla.model.items.FaccionEquipo.ENEMIGA
                : faccion == com.legendoftecla.model.items.FaccionEquipo.HUMANA;
    }

    /**
     * Ejecuta equiparArmadura.
      * @param armadura valor de {@code armadura}
     */
    protected void equiparArmadura(Armadura armadura) throws AccionInvalidaException {
        Validaciones.noNulo(armadura, "Armadura");
        if (!puedeUsar(armadura)) {
            throw new AccionInvalidaException(
                    "La biologia o tecnologia de esta armadura pertenece a otra faccion.");
        }
        if (armaduraEquipada != null) {
            throw new AccionInvalidaException("Ya hay una armadura equipada.");
        }
        int nuevaSaludMaxima = sumarEstadistica(saludMaxima, armadura.getBonusSalud(), "Salud maxima");
        int nuevaEnergiaMaxima = sumarEstadistica(energiaMaxima, armadura.getBonusEnergia(), "Energia maxima");
        setArmaduraEquipada(armadura);
        setSaludMaxima(nuevaSaludMaxima);
        setEnergiaMaxima(nuevaEnergiaMaxima);
        setSalud(salud + armadura.getBonusSalud());
        setEnergia(energia + armadura.getBonusEnergia());
    }

    /** Equipa un único binocular, que se consumirá cuando sea usado. */
    protected void equiparBinocular(Binocular binocular) throws AccionInvalidaException {
        Validaciones.noNulo(binocular, "Binocular");
        if (binocularEquipado != null) {
            throw new AccionInvalidaException("Ya hay un binocular equipado.");
        }
        setBinocularEquipado(binocular);
    }

    /**
     * Ejecuta recibirDanio.
      * @param danio valor de {@code danio}
     */
    public void recibirDanio(int danio) {
        recibirDanio(danio, 0);
    }

    /**
     * Recibe dano descontando la defensa no atravesada por el arma.
     *
     * @param danio dano bruto
     * @param penetracionArmadura puntos de defensa ignorados
     */
    public void recibirDanio(int danio, int penetracionArmadura) {
        Validaciones.enteroEntre(danio, 0, Limites.ESTADISTICA, "Dano recibido");
        Validaciones.enteroEntre(penetracionArmadura, 0, Limites.ESTADISTICA,
                "Penetracion de armadura");
        int mitigado = danio;
        if (armaduraEquipada != null) {
            int defensaEfectiva = Math.max(0,
                    armaduraEquipada.getDefensa() - penetracionArmadura);
            mitigado = Math.max(0, danio - defensaEfectiva);
        }
        setSalud(salud - mitigado);
    }

    /**
     * Ejecuta recuperarSalud.
      * @param cantidad valor de {@code cantidad}
     */
    public void recuperarSalud(int cantidad) {
        Validaciones.enteroEntre(cantidad, 0, Limites.ESTADISTICA, "Salud recuperada");
        setSalud(salud + cantidad);
    }

    /**
     * Ejecuta recuperarEnergia.
      * @param cantidad valor de {@code cantidad}
     */
    public void recuperarEnergia(int cantidad) {
        Validaciones.enteroEntre(cantidad, 0, Limites.ESTADISTICA, "Energia recuperada");
        setEnergia(energia + cantidad);
    }

    /**
     * Ejecuta escalarSalud.
      * @param factor valor de {@code factor}
     */
    public void escalarSalud(double factor) {
        Validaciones.decimalEntre(factor, 0.01, 100.0, "Factor de salud");
        int nuevaSaludMaxima = Math.max(1, (int) Math.round(saludMaxima * factor));
        double proporcionActual = saludMaxima <= 0 ? 1.0 : (double) salud / saludMaxima;
        setSaludMaxima(nuevaSaludMaxima);
        setSalud(Math.max(1, (int) Math.round(saludMaxima * proporcionActual)));
    }

    /**
     * Ejecuta gastarEnergia.
      * @param cantidad valor de {@code cantidad}
      * @throws com.legendoftecla.exceptions.AccionInvalidaException si la operacion no puede completarse
     */
    public void gastarEnergia(int cantidad) throws AccionInvalidaException {
        Validaciones.enteroEntre(cantidad, 0, Limites.ESTADISTICA, "Energia gastada");
        if (energia < cantidad) {
            throw new AccionInvalidaException("No tienes energia suficiente.");
        }
        setEnergia(energia - cantidad);
    }

    /**
     * Ejecuta aumentarVisionTemporal.
      * @param incremento valor de {@code incremento}
     */
    public void aumentarVisionTemporal(int incremento) {
        int validado = Validaciones.enteroEntre(
                incremento, 0, Limites.ESTADISTICA, "Incremento de vision");
        setVisionTemporal(Math.max(visionTemporal, validado));
    }

    /**
     * Ejecuta aplicarPenalizacionEnergiaSiguienteTurno.
      * @param porcentaje valor de {@code porcentaje}
     */
    public void aplicarPenalizacionEnergiaSiguienteTurno(double porcentaje) {
        double validado = Validaciones.decimalEntre(porcentaje, 0.0, 1.0, "Penalizacion de energia");
        setPenalizacionEnergiaSiguienteTurno(
                Math.max(penalizacionEnergiaSiguienteTurno, validado));
    }

    /**
     * Ejecuta resetTurno.
     */
    public void resetTurno() {
        setVisionTemporal(0);
    }

    private int sumarEstadistica(int base, int incremento, String campo) {
        long resultado = (long) base + incremento;
        if (resultado > Limites.ESTADISTICA) {
            throw new IllegalArgumentException(campo + " supera el limite permitido.");
        }
        return (int) resultado;
    }
}
