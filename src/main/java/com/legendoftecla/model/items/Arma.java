package com.legendoftecla.model.items;

import com.legendoftecla.exceptions.ObjetoNoUsableException;
import com.legendoftecla.model.characters.Personaje;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad Arma del juego.
 */
public final class Arma extends Objeto {
    private int danio;
    private boolean dosManos;
    private int capacidadCargador;
    private int municionActual;
    private TipoMunicion tipoMunicion;
    private CategoriaArma categoria;
    private FaccionEquipo faccion;
    private ClaseArma claseArma;
    private int penetracionArmadura;

    /**
     * Ejecuta Arma.
      * @param danio valor de {@code danio}
      * @param descripcion valor de {@code descripcion}
      * @param dosManos valor de {@code dosManos}
      * @param nombre valor de {@code nombre}
      * @param peso valor de {@code peso}
     */
    public Arma(String nombre, String descripcion, double peso, int danio, boolean dosManos) {
        this(nombre, descripcion, peso, danio, dosManos,
                CategoriaArma.FUEGO, TipoMunicion.INFINITA, 0, 0);
    }

    /** Crea un arma de municion finita con cargador inicial validado. */
    public Arma(String nombre, String descripcion, double peso, int danio,
            boolean dosManos, TipoMunicion tipoMunicion,
            int capacidadCargador, int municionActual) {
        this(nombre, descripcion, peso, danio, dosManos,
                inferirCategoria(tipoMunicion), tipoMunicion,
                capacidadCargador, municionActual);
    }

    /** Crea un arma con familia, alcance y proyectil explicitamente compatibles. */
    public Arma(String nombre, String descripcion, double peso, int danio,
            boolean dosManos, CategoriaArma categoria, TipoMunicion tipoMunicion,
            int capacidadCargador, int municionActual) {
        this(nombre, descripcion, peso, danio, dosManos, categoria,
                tipoMunicion, capacidadCargador, municionActual,
                FaccionEquipo.HUMANA);
    }

    /** Crea un arma vinculada a una faccion concreta. */
    public Arma(String nombre, String descripcion, double peso, int danio,
            boolean dosManos, CategoriaArma categoria, TipoMunicion tipoMunicion,
            int capacidadCargador, int municionActual, FaccionEquipo faccion) {
        this(nombre, descripcion, peso, danio, dosManos, categoria, tipoMunicion,
                capacidadCargador, municionActual, faccion,
                inferirClase(categoria, tipoMunicion, dosManos), 0);
    }

    /** Crea un modelo completo con subfamilia y capacidad antiblindaje. */
    public Arma(String nombre, String descripcion, double peso, int danio,
            boolean dosManos, CategoriaArma categoria, TipoMunicion tipoMunicion,
            int capacidadCargador, int municionActual, FaccionEquipo faccion,
            ClaseArma claseArma, int penetracionArmadura) {
        super(nombre, descripcion, peso);
        setDanio(danio);
        setDosManos(dosManos);
        this.categoria = Validaciones.noNulo(categoria, "Categoria de arma");
        this.tipoMunicion = Validaciones.noNulo(tipoMunicion, "Tipo de municion");
        this.faccion = Validaciones.noNulo(faccion, "Faccion del arma");
        this.claseArma = Validaciones.noNulo(claseArma, "Clase del arma");
        this.penetracionArmadura = Validaciones.enteroEntre(
                penetracionArmadura, 0, Limites.ESTADISTICA, "Penetracion de armadura");
        validarCompatibilidad(categoria, tipoMunicion);
        validarClase(categoria, claseArma);
        if (tipoMunicion == TipoMunicion.INFINITA) {
            this.capacidadCargador = 0;
            this.municionActual = 0;
        } else {
            if (capacidadCargador < 1 || municionActual < 0
                    || municionActual > capacidadCargador) {
                throw new IllegalArgumentException("Cargador invalido.");
            }
            this.capacidadCargador = capacidadCargador;
            this.municionActual = municionActual;
        }
    }

    /**
     * Ejecuta getDanio.
      * @return resultado de la operacion
     */
    public int getDanio() {
        return danio;
    }

    /** @param danio dano positivo y acotado */
    public void setDanio(int danio) {
        this.danio = Validaciones.enteroEntre(danio, 1, Limites.ESTADISTICA, "Dano del arma");
    }

    /**
     * Ejecuta isDosManos.
      * @return resultado de la operacion
     */
    public boolean isDosManos() {
        return dosManos;
    }

    /** @param dosManos estado solicitado */
    public void setDosManos(boolean dosManos) {
        this.dosManos = dosManos;
    }

    public int getCapacidadCargador() { return capacidadCargador; }
    public int getMunicionActual() { return municionActual; }
    public TipoMunicion getTipoMunicion() { return tipoMunicion; }
    public CategoriaArma getCategoria() { return categoria; }
    /** @return bando capaz de utilizar la tecnologia del arma */
    public FaccionEquipo getFaccion() { return faccion; }
    /** @return subfamilia tactica concreta */
    public ClaseArma getClaseArma() { return claseArma; }
    /** @param claseArma nueva subfamilia tactica */
    public void setClaseArma(ClaseArma claseArma) {
        ClaseArma validada = Validaciones.noNulo(claseArma, "Clase del arma");
        validarClase(categoria, validada);
        this.claseArma = validada;
    }
    /** @return puntos de defensa ignorados por impacto */
    public int getPenetracionArmadura() { return penetracionArmadura; }
    /** @param penetracion puntos de defensa ignorados */
    public void setPenetracionArmadura(int penetracion) {
        penetracionArmadura = Validaciones.enteroEntre(
                penetracion, 0, Limites.ESTADISTICA, "Penetracion de armadura");
    }
    /** @param faccion nueva procedencia tecnologica del arma */
    public void setFaccion(FaccionEquipo faccion) {
        this.faccion = Validaciones.noNulo(faccion, "Faccion del arma");
    }
    /** @return si el arma esta vinculada a anatomia o tecnologia enemiga */
    public boolean esEnemiga() { return faccion == FaccionEquipo.ENEMIGA; }
    /** @param categoria nueva categoria compatible con la municion configurada */
    public void setCategoria(CategoriaArma categoria) {
        CategoriaArma validada = Validaciones.noNulo(categoria, "Categoria de arma");
        validarCompatibilidad(validada, tipoMunicion);
        validarClase(validada, claseArma);
        this.categoria = validada;
    }
    /** @param capacidad nueva capacidad compatible con la carga actual */
    public void setCapacidadCargador(int capacidad) {
        if (tipoMunicion == TipoMunicion.INFINITA) {
            if (capacidad != 0) throw new IllegalArgumentException("Un arma infinita no tiene cargador.");
        } else if (capacidad < 1 || capacidad < municionActual) {
            throw new IllegalArgumentException("Capacidad de cargador invalida.");
        }
        capacidadCargador = capacidad;
    }
    /** @param actual carga entre cero y la capacidad */
    public void setMunicionActual(int actual) {
        if (tipoMunicion == TipoMunicion.INFINITA) {
            if (actual != 0) throw new IllegalArgumentException("Municion infinita invalida.");
        } else if (actual < 0 || actual > capacidadCargador) {
            throw new IllegalArgumentException("Municion actual invalida.");
        }
        municionActual = actual;
    }
    /** @param tipo tipo compatible; cambiarlo conserva las reglas del cargador */
    public void setTipoMunicion(TipoMunicion tipo) {
        TipoMunicion validado = Validaciones.noNulo(tipo, "Tipo de municion");
        validarCompatibilidad(categoria, validado);
        tipoMunicion = validado;
        if (tipo == TipoMunicion.INFINITA) { capacidadCargador = 0; municionActual = 0; }
    }
    public boolean usaMunicionInfinita() { return tipoMunicion == TipoMunicion.INFINITA; }
    public boolean puedeDisparar() { return usaMunicionInfinita() || municionActual > 0; }
    /** @return alcance tactico base en celdas */
    public int getAlcance() {
        return claseArma.getAlcance();
    }
    /** @param distancia distancia Manhattan al objetivo */
    public boolean alcanza(int distancia) {
        return distancia >= 0 && distancia <= getAlcance();
    }

    /** Consume un proyectil o falla sin modificar el estado. */
    public boolean consumirDisparo() {
        if (usaMunicionInfinita()) return true;
        if (municionActual <= 0) return false;
        municionActual--;
        return true;
    }

    /** Recarga parcialmente con un paquete compatible y devuelve proyectiles movidos. */
    public int recargar(Municion municion) {
        Validaciones.noNulo(municion, "Municion");
        if (usaMunicionInfinita() || municion.getTipo() != tipoMunicion) return 0;
        int cargados = municion.consumir(capacidadCargador - municionActual);
        municionActual += cargados;
        return cargados;
    }

    /** @return resumen apto para consola y GUI */
    public String estadoArma() {
        return usaMunicionInfinita() ? getNombre() + ": "
                + claseArma.name().toLowerCase() + " | dano " + danio
                + " | no requiere municion"
                : getNombre() + ": " + municionActual + "/" + capacidadCargador
                        + " " + tipoMunicion.name().toLowerCase()
                        + " | dano " + danio + " | penetracion " + penetracionArmadura;
    }

    private static CategoriaArma inferirCategoria(TipoMunicion tipo) {
        return switch (Validaciones.noNulo(tipo, "Tipo de municion")) {
            case FLECHA -> CategoriaArma.ARCO;
            case VIROTE -> CategoriaArma.BALLESTA;
            case CUCHILLO_ARROJADIZO -> CategoriaArma.ARROJADIZA;
            default -> CategoriaArma.FUEGO;
        };
    }

    private static ClaseArma inferirClase(CategoriaArma categoria,
            TipoMunicion tipo, boolean dosManos) {
        return switch (categoria) {
            case MELE -> dosManos ? ClaseArma.ESPADA_DOS_MANOS : ClaseArma.ESPADA_UNA_MANO;
            case ARROJADIZA -> ClaseArma.CUCHILLO_ARROJADIZO;
            case ARCO -> ClaseArma.ARCO;
            case BALLESTA -> ClaseArma.BALLESTA;
            case FUEGO -> switch (tipo) {
                case PISTOLA -> ClaseArma.PISTOLA;
                case SUBFUSIL -> ClaseArma.SUBFUSIL;
                case ESCOPETA -> ClaseArma.ESCOPETA;
                case RIFLE -> ClaseArma.RIFLE_ASALTO;
                case PESADA -> ClaseArma.AMETRALLADORA;
                case COHETE -> ClaseArma.LANZACOHETES;
                case ENERGIA, INFINITA -> ClaseArma.ENERGIA;
                default -> throw new IllegalArgumentException("Municion de fuego incompatible.");
            };
        };
    }

    private static void validarCompatibilidad(CategoriaArma categoria, TipoMunicion tipo) {
        boolean compatible = switch (categoria) {
            case MELE -> tipo == TipoMunicion.INFINITA;
            case ARROJADIZA -> tipo == TipoMunicion.CUCHILLO_ARROJADIZO;
            case ARCO -> tipo == TipoMunicion.FLECHA;
            case BALLESTA -> tipo == TipoMunicion.VIROTE;
            case FUEGO -> tipo == TipoMunicion.INFINITA
                    || tipo == TipoMunicion.PISTOLA || tipo == TipoMunicion.RIFLE
                    || tipo == TipoMunicion.SUBFUSIL || tipo == TipoMunicion.ESCOPETA
                    || tipo == TipoMunicion.PESADA || tipo == TipoMunicion.COHETE
                    || tipo == TipoMunicion.ENERGIA;
        };
        if (!compatible) {
            throw new IllegalArgumentException("Categoria y municion incompatibles.");
        }
    }

    private static void validarClase(CategoriaArma categoria, ClaseArma clase) {
        CategoriaArma esperada = switch (clase) {
            case ESPADA_UNA_MANO, ESPADA_DOS_MANOS, CUCHILLO -> CategoriaArma.MELE;
            case CUCHILLO_ARROJADIZO -> CategoriaArma.ARROJADIZA;
            case ARCO -> CategoriaArma.ARCO;
            case BALLESTA -> CategoriaArma.BALLESTA;
            default -> CategoriaArma.FUEGO;
        };
        if (categoria != esperada) {
            throw new IllegalArgumentException("Categoria y clase de arma incompatibles.");
        }
    }

    @Override
    /**
     * Ejecuta usar.
     */
    public void usar(Personaje personaje) throws ObjetoNoUsableException {
        throw new ObjetoNoUsableException("Las armas no se usan directamente; se equipan.");
    }
}

