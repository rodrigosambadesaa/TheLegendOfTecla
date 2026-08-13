package com.legendoftecla.performance;

import com.legendoftecla.console.Consola;
import com.legendoftecla.console.TipoMensaje;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.engine.FabricaJuego;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.Posicion;
import com.legendoftecla.persistence.PersistenciaPartida;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Pruebas de memoria reales ejecutadas en procesos con heap deliberadamente reducido. */
class MemoriaEntidadesTest {
    private static final int ALIADOS_MAXIMOS = 4_999;
    private static final int ENEMIGOS_MAXIMOS = 5_000;
    private static final Duration LIMITE_PROCESO = Duration.ofSeconds(120);

    @TempDir
    Path temporal;

    @Test
    void escenarioMaximoSeGuardaYCargaConUnHeapDe512MiB() throws Exception {
        ResultadoProceso resultado = ejecutarAislado(
                "maximo", "512m", temporal.resolve("maximo.save.json"));

        assertEquals(0, resultado.codigo(), resultado.salida());
        assertTrue(resultado.salida().contains(
                "MEMORY_OK allies=4999 enemies=5000"), resultado.salida());
        assertTrue(!resultado.salida().contains("OutOfMemoryError"), resultado.salida());
        System.out.print(resultado.salida());
    }

    @Test
    void partidasGrandesConsecutivasSeLiberanConUnHeapDe128MiB() throws Exception {
        ResultadoProceso resultado = ejecutarAislado(
                "repetido", "128m", temporal.resolve("repetido.save.json"));

        assertEquals(0, resultado.codigo(), resultado.salida());
        assertTrue(resultado.salida().contains("MEMORY_REUSE_OK runs=6"),
                resultado.salida());
        assertTrue(!resultado.salida().contains("OutOfMemoryError"), resultado.salida());
        System.out.print(resultado.salida());
    }

    private ResultadoProceso ejecutarAislado(String modo, String heap, Path guardado)
            throws Exception {
        String ejecutable = Path.of(System.getProperty("java.home"), "bin",
                System.getProperty("os.name").toLowerCase().contains("win")
                        ? "java.exe" : "java").toString();
        String classpath = System.getProperty("surefire.test.class.path",
                System.getProperty("java.class.path"));
        Path salida = temporal.resolve("memoria-" + modo + ".log");
        Process proceso = new ProcessBuilder(List.of(
                ejecutable, "-Xms32m", "-Xmx" + heap,
                "-Djava.awt.headless=true", "-cp", classpath,
                MemoriaEntidadesTest.class.getName(), modo, guardado.toString()))
                .redirectErrorStream(true)
                .redirectOutput(salida.toFile())
                .start();
        boolean terminado = proceso.waitFor(
                LIMITE_PROCESO.toSeconds(), TimeUnit.SECONDS);
        if (!terminado) {
            proceso.destroyForcibly();
            proceso.waitFor(5, TimeUnit.SECONDS);
        }
        String texto = Files.exists(salida)
                ? Files.readString(salida, StandardCharsets.UTF_8) : "";
        return new ResultadoProceso(terminado ? proceso.exitValue() : -1, texto);
    }

    /** Punto de entrada del proceso limitado; no se ejecuta dentro del heap de Surefire. */
    public static void main(String[] argumentos) throws Exception {
        if (argumentos.length < 2) {
            throw new IllegalArgumentException("Falta modo o archivo temporal.");
        }
        if ("maximo".equals(argumentos[0])) {
            ejecutarMaximo(Path.of(argumentos[1]));
        } else if ("repetido".equals(argumentos[0])) {
            ejecutarRepetido();
        } else {
            throw new IllegalArgumentException("Modo de memoria desconocido.");
        }
    }

    private static void ejecutarMaximo(Path guardado) throws Exception {
        Juego juego = crearJuego(ALIADOS_MAXIMOS, Dificultad.DEMENTE, 7001L);
        exigir(juego.getAliados().size() == ALIADOS_MAXIMOS,
                "Cantidad aliada incompleta");
        exigir(juego.getEnemigos().size() == ENEMIGOS_MAXIMOS,
                "Cantidad enemiga incompleta");
        int objetos = contarObjetosMapa(juego);
        exigir(objetos >= 4_400, "Los suministros escalados estan incompletos");
        MotorPartida motor = new MotorPartida(juego);
        String estado = motor.getEstadoAliados();
        exigir(estado.contains("ALIADOS 4999"), "Estado aliado incompleto");

        PersistenciaPartida.guardar(juego, guardado, 7001L);
        Juego cargado = PersistenciaPartida.cargar(guardado, new ConsolaNula());
        exigir(cargado.getAliados().size() == ALIADOS_MAXIMOS,
                "La carga perdio aliados");
        exigir(cargado.getEnemigos().size() == ENEMIGOS_MAXIMOS,
                "La carga perdio enemigos");
        imprimirMemoria("MEMORY_OK allies=4999 enemies=5000 objects=" + objetos);
    }

    private static void ejecutarRepetido() throws Exception {
        long mayorUso = 0;
        for (int indice = 0; indice < 6; indice++) {
            Juego juego = crearJuego(300, Dificultad.DIFICIL, 8000L + indice);
            exigir(juego.getAliados().size() == 300, "Cantidad aliada incompleta");
            exigir(juego.getEnemigos().size() == 301, "Cantidad enemiga incompleta");
            juego = null;
            System.gc();
            mayorUso = Math.max(mayorUso, memoriaUsada());
        }
        imprimirMemoria("MEMORY_REUSE_OK runs=6 peakAfterGcMiB=" + mib(mayorUso));
    }

    private static Juego crearJuego(int aliados, Dificultad dificultad, long seed)
            throws Exception {
        ConfiguracionPartida configuracion = new ConfiguracionPartida(
                "Memoria", "marine", "procedural", dificultad,
                new DimensionesMapa(15, 21), null, false, 1);
        configuracion.setCantidadAliados(aliados);
        configuracion.setNivelAliados(100);
        configuracion.setSeed(seed);
        return FabricaJuego.crear(new ConsolaNula(), configuracion);
    }

    private static void exigir(boolean condicion, String mensaje) {
        if (!condicion) throw new IllegalStateException(mensaje);
    }

    private static int contarObjetosMapa(Juego juego) {
        int objetos = 0;
        for (int fila = 0; fila < juego.getMapa().getFilas(); fila++) {
            for (int columna = 0; columna < juego.getMapa().getColumnas(); columna++) {
                objetos += juego.getMapa().getCelda(new Posicion(fila, columna))
                        .getObjetos().size();
            }
        }
        return objetos;
    }

    private static void imprimirMemoria(String prefijo) {
        System.gc();
        long usada = memoriaUsada();
        long maxima = ManagementFactory.getMemoryMXBean()
                .getHeapMemoryUsage().getMax();
        System.out.println(prefijo + " heapUsedMiB=" + mib(usada)
                + " heapMaxMiB=" + mib(maxima));
    }

    private static long memoriaUsada() {
        return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed();
    }

    private static long mib(long bytes) {
        return bytes / (1024 * 1024);
    }

    private record ResultadoProceso(int codigo, String salida) { }

    private static final class ConsolaNula implements Consola {
        @Override public void imprimir(String mensaje) { }
        @Override public void imprimir(String mensaje, TipoMensaje tipo) { }
        @Override public String leer(String descripcion) { return ""; }
    }
}
