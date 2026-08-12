# Plan de mejora técnica

> Auditoría inicial: 4 de agosto de 2026. Este documento describe el estado
> observado en `main` antes de iniciar refactorizaciones funcionales. Es un plan
> incremental: una fase no se considera cerrada hasta conservar las pruebas de
> regresión y superar `mvn clean verify`.

## 1. Alcance y línea base

El repositorio es una aplicación monolítica de escritorio/consola escrita para
Java 17 y construida con Maven. Tiene 74 fuentes Java (aproximadamente 10 453
líneas) y dos suites JUnit 5 (23 casos, aproximadamente 936 líneas). El JAR
ejecutable se crea con Shade. Swing, consola, escenarios TXT/JSON, mapas grandes,
editor y aliados se integran sobre el mismo modelo y `MotorPartida`.

La ejecución inicial de `mvn --batch-mode clean verify` quedó bloqueada antes de
compilar: Maven Central respondió HTTP 403 al resolver
`maven-clean-plugin:3.2.0`. Es una limitación del entorno de auditoría, no un
fallo observado del código. No había artefactos Maven en caché con los que
realizar una ejecución sin conexión. Por ello, las comprobaciones funcionales
por modos quedan como criterio obligatorio de CI y de la siguiente sesión con
acceso al repositorio de dependencias; no se presenta como validado aquello que
no pudo ejecutarse.

## 2. Arquitectura actual

La arquitectura es una aplicación por paquetes, cercana a una separación por
capas pero con dependencias cruzadas entre dominio y presentación:

```text
Main
 ├─ config.OpcionesInicio
 ├─ console.Consola / ConsolaNormal
 ├─ gui.VentanaPrincipal
 └─ engine.FabricaJuego ──> loader.CargadorJuego*
                            └─> model.world.Juego

console ───────────────┐
gui.Swing ─────────────┼─> engine.MotorPartida ─> commands
                      │          │                 │
                      │          └──────────────┐  │
                      └─────────────────────────> model
loader TXT/JSON ────────────────────────────────> model
model.items <───────────────────────────────> model.characters
model.world ─> model.characters, y Juego ─> console.Consola
validation/constants/exceptions <──────── casi todos los paquetes
```

### Paquetes y responsabilidades

| Paquete | Responsabilidad observada | Dependencias principales |
|---|---|---|
| raíz / `config` | arranque, argumentos, selección de interfaz y modo | consola, GUI, motor, validación |
| `console` | entrada/salida textual y presentación de mensajes | excepciones/constantes |
| `gui` | configuración, juego, mapa por celdas y editor Swing | motor, modelo, loader JSON |
| `engine` | creación, ciclo, finalización y turnos de jugador/NPC/aliados | comandos, loaders, modelo, consola |
| `commands` | parseo y patrón Command para todas las acciones | modelo y excepciones |
| `loader` | modos predeterminado/grande/TXT/JSON, DTO JSON y generación | modelo, Gson, consola |
| `model.characters` | jugador, enemigos, aliado, mochila, combate/equipo | objetos y mundo |
| `model.items` | objetos consumibles/equipables y explosivos | personajes |
| `model.world` | celdas, mapa, partida y puntuación | personajes, objetos y consola |
| `validation`, `constants`, `exceptions` | invariantes, límites y errores de negocio | sin infraestructura pesada |

### Flujos principales

1. `Main` interpreta argumentos y abre Swing en el EDT o prepara la consola.
2. `FabricaJuego` elige el cargador según `default`, `grande` o `ficheros`.
3. Los cargadores construyen `Mapa`, `Jugador`, enemigos, objetos y aliados.
4. Ambas presentaciones envían texto a `MotorPartida.ejecutarComando`.
5. `CommandParser` crea comandos; estos mutan el modelo mediante
   `CommandContext`. Tras cada acción, el motor procesa NPC, aliados y final.
6. La GUI lee el mismo `Juego`; no existe un segundo motor gráfico.

## 3. Tamaño, responsabilidades y acoplamiento

### Clases prioritarias

| Clase | Líneas | Responsabilidades mezcladas / riesgo |
|---|---:|---|
| `MotorPartida` | 1 008 | coordinación, parseo, turnos, combate NPC, IA aliada, BFS, suministros, evacuación, rescate, mensajes y puntuación |
| `Personaje` | 623 | estado, invariantes, movimiento, inventario, equipo, uso de objetos y combate |
| `EscenarioDefinicion` | 568 | DTO raíz, DTO anidados, normalización, validación y copias defensivas |
| `PanelJuego` | 546 | composición visual, adaptación de comandos, diálogos, habilitación contextual, historial y refresco |
| `PanelEditorMapa` | 497 | edición, herramientas, formularios, validación, persistencia y navegación |
| `Juego` | 411 | agregado de partida, colecciones, inspección, aliados, ayuda y condiciones finales; además depende de `Consola` |
| `Mapa` | 313 | almacenamiento, validación, consultas y render ASCII |
| `CommandParser` | 253 | registro, aliases, tokenización y gramática de cada comando |

Los puntos de mayor acoplamiento son: `MotorPartida` conoce casi todos los
objetos y reglas; `Juego` (modelo) conoce una interfaz de presentación;
`PanelJuego` consulta detalles concretos de objetos/personajes para reconstruir
reglas contextuales; y los cargadores instancian directamente todos los
subtipos. Mover paquetes ahora produciría una refactorización masiva sin aportar
seguridad; primero se introducirán contratos y pruebas en las ubicaciones
actuales.

## 4. Funcionalidad preservada

La auditoría del código, documentación y datos confirma contratos para consola
y Swing, mapa ASCII/gráfico, modos predeterminado/grande (50 variantes)/TXT/JSON,
dificultad y dimensiones, comandos simples/compuestos/repetidos, exploración,
inventario/equipo, combate/explosivos, editor JSON, aliados automáticos,
recogida/equipo/asistencia/rescate/evacuación, puntuación y finales, Javadoc,
Docker de consola, GUI noVNC y portal documental. Ninguna fase debe retirar ni
cambiar silenciosamente estos contratos.

## 5. Pruebas existentes

`IntegracionModosGuiTest` concentra 20 pruebas de integración. Cubre ocultación
de objetos, flujo consola-motor, round trip JSON, render headless de los tres
modos y editor, botones, estado de aliados, variantes deterministas, suministros
por dificultad, explosivos, varias decisiones de asistencia/rescate y controles
numéricos. Produce PNG bajo `target/gui-smoke/`.

`ValidacionInternaTest` aporta tres pruebas de invariantes. Una exige por
reflexión getters y setters públicos para cada campo de numerosas clases: se
considera una restricción académica/de compatibilidad, no una arquitectura
objetivo. Las otras comprueban límites, copias inmutables y validación interna.

### Huecos de cobertura prioritarios

* Parser: entrada nula/vacía, comando desconocido, argumentos sobrantes,
  números/direcciones inválidos y límites de repetición.
* Motor: bordes/muros, inventario lleno/peso, objeto o enemigo inexistente,
  enemigo ya muerto, explosivo fuera de alcance y todos los estados finales.
* Loader: TXT y JSON corruptos/desconocidos, duplicados, ausencia de ruta,
  dimensiones extremas y mensajes diagnósticos.
* Aliados: energía/ruta imposibles, contención por el mismo objeto, bloqueo,
  conflictos de movimiento y métricas reproducibles.
* GUI: EDT, accesibilidad, acciones deshabilitadas y equivalencia completa con
  consola. Deben seguir siendo headless salvo una prueba smoke en Xvfb.
* Infraestructura: arranque real del JAR, Compose/noVNC, compatibilidad de datos
  históricos y restauración de guardados todavía inexistentes.

## 6. Riesgos de regresión y mitigación

| Riesgo | Mitigación |
|---|---|
| Cambiar la semántica de turno al extraer servicios | pruebas de caracterización antes de mover una regla; `MotorPartida` conserva su API |
| Divergencia consola/GUI | ambas envían comandos al mismo motor; prohibir reglas de dominio nuevas en Swing/consola |
| Romper TXT/JSON | fixtures históricos permanentes, valores por defecto, round trip y migradores versionados |
| Alterar determinismo de mapas/aliados | fijar semilla/variante en pruebas y versionar el algoritmo si cambia |
| Setter deja estados imposibles | conservar firma por compatibilidad, deprecar mutadores peligrosos y delegar en métodos de dominio validados |
| Exponer colecciones mutables | vistas inmutables/copias; métodos explícitos para agregar, retirar y transicionar |
| Bloquear el EDT | I/O, rutas grandes y guardado en `SwingWorker`; actualización visual únicamente en EDT |
| Endurecer calidad de golpe | umbral inicial medido y deuda documentada; incrementar, nunca excluir dominio esencial |
| CVE/análisis dependiente de red | ejecución CI separada y explícita, caché de Maven/NVD cuando sea posible |

## 7. Plan incremental

1. **Auditoría y línea base:** este documento, inventario verificable y registro
   honesto de limitaciones.
2. **CI:** Java 17, caché Maven, `clean verify`, Javadoc y artefactos (JAR,
   JaCoCo, Javadoc y capturas).
3. **Calidad:** JaCoCo HTML/XML con umbral inicial y política de incremento,
   Enforcer, Checkstyle razonable, SpotBugs y Dependency-Check; Dependabot.
4. **Pruebas:** dividir las dos suites por responsabilidad sin reescribirlas y
   añadir casos negativos/contratos por motor, comandos, loaders, modelo, GUI e
   integración.
5. **Encapsulación:** caracterizar setters; deprecar sustituciones peligrosas;
   introducir métodos de dominio y colecciones defensivas.
6. **Servicios:** extraer primero rutas, luego movimiento, combate, inventario,
   aliados y finalización. `MotorPartida` queda como coordinador/fachada estable.
7. **Eventos:** publicador síncrono pequeño e interfaces de suscriptor; eventos
   inmutables, sin framework ni dependencia desde dominio hacia Swing.
8. **Guardado:** DTO distinto del escenario, `schemaVersion`, instantáneas
   completas, escritura temporal+atómica, backup, migración, comandos y GUI.
9. **IA explicable:** estados, contexto/decisión, cinco estrategias, explicación
   y métricas; mantener la estrategia actual como equilibrada durante migración.
10. **Rutas:** interfaz, resultados diagnósticos, BFS/Dijkstra/A*, caché e
    invalidación; pruebas de colisión y mapas 50x50/100x100.
11. **Terreno y combate táctico:** modelo optativo y valores `NORMAL` por defecto;
    puntos de acción únicamente como modo compatible y desactivado inicialmente.
12. **Contenido/misiones/campaña:** catálogos validados, objetivos versionados,
    editor y campaña de demostración basada en esos contratos.
13. **Swing, accesibilidad e i18n:** layout/temas/zoom; teclado y nombres
    accesibles; `ResourceBundle` español/gallego/inglés.
14. **Operación:** SLF4J/Logback con rotación, mediciones/JMH justificadas,
    jpackage/releases/checksums y versionado semántico.
15. **Documentación/demostración/extensión:** documentos públicos contrastados
    con código, capturas locales y ejemplos probados.

Cada paso funcional tendrá su propio commit; una refactorización y una función
grande no compartirán commit.

## 8. Compatibilidad de escenarios TXT y JSON

* TXT mantiene los tres archivos y gramática actuales. Las extensiones serán
  opcionales, ignorables por lectores antiguos cuando sea viable y nunca
  reinterpretarán columnas existentes.
* JSON conserva `version` y todos los nombres actuales. Nuevas propiedades
  serán opcionales; ausencia de terreno equivale a `NORMAL`, y ausencia de
  misión reproduce la victoria actual. Gson debe tolerar propiedades futuras
  pero rechazar tipos/valores esenciales inválidos con contexto.
* Los fixtures de `data/` se prueban sin modificarlos. Se añadirán fixtures por
  versión y pruebas de carga + serialización + recarga semánticamente equivalente.
* Un cambio no retrocompatible exige migrador explícito, backup y documentación;
  nunca se sobrescribirá automáticamente el original.
* `EscenarioDefinicion` describe el comienzo; `EstadoPartida` será una instantánea
  separada. Cargar una partida no se hará pasar por cargar un escenario.

## 9. Un solo motor para consola y GUI

`MotorPartida.ejecutarComando(String)` continúa siendo la fachada común durante
la transición. Los servicios extraídos reciben el agregado/DTO necesario y no
componentes Swing ni flujos de consola. La presentación consume resultados y
eventos tipados; el texto es un adaptador, no el resultado de negocio. Cada
acción nueva se implementará una vez como caso de uso y tendrá adaptador de
comando y botón. Una prueba contractual ejecutará la misma secuencia desde
ambos adaptadores y comparará el estado, no solo los mensajes.

## 10. Criterios de aceptación y puerta por fase

Para cada fase:

1. contratos existentes y fixtures históricos permanecen;
2. `mvn --batch-mode clean verify` termina correctamente con Java 17;
3. Javadoc, análisis estático y umbral JaCoCo pasan sin silenciar pruebas;
4. el JAR responde a `--help` y arranca en los tres modos;
5. las pruebas GUI headless generan capturas y una smoke test Xvfb valida Swing;
6. Docker de consola, noVNC y Javadoc construyen y pasan healthcheck cuando la
   fase toca distribución/presentación;
7. cambios de contrato incluyen migración, documentación y prueba;
8. riesgos/deuda que no pertenezcan a la fase se registran, no se ocultan.

La meta de cobertura es al menos 80 % en modelo/motor, 75 % en comandos/loaders
y 70–75 % total. El primer umbral automático debe basarse en el informe real y
subir de forma monotónica; no se excluirán GUI o clases centrales para fabricar
el porcentaje. La finalización global exige además guardado/reanudación, IA
explicable, misiones y campaña, por lo que esta auditoría no declara completado
el programa completo de 25 fases.
## 11. Estado de verificación actualizado

El 5 de agosto de 2026 se repitió la línea base con Temurin 17.0.20 y Maven
3.9.11. `mvn --batch-mode --no-transfer-progress clean verify` finalizó
correctamente: 39 pruebas, JAR ejecutable, JaCoCo, Checkstyle, SpotBugs y
Javadoc. La cobertura medida es 70,39 % de líneas (2 921/4 150) y 50,27 % de
ramas (926/1 842); por ello, la puerta mínima de JaCoCo queda fijada en 70 %.

También se construyeron las imágenes de consola, GUI y documentación. El JAR
del contenedor de consola respondió a `--help`; la GUI noVNC y la web de
Javadoc alcanzaron estado `healthy`, y `/vnc.html`, `/health` y
`/api/index.html` respondieron HTTP 200. Los servicios se detuvieron tras la
prueba. Sigue pendiente elevar `commands` del 69,3 % al objetivo del 75 %, así
como implementar las fases funcionales posteriores sin reducir esta línea base.
