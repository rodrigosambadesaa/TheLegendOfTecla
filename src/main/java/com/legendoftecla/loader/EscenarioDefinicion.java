package com.legendoftecla.loader;

import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Modelo encapsulado y serializable del formato {@code escenario.json}. */
public class EscenarioDefinicion {
    private int version;
    private String nombre;
    private String descripcion;
    private int filas;
    private int columnas;
    private int pasosMaximos;
    private boolean conAliados;
    private Punto inicio;
    private Punto objetivo;
    private List<CeldaDef> celdas;
    private List<PersonajeDef> enemigos;
    private List<ObjetoDef> objetos;

    /** Crea una definicion con los valores predeterminados del editor. */
    public EscenarioDefinicion() {
        setVersion(1);
        setNombre("Nuevo escenario");
        setDescripcion("Escenario creado con el editor grafico");
        setFilas(10);
        setColumnas(10);
        setPasosMaximos(160);
        setConAliados(false);
        setInicio(new Punto(0, 0));
        setObjetivo(new Punto(9, 9));
        setCeldas(List.of());
        setEnemigos(List.of());
        setObjetos(List.of());
    }

    /**
     * Crea un escenario rectangular inicializado completamente.
     *
     * @param filas numero de filas
     * @param columnas numero de columnas
     * @return escenario nuevo
     */
    public static EscenarioDefinicion nuevo(int filas, int columnas) {
        EscenarioDefinicion escenario = new EscenarioDefinicion();
        escenario.setFilas(filas);
        escenario.setColumnas(columnas);
        escenario.setInicio(new Punto(0, 0));
        escenario.setObjetivo(new Punto(filas - 1, columnas - 1));
        for (int fila = 0; fila < filas; fila++) {
            for (int columna = 0; columna < columnas; columna++) {
                escenario.agregarCelda(new CeldaDef(
                        fila, columna, "Celda " + fila + "," + columna, true));
            }
        }
        return escenario;
    }

    /** Reaplica todos los valores cargados por Gson a traves de sus setters. */
    public void normalizar() {
        setVersion(version);
        setNombre(nombre == null ? "Escenario sin nombre" : nombre);
        setDescripcion(descripcion == null ? "" : descripcion);
        setFilas(filas);
        setColumnas(columnas);
        setPasosMaximos(pasosMaximos);
        setConAliados(conAliados);
        setInicio(inicio == null ? new Punto(0, 0) : inicio);
        setObjetivo(objetivo == null ? new Punto(filas - 1, columnas - 1) : objetivo);
        setCeldas(celdas == null ? List.of() : celdas);
        setEnemigos(enemigos == null ? List.of() : enemigos);
        setObjetos(objetos == null ? List.of() : objetos);
        this.celdas.forEach(CeldaDef::normalizar);
        this.enemigos.forEach(PersonajeDef::normalizar);
        this.objetos.forEach(ObjetoDef::normalizar);
    }

    /**
     * Busca una celda por coordenadas.
     *
     * @param fila fila buscada
     * @param columna columna buscada
     * @return celda encontrada o {@code null}
     */
    public CeldaDef celda(int fila, int columna) {
        return celdas.stream()
                .filter(celda -> celda.getFila() == fila && celda.getColumna() == columna)
                .findFirst().orElse(null);
    }

    /** @return version del formato */
    public int getVersion() {
        return version;
    }

    /** @param version version entre 1 y 100 */
    public void setVersion(int version) {
        this.version = Validaciones.enteroEntre(version, 1, 100, "Version del escenario");
    }

    /** @return nombre del escenario */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre nombre obligatorio */
    public void setNombre(String nombre) {
        this.nombre = Validaciones.textoObligatorio(nombre, "Nombre del escenario", Limites.TEXTO_CORTO);
    }

    /** @return descripcion del escenario */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion descripcion no nula */
    public void setDescripcion(String descripcion) {
        this.descripcion = Validaciones.texto(descripcion, "Descripcion del escenario", Limites.DESCRIPCION);
    }

    /** @return filas del escenario */
    public int getFilas() {
        return filas;
    }

    /** @param filas filas dentro de los limites de mapa */
    public void setFilas(int filas) {
        this.filas = Validaciones.enteroEntre(
                filas, Limites.MAPA_MINIMO, Limites.MAPA_MAXIMO, "Filas");
    }

    /** @return columnas del escenario */
    public int getColumnas() {
        return columnas;
    }

    /** @param columnas columnas dentro de los limites de mapa */
    public void setColumnas(int columnas) {
        this.columnas = Validaciones.enteroEntre(
                columnas, Limites.MAPA_MINIMO, Limites.MAPA_MAXIMO, "Columnas");
    }

    /** @return pasos maximos */
    public int getPasosMaximos() {
        return pasosMaximos;
    }

    /** @param pasosMaximos limite positivo de pasos */
    public void setPasosMaximos(int pasosMaximos) {
        this.pasosMaximos = Validaciones.enteroEntre(
                pasosMaximos, 1, Limites.PASOS_MAXIMOS, "Pasos maximos");
    }

    /** @return {@code true} si se proponen aliados */
    public boolean isConAliados() {
        return conAliados;
    }

    /** @param conAliados estado solicitado */
    public void setConAliados(boolean conAliados) {
        this.conAliados = conAliados;
    }

    /** @return punto de inicio */
    public Punto getInicio() {
        return new Punto(inicio.getFila(), inicio.getColumna());
    }

    /** @param inicio punto inicial no nulo */
    public void setInicio(Punto inicio) {
        Punto validado = Validaciones.noNulo(inicio, "Inicio");
        this.inicio = new Punto(validado.getFila(), validado.getColumna());
    }

    /** @return punto objetivo */
    public Punto getObjetivo() {
        return new Punto(objetivo.getFila(), objetivo.getColumna());
    }

    /** @param objetivo punto objetivo no nulo */
    public void setObjetivo(Punto objetivo) {
        Punto validado = Validaciones.noNulo(objetivo, "Objetivo");
        this.objetivo = new Punto(validado.getFila(), validado.getColumna());
    }

    /** @return vista inmutable de las celdas */
    public List<CeldaDef> getCeldas() {
        return Collections.unmodifiableList(celdas);
    }

    /** @param celdas coleccion no nula y sin elementos nulos */
    public void setCeldas(List<CeldaDef> celdas) {
        List<CeldaDef> copia = copiarLista(celdas, "Celdas");
        if (copia.size() > Limites.MAPA_MAXIMO * Limites.MAPA_MAXIMO) {
            throw new IllegalArgumentException("El escenario contiene demasiadas celdas.");
        }
        this.celdas = copia;
    }

    /** @param celda celda que se incorpora */
    public void agregarCelda(CeldaDef celda) {
        if (celdas.size() >= Limites.MAPA_MAXIMO * Limites.MAPA_MAXIMO) {
            throw new IllegalStateException("No se pueden agregar mas celdas.");
        }
        List<CeldaDef> copia = new ArrayList<>(celdas);
        copia.add(Validaciones.noNulo(celda, "Celda"));
        setCeldas(copia);
    }

    /** @return vista inmutable de los enemigos */
    public List<PersonajeDef> getEnemigos() {
        return Collections.unmodifiableList(enemigos);
    }

    /** @param enemigos coleccion no nula y sin elementos nulos */
    public void setEnemigos(List<PersonajeDef> enemigos) {
        this.enemigos = copiarLista(enemigos, "Enemigos");
    }

    /** @param enemigo enemigo que se incorpora */
    public void agregarEnemigo(PersonajeDef enemigo) {
        List<PersonajeDef> copia = new ArrayList<>(enemigos);
        copia.add(Validaciones.noNulo(enemigo, "Enemigo"));
        setEnemigos(copia);
    }

    /** @return vista inmutable de los objetos */
    public List<ObjetoDef> getObjetos() {
        return Collections.unmodifiableList(objetos);
    }

    /** @param objetos coleccion no nula y sin elementos nulos */
    public void setObjetos(List<ObjetoDef> objetos) {
        this.objetos = copiarLista(objetos, "Objetos");
    }

    /** @param objeto objeto que se incorpora */
    public void agregarObjeto(ObjetoDef objeto) {
        List<ObjetoDef> copia = new ArrayList<>(objetos);
        copia.add(Validaciones.noNulo(objeto, "Objeto"));
        setObjetos(copia);
    }

    /**
     * Elimina personajes y objetos situados en una coordenada.
     *
     * @param fila fila limpiada
     * @param columna columna limpiada
     */
    public void eliminarContenido(int fila, int columna) {
        List<PersonajeDef> enemigosRestantes = new ArrayList<>(enemigos);
        enemigosRestantes.removeIf(personaje -> personaje.getFila() == fila
                && personaje.getColumna() == columna);
        setEnemigos(enemigosRestantes);
        List<ObjetoDef> objetosRestantes = new ArrayList<>(objetos);
        objetosRestantes.removeIf(objeto -> objeto.getFila() == fila && objeto.getColumna() == columna);
        setObjetos(objetosRestantes);
    }

    private <T> List<T> copiarLista(List<T> valores, String campo) {
        Validaciones.noNulo(valores, campo);
        if (valores.stream().anyMatch(java.util.Objects::isNull)) {
            throw new IllegalArgumentException(campo + " no puede contener elementos nulos.");
        }
        return new ArrayList<>(valores);
    }

    /** Coordenada serializable con setters acotados. */
    public static class Punto {
        private int fila;
        private int columna;

        /** Crea el punto de origen. */
        public Punto() {
            setFila(0);
            setColumna(0);
        }

        /**
         * Crea un punto.
         *
         * @param fila fila
         * @param columna columna
         */
        public Punto(int fila, int columna) {
            setFila(fila);
            setColumna(columna);
        }

        /** @return fila */
        public int getFila() {
            return fila;
        }

        /** @param fila fila acotada */
        public void setFila(int fila) {
            this.fila = Validaciones.enteroEntre(fila, -Limites.COORDENADA_ABSOLUTA,
                    Limites.COORDENADA_ABSOLUTA, "Fila");
        }

        /** @return columna */
        public int getColumna() {
            return columna;
        }

        /** @param columna columna acotada */
        public void setColumna(int columna) {
            this.columna = Validaciones.enteroEntre(columna, -Limites.COORDENADA_ABSOLUTA,
                    Limites.COORDENADA_ABSOLUTA, "Columna");
        }

        void normalizar() {
            setFila(fila);
            setColumna(columna);
        }
    }

    /** Celda serializable encapsulada. */
    public static class CeldaDef extends Punto {
        private String descripcion;
        private boolean transitable;
        private boolean oscura;
        private boolean sueloMadera;
        private boolean antorchaMural;
        private boolean fuenteAgua;
        private int nivelFuego;
        private String elementoTipo;
        private String elementoId;
        private String elementoEstado;
        private String referencia;
        private int resistencia;
        private int dificultad;

        /** Crea una celda predeterminada. */
        public CeldaDef() {
            setDescripcion("Celda");
            setTransitable(true);
            setOscura(false);
            setSueloMadera(false);
            setAntorchaMural(false);
            setFuenteAgua(false);
            setNivelFuego(0);
            setElementoTipo(null);
            setElementoId(null);
            setElementoEstado(null);
            setReferencia(null);
            setResistencia(10);
            setDificultad(5);
        }

        /**
         * Crea una celda completa.
         *
         * @param fila fila
         * @param columna columna
         * @param descripcion descripcion
         * @param transitable transitabilidad
         */
        public CeldaDef(int fila, int columna, String descripcion, boolean transitable) {
            this();
            setFila(fila);
            setColumna(columna);
            setDescripcion(descripcion);
            setTransitable(transitable);
        }

        /** @return descripcion */
        public String getDescripcion() {
            return descripcion;
        }

        /** @param descripcion descripcion no nula */
        public void setDescripcion(String descripcion) {
            this.descripcion = Validaciones.texto(descripcion, "Descripcion de celda", Limites.DESCRIPCION);
        }

        /** @return transitabilidad */
        public boolean isTransitable() {
            return transitable;
        }

        /** @param transitable estado solicitado */
        public void setTransitable(boolean transitable) {
            this.transitable = transitable;
        }

        public boolean isOscura() { return oscura; }
        public void setOscura(boolean oscura) { this.oscura = oscura; }
        public boolean isSueloMadera() { return sueloMadera; }
        public void setSueloMadera(boolean sueloMadera) { this.sueloMadera = sueloMadera; }
        public boolean hasAntorchaMural() { return antorchaMural; }
        public boolean isAntorchaMural() { return antorchaMural; }
        public void setAntorchaMural(boolean antorchaMural) { this.antorchaMural = antorchaMural; }
        public boolean hasFuenteAgua() { return fuenteAgua; }
        public boolean isFuenteAgua() { return fuenteAgua; }
        public void setFuenteAgua(boolean fuenteAgua) { this.fuenteAgua = fuenteAgua; }
        public int getNivelFuego() { return nivelFuego; }
        public void setNivelFuego(int nivelFuego) {
            this.nivelFuego = Validaciones.enteroEntre(nivelFuego, 0, 3, "Nivel de fuego");
        }
        public String getElementoTipo() { return elementoTipo; }
        public void setElementoTipo(String elementoTipo) { this.elementoTipo = elementoTipo; }
        public String getElementoId() { return elementoId; }
        public void setElementoId(String elementoId) { this.elementoId = elementoId; }
        public String getElementoEstado() { return elementoEstado; }
        public void setElementoEstado(String elementoEstado) { this.elementoEstado = elementoEstado; }
        public String getReferencia() { return referencia; }
        public void setReferencia(String referencia) { this.referencia = referencia; }
        public int getResistencia() { return resistencia; }
        public void setResistencia(int resistencia) { this.resistencia = Math.max(1, resistencia); }
        public int getDificultad() { return dificultad; }
        public void setDificultad(int dificultad) { this.dificultad = Math.max(0, dificultad); }

        void normalizar() {
            super.normalizar();
            setDescripcion(descripcion == null ? "" : descripcion);
            setTransitable(transitable);
            setOscura(oscura);
            setSueloMadera(sueloMadera);
            setAntorchaMural(antorchaMural);
            setFuenteAgua(fuenteAgua);
            setNivelFuego(nivelFuego);
            setElementoTipo(elementoTipo);
            setElementoId(elementoId);
            setElementoEstado(elementoEstado);
            setReferencia(referencia);
            setResistencia(resistencia <= 0 ? 10 : resistencia);
            setDificultad(dificultad);
        }
    }

    /** Personaje serializable encapsulado. */
    public static class PersonajeDef extends Punto {
        private String tipo;
        private String nombre;
        private int salud;
        private int energia;
        private int vision;

        /** Crea una definicion de personaje predeterminada. */
        public PersonajeDef() {
            setTipo("sectoid");
            setNombre("Personaje");
            setSalud(70);
            setEnergia(70);
            setVision(2);
        }

        /** @return tipo */
        public String getTipo() {
            return tipo;
        }

        /** @param tipo tipo obligatorio */
        public void setTipo(String tipo) {
            this.tipo = Validaciones.textoObligatorio(tipo, "Tipo de personaje", Limites.TEXTO_CORTO);
        }

        /** @return nombre */
        public String getNombre() {
            return nombre;
        }

        /** @param nombre nombre obligatorio */
        public void setNombre(String nombre) {
            this.nombre = Validaciones.textoObligatorio(nombre, "Nombre del personaje", Limites.TEXTO_CORTO);
        }

        /** @return salud */
        public int getSalud() {
            return salud;
        }

        /** @param salud salud positiva */
        public void setSalud(int salud) {
            this.salud = Validaciones.enteroEntre(salud, 1, Limites.ESTADISTICA, "Salud");
        }

        /** @return energia */
        public int getEnergia() {
            return energia;
        }

        /** @param energia energia positiva */
        public void setEnergia(int energia) {
            this.energia = Validaciones.enteroEntre(energia, 1, Limites.ESTADISTICA, "Energia");
        }

        /** @return vision */
        public int getVision() {
            return vision;
        }

        /** @param vision alcance positivo */
        public void setVision(int vision) {
            this.vision = Validaciones.enteroEntre(vision, 1, Limites.ESTADISTICA, "Vision");
        }

        void normalizar() {
            super.normalizar();
            setTipo(tipo);
            setNombre(nombre);
            setSalud(salud);
            setEnergia(energia);
            setVision(vision);
        }
    }

    /** Objeto serializable encapsulado. */
    public static class ObjetoDef extends Punto {
        private String tipo;
        private String nombre;
        private String descripcion;
        private double peso;
        private int valor;
        private int valorSecundario;
        private int valorTerciario;
        private boolean dosManos;
        private String tipoMunicion;
        private int capacidadCargador;
        private int municionActual;
        private int cantidad;
        private String categoriaArma;
        private String tipoGranada;

        /** Crea una definicion de objeto predeterminada. */
        public ObjetoDef() {
            setTipo("botiquin");
            setNombre("Objeto");
            setDescripcion("Objeto del escenario");
            setPeso(1.0);
            setValor(20);
            setValorSecundario(0);
            setValorTerciario(0);
            setDosManos(false);
            setTipoMunicion(null);
            setCapacidadCargador(0);
            setMunicionActual(0);
            setCantidad(0);
            setCategoriaArma(null);
            setTipoGranada(null);
        }

        /** @return tipo */
        public String getTipo() {
            return tipo;
        }

        /** @param tipo tipo obligatorio */
        public void setTipo(String tipo) {
            this.tipo = Validaciones.textoObligatorio(tipo, "Tipo de objeto", Limites.TEXTO_CORTO);
        }

        /** @return nombre */
        public String getNombre() {
            return nombre;
        }

        /** @param nombre nombre obligatorio */
        public void setNombre(String nombre) {
            this.nombre = Validaciones.textoObligatorio(nombre, "Nombre del objeto", Limites.TEXTO_CORTO);
        }

        /** @return descripcion */
        public String getDescripcion() {
            return descripcion;
        }

        /** @param descripcion descripcion no nula */
        public void setDescripcion(String descripcion) {
            this.descripcion = Validaciones.texto(descripcion, "Descripcion del objeto", Limites.DESCRIPCION);
        }

        /** @return peso */
        public double getPeso() {
            return peso;
        }

        /** @param peso peso no negativo */
        public void setPeso(double peso) {
            this.peso = Validaciones.decimalEntre(peso, 0.0, Limites.PESO_MAXIMO, "Peso");
        }

        /** @return valor principal */
        public int getValor() {
            return valor;
        }

        /** @param valor valor principal no negativo */
        public void setValor(int valor) {
            this.valor = Validaciones.enteroEntre(valor, 0, Limites.ESTADISTICA, "Valor");
        }

        /** @return valor secundario */
        public int getValorSecundario() {
            return valorSecundario;
        }

        /** @param valorSecundario valor secundario no negativo */
        public void setValorSecundario(int valorSecundario) {
            this.valorSecundario = Validaciones.enteroEntre(
                    valorSecundario, 0, Limites.ESTADISTICA, "Valor secundario");
        }

        /** @return valor terciario */
        public int getValorTerciario() {
            return valorTerciario;
        }

        /** @param valorTerciario valor terciario no negativo */
        public void setValorTerciario(int valorTerciario) {
            this.valorTerciario = Validaciones.enteroEntre(
                    valorTerciario, 0, Limites.ESTADISTICA, "Valor terciario");
        }

        /** @return {@code true} para armas de dos manos */
        public boolean isDosManos() {
            return dosManos;
        }

        /** @param dosManos estado solicitado */
        public void setDosManos(boolean dosManos) {
            this.dosManos = dosManos;
        }

        /** @return familia de municion opcional */
        public String getTipoMunicion() { return tipoMunicion; }
        /** @param tipoMunicion familia opcional compatible con {@code TipoMunicion} */
        public void setTipoMunicion(String tipoMunicion) { this.tipoMunicion = tipoMunicion; }
        /** @return capacidad de cargador, cero para objetos antiguos */
        public int getCapacidadCargador() { return capacidadCargador; }
        /** @param capacidadCargador capacidad no negativa */
        public void setCapacidadCargador(int capacidadCargador) {
            this.capacidadCargador = Validaciones.enteroEntre(
                    capacidadCargador, 0, Limites.ESTADISTICA, "Capacidad de cargador");
        }
        /** @return proyectiles cargados */
        public int getMunicionActual() { return municionActual; }
        /** @param municionActual proyectiles cargados no negativos */
        public void setMunicionActual(int municionActual) {
            this.municionActual = Validaciones.enteroEntre(
                    municionActual, 0, Limites.ESTADISTICA, "Municion actual");
        }
        /** @return cantidad del paquete de municion */
        public int getCantidad() { return cantidad; }
        /** @param cantidad cantidad no negativa */
        public void setCantidad(int cantidad) {
            this.cantidad = Validaciones.enteroEntre(
                    cantidad, 0, Limites.ESTADISTICA, "Cantidad");
        }
        /** @return categoria opcional del arma */
        public String getCategoriaArma() { return categoriaArma; }
        /** @param categoriaArma categoria opcional compatible con {@code CategoriaArma} */
        public void setCategoriaArma(String categoriaArma) { this.categoriaArma = categoriaArma; }
        /** @return variante opcional de granada */
        public String getTipoGranada() { return tipoGranada; }
        /** @param tipoGranada variante opcional compatible con {@code TipoGranada} */
        public void setTipoGranada(String tipoGranada) { this.tipoGranada = tipoGranada; }

        void normalizar() {
            super.normalizar();
            setTipo(tipo);
            setNombre(nombre);
            setDescripcion(descripcion == null ? "" : descripcion);
            setPeso(peso);
            setValor(valor);
            setValorSecundario(valorSecundario);
            setValorTerciario(valorTerciario);
            setDosManos(dosManos);
            setTipoMunicion(tipoMunicion);
            setCapacidadCargador(capacidadCargador);
            setMunicionActual(municionActual);
            setCantidad(cantidad);
            setCategoriaArma(categoriaArma);
            setTipoGranada(tipoGranada);
        }
    }
}
