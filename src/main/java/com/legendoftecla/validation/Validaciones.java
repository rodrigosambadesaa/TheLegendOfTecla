package com.legendoftecla.validation;

import java.util.Objects;

/** Funciones de validacion utilizadas dentro de las propias clases. */
public final class Validaciones {
    private Validaciones() {
    }

    /**
     * Exige una referencia no nula.
     *
     * @param valor referencia recibida
     * @param campo nombre del atributo para el mensaje
     * @param <T> tipo de la referencia
     * @return la misma referencia validada
     */
    public static <T> T noNulo(T valor, String campo) {
        return Objects.requireNonNull(valor, campo + " no puede ser nulo.");
    }

    /**
     * Valida y normaliza un texto obligatorio.
     *
     * @param valor texto recibido
     * @param campo nombre del atributo
     * @param longitudMaxima limite inclusivo
     * @return texto sin espacios exteriores
     */
    public static String textoObligatorio(String valor, String campo, int longitudMaxima) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(campo + " es obligatorio.");
        }
        String normalizado = valor.trim();
        if (normalizado.length() > longitudMaxima) {
            throw new IllegalArgumentException(campo + " no puede superar " + longitudMaxima + " caracteres.");
        }
        return normalizado;
    }

    /**
     * Valida un texto que puede estar vacio, pero no ser nulo ni excesivo.
     *
     * @param valor texto recibido
     * @param campo nombre del atributo
     * @param longitudMaxima limite inclusivo
     * @return texto validado
     */
    public static String texto(String valor, String campo, int longitudMaxima) {
        if (valor == null) {
            throw new IllegalArgumentException(campo + " no puede ser nulo.");
        }
        if (valor.length() > longitudMaxima) {
            throw new IllegalArgumentException(campo + " no puede superar " + longitudMaxima + " caracteres.");
        }
        return valor;
    }

    /**
     * Valida y normaliza un texto opcional.
     *
     * @param valor texto recibido o {@code null}
     * @param campo nombre del atributo
     * @param longitudMaxima limite inclusivo
     * @return {@code null} o el texto sin espacios exteriores
     */
    public static String textoOpcional(String valor, String campo, int longitudMaxima) {
        return valor == null ? null : textoObligatorio(valor, campo, longitudMaxima);
    }

    /**
     * Exige que un entero pertenezca a un intervalo cerrado.
     *
     * @param valor numero recibido
     * @param minimo limite inferior inclusivo
     * @param maximo limite superior inclusivo
     * @param campo nombre del atributo
     * @return el mismo valor validado
     */
    public static int enteroEntre(int valor, int minimo, int maximo, String campo) {
        if (valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(campo + " debe estar entre " + minimo + " y " + maximo + ".");
        }
        return valor;
    }

    /**
     * Exige que un decimal finito pertenezca a un intervalo cerrado.
     *
     * @param valor numero recibido
     * @param minimo limite inferior inclusivo
     * @param maximo limite superior inclusivo
     * @param campo nombre del atributo
     * @return el mismo valor validado
     */
    public static double decimalEntre(double valor, double minimo, double maximo, String campo) {
        if (!Double.isFinite(valor) || valor < minimo || valor > maximo) {
            throw new IllegalArgumentException(campo + " debe ser finito y estar entre "
                    + minimo + " y " + maximo + ".");
        }
        return valor;
    }
}
