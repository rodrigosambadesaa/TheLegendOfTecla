package com.legendoftecla.tools;

import com.legendoftecla.console.Consola;
import com.legendoftecla.constants.CondicionVictoria;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.audio.GestorSonido;
import com.legendoftecla.engine.ConfiguracionPartida;
import com.legendoftecla.engine.FabricaJuego;
import com.legendoftecla.engine.MotorPartida;
import com.legendoftecla.exceptions.JuegoException;
import com.legendoftecla.model.world.DimensionesMapa;
import com.legendoftecla.model.world.Juego;
import com.legendoftecla.model.world.SistemaPuntuacion;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPOutputStream;

/** Ejecuta Monte Carlo sobre {@link MotorPartida}, sin modelos de cohortes. */
public final class GeneradorProbabilidadesReales {
    private static final long SEMILLA_RAIZ = 20_260_814_01L;
    private static final int REPETICIONES_PREDETERMINADAS = 300;
    private static final Escenario BASE = new Escenario("BASE", "base", "referencia",
            Dificultad.NORMAL, 25, 25, 10, 10, 10,
            CondicionVictoria.SOLO_JUGADOR);

    private GeneradorProbabilidadesReales() { }

    /**
     * Genera muestras crudas comprimidas, agregados con intervalo Wilson y un
     * informe Markdown directamente publicable.
     *
     * @param args admite --runs=N, --seed=N y --output=ruta
     * @throws Exception si una partida o la escritura no pueden completarse
     */
    public static void main(String[] args) throws Exception {
        System.setProperty(GestorSonido.PROPIEDAD_DESACTIVADO, "true");
        Opciones opciones = Opciones.parsear(args);
        generar(opciones);
    }

    static void generar(Opciones opciones) throws Exception {
        Files.createDirectories(opciones.salida());
        List<Escenario> escenarios = construirMatriz();
        List<Resumen> resumenes = new ArrayList<>();
        Path muestras = opciones.salida().resolve("samples.csv.gz");
        ExecutorService ejecutor = Executors.newFixedThreadPool(opciones.hilos());
        try (BufferedWriter salidaMuestras = gzip(muestras)) {
            salidaMuestras.write(Resultado.cabeceraCsv());
            salidaMuestras.newLine();
            for (int indice = 0; indice < escenarios.size(); indice++) {
                Escenario escenario = escenarios.get(indice);
                Acumulador acumulador = new Acumulador(escenario);
                List<Callable<Ejecucion>> trabajos = new ArrayList<>();
                for (int repeticion = 0; repeticion < opciones.repeticiones(); repeticion++) {
                    long semilla = mezclarSemilla(opciones.semilla(), indice, repeticion);
                    boolean capturar = repeticion == 0;
                    trabajos.add(() -> {
                        ConsolaMedicion consola = capturar
                                ? new ConsolaMedicion(true) : ConsolaMedicion.silenciosa();
                        return new Ejecucion(simular(escenario, semilla, consola),
                                consola.contenido());
                    });
                }
                List<Future<Ejecucion>> ejecuciones = ejecutor.invokeAll(trabajos);
                for (int repeticion = 0; repeticion < ejecuciones.size(); repeticion++) {
                    Ejecucion ejecucion = ejecuciones.get(repeticion).get();
                    Resultado resultado = ejecucion.resultado();
                    acumulador.agregar(resultado);
                    salidaMuestras.write(resultado.csv());
                    salidaMuestras.newLine();
                    if (repeticion == 0) {
                        escribirGzip(opciones.salida().resolve(
                                "trace-" + escenario.id().toLowerCase(Locale.ROOT) + ".log.gz"),
                                ejecucion.traza());
                    }
                }
                Resumen resumen = acumulador.resumir();
                resumenes.add(resumen);
                System.out.printf(Locale.ROOT,
                        "%s %d runs | mision %.1f%% | bando humano %.1f%% | censura %d%n",
                        escenario.id(), resumen.runs(), resumen.porcentajeMision(),
                        resumen.porcentajeBando(), resumen.censuradas());
            }
        } finally {
            ejecutor.shutdownNow();
        }
        escribirResumenCsv(opciones.salida().resolve("summary.csv"), resumenes);
        escribirEstadisticasJson(opciones.salida().resolve("statistics.json"), opciones, resumenes);
        String informe = informeMarkdown(opciones, resumenes);
        Files.writeString(opciones.salida().resolve("README.md"), informe, StandardCharsets.UTF_8);
        Files.writeString(Path.of("docs", "PROBABILIDADES_REALES.md"), informe, StandardCharsets.UTF_8);
        escribirManifest(opciones.salida());
    }

    static List<Escenario> construirMatriz() {
        List<Escenario> escenarios = new ArrayList<>();
        for (Dificultad dificultad : Dificultad.values()) {
            escenarios.add(BASE.con("D-" + dificultad.name(), "dificultad",
                    dificultad.name(), dificultad, BASE.filas(), BASE.columnas(),
                    BASE.aliados(), BASE.nivelAliados(), BASE.nivelJugador(), BASE.condicion()));
        }
        for (int aliados : new int[] {0, 1, 5, 10, 25, 50, 100, 250}) {
            escenarios.add(BASE.con(String.format(Locale.ROOT, "P-%03d", aliados),
                    "poblacion", Integer.toString(aliados), BASE.dificultad(),
                    BASE.filas(), BASE.columnas(), aliados, BASE.nivelAliados(),
                    BASE.nivelJugador(), BASE.condicion()));
        }
        for (int[] dimensiones : new int[][] {{10, 10}, {15, 25}, {30, 30}, {50, 50}}) {
            String valor = dimensiones[0] + "x" + dimensiones[1];
            escenarios.add(BASE.con("M-" + valor, "mapa", valor, BASE.dificultad(),
                    dimensiones[0], dimensiones[1], BASE.aliados(), BASE.nivelAliados(),
                    BASE.nivelJugador(), BASE.condicion()));
        }
        for (int nivel : new int[] {1, 5, 10, 25, 50, 100}) {
            escenarios.add(BASE.con(String.format(Locale.ROOT, "LA-%03d", nivel),
                    "nivel_aliados", Integer.toString(nivel), BASE.dificultad(),
                    BASE.filas(), BASE.columnas(), BASE.aliados(), nivel,
                    BASE.nivelJugador(), BASE.condicion()));
        }
        for (int nivel : new int[] {1, 10, 25, 50, 100}) {
            escenarios.add(BASE.con(String.format(Locale.ROOT, "LJ-%03d", nivel),
                    "nivel_jugador", Integer.toString(nivel), BASE.dificultad(),
                    BASE.filas(), BASE.columnas(), BASE.aliados(), BASE.nivelAliados(),
                    nivel, BASE.condicion()));
        }
        for (CondicionVictoria condicion : CondicionVictoria.values()) {
            escenarios.add(BASE.con("C-" + condicion.name(), "condicion",
                    condicion.name(), BASE.dificultad(), BASE.filas(), BASE.columnas(),
                    BASE.aliados(), BASE.nivelAliados(), BASE.nivelJugador(), condicion));
        }
        return List.copyOf(escenarios);
    }

    static Resultado simular(Escenario escenario, long semilla, ConsolaMedicion consola)
            throws JuegoException {
        ConfiguracionPartida configuracion = new ConfiguracionPartida(
                "Auto", "marine", "procedural", escenario.dificultad(),
                new DimensionesMapa(escenario.filas(), escenario.columnas()), null,
                escenario.aliados(), escenario.condicion(), 1);
        configuracion.setNivelAliados(escenario.nivelAliados());
        configuracion.setNivelJugador(escenario.nivelJugador());
        configuracion.setSeed(semilla);
        Juego juego = FabricaJuego.crear(consola, configuracion);
        MotorPartida motor = new MotorPartida(juego);
        motor.setRandom(new Random(semilla ^ 0x5EEDC0DEL));
        JugadorAutomatico jugadorAutomatico = new JugadorAutomatico();
        int aliadosIniciales = juego.getAliadosIniciales();
        int enemigosIniciales = juego.getEnemigos().size();
        int limiteTurnos = Math.max(500, Math.min(5_000, juego.getPasosMaximos() + 1_000));
        int turnos = 0;
        boolean muerteJugador = false;
        while (!motor.isFinalizada() && turnos < limiteTurnos) {
            turnos++;
            if (motor.isModoEspectadorDisponible()) {
                consola.accion(turnos, "ESPECTADOR");
                motor.avanzarTurnoEspectador();
            } else {
                String comando = jugadorAutomatico.decidir(motor);
                consola.accion(turnos, comando);
                motor.ejecutarComando(comando);
            }
            muerteJugador |= juego.getJugador().getSalud() <= 0;
        }
        boolean censurada = !motor.isFinalizada();
        boolean victoriaBando = motor.getResultadoBatalla()
                == MotorPartida.ResultadoBatalla.VICTORIA_HUMANA;
        boolean mision = !censurada && !muerteJugador
                && motor.getEstadoFinal() == SistemaPuntuacion.EstadoFinalPartida.VICTORIA
                && juego.jugadorGano();
        long aliadosVivos = juego.getAliados().stream()
                .filter(aliado -> aliado.getSalud() > 0).count()
                + juego.getAliadosExtraidos();
        long enemigosVivos = juego.getEnemigos().stream()
                .filter(enemigo -> enemigo.getSalud() > 0).count();
        String desenlace = censurada ? "CENSURADA"
                : victoriaBando ? "VICTORIA_HUMANA" : "VICTORIA_ENEMIGA";
        return new Resultado(escenario, semilla, aliadosIniciales, enemigosIniciales,
                turnos, mision, victoriaBando, muerteJugador,
                juego.getJugador().getSalud() > 0
                        && juego.getJugador().getPosicion().equals(juego.getMapa().getObjetivo()),
                juego.getAliadosExtraidos() == aliadosIniciales,
                juego.getAliadosExtraidos(), aliadosVivos, enemigosVivos,
                censurada, desenlace);
    }

    private static long mezclarSemilla(long raiz, int escenario, int repeticion) {
        long valor = raiz + 0x9E3779B97F4A7C15L * (escenario + 1L)
                + 0xBF58476D1CE4E5B9L * (repeticion + 1L);
        valor = (valor ^ (valor >>> 30)) * 0xBF58476D1CE4E5B9L;
        valor = (valor ^ (valor >>> 27)) * 0x94D049BB133111EBL;
        return valor ^ (valor >>> 31);
    }

    private static BufferedWriter gzip(Path archivo) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(
                Files.newOutputStream(archivo, StandardOpenOption.CREATE,
                        StandardOpenOption.TRUNCATE_EXISTING)), StandardCharsets.UTF_8));
    }

    private static void escribirGzip(Path archivo, String contenido) throws IOException {
        try (BufferedWriter salida = gzip(archivo)) {
            salida.write(contenido);
        }
    }

    private static void escribirResumenCsv(Path archivo, List<Resumen> resumenes) throws IOException {
        StringBuilder csv = new StringBuilder(Resumen.cabeceraCsv()).append('\n');
        resumenes.forEach(resumen -> csv.append(resumen.csv()).append('\n'));
        Files.writeString(archivo, csv, StandardCharsets.UTF_8);
    }

    private static void escribirEstadisticasJson(Path archivo, Opciones opciones,
            List<Resumen> resumenes) throws IOException {
        StringBuilder json = new StringBuilder("{\n")
                .append("  \"model\": \"motor-partida-real-v1\",\n")
                .append("  \"rootSeed\": ").append(opciones.semilla()).append(",\n")
                .append("  \"runsPerScenario\": ").append(opciones.repeticiones()).append(",\n")
                .append("  \"threads\": ").append(opciones.hilos()).append(",\n")
                .append("  \"scenarioCount\": ").append(resumenes.size()).append(",\n")
                .append("  \"totalRuns\": ").append((long) resumenes.size()
                        * opciones.repeticiones()).append(",\n")
                .append("  \"scenarios\": [\n");
        for (int i = 0; i < resumenes.size(); i++) {
            Resumen r = resumenes.get(i);
            json.append("    {\"id\":\"").append(r.escenario().id())
                    .append("\",\"missionWinPct\":").append(decimal(r.porcentajeMision()))
                    .append(",\"humanSideWinPct\":").append(decimal(r.porcentajeBando()))
                    .append(",\"censored\":").append(r.censuradas()).append('}');
            if (i + 1 < resumenes.size()) json.append(',');
            json.append('\n');
        }
        json.append("  ]\n}\n");
        Files.writeString(archivo, json, StandardCharsets.UTF_8);
    }

    private static void escribirManifest(Path directorio) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        StringBuilder manifest = new StringBuilder();
        try (var archivos = Files.list(directorio)) {
            for (Path archivo : archivos.filter(Files::isRegularFile)
                    .filter(path -> !path.getFileName().toString().equals("manifest.sha256"))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString()))
                    .toList()) {
                String hash = HexFormat.of().formatHex(
                        sha256.digest(Files.readAllBytes(archivo)));
                manifest.append(hash).append("  ")
                        .append(archivo.getFileName()).append('\n');
                sha256.reset();
            }
        }
        Files.writeString(directorio.resolve("manifest.sha256"), manifest,
                StandardCharsets.UTF_8);
    }

    private static String informeMarkdown(Opciones opciones, List<Resumen> resumenes) {
        StringBuilder md = new StringBuilder()
                .append("# Probabilidades empíricas de una partida autónoma\n\n")
                .append("Estos porcentajes proceden de **").append((long) resumenes.size()
                        * opciones.repeticiones()).append(" partidas completas ejecutadas por ")
                .append("`MotorPartida`**, no del antiguo modelo agregado de cohortes. ")
                .append("Cada una de las ").append(resumenes.size()).append(" configuraciones usa ")
                .append(opciones.repeticiones()).append(" semillas independientes.\n\n")
                .append("## Cómo leerlos\n\n")
                .append("- `Misión`: el jugador sobrevive y satisface la condición de evacuación elegida.\n")
                .append("- `Bando humano`: también incluye una evacuación aliada posterior a la muerte del jugador.\n")
                .append("- `IC 95 %`: intervalo Wilson; expresa incertidumbre muestral, no diferencias entre estrategias humanas.\n")
                .append("- Se cambia una sola variable respecto a la referencia: Normal, mapa 25x25, ")
                .append("10 aliados de nivel 10, jugador nivel 10 y evacuación solo del jugador.\n\n")
                .append("La política automática usa formación defensiva, consume suministros por debajo de los ")
                .append("umbrales de seguridad, ataca amenazas visibles y avanza por la ruta más corta. ")
                .append("Por tanto son probabilidades reales **para esa política**, no una garantía para cualquier jugador.\n\n")
                .append("## Resultados\n\n")
                .append("| Variable | Valor | Entidades A/E | Misión (IC 95 %) | Bando humano (IC 95 %) | Turnos medios | Censuradas |\n")
                .append("|---|---:|---:|---:|---:|---:|---:|\n");
        for (Resumen r : resumenes) {
            md.append('|').append(r.escenario().eje()).append('|')
                    .append(r.escenario().valor()).append('|')
                    .append(decimal(r.mediaAliados())).append('/')
                    .append(decimal(r.mediaEnemigos())).append('|')
                    .append(intervalo(r.victoriasMision(), r.runs())).append('|')
                    .append(intervalo(r.victoriasBando(), r.runs())).append('|')
                    .append(decimal(r.mediaTurnos())).append('|')
                    .append(r.censuradas()).append("|\n");
        }
        Resumen muyFacil = buscar(resumenes, "D-MUY_FACIL");
        Resumen normal = buscar(resumenes, "D-NORMAL");
        Resumen pesadilla = buscar(resumenes, "D-PESADILLA");
        Resumen poblacionCero = buscar(resumenes, "P-000");
        Resumen poblacionCien = buscar(resumenes, "P-100");
        Resumen poblacionMaxima = buscar(resumenes, "P-250");
        Resumen nivelAliadoUno = buscar(resumenes, "LA-001");
        Resumen nivelAliadoCien = buscar(resumenes, "LA-100");
        Resumen nivelJugadorUno = buscar(resumenes, "LJ-001");
        Resumen nivelJugadorCien = buscar(resumenes, "LJ-100");
        Resumen solo = buscar(resumenes, "C-SOLO_JUGADOR");
        Resumen todos = buscar(resumenes, "C-JUGADOR_Y_ALIADOS");
        long censuradas = resumenes.stream().mapToLong(Resumen::censuradas).sum();
        md.append("\n## Hallazgos principales\n\n")
                .append("- La misión cae de ").append(decimal(muyFacil.porcentajeMision()))
                .append(" % en Muy fácil a ").append(decimal(normal.porcentajeMision()))
                .append(" % en Normal y ").append(decimal(pesadilla.porcentajeMision()))
                .append(" % en Pesadilla. Las pequeñas inversiones entre categorías adyacentes ")
                .append("deben leerse con sus intervalos, que se solapan.\n")
                .append("- Con esta estrategia, aumentar la población no compensa el fuego concentrado: ")
                .append(decimal(poblacionCero.porcentajeMision())).append(" % sin aliados, ")
                .append(decimal(poblacionCien.porcentajeMision())).append(" % con 100 y ")
                .append(decimal(poblacionMaxima.porcentajeMision())).append(" % con 250. ")
                .append("Esto no compara composiciones o formaciones alternativas.\n")
                .append("- Subir aliados de nivel 1 a 100 eleva la misión de ")
                .append(decimal(nivelAliadoUno.porcentajeMision())).append(" % a ")
                .append(decimal(nivelAliadoCien.porcentajeMision())).append(" %; subir al jugador ")
                .append("de nivel 1 a 100 la eleva de ")
                .append(decimal(nivelJugadorUno.porcentajeMision())).append(" % a ")
                .append(decimal(nivelJugadorCien.porcentajeMision())).append(" %.\n")
                .append("- Exigir a todo el escuadrón reduce el éxito de ")
                .append(decimal(solo.porcentajeMision())).append(" % a ")
                .append(decimal(todos.porcentajeMision())).append(" % en la referencia.\n")
                .append("- Partidas censuradas: ").append(censuradas)
                .append(". Todas las semillas tienen un desenlace observado.\n\n")
                .append("Son análisis de sensibilidad de una variable cada vez; no deben multiplicarse ")
                .append("para predecir combinaciones no ejecutadas.\n")
                .append("\n## Reproducción y trazabilidad\n\n")
                .append("Semilla raíz: `").append(opciones.semilla()).append("`. ")
                .append("`samples.csv.gz` contiene una fila por partida; `summary.csv` y ")
                .append("`statistics.json` contienen los agregados. La primera ejecución de cada ")
                .append("configuración se conserva en `trace-*.log.gz`, incluyendo comandos y acciones ")
                .append("emitidas por jugador, aliados y enemigos. `manifest.sha256` permite comprobar ")
                .append("que los artefactos no han cambiado.\n\n")
                .append("Ejemplo de regeneración:\n\n")
                .append("```powershell\n")
                .append("mvn -q -DskipTests package\n")
                .append("java -cp target/classes com.legendoftecla.tools.GeneradorProbabilidadesReales ")
                .append("--runs=").append(opciones.repeticiones())
                .append(" --seed=").append(opciones.semilla())
                .append(" --threads=").append(opciones.hilos())
                .append(" --output=docs/runs/monte-carlo-real-20260814\n")
                .append("```\n");
        return md.toString();
    }

    private static Resumen buscar(List<Resumen> resumenes, String id) {
        return resumenes.stream().filter(resumen -> resumen.escenario().id().equals(id))
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "Falta el escenario requerido en el informe: " + id));
    }

    private static String intervalo(long exitos, long total) {
        double porcentaje = total == 0 ? 0 : 100.0 * exitos / total;
        double[] wilson = wilson(exitos, total);
        return decimal(porcentaje) + "% (" + decimal(wilson[0] * 100)
                + "–" + decimal(wilson[1] * 100) + ")";
    }

    static double[] wilson(long exitos, long total) {
        if (total <= 0) return new double[] {0, 0};
        double z = 1.959963984540054;
        double p = (double) exitos / total;
        double denominador = 1 + z * z / total;
        double centro = (p + z * z / (2 * total)) / denominador;
        double margen = z * Math.sqrt((p * (1 - p) + z * z / (4 * total)) / total)
                / denominador;
        return new double[] {Math.max(0, centro - margen), Math.min(1, centro + margen)};
    }

    private static String decimal(double valor) {
        return String.format(Locale.ROOT, "%.1f", valor);
    }

    record Escenario(String id, String eje, String valor, Dificultad dificultad,
            int filas, int columnas, int aliados, int nivelAliados,
            int nivelJugador, CondicionVictoria condicion) {
        Escenario con(String nuevoId, String nuevoEje, String nuevoValor,
                Dificultad nuevaDificultad, int nuevasFilas, int nuevasColumnas,
                int nuevosAliados, int nuevoNivelAliados, int nuevoNivelJugador,
                CondicionVictoria nuevaCondicion) {
            return new Escenario(nuevoId, nuevoEje, nuevoValor, nuevaDificultad,
                    nuevasFilas, nuevasColumnas, nuevosAliados, nuevoNivelAliados,
                    nuevoNivelJugador, nuevaCondicion);
        }
    }

    record Resultado(Escenario escenario, long semilla, int aliadosIniciales,
            int enemigosIniciales, int turnos, boolean mision, boolean victoriaBando,
            boolean muerteJugador, boolean jugadorEvacuado, boolean todosAliadosEvacuados,
            int aliadosEvacuados, long aliadosVivos, long enemigosVivos,
            boolean censurada, String desenlace) {
        static String cabeceraCsv() {
            return "scenario,axis,value,seed,difficulty,rows,cols,allies_requested,"
                    + "allies_initial,enemies_initial,ally_level,player_level,condition,turns,"
                    + "mission_win,human_side_win,player_died,player_evacuated,all_allies_evacuated,"
                    + "allies_evacuated,allies_alive,enemies_alive,censored,outcome";
        }
        String csv() {
            return String.join(",", escenario.id(), escenario.eje(), escenario.valor(),
                    Long.toString(semilla), escenario.dificultad().name(),
                    Integer.toString(escenario.filas()), Integer.toString(escenario.columnas()),
                    Integer.toString(escenario.aliados()), Integer.toString(aliadosIniciales),
                    Integer.toString(enemigosIniciales), Integer.toString(escenario.nivelAliados()),
                    Integer.toString(escenario.nivelJugador()), escenario.condicion().name(),
                    Integer.toString(turnos), Boolean.toString(mision),
                    Boolean.toString(victoriaBando), Boolean.toString(muerteJugador),
                    Boolean.toString(jugadorEvacuado), Boolean.toString(todosAliadosEvacuados),
                    Integer.toString(aliadosEvacuados), Long.toString(aliadosVivos),
                    Long.toString(enemigosVivos), Boolean.toString(censurada), desenlace);
        }
    }

    private record Ejecucion(Resultado resultado, String traza) { }

    private static final class Acumulador {
        private final Escenario escenario;
        private long runs;
        private long victoriasMision;
        private long victoriasBando;
        private long censuradas;
        private long turnos;
        private long aliados;
        private long enemigos;

        private Acumulador(Escenario escenario) { this.escenario = escenario; }
        void agregar(Resultado resultado) {
            runs++;
            if (resultado.mision()) victoriasMision++;
            if (resultado.victoriaBando()) victoriasBando++;
            if (resultado.censurada()) censuradas++;
            turnos += resultado.turnos();
            aliados += resultado.aliadosIniciales();
            enemigos += resultado.enemigosIniciales();
        }
        Resumen resumir() {
            return new Resumen(escenario, runs, victoriasMision, victoriasBando,
                    censuradas, (double) turnos / runs, (double) aliados / runs,
                    (double) enemigos / runs);
        }
    }

    record Resumen(Escenario escenario, long runs, long victoriasMision,
            long victoriasBando, long censuradas, double mediaTurnos,
            double mediaAliados, double mediaEnemigos) {
        double porcentajeMision() { return 100.0 * victoriasMision / runs; }
        double porcentajeBando() { return 100.0 * victoriasBando / runs; }
        static String cabeceraCsv() {
            return "scenario,axis,value,difficulty,rows,cols,allies,ally_level,player_level,"
                    + "condition,runs,mission_wins,mission_win_pct,human_side_wins,"
                    + "human_side_win_pct,censored,mean_turns,mean_allies,mean_enemies";
        }
        String csv() {
            return String.join(",", escenario.id(), escenario.eje(), escenario.valor(),
                    escenario.dificultad().name(), Integer.toString(escenario.filas()),
                    Integer.toString(escenario.columnas()), Integer.toString(escenario.aliados()),
                    Integer.toString(escenario.nivelAliados()), Integer.toString(escenario.nivelJugador()),
                    escenario.condicion().name(), Long.toString(runs), Long.toString(victoriasMision),
                    decimal(porcentajeMision()), Long.toString(victoriasBando),
                    decimal(porcentajeBando()), Long.toString(censuradas), decimal(mediaTurnos),
                    decimal(mediaAliados), decimal(mediaEnemigos));
        }
    }

    record Opciones(int repeticiones, long semilla, int hilos, Path salida) {
        static Opciones parsear(String[] args) {
            int repeticiones = REPETICIONES_PREDETERMINADAS;
            long semilla = SEMILLA_RAIZ;
            int hilos = Math.max(1, Math.min(4,
                    Runtime.getRuntime().availableProcessors()));
            Path salida = Path.of("docs", "runs", "monte-carlo-real-20260814");
            for (String arg : args) {
                if (arg.startsWith("--runs=")) {
                    repeticiones = Integer.parseInt(arg.substring("--runs=".length()));
                } else if (arg.startsWith("--seed=")) {
                    semilla = Long.parseLong(arg.substring("--seed=".length()));
                } else if (arg.startsWith("--threads=")) {
                    hilos = Integer.parseInt(arg.substring("--threads=".length()));
                } else if (arg.startsWith("--output=")) {
                    salida = Path.of(arg.substring("--output=".length()));
                } else {
                    throw new IllegalArgumentException("Opcion desconocida: " + arg);
                }
            }
            if (repeticiones < 1 || repeticiones > 10_000) {
                throw new IllegalArgumentException("--runs debe estar entre 1 y 10000");
            }
            if (hilos < 1 || hilos > 32) {
                throw new IllegalArgumentException("--threads debe estar entre 1 y 32");
            }
            return new Opciones(repeticiones, semilla, hilos, salida.normalize());
        }
    }

    /** Consola sin memoria salvo en la traza auditable solicitada. */
    static final class ConsolaMedicion implements Consola {
        private final boolean capturar;
        private final StringBuilder contenido = new StringBuilder();
        private ConsolaMedicion(boolean capturar) { this.capturar = capturar; }
        static ConsolaMedicion silenciosa() { return new ConsolaMedicion(false); }
        void accion(int turno, String accion) {
            if (capturar) contenido.append("[turno ").append(turno)
                    .append("] jugador: ").append(accion).append('\n');
        }
        String contenido() { return contenido.toString(); }
        @Override public void imprimir(String mensaje) {
            if (capturar) contenido.append(mensaje).append('\n');
        }
        @Override public String leer(String descripcion) { return ""; }
    }
}
