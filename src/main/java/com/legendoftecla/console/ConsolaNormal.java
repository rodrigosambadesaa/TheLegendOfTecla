package com.legendoftecla.console;

import com.legendoftecla.exceptions.FinEntradaException;

import java.util.Scanner;

/**
 * Representa la entidad ConsolaNormal del juego.
 */
public class ConsolaNormal implements Consola {
    /**
     * Crea una consola conectada a la entrada y salida estandar del proceso.
     */
    public ConsolaNormal() {
        // Los recursos de entrada y la configuracion de color se inicializan en los campos.
    }

    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    private final Scanner scanner = new Scanner(System.in);
    private final boolean colorActivo = System.getenv("NO_COLOR") == null;

    @Override
    /**
     * Ejecuta imprimir.
     */
    public void imprimir(String mensaje) {
        imprimir(mensaje, inferirTipo(mensaje));
    }

    @Override
    /**
     * Ejecuta imprimir.
     */
    public void imprimir(String mensaje, TipoMensaje tipo) {
        if (!colorActivo) {
            System.out.println(mensaje);
            return;
        }
        System.out.println(color(tipo) + mensaje + RESET);
    }

    @Override
    /**
     * Ejecuta leer.
     */
    public String leer(String descripcion) {
        if (colorActivo) {
            System.out.print(CYAN + descripcion + " " + RESET);
        } else {
            System.out.print(descripcion + " ");
        }
        if (!scanner.hasNextLine()) {
            System.out.println();
            throw new FinEntradaException();
        }
        return scanner.nextLine();
    }

    private String color(TipoMensaje tipo) {
        return switch (tipo) {
            case EXITO -> GREEN;
            case ERROR -> RED;
            case ADVERTENCIA -> YELLOW;
            case ESTADO -> CYAN;
            case INFO -> BLUE;
        };
    }

    private TipoMensaje inferirTipo(String mensaje) {
        String normalizado = mensaje.toLowerCase();
        if (normalizado.contains("error") || normalizado.contains("muerto") || normalizado.contains("no puedes")
                || normalizado.contains("invalido")) {
            return TipoMensaje.ERROR;
        }
        if (normalizado.contains("te ataca") || normalizado.contains("contacto hostil")
                || normalizado.contains("hostil")) {
            return TipoMensaje.ADVERTENCIA;
        }
        if (normalizado.contains("victoria") || normalizado.contains("has llegado") || normalizado.contains("te mueves")
                || normalizado.contains("recoges") || normalizado.contains("equipado")
                || normalizado.contains("atacas")) {
            return TipoMensaje.EXITO;
        }
        if (normalizado.contains("superaste") || normalizado.contains("cuidado")
                || normalizado.contains("advertencia")) {
            return TipoMensaje.ADVERTENCIA;
        }
        if (normalizado.contains("salud(") || normalizado.contains("energia(") || normalizado.contains("pasos ")) {
            return TipoMensaje.ESTADO;
        }
        return TipoMensaje.INFO;
    }
}
