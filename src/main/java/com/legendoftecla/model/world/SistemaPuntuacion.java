package com.legendoftecla.model.world;

import com.legendoftecla.model.characters.Jugador;
import com.legendoftecla.model.characters.Aliado;
import com.legendoftecla.validation.Limites;
import com.legendoftecla.validation.Validaciones;


/**
 * Representa la entidad SistemaPuntuacion del juego.
 */
public final class SistemaPuntuacion {
    private static final int MAX_PUNTOS_SALUD = 300;
    private static final int MAX_PUNTOS_ENERGIA = 200;
    private static final int MAX_PUNTOS_PASOS = 250;
    private static final int MAX_PUNTOS_PROGRESO = 150;
    private static final int MAX_PUNTOS_ENEMIGOS = 100;
    private static final int MAX_PUNTOS_PROGRESO_ALIADO = 200;

    private SistemaPuntuacion() {
    }

    /**
     * Ejecuta calcular.
      * @param estado valor de {@code estado}
      * @param juego valor de {@code juego}
      * @return resultado de la operacion
     */
    public static ResultadoPuntuacion calcular(Juego juego, EstadoFinalPartida estado) {
        Jugador jugador = juego.getJugador();
        Mapa mapa = juego.getMapa();

        int puntosSalud = Math.round(
                MAX_PUNTOS_SALUD * porcentajeSeguro(jugador.getSalud(), jugador.getSaludMaxima()));
        int puntosEnergia = Math.round(
                MAX_PUNTOS_ENERGIA * porcentajeSeguro(jugador.getEnergia(), jugador.getEnergiaMaxima()));

        float eficienciaPasos = 1.0f - porcentajeSeguro(juego.getPasos(), juego.getPasosMaximos());
        int puntosPasos = Math.round(MAX_PUNTOS_PASOS * clamp01(eficienciaPasos));

        int distanciaInicial = mapa.getInicio().distanciaManhattan(mapa.getObjetivo());
        int distanciaActual = jugador.getPosicion().distanciaManhattan(mapa.getObjetivo());
        float progreso = distanciaInicial <= 0 ? 1.0f : 1.0f - ((float) distanciaActual / distanciaInicial);
        int puntosProgreso = Math.round(MAX_PUNTOS_PROGRESO * clamp01(progreso));

        long derrotados = juego.getEnemigos().stream().filter(e -> e.getSalud() <= 0).count();
        int puntosEnemigos = (int) Math.min(MAX_PUNTOS_ENEMIGOS, derrotados * 20);

        int bonusResultado = switch (estado) {
            case VICTORIA -> 200;
            case MUERTE -> -200;
            case SIN_PASOS -> -100;
            case SALIDA_MANUAL -> 0;
        };

        int total = puntosSalud + puntosEnergia + puntosPasos + puntosProgreso + puntosEnemigos + bonusResultado;

        return new ResultadoPuntuacion(total, puntosSalud, puntosEnergia, puntosPasos, puntosProgreso, puntosEnemigos,
                bonusResultado, derrotados);
    }

    /**
     * Calcula la puntuacion individual y actual de un aliado.
     *
     * @param juego partida que determina progreso y evacuacion
     * @param aliado participante evaluado
     * @return desglose inmutable entre cero y mil puntos
     */
    public static PuntuacionAliado calcularAliado(Juego juego, Aliado aliado) {
        Validaciones.noNulo(juego, "Juego");
        Validaciones.noNulo(aliado, "Aliado");
        int salud = Math.round(MAX_PUNTOS_SALUD
                * porcentajeSeguro(aliado.getSalud(), aliado.getSaludMaxima()));
        int energia = Math.round(MAX_PUNTOS_ENERGIA
                * porcentajeSeguro(aliado.getEnergia(), aliado.getEnergiaMaxima()));
        Mapa mapa = juego.getMapa();
        int distanciaInicial = mapa.getInicio().distanciaManhattan(mapa.getObjetivo());
        int distanciaActual = aliado.getPosicion().distanciaManhattan(mapa.getObjetivo());
        float avance = distanciaInicial <= 0 ? 1.0f
                : 1.0f - (float) distanciaActual / distanciaInicial;
        int progreso = Math.round(MAX_PUNTOS_PROGRESO_ALIADO * clamp01(avance));
        int supervivencia = juego.estaAliadoExtraido(aliado) ? 300
                : aliado.getSalud() > 0 ? 100 : -100;
        int total = Math.max(0, salud + energia + progreso + supervivencia);
        return new PuntuacionAliado(total, salud, energia, progreso, supervivencia);
    }

    /** Desglose de puntuacion individual visible durante y al terminar la partida. */
    public record PuntuacionAliado(
            int total, int salud, int energia, int progreso, int supervivencia) {
        /** Valida que el desglose respete los limites defensivos del dominio. */
        public PuntuacionAliado {
            Validaciones.enteroEntre(total, 0, Limites.ESTADISTICA, "Puntuacion aliada");
            Validaciones.enteroEntre(salud, 0, MAX_PUNTOS_SALUD, "Salud aliada");
            Validaciones.enteroEntre(energia, 0, MAX_PUNTOS_ENERGIA, "Energia aliada");
            Validaciones.enteroEntre(progreso, 0, MAX_PUNTOS_PROGRESO_ALIADO,
                    "Progreso aliado");
            Validaciones.enteroEntre(supervivencia, -100, 300, "Supervivencia aliada");
        }
    }

    private static float porcentajeSeguro(int actual, int maximo) {
        if (maximo <= 0) {
            return 0.0f;
        }
        return clamp01((float) actual / maximo);
    }

    private static float clamp01(float valor) {
        return Math.max(0.0f, Math.min(1.0f, valor));
    }

    /**
     * Representa {@code EstadoFinalPartida} dentro del dominio del juego.
     */
    public enum EstadoFinalPartida {
        /**
         * Valor publico {@code VICTORIA} utilizado por el modelo del juego.
         */
        VICTORIA,
        /**
         * Valor publico {@code MUERTE} utilizado por el modelo del juego.
         */
        MUERTE,
        /**
         * Valor publico {@code SIN_PASOS} utilizado por el modelo del juego.
         */
        SIN_PASOS,
        /**
         * Valor publico {@code valor} utilizado por el modelo del juego.
         */
        SALIDA_MANUAL
    }

    /**
     * Representa {@code ResultadoPuntuacion} dentro del dominio del juego.
     */
    public static final class ResultadoPuntuacion {
        private int total;
        private int salud;
        private int energia;
        private int pasos;
        private int progreso;
        private int enemigos;
        private int resultado;
        private long enemigosDerrotados;

        private ResultadoPuntuacion(int total, int salud, int energia, int pasos, int progreso, int enemigos,
                int resultado, long enemigosDerrotados) {
            setTotal(total);
            setSalud(salud);
            setEnergia(energia);
            setPasos(pasos);
            setProgreso(progreso);
            setEnemigos(enemigos);
            setResultado(resultado);
            setEnemigosDerrotados(enemigosDerrotados);
        }

        /**
         * Ejecuta getTotal.
          * @return resultado de la operacion
         */
        public int getTotal() {
            return total;
        }

        /** @param total puntuacion total acotada */
        public void setTotal(int total) {
            this.total = Validaciones.enteroEntre(
                    total, -Limites.ESTADISTICA, Limites.ESTADISTICA, "Puntuacion total");
        }

        /** @return puntos por salud */
        public int getSalud() {
            return salud;
        }

        /** @param salud puntos no negativos */
        public void setSalud(int salud) {
            this.salud = Validaciones.enteroEntre(salud, 0, Limites.ESTADISTICA, "Puntos de salud");
        }

        /** @return puntos por energia */
        public int getEnergia() {
            return energia;
        }

        /** @param energia puntos no negativos */
        public void setEnergia(int energia) {
            this.energia = Validaciones.enteroEntre(energia, 0, Limites.ESTADISTICA, "Puntos de energia");
        }

        /** @return puntos por pasos */
        public int getPasos() {
            return pasos;
        }

        /** @param pasos puntos no negativos */
        public void setPasos(int pasos) {
            this.pasos = Validaciones.enteroEntre(pasos, 0, Limites.ESTADISTICA, "Puntos de pasos");
        }

        /** @return puntos por progreso */
        public int getProgreso() {
            return progreso;
        }

        /** @param progreso puntos no negativos */
        public void setProgreso(int progreso) {
            this.progreso = Validaciones.enteroEntre(
                    progreso, 0, Limites.ESTADISTICA, "Puntos de progreso");
        }

        /** @return puntos por enemigos */
        public int getEnemigos() {
            return enemigos;
        }

        /** @param enemigos puntos no negativos */
        public void setEnemigos(int enemigos) {
            this.enemigos = Validaciones.enteroEntre(
                    enemigos, 0, Limites.ESTADISTICA, "Puntos de enemigos");
        }

        /** @return ajuste por resultado */
        public int getResultado() {
            return resultado;
        }

        /** @param resultado ajuste acotado */
        public void setResultado(int resultado) {
            this.resultado = Validaciones.enteroEntre(
                    resultado, -Limites.ESTADISTICA, Limites.ESTADISTICA, "Ajuste de resultado");
        }

        /** @return enemigos derrotados */
        public long getEnemigosDerrotados() {
            return enemigosDerrotados;
        }

        /** @param enemigosDerrotados cantidad no negativa */
        public void setEnemigosDerrotados(long enemigosDerrotados) {
            if (enemigosDerrotados < 0 || enemigosDerrotados > Limites.ESTADISTICA) {
                throw new IllegalArgumentException("Enemigos derrotados fuera de limites.");
            }
            this.enemigosDerrotados = enemigosDerrotados;
        }

        /**
         * Ejecuta formatearDesglose.
          * @return resultado de la operacion
         */
        public String[] formatearDesglose() {
            return new String[] {
                    "Puntuacion final: " + total,
                    "  - Salud restante: " + salud,
                    "  - Energia restante: " + energia,
                    "  - Eficiencia en pasos: " + pasos,
                    "  - Progreso hacia objetivo: " + progreso,
                    "  - Enemigos derrotados (" + enemigosDerrotados + "): " + enemigos,
                    "  - Ajuste por resultado final: " + resultado
            };
        }
    }
}
