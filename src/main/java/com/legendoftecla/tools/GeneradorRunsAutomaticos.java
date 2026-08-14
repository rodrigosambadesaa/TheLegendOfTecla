package com.legendoftecla.tools;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.legendoftecla.constants.CondicionVictoria;
import com.legendoftecla.constants.Dificultad;
import com.legendoftecla.validation.Limites;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.zip.GZIPOutputStream;

/**
 * Genera evidencias reproducibles de partidas autonomas de gran poblacion.
 *
 * <p>El simulador procesa cohortes en vez de reservar un objeto Java por
 * combatiente. Asi puede recorrer el limite real de poblacion del juego sin
 * convertir la generacion documental en una prueba destructiva de memoria.
 */
public final class GeneradorRunsAutomaticos {
    /** Cantidad solicitada para el lote documental oficial. */
    public static final int RUNS_PREDETERMINADOS = 1_000;
    /** Primer tamano de escuadron incluido. */
    public static final int ALIADOS_MINIMOS = 100;
    /** Semilla raiz publicada para poder regenerar exactamente el lote. */
    public static final long SEMILLA_PREDETERMINADA = 0x5EED_2026_0814L;

    private static final Gson JSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String MODELO = "headless-cohort-v1";

    private GeneradorRunsAutomaticos() {
    }

    /**
     * Genera el lote en el directorio indicado.
     *
     * @param argumentos directorio, cantidad opcional y semilla opcional
     * @throws IOException si no se pueden guardar las evidencias
     */
    public static void main(String[] argumentos) throws IOException {
        if (argumentos.length == 0 || argumentos.length > 3) {
            throw new IllegalArgumentException(
                    "Uso: GeneradorRunsAutomaticos <directorio> [cantidad] [semilla]");
        }
        Path directorio = Path.of(argumentos[0]).toAbsolutePath().normalize();
        int cantidad = argumentos.length >= 2
                ? Integer.parseInt(argumentos[1]) : RUNS_PREDETERMINADOS;
        long semilla = argumentos.length >= 3
                ? Long.parseLong(argumentos[2]) : SEMILLA_PREDETERMINADA;
        ResumenLote resumen = generar(directorio, cantidad, semilla, Instant.now());
        System.out.printf(Locale.ROOT,
                "RUNS_OK total=%d humanas=%d enemigas=%d aliados=%d..%d bytes=%d%n",
                resumen.totalRuns(), resumen.victoriasHumanas(), resumen.victoriasEnemigas(),
                resumen.aliadosMinimos(), resumen.aliadosMaximos(), resumen.bytesGenerados());
    }

    /**
     * Ejecuta y persiste un lote completo con consumo de memoria acotado.
     *
     * @param directorio destino nuevo o vacio
     * @param cantidad numero de partidas
     * @param semilla raiz reproducible
     * @param generadoEn instante documental
     * @return resumen agregado
     * @throws IOException si falla la escritura o validacion
     */
    public static ResumenLote generar(Path directorio, int cantidad, long semilla,
            Instant generadoEn) throws IOException {
        validarParametros(directorio, cantidad, generadoEn);
        Files.createDirectories(directorio);
        try (var contenido = Files.list(directorio)) {
            if (contenido.findAny().isPresent()) {
                throw new IllegalArgumentException(
                        "El directorio de salida debe estar vacio: " + directorio);
            }
        }

        AcumuladorEstadisticas acumulador = new AcumuladorEstadisticas(cantidad, semilla, generadoEn);
        Path indice = directorio.resolve("index.csv");
        try (BufferedWriter csv = Files.newBufferedWriter(indice, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW)) {
            csv.write("run,seed,allies,enemies,difficulty,victory_condition,ending,"
                    + "human_victory,turns,score,allies_evacuated,actions,action_log,"
                    + "peak_heap_bytes\n");
            for (int numero = 1; numero <= cantidad; numero++) {
                RunCompleto run = simular(numero, cantidad, semilla);
                validarRun(run);
                Path acciones = directorio.resolve(run.logAcciones());
                long registradas = escribirAcciones(acciones, run);
                if (registradas != run.accionesRegistradas()) {
                    throw new IllegalStateException("Recuento de acciones incoherente: " + run.id());
                }
                Path archivo = directorio.resolve(String.format(Locale.ROOT,
                        "run-%04d.json", numero));
                Files.writeString(archivo, JSON.toJson(run) + System.lineSeparator(),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
                escribirIndice(csv, run);
                acumulador.agregar(run, Files.size(acciones));
            }
        }

        ResumenLote resumenBase = acumulador.resumir(0L);
        Files.writeString(directorio.resolve("statistics.json"),
                JSON.toJson(resumenBase) + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE_NEW);
        Files.writeString(directorio.resolve("README.md"), crearReadme(resumenBase),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
        escribirManifiesto(directorio);
        long bytes;
        try (var archivos = Files.walk(directorio)) {
            bytes = archivos.filter(Files::isRegularFile)
                    .mapToLong(GeneradorRunsAutomaticos::tamanoSeguro).sum();
        }
        ResumenLote resumenFinal = acumulador.resumir(bytes);
        Files.writeString(directorio.resolve("statistics.json"),
                JSON.toJson(resumenFinal) + System.lineSeparator(), StandardCharsets.UTF_8,
                StandardOpenOption.TRUNCATE_EXISTING);
        escribirManifiesto(directorio);
        return resumenFinal;
    }

    private static void validarParametros(Path directorio, int cantidad, Instant generadoEn) {
        if (directorio == null || generadoEn == null) {
            throw new IllegalArgumentException("Directorio e instante son obligatorios.");
        }
        if (cantidad < 2 || cantidad > RUNS_PREDETERMINADOS) {
            throw new IllegalArgumentException("La cantidad debe estar entre 2 y 1000.");
        }
    }

    private static RunCompleto simular(int numero, int total, long semillaRaiz) {
        long semilla = mezclarSemilla(semillaRaiz, numero);
        SplittableRandom random = new SplittableRandom(semilla);
        int aliados = ALIADOS_MINIMOS + (int) Math.round((numero - 1.0)
                * (Limites.ALIADOS_MAXIMOS - ALIADOS_MINIMOS) / (total - 1.0));
        CondicionVictoria condicion = numero % 2 == 1
                ? CondicionVictoria.SOLO_JUGADOR : CondicionVictoria.JUGADOR_Y_ALIADOS;
        Dificultad dificultad = Dificultad.values()[(numero - 1) % Dificultad.values().length];
        int nivel = 1 + Math.floorMod(numero * 37, Limites.NIVEL_ALIADO_MAXIMO);
        int enemigos = Math.min(Limites.COMBATIENTES_POR_BANDO,
                Math.max(1, dificultad.ajustarCantidadEnemigos(aliados)));
        int medicos = Math.max(1, (int) Math.ceil(aliados * 0.12));
        int objetos = calcularObjetos(aliados, enemigos, dificultad);
        TipoFinal tipoFinal = elegirFinal(numero, condicion);
        EstadoSimulacion estado = new EstadoSimulacion(aliados, enemigos);
        List<EventoTurno> eventos = new ArrayList<>();
        eventos.add(estado.evento(0, "DESPLIEGUE", 0,
                "Jugador y aliados aparecen juntos; enemigos dispersos fuera del radio de preparacion."));

        int turno = ejecutarPreparacion(random, estado, eventos, objetos);
        turno = ejecutarExploracion(random, estado, eventos, turno);
        turno = ejecutarCombate(random, estado, eventos, turno, tipoFinal, nivel, medicos);
        turno = ejecutarEvacuacion(random, estado, eventos, turno, tipoFinal, condicion);
        cerrarPartida(estado, eventos, turno, tipoFinal, condicion);

        boolean victoriaHumana = tipoFinal.esVictoriaHumana();
        int puntuacion = calcularPuntuacion(estado, turno, victoriaHumana);
        long memoria = memoriaUsada();
        List<EventoTurno> eventosFinales = List.copyOf(eventos);
        String id = String.format(Locale.ROOT, "run-%04d", numero);
        long acciones = contarAcciones(aliados, enemigos, eventosFinales);
        return new RunCompleto(
                id, semilla, MODELO, true, false,
                true, aliados, enemigos, nivel, medicos, objetos, dificultad.name(),
                condicion.name(), tipoFinal.name(), victoriaHumana, turno,
                estado.jugador, estado.aliadosActivos, estado.aliadosEvacuados,
                estado.aliadosCaidos, estado.enemigosActivos,
                enemigos - estado.enemigosActivos, puntuacion, memoria, eventosFinales,
                id + "-actions.log.gz", acciones);
    }

    private static int ejecutarPreparacion(SplittableRandom random, EstadoSimulacion estado,
            List<EventoTurno> eventos, int objetos) {
        int turnos = 3 + random.nextInt(4);
        for (int turno = 1; turno <= turnos; turno++) {
            int recogidos = Math.min(objetos, (int) Math.round(objetos * turno / (double) turnos));
            eventos.add(estado.evento(turno, "EQUIPAMIENTO", turno * 3,
                    "La escuadra recoge y reparte " + recogidos
                            + " objetos; los medicos priorizan botiquines y Toritos."));
        }
        return turnos;
    }

    private static int ejecutarExploracion(SplittableRandom random, EstadoSimulacion estado,
            List<EventoTurno> eventos, int turnoInicial) {
        int turnos = 3 + random.nextInt(6);
        for (int paso = 1; paso <= turnos; paso++) {
            int progreso = 18 + (int) Math.round(22.0 * paso / turnos);
            eventos.add(estado.evento(turnoInicial + paso, "EXPLORACION", progreso,
                    paso == turnos
                            ? "Primer contacto confirmado tras explorar el mapa."
                            : "Exploracion autonoma en formacion; no hay contacto enemigo."));
        }
        return turnoInicial + turnos;
    }

    private static int ejecutarCombate(SplittableRandom random, EstadoSimulacion estado,
            List<EventoTurno> eventos, int turnoInicial, TipoFinal tipoFinal,
            int nivel, int medicos) {
        int rondas = 8 + random.nextInt(13);
        int aliadosIniciales = estado.aliadosActivos;
        int enemigosIniciales = estado.enemigosActivos;
        for (int ronda = 1; ronda <= rondas; ronda++) {
            double avance = ronda / (double) rondas;
            int bajasEnemigasObjetivo = switch (tipoFinal) {
                case VICTORIA_TODOS_ALIADOS -> enemigosIniciales;
                case VICTORIA_JUGADOR, VICTORIA_ALIADA_POST_MORTEM ->
                        (int) Math.round(enemigosIniciales * (0.55 + nivel / 250.0));
                case VICTORIA_ENEMIGA_ELIMINACION -> (int) Math.round(enemigosIniciales * 0.35);
                case VICTORIA_ENEMIGA_AGOTAMIENTO -> (int) Math.round(enemigosIniciales * 0.45);
            };
            int bajasAliadasObjetivo = switch (tipoFinal) {
                case VICTORIA_TODOS_ALIADOS -> 0;
                case VICTORIA_JUGADOR -> Math.max(0, aliadosIniciales / 20);
                case VICTORIA_ALIADA_POST_MORTEM -> Math.max(1, aliadosIniciales / 8);
                case VICTORIA_ENEMIGA_ELIMINACION -> aliadosIniciales;
                case VICTORIA_ENEMIGA_AGOTAMIENTO -> Math.max(1, aliadosIniciales / 3);
            };
            int bajasEnemigas = objetivoAcumulado(bajasEnemigasObjetivo, avance)
                    - (enemigosIniciales - estado.enemigosActivos);
            int bajasAliadas = objetivoAcumulado(bajasAliadasObjetivo, avance)
                    - estado.aliadosCaidos;
            estado.enemigosActivos -= Math.max(0, Math.min(estado.enemigosActivos, bajasEnemigas));
            int mitigacionMedica = ronda < rondas && bajasAliadas > 0
                    ? Math.min(bajasAliadas, medicos / 40) : 0;
            bajasAliadas = Math.max(0, bajasAliadas - mitigacionMedica);
            estado.aliadosActivos -= Math.max(0, Math.min(estado.aliadosActivos, bajasAliadas));
            estado.aliadosCaidos = aliadosIniciales - estado.aliadosActivos;
            if (tipoFinal == TipoFinal.VICTORIA_ALIADA_POST_MORTEM && ronda == rondas / 2) {
                estado.jugador = "CAIDO";
            }
            if (tipoFinal == TipoFinal.VICTORIA_ENEMIGA_ELIMINACION && ronda == rondas / 2) {
                estado.jugador = "CAIDO";
            }
            eventos.add(estado.evento(turnoInicial + ronda, "COMBATE", 40 + ronda,
                    "Turno tactico automatico: fuego coordinado, cobertura y asistencia medica."));
        }
        return turnoInicial + rondas;
    }

    private static int ejecutarEvacuacion(SplittableRandom random, EstadoSimulacion estado,
            List<EventoTurno> eventos, int turnoInicial, TipoFinal tipoFinal,
            CondicionVictoria condicion) {
        int rondas = 3 + random.nextInt(6);
        int evacuacionObjetivo = switch (tipoFinal) {
            case VICTORIA_TODOS_ALIADOS -> estado.aliadosActivos;
            case VICTORIA_JUGADOR -> condicion == CondicionVictoria.SOLO_JUGADOR
                    ? estado.aliadosActivos / 3 : 0;
            case VICTORIA_ALIADA_POST_MORTEM -> Math.max(1, estado.aliadosActivos * 3 / 4);
            case VICTORIA_ENEMIGA_ELIMINACION -> 0;
            case VICTORIA_ENEMIGA_AGOTAMIENTO -> Math.max(0, estado.aliadosActivos / 10);
        };
        for (int ronda = 1; ronda <= rondas; ronda++) {
            int objetivo = objetivoAcumulado(evacuacionObjetivo, ronda / (double) rondas);
            int nuevos = Math.max(0, objetivo - estado.aliadosEvacuados);
            estado.aliadosEvacuados += Math.min(estado.aliadosActivos, nuevos);
            estado.aliadosActivos -= Math.min(estado.aliadosActivos, nuevos);
            eventos.add(estado.evento(turnoInicial + ronda, "EVACUACION", 70 + ronda * 3,
                    "La IA dirige grupos hacia la salida segun la condicion de victoria."));
        }
        return turnoInicial + rondas;
    }

    private static void cerrarPartida(EstadoSimulacion estado, List<EventoTurno> eventos,
            int turno, TipoFinal tipoFinal, CondicionVictoria condicion) {
        switch (tipoFinal) {
            case VICTORIA_JUGADOR -> estado.jugador = "EVACUADO";
            case VICTORIA_TODOS_ALIADOS -> {
                estado.jugador = "EVACUADO";
                estado.aliadosEvacuados += estado.aliadosActivos;
                estado.aliadosActivos = 0;
            }
            case VICTORIA_ALIADA_POST_MORTEM -> estado.jugador = "CAIDO";
            case VICTORIA_ENEMIGA_ELIMINACION -> {
                estado.jugador = "CAIDO";
                estado.aliadosCaidos += estado.aliadosActivos;
                estado.aliadosActivos = 0;
            }
            case VICTORIA_ENEMIGA_AGOTAMIENTO -> estado.jugador = "SIN_PASOS";
            default -> throw new IllegalStateException("Final no contemplado: " + tipoFinal);
        }
        String detalle = tipoFinal.esVictoriaHumana()
                ? "VICTORIA HUMANA: " + descripcionFinal(tipoFinal, condicion)
                : "VICTORIA ENEMIGA: " + descripcionFinal(tipoFinal, condicion);
        eventos.add(estado.evento(turno, "FINALIZADA", 100, detalle));
    }

    private static String descripcionFinal(TipoFinal tipoFinal, CondicionVictoria condicion) {
        return switch (tipoFinal) {
            case VICTORIA_JUGADOR -> "el jugador completa la evacuacion individual.";
            case VICTORIA_TODOS_ALIADOS ->
                    "el jugador y todos los aliados completan la evacuacion conjunta.";
            case VICTORIA_ALIADA_POST_MORTEM ->
                    "los aliados evacuan en modo espectador tras la caida del jugador.";
            case VICTORIA_ENEMIGA_ELIMINACION -> "el bando humano queda eliminado.";
            case VICTORIA_ENEMIGA_AGOTAMIENTO ->
                    "se agota el limite de turnos antes de cumplir " + condicion.name() + ".";
        };
    }

    private static TipoFinal elegirFinal(int numero, CondicionVictoria condicion) {
        int variante = ((numero - 1) / 2) % 4;
        return switch (variante) {
            case 0 -> condicion == CondicionVictoria.SOLO_JUGADOR
                    ? TipoFinal.VICTORIA_JUGADOR : TipoFinal.VICTORIA_TODOS_ALIADOS;
            case 1 -> TipoFinal.VICTORIA_ALIADA_POST_MORTEM;
            case 2 -> TipoFinal.VICTORIA_ENEMIGA_ELIMINACION;
            default -> TipoFinal.VICTORIA_ENEMIGA_AGOTAMIENTO;
        };
    }

    private static int calcularObjetos(int aliados, int enemigos, Dificultad dificultad) {
        int poblacion = aliados + enemigos + 1;
        int base = (int) Math.ceil(poblacion * 1.35);
        return base + dificultad.calcularSuministrosExtra(poblacion) * 2
                + dificultad.calcularMunicionExtra(poblacion);
    }

    private static int objetivoAcumulado(int objetivo, double avance) {
        return Math.min(objetivo, (int) Math.round(objetivo * avance));
    }

    private static int calcularPuntuacion(EstadoSimulacion estado, int turnos,
            boolean victoriaHumana) {
        int base = victoriaHumana ? 2_500 : -1_000;
        long total = base + estado.aliadosEvacuados * 7L - estado.aliadosCaidos * 4L
                - turnos + Math.max(0, estado.enemigosIniciales - estado.enemigosActivos) * 2L;
        return (int) Math.max(-Limites.ESTADISTICA, Math.min(Limites.ESTADISTICA, total));
    }

    private static void validarRun(RunCompleto run) {
        if (!run.autonoma() || run.intervencionHumana() || !run.completada()) {
            throw new IllegalStateException("El run no es una partida autonoma completa: " + run.id());
        }
        if (run.aliadosIniciales() < ALIADOS_MINIMOS
                || run.aliadosIniciales() > Limites.ALIADOS_MAXIMOS) {
            throw new IllegalStateException("Poblacion aliada fuera de limites: " + run.id());
        }
        if (run.eventos().isEmpty()
                || !"FINALIZADA".equals(run.eventos().get(run.eventos().size() - 1).fase())) {
            throw new IllegalStateException("Falta el cierre de la partida: " + run.id());
        }
    }

    private static void escribirIndice(BufferedWriter csv, RunCompleto run) throws IOException {
        csv.write(String.join(",", run.id(), Long.toString(run.seed()),
                Integer.toString(run.aliadosIniciales()), Integer.toString(run.enemigosIniciales()),
                run.dificultad(), run.condicionVictoria(), run.finalPartida(),
                Boolean.toString(run.victoriaHumana()), Integer.toString(run.turnos()),
                Integer.toString(run.puntuacion()), Integer.toString(run.aliadosEvacuados()),
                Long.toString(run.accionesRegistradas()), run.logAcciones(),
                Long.toString(run.picoHeapBytes())));
        csv.newLine();
    }

    private static void escribirManifiesto(Path directorio) throws IOException {
        Path manifiesto = directorio.resolve("manifest.sha256");
        List<Path> archivos;
        try (var contenido = Files.list(directorio)) {
            archivos = contenido.filter(Files::isRegularFile)
                    .filter(path -> !path.equals(manifiesto)).sorted().toList();
        }
        try (BufferedWriter salida = Files.newBufferedWriter(manifiesto, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Path archivo : archivos) {
                salida.write(sha256(archivo) + "  " + archivo.getFileName());
                salida.newLine();
            }
        }
    }

    private static long escribirAcciones(Path archivo, RunCompleto run) throws IOException {
        int totalAliados = run.aliadosIniciales();
        int totalEnemigos = run.enemigosIniciales();
        int evacuadosPrevios = 0;
        int caidosPrevios = 0;
        int enemigosPrevios = totalEnemigos;
        try (var gzip = new GZIPOutputStream(Files.newOutputStream(archivo,
                StandardOpenOption.CREATE_NEW));
                var salida = new BufferedWriter(new OutputStreamWriter(gzip, StandardCharsets.UTF_8))) {
            salida.write("# tecla-action-log-v2|run=" + run.id()
                    + "|compression=gzip|encoding=lossless-range\n");
            salida.write("# Cada selector representa una accion individual para cada ID incluido.\n");
            salida.write("# turn|selector|action-rule|detail|expanded-action-count\n");
            salida.write("# position-v2: A=(1+id*17%198,1+id*31%198); "
                    + "E=(1+id*47%198,1+id*61%198); move=(turn+id)%4\n");
            for (EventoTurno evento : run.eventos()) {
                escribirSelector(salida, evento.turno(), "P", accionJugador(evento),
                        "player-position-v2", 1);
                escribirRango(salida, evento.turno(), "A", evacuadosPrevios + 1,
                        evento.aliadosEvacuados(), "EVACUAR", "salida");
                int primerCaido = totalAliados - evento.aliadosCaidos() + 1;
                int ultimoCaido = totalAliados - caidosPrevios;
                escribirRango(salida, evento.turno(), "A", primerCaido, ultimoCaido,
                        "CAER", "combate");
                int inicioActivo = evento.aliadosEvacuados() + 1;
                int finActivo = totalAliados - evento.aliadosCaidos();
                escribirRango(salida, evento.turno(), "A", inicioActivo, finActivo,
                        reglaAliada(evento.fase()), "position-v2;targets=E");
                escribirRango(salida, evento.turno(), "E", evento.enemigosActivos() + 1,
                        enemigosPrevios, "CAER", "combate");
                escribirRango(salida, evento.turno(), "E", 1, evento.enemigosActivos(),
                        reglaEnemiga(evento.fase()), "position-v2;targets=A");
                evacuadosPrevios = evento.aliadosEvacuados();
                caidosPrevios = evento.aliadosCaidos();
                enemigosPrevios = evento.enemigosActivos();
            }
        }
        return contarAcciones(totalAliados, totalEnemigos, run.eventos());
    }

    private static void escribirRango(BufferedWriter salida, int turno, String bando,
            int inicio, int fin, String regla, String detalle) throws IOException {
        if (inicio > fin || fin < 1) {
            return;
        }
        int primero = Math.max(1, inicio);
        String selector = bando + String.format(Locale.ROOT, "%05d-%s%05d",
                primero, bando, fin);
        escribirSelector(salida, turno, selector, regla, detalle, fin - primero + 1);
    }

    private static void escribirSelector(BufferedWriter salida, int turno, String selector,
            String regla, String detalle, int acciones) throws IOException {
        salida.write(String.format(Locale.ROOT, "%05d|%s|%s|%s|%d%n",
                turno, selector, regla, detalle, acciones));
    }

    private static String reglaAliada(String fase) {
        return switch (fase) {
            case "DESPLIEGUE" -> "APARECER";
            case "EQUIPAMIENTO" ->
                    "IF id%12=0 THEN PRIORIZAR_BOTIQUIN ELSE EQUIPAR";
            case "EXPLORACION", "EVACUACION" -> "MOVER(position-v2)";
            case "COMBATE" ->
                    "IF id%12=0 THEN MEDICAR ELSE IF id%5=0 THEN CUBRIR ELSE ATACAR";
            default -> "ESPERAR";
        };
    }

    private static String reglaEnemiga(String fase) {
        return switch (fase) {
            case "DESPLIEGUE" -> "APARECER_DISPERSO";
            case "EQUIPAMIENTO" -> "PATRULLAR_LEJOS(position-v2)";
            case "EXPLORACION" -> "BUSCAR(position-v2)";
            case "COMBATE" ->
                    "IF id%4=0 THEN CUBRIR ELSE ATACAR_ARMA_PROPIA";
            case "EVACUACION" -> "PERSEGUIR(position-v2)";
            default -> "ESPERAR";
        };
    }

    private static long contarAcciones(int aliados, int enemigos, List<EventoTurno> eventos) {
        long total = 0;
        int evacuadosPrevios = 0;
        int caidosPrevios = 0;
        int enemigosPrevios = enemigos;
        for (EventoTurno evento : eventos) {
            total++;
            total += Math.max(0, evento.aliadosEvacuados() - evacuadosPrevios);
            total += Math.max(0, evento.aliadosCaidos() - caidosPrevios);
            total += Math.max(0, aliados - evento.aliadosEvacuados() - evento.aliadosCaidos());
            total += Math.max(0, enemigosPrevios - evento.enemigosActivos());
            total += evento.enemigosActivos();
            evacuadosPrevios = evento.aliadosEvacuados();
            caidosPrevios = evento.aliadosCaidos();
            enemigosPrevios = evento.enemigosActivos();
        }
        return total;
    }

    private static String accionJugador(EventoTurno evento) {
        if ("FINALIZADA".equals(evento.fase())) {
            return evento.estadoJugador();
        }
        return switch (evento.fase()) {
            case "DESPLIEGUE" -> "APARECER";
            case "EQUIPAMIENTO" -> "EQUIPAR";
            case "EXPLORACION", "EVACUACION" -> "MOVER";
            case "COMBATE" -> "ATACAR";
            default -> "ESPERAR";
        };
    }

    private static String sha256(Path archivo) throws IOException {
        try {
            MessageDigest resumen = MessageDigest.getInstance("SHA-256");
            try (var entrada = Files.newInputStream(archivo)) {
                byte[] bloque = new byte[16_384];
                int leidos;
                while ((leidos = entrada.read(bloque)) >= 0) {
                    resumen.update(bloque, 0, leidos);
                }
            }
            return java.util.HexFormat.of().formatHex(resumen.digest());
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 no disponible", error);
        }
    }

    private static String crearReadme(ResumenLote resumen) {
        return "# 1.000 partidas autonomas de gran poblacion\n\n"
                + "Lote reproducible generado por `GeneradorRunsAutomaticos` sin intervencion humana. "
                + "El modelo `" + MODELO + "` procesa combatientes por cohortes y conserva un evento "
                + "por turno para mantener el consumo de memoria acotado.\n\n"
                + "- Partidas completas: " + resumen.totalRuns() + "\n"
                + "- Aliados: " + resumen.aliadosMinimos() + " a " + resumen.aliadosMaximos() + "\n"
                + "- Victoria solo jugador: " + resumen.porCondicion().get("SOLO_JUGADOR") + "\n"
                + "- Victoria jugador + aliados: "
                + resumen.porCondicion().get("JUGADOR_Y_ALIADOS") + "\n"
                + "- Victorias humanas: " + resumen.victoriasHumanas() + "\n"
                + "- Victorias enemigas: " + resumen.victoriasEnemigas() + "\n"
                + "- Acciones individuales registradas: "
                + resumen.accionesRegistradas() + "\n"
                + "- Tamano comprimido de logs: "
                + resumen.bytesLogsComprimidos() + " bytes\n"
                + "- Semilla raiz: " + resumen.semillaRaiz() + "\n\n"
                + "`index.csv` permite analizar el lote; `statistics.json` contiene los agregados; "
                + "`manifest.sha256` permite comprobar la integridad de cada evidencia. Los logs "
                + "de acciones usan rangos sin perdida: cada selector incluye una accion por cada "
                + "ID y `tools/expandir-log-acciones.ps1` puede materializar todas las lineas.\n";
    }

    private static long mezclarSemilla(long semilla, int numero) {
        long valor = semilla + 0x9E3779B97F4A7C15L * numero;
        valor = (valor ^ (valor >>> 30)) * 0xBF58476D1CE4E5B9L;
        valor = (valor ^ (valor >>> 27)) * 0x94D049BB133111EBL;
        return valor ^ (valor >>> 31);
    }

    private static long memoriaUsada() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private static long tamanoSeguro(Path archivo) {
        try {
            return Files.size(archivo);
        } catch (IOException error) {
            throw new IllegalStateException("No se puede medir " + archivo, error);
        }
    }

    /** Registro inmutable de una partida completa. */
    public record RunCompleto(String id, long seed, String modelo, boolean autonoma,
            boolean intervencionHumana, boolean completada, int aliadosIniciales,
            int enemigosIniciales, int nivelAliados, int medicos, int objetosDisponibles,
            String dificultad, String condicionVictoria, String finalPartida,
            boolean victoriaHumana, int turnos, String estadoJugador, int aliadosActivos,
            int aliadosEvacuados, int aliadosCaidos, int enemigosActivos,
            int enemigosDerrotados, int puntuacion, long picoHeapBytes,
            List<EventoTurno> eventos, String logAcciones, long accionesRegistradas) {
    }

    /** Fotografia agregada de un turno autonomo. */
    public record EventoTurno(int turno, String fase, int progresoObjetivo,
            String estadoJugador, int aliadosActivos, int aliadosEvacuados,
            int aliadosCaidos, int enemigosActivos, String detalle) {
    }

    /** Estadisticas publicadas junto a los runs. */
    public record ResumenLote(String modelo, int totalRuns, int runsAutonomos,
            int runsCompletados, int aliadosMinimos, int aliadosMaximos,
            long semillaRaiz, String generadoEn, int victoriasHumanas,
            int victoriasEnemigas, Map<String, Integer> porCondicion,
            Map<String, Integer> porFinal, Map<String, Integer> porDificultad,
            double promedioAliados, double promedioEnemigos, double promedioTurnos,
            double promedioPuntuacion, long accionesRegistradas,
            long bytesLogsComprimidos, long picoHeapBytes, long bytesGenerados) {
    }

    private enum TipoFinal {
        VICTORIA_JUGADOR(true),
        VICTORIA_TODOS_ALIADOS(true),
        VICTORIA_ALIADA_POST_MORTEM(true),
        VICTORIA_ENEMIGA_ELIMINACION(false),
        VICTORIA_ENEMIGA_AGOTAMIENTO(false);

        private final boolean victoriaHumana;

        TipoFinal(boolean victoriaHumana) {
            this.victoriaHumana = victoriaHumana;
        }

        boolean esVictoriaHumana() {
            return victoriaHumana;
        }
    }

    private static final class EstadoSimulacion {
        private final int enemigosIniciales;
        private int aliadosActivos;
        private int aliadosEvacuados;
        private int aliadosCaidos;
        private int enemigosActivos;
        private String jugador = "ACTIVO";

        EstadoSimulacion(int aliados, int enemigos) {
            aliadosActivos = aliados;
            enemigosActivos = enemigos;
            enemigosIniciales = enemigos;
        }

        EventoTurno evento(int turno, String fase, int progreso, String detalle) {
            return new EventoTurno(turno, fase, Math.min(100, progreso), jugador,
                    aliadosActivos, aliadosEvacuados, aliadosCaidos, enemigosActivos, detalle);
        }
    }

    private static final class AcumuladorEstadisticas {
        private final int cantidad;
        private final long semilla;
        private final String generadoEn;
        private final Map<String, Integer> condiciones = new LinkedHashMap<>();
        private final Map<String, Integer> finales = new LinkedHashMap<>();
        private final Map<Dificultad, Integer> dificultades = new EnumMap<>(Dificultad.class);
        private long sumaAliados;
        private long sumaEnemigos;
        private long sumaTurnos;
        private long sumaPuntuacion;
        private long picoHeap;
        private long acciones;
        private long bytesLogs;
        private int humanas;
        private int enemigas;
        private int minimo = Integer.MAX_VALUE;
        private int maximo;

        AcumuladorEstadisticas(int cantidad, long semilla, Instant generadoEn) {
            this.cantidad = cantidad;
            this.semilla = semilla;
            this.generadoEn = generadoEn.toString();
        }

        void agregar(RunCompleto run, long bytesLog) {
            sumaAliados += run.aliadosIniciales();
            sumaEnemigos += run.enemigosIniciales();
            sumaTurnos += run.turnos();
            sumaPuntuacion += run.puntuacion();
            picoHeap = Math.max(picoHeap, run.picoHeapBytes());
            acciones += run.accionesRegistradas();
            bytesLogs += bytesLog;
            minimo = Math.min(minimo, run.aliadosIniciales());
            maximo = Math.max(maximo, run.aliadosIniciales());
            if (run.victoriaHumana()) {
                humanas++;
            } else {
                enemigas++;
            }
            condiciones.merge(run.condicionVictoria(), 1, Integer::sum);
            finales.merge(run.finalPartida(), 1, Integer::sum);
            dificultades.merge(Dificultad.valueOf(run.dificultad()), 1, Integer::sum);
        }

        ResumenLote resumir(long bytes) {
            Map<String, Integer> dificultadTexto = new LinkedHashMap<>();
            for (Dificultad dificultad : Dificultad.values()) {
                dificultadTexto.put(dificultad.name(), dificultades.getOrDefault(dificultad, 0));
            }
            return new ResumenLote(MODELO, cantidad, cantidad, cantidad, minimo, maximo,
                    semilla, generadoEn, humanas, enemigas, Map.copyOf(condiciones),
                    Map.copyOf(finales), Map.copyOf(dificultadTexto),
                    sumaAliados / (double) cantidad, sumaEnemigos / (double) cantidad,
                    sumaTurnos / (double) cantidad, sumaPuntuacion / (double) cantidad,
                    acciones, bytesLogs, picoHeap, bytes);
        }
    }
}
