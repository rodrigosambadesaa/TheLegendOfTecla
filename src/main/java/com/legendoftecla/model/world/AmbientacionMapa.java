package com.legendoftecla.model.world;

import com.legendoftecla.model.elements.Barricada;
import com.legendoftecla.model.elements.Cofre;
import com.legendoftecla.model.elements.ElementoMapa;
import com.legendoftecla.model.elements.Interruptor;
import com.legendoftecla.model.elements.ParedDebil;
import com.legendoftecla.model.elements.Puerta;
import com.legendoftecla.model.elements.Terminal;
import com.legendoftecla.model.elements.Trampa;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/** Crea descripciones espaciales estables y refleja el estado actual de cada celda. */
public final class AmbientacionMapa {
    private static final String MARCA_AMBIENTACION = "Entorno del escenario:";

    private AmbientacionMapa() {
    }

    /**
     * Completa todas las celdas sin borrar el texto narrativo aportado por un escenario.
     * La marca hace que la operacion sea segura al cargar o reasignar un mapa.
     *
     * @param mapa mapa que se va a ambientar
     */
    public static void completar(Mapa mapa) {
        Objects.requireNonNull(mapa, "Mapa");
        for (int fila = 0; fila < mapa.getFilas(); fila++) {
            for (int columna = 0; columna < mapa.getColumnas(); columna++) {
                Posicion posicion = new Posicion(fila, columna);
                Celda celda = mapa.getCelda(posicion);
                completarCelda(mapa, posicion, celda);
            }
        }
    }

    /**
     * Describe la celda con sus propiedades dinamicas en el instante de observarla.
     *
     * @param mapa mapa observado
     * @param posicion celda observada
     * @return descripcion narrativa y estado ambiental actual
     */
    public static String describir(Mapa mapa, Posicion posicion) {
        Objects.requireNonNull(mapa, "Mapa");
        Objects.requireNonNull(posicion, "Posicion");
        Celda celda = mapa.getCelda(posicion);
        completarCelda(mapa, posicion, celda);
        StringBuilder descripcion = new StringBuilder(celda.getDescripcion());
        descripcion.append(System.lineSeparator()).append("Estado actual del lugar: ");
        descripcion.append(describirTerreno(celda)).append(' ');
        descripcion.append(describirIluminacion(celda));
        if (celda.hasFuenteAgua()) {
            descripcion.append(" Una fuente de agua accesible forma parte de este espacio.");
        }
        descripcion.append(describirFuego(celda));
        String elementos = describirElementos(celda.getElementos());
        if (!elementos.isBlank()) {
            descripcion.append(' ').append(elementos);
        }
        return descripcion.toString();
    }

    private static void completarCelda(Mapa mapa, Posicion posicion, Celda celda) {
        if (celda != null && !celda.getDescripcion().contains(MARCA_AMBIENTACION)) {
            celda.setDescripcion(crearDescripcionBase(mapa, posicion, celda));
        }
    }

    private static String crearDescripcionBase(Mapa mapa, Posicion posicion, Celda celda) {
        StringBuilder descripcion = new StringBuilder();
        String original = celda.getDescripcion().trim();
        if (!esGenerica(original)) {
            descripcion.append(asegurarPunto(original)).append(' ');
        }
        descripcion.append(MARCA_AMBIENTACION).append(" esta celda ocupa ")
                .append(ubicar(mapa, posicion)).append(" de \"")
                .append(mapa.getNombre()).append("\" (fila ")
                .append(posicion.getFila() + 1).append(", columna ")
                .append(posicion.getColumna() + 1).append("). ");
        String contexto = mapa.getDescripcion().trim();
        if (!contexto.isBlank()) {
            descripcion.append("El contexto del lugar es: ")
                    .append(asegurarPunto(contexto)).append(' ');
        }
        if (posicion.equals(mapa.getInicio())) {
            descripcion.append("Es el punto de despliegue y referencia de entrada del grupo.");
        } else if (posicion.equals(mapa.getObjetivo())) {
            descripcion.append("Es la zona objetivo que marca el destino de la expedicion.");
        } else {
            descripcion.append(detalleEspacial(mapa, posicion));
        }
        return descripcion.toString().trim();
    }

    private static String describirTerreno(Celda celda) {
        String suelo = celda.getTipoSuelo() == TipoSuelo.MADERA
                ? "tablones de madera" : "piedra firme";
        if (celda.isTerrenoTransitable()) {
            return "El suelo es de " + suelo
                    + " y el terreno permite el paso, salvo por los elementos que lo ocupen.";
        }
        return "El suelo visible es de " + suelo
                + " y la propia estructura del terreno impide atravesar esta celda.";
    }

    private static String describirIluminacion(Celda celda) {
        if (celda.isOscuridadPermanente()) {
            return celda.hasAntorchaMural()
                    ? "Una oscuridad permanente domina el entorno, aunque una antorcha mural"
                            + " aporta luz local."
                    : "Una oscuridad permanente domina el entorno y anula la luz ambiental.";
        }
        if (celda.hasAntorchaMural()) {
            return "Una antorcha mural proyecta luz y sombras sobre la zona.";
        }
        if (celda.isOscura()) {
            return "La zona carece de iluminacion propia y permanece oscura.";
        }
        return "La iluminacion ambiental permite distinguir con claridad el escenario.";
    }

    private static String describirFuego(Celda celda) {
        return switch (celda.getNivelFuego()) {
            case 1 -> " Hay un foco pequeno de fuego y humo en la celda.";
            case 2 -> " Las llamas se han extendido y hacen peligroso permanecer aqui.";
            case 3 -> " Un incendio intenso domina la celda y amenaza todo lo que contiene.";
            default -> " No hay fuego activo en este lugar.";
        };
    }

    private static String describirElementos(List<ElementoMapa> elementos) {
        List<String> visibles = new ArrayList<>();
        for (ElementoMapa elemento : elementos) {
            String descripcion = describirElemento(elemento);
            if (!descripcion.isBlank()) {
                visibles.add(descripcion);
            }
        }
        if (visibles.isEmpty()) {
            return "No se observan estructuras interactivas adicionales.";
        }
        return "Elementos del escenario: " + String.join("; ", visibles) + ".";
    }

    private static String describirElemento(ElementoMapa elemento) {
        String id = " '" + elemento.getId() + "'";
        if (elemento instanceof Trampa trampa) {
            if (!trampa.isDetectada() && trampa.isActiva()) {
                return "";
            }
            String tipo = nombreNatural(elemento);
            return tipo + id + (trampa.isActiva() ? " detectada y activa" : " desactivada");
        }
        if (elemento instanceof Puerta puerta) {
            return "puerta" + id + " " + puerta.getEstado().name().toLowerCase(Locale.ROOT)
                    + estadoPaso(elemento);
        }
        if (elemento instanceof Barricada barricada) {
            return "barricada" + id + " con cobertura "
                    + barricada.getCobertura().name().toLowerCase(Locale.ROOT)
                    + " orientada al "
                    + barricada.getOrientacion().name().toLowerCase(Locale.ROOT)
                    + estadoPaso(elemento);
        }
        if (elemento instanceof Cofre cofre) {
            return "cofre" + id + (cofre.isAbierto() ? " abierto" : " cerrado")
                    + estadoPaso(elemento);
        }
        if (elemento instanceof Terminal terminal) {
            return "terminal" + id + (terminal.isHackeado() ? " ya hackeado" : " operativo")
                    + " de dificultad " + terminal.getDificultad();
        }
        if (elemento instanceof Interruptor interruptor) {
            return "interruptor" + id + (interruptor.isActivo() ? " activo" : " inactivo");
        }
        if (elemento instanceof ParedDebil) {
            return "pared debil" + id + estadoPaso(elemento);
        }
        return nombreNatural(elemento) + id + estadoPaso(elemento);
    }

    private static String estadoPaso(ElementoMapa elemento) {
        if (elemento.estaDestruido()) {
            return ", destruido y sin bloquear el paso";
        }
        return elemento.permitePaso() ? ", permite el paso" : ", bloquea el paso";
    }

    private static String nombreNatural(ElementoMapa elemento) {
        String nombre = elemento.getClass().getSimpleName()
                .replaceAll("([a-z])([A-Z])", "$1 $2");
        return nombre.toLowerCase(Locale.ROOT);
    }

    private static String ubicar(Mapa mapa, Posicion posicion) {
        String vertical = tercio(posicion.getFila(), mapa.getFilas(), "norte", "sur");
        String horizontal = tercio(posicion.getColumna(), mapa.getColumnas(), "oeste", "este");
        if (vertical.equals("centro") && horizontal.equals("centro")) {
            return "el sector central";
        }
        if (vertical.equals("centro")) {
            return "el sector " + horizontal;
        }
        if (horizontal.equals("centro")) {
            return "el sector " + vertical;
        }
        return "el sector " + vertical + "-" + horizontal;
    }

    private static String tercio(int indice, int longitud, String primero, String ultimo) {
        if (indice * 3 < longitud) {
            return primero;
        }
        if ((indice + 1) * 3 > longitud * 2) {
            return ultimo;
        }
        return "centro";
    }

    private static String detalleEspacial(Mapa mapa, Posicion posicion) {
        int variante = Math.floorMod(Objects.hash(mapa.getNombre(),
                posicion.getFila(), posicion.getColumna()), 3);
        return switch (variante) {
            case 0 -> "Su posicion la convierte en una referencia reconocible dentro del recorrido.";
            case 1 -> "Sus limites enlazan visualmente este punto con los sectores contiguos.";
            default -> "La disposicion espacial permite identificar este tramo durante la exploracion.";
        };
    }

    private static boolean esGenerica(String descripcion) {
        String normalizada = descripcion.toLowerCase(Locale.ROOT);
        return normalizada.isBlank()
                || normalizada.matches("celda(?:\\s+\\d+\\s*,\\s*\\d+)?")
                || normalizada.matches("sector(?:\\s+\\d+\\s*,\\s*\\d+)?")
                || normalizada.equals("muro")
                || normalizada.equals("terreno")
                || normalizada.equals("punto de despliegue")
                || normalizada.equals("zona objetivo");
    }

    private static String asegurarPunto(String texto) {
        return texto.endsWith(".") || texto.endsWith("!") || texto.endsWith("?")
                ? texto : texto + ".";
    }
}
