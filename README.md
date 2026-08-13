# The Legend of Tecla (Java)

[![CI](https://github.com/rodrigosambadesaa/TheLegendOfTecla/actions/workflows/ci.yml/badge.svg)](https://github.com/rodrigosambadesaa/TheLegendOfTecla/actions/workflows/ci.yml)
[![Java 17](https://img.shields.io/badge/Java-17-007396?logo=openjdk)](https://openjdk.org/projects/jdk/17/)
[![Maven](https://img.shields.io/badge/Maven-3.9%2B-C71A36?logo=apachemaven)](https://maven.apache.org/)
[![Coverage](https://img.shields.io/badge/coverage-JaCoCo-brightgreen)](https://github.com/rodrigosambadesaa/TheLegendOfTecla/actions/workflows/ci.yml)
[![License](https://img.shields.io/github/license/rodrigosambadesaa/TheLegendOfTecla)](LICENSE)
[![Latest release](https://img.shields.io/github/v/release/rodrigosambadesaa/TheLegendOfTecla?display_name=tag)](https://github.com/rodrigosambadesaa/TheLegendOfTecla/releases/latest)

Implementacion en Java del proyecto de POO por entregas (P1, P2, P3) y con ampliaciones voluntarias:

- Mapa ASCII con celdas transitables/no transitables.
- Jerarquia de personajes y objetos con herencia/polimorfismo.
- Inventario con peso y capacidad.
- Equipar/desequipar, usar objetos, atacar NPCs, recorrido.
- Interfaz de comandos y comandos compuestos/repetidos.
- Cargador por defecto y cargador por ficheros.
- Modo de mapa grande (50x50) con 50 variantes deterministas seleccionables.
- Aliados opcionales en todos los modos, con cantidad y atributos calculados automaticamente.
- Aliados que inspeccionan cada celda antes de descubrir, recoger, sustituir, equipar o tirar objetos.
- Binoculares de un solo uso para el jugador y los aliados, también cuando están equipados.
- Accion `descansar`: recupera salud y energia a cambio de atraer a los enemigos durante su turno.
- Orden `pedir ayuda` y boton equivalente: los aliados sin suministros exploran antes de acudir.
- Formaciones defensiva y ofensiva en consola y GUI: los aliados acompañan al jugador,
  el miembro en mejor estado busca suministros cerca del grupo y los enemigos adaptan sus objetivos.
- Registro de combate completo para jugador, aliados y enemigos: atacante, objetivo, vida quitada,
  vida restante y muerte, compartido por consola y GUI.
- Zonas oscuras aleatorias o fijas, linternas reutilizables, antorchas murales derribables,
  suelos de madera, incendios propagables y daño ambiental para todos los personajes.
- Fuentes y cubos de agua reutilizables; los aliados también recogen, rellenan y usan cubos.
- Representación ambiental ASCII y gráfica, tooltips y efectos de sonido posicionales CC0.
- Seguimiento permanente de cada aliado: vida, energia, posicion, objetos, equipo, combate y evacuacion.
- Rescate energetico seguro: calculo de ruta/reserva, autoabastecimiento y busqueda de Toritos sin recursos infinitos.
- Suministros de energia escalados y Toritos distribuidos sobre una ruta transitable en mapas grandes.
- Energia inicial proporcional a la ruta transitable real en mapas grandes, tanto para jugador como aliados.
- Bucle principal con captura de excepciones.
- Interfaz grafica completa en una unica ventana para los tres modos.
- Mapa grafico con celdas, jugador, objetivo, objetos, aliados y enemigos.
- Botones para coger, usar, tirar, equipar, desequipar, atacar, descansar, lanzar explosivos y pedir ayuda.
- Editor grafico de escenarios completos con persistencia JSON.
- Bus de eventos de dominio de instancia con orden determinista, reloj inyectable y listeners aislados.
- Nueve efectos temporales con duracion, acumulacion e interacciones entre fuego, agua y descanso.
- Puertas, credenciales, terminales, interruptores, barricadas, cobertura y cinco clases de trampas.
- Municion finita, cargadores, recarga, intercambio atomico entre personajes y crafting extensible.
- IA State/Strategy con ruido, memoria y ocho estados de alerta; seis roles enemigos y dos jefes por fases.
- Misiones/objetivos opcionales, campana, XP, niveles, habilidades, logros y estadisticas por eventos.
- Tres generadores procedurales reproducibles, savegames versionados y replay con validacion SHA-256.
- Despliegue de escuadron unificado: jugador y aliados parten de la casilla inicial transitable;
  los aliados priorizan asistir al jugador y despues explorar, mientras los enemigos forman
  anillos de intercepcion mas exigentes cuando la partida incluye aliados.

## Especificaciones y validación

- [P1 (2014)](2014_POO_Proyecto_P1.pdf)
- [P2 (2015)](2015_POO_Proyecto_P2.pdf)
- [P3 (2015)](2015_POO_Proyecto_P3.pdf)
- [P4 opcional (2015)](2015_POO_Proyecto_OPT.pdf)
- [Matriz de cumplimiento](docs/CUMPLIMIENTO_ESPECIFICACIONES.md)
- [Pruebas reproducibles de consola y GUI](docs/partidas_probadas.md)
- [Informe final de la ampliacion roguelike](docs/INFORME_FINAL_ROGUELIKE.md)

## Interfaz grafica

### Con Docker y un solo comando

En Windows PowerShell, este lanzador construye el contenedor, espera a que este
preparado y abre la GUI en el navegador:

```powershell
.\run-gui.ps1
```

Si la politica de ejecucion de PowerShell bloquea scripts locales:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-gui.ps1
```

En Linux o dentro de WSL2:

```bash
bash ./run-gui-linux.sh
```

En macOS (Intel o Apple Silicon), con Docker Desktop iniciado:

```bash
bash ./run-gui-macos.command
```

Los lanzadores Linux/macOS utilizan [run-gui.sh](run-gui.sh), esperan a que
noVNC responda y abren el navegador. En WSL se usa Docker nativo si la
integracion esta activada y, si no lo esta, se detecta automaticamente
`docker.exe`. No se necesita X11, WSLg ni XQuartz.

Tambien puede iniciarse directamente con Docker Compose:

```bash
docker compose up --build --detach gui
```

Despues abre `http://localhost:6080/vnc.html?autoconnect=1&resize=scale`. No hace
falta instalar VcXsrv ni configurar `DISPLAY`: el contenedor incluye su propio
escritorio virtual y noVNC. Para detenerlo:

```bash
docker compose stop gui
```

Para no abrir el navegador o reutilizar una imagen ya construida:

```bash
bash ./run-gui.sh --no-open --no-build
```

El puerto puede cambiarse, por ejemplo, con
`TECLA_GUI_PORT=6081 bash ./run-gui.sh`.

### Ejecucion local

Despues de compilar, abre el juego completo en una ventana:

```bash
mvn clean package
java -jar target/the-legend-of-tecla.jar --gui
```

Para abrir directamente el editor de mapas:

```bash
java -jar target/the-legend-of-tecla.jar --editor
```

La GUI permite seleccionar los modos predeterminado, grande y desde ficheros,
activar aliados mediante una casilla y elegir entre la cantidad calculada por el
juego o una cantidad exacta indicada por el jugador. Tambien permite decidir si
basta con que llegue el jugador o deben llegar todos los aliados, y elegir una de
las 50 variantes del mapa grande. La consola ofrece las mismas opciones.

El campo de comandos acepta también las formas históricas compuestas, entre ellas
`mover este 3`, `atacar 4e 2`, `atacar 4e alien_azul 2` y
`equipar lanzacohetes ametralladora`. Los mismos comandos funcionan en consola.

## Ejecutar con Docker

La forma mas sencilla inicia directamente una partida de consola:

```bash
docker compose run --rm juego
```

Para elegir nombre, clase, modo, dificultad y dimensiones mediante el asistente:

```bash
docker compose run --rm juego --interactivo
```

La documentacion completa, incluyendo Docker, ejecucion local, argumentos y
el editor grafico, esta en [EJECUCION.md](EJECUCION.md).

## Javadoc

La documentacion tecnica dispone de una web Docker independiente. Incluye una
portada en espanol con arquitectura, responsabilidades, flujos de arranque y
turno, balance, ayuda aliada y guias de extension; desde ella se accede al
Javadoc navegable y buscable generado directamente desde el codigo:

```bash
docker compose up --build --detach javadoc-web
```

Abre despues `http://localhost:8081`. La publicacion queda limitada al equipo
local. Puede elegirse otro puerto con
`TECLA_DOCS_PORT=8090 docker compose up --build --detach javadoc-web`.

Para detener la web:

```bash
docker compose stop javadoc-web
```

El servicio independiente `javadoc` conserva la generacion de archivos HTML
en `target/reports/apidocs/`:

```bash
docker compose run --rm javadoc
```

La misma API se genera localmente con `mvn javadoc:javadoc` y durante
`mvn verify`.

## Ejecutar localmente

Con JDK 17+ y Maven 3.9+:

```bash
mvn clean package
java -jar target/the-legend-of-tecla.jar --rapido
```

Al iniciar el juego, el modo se elige con:

- `1`: mapa por defecto.
- `2`: mapa grande 50x50 con 50 variantes.
- `3`: carga desde ficheros.
- `4`: mapa procedural reproducible (por CLI: `--modo procedural --seed 12345`).

En todos los modos puede elegirse `no`, `auto` o una cantidad entre 1 y 1000.
`auto` calcula el numero segun el mapa y la dificultad; una cifra despliega
exactamente esa cantidad. Por CLI se usan, por ejemplo, `--aliados auto` y
`--aliados 12`. El nivel comun puede dejarse automatico o personalizarse entre
1 y 100 con `--nivel-aliados 15`; cada nivel mejora salud, energia, vision de
forma gradual y capacidad de carga. Si se activan, se elige tambien entre victoria de
`solo_jugador` o de `jugador_y_aliados`, sin importar el orden de llegada.

El estado del escuadron muestra la puntuacion individual de cada aliado y su
total. Cada puntuacion se actualiza durante la partida a partir de salud, energia,
progreso hacia la salida y supervivencia; evacuar al aliado concede el mayor bonus.

## Formato basico de ficheros (modo ficheros)

### mapa.txt

```text
6x6
0,0
5,5
# Directivas opcionales: tipo;fila;columna
oscura;2;2
madera;2;2
antorcha;2;2
fuente;3;1
```

### objetos.txt

```text
# nombre;tipo;fila;columna
botiquin_A;botiquin;1;1
rifle_A;arma;2;3
linterna_A;linterna;0;1
cubo_A;cuboagua;1;0
```

Las celdas `oscura` declaradas en TXT permanecen oscuras durante toda la partida.
En `escenario.json`, cada celda admite los booleanos `oscura`, `sueloMadera`,
`antorchaMural` y `fuenteAgua`. Los objetos JSON admiten además los tipos
`linterna` (`valor` es su alcance) y `cuboagua` (`valor > 0` significa lleno).

### enemigos.txt

```text
# tipo;nombre;fila;columna
sectoid;Sectoid_A;3;2
heavyfloater;Heavy_1;4;4
```

## Ficheros de ejemplo incluidos

- [data/escenario_basico/mapa.txt](data/escenario_basico/mapa.txt)
- [data/escenario_basico/objetos.txt](data/escenario_basico/objetos.txt)
- [data/escenario_basico/enemigos.txt](data/escenario_basico/enemigos.txt)
- [data/escenario_grande/mapa.txt](data/escenario_grande/mapa.txt)
- [data/escenario_grande/objetos.txt](data/escenario_grande/objetos.txt)
- [data/escenario_grande/enemigos.txt](data/escenario_grande/enemigos.txt)

Los aliados de escenarios TXT se controlan con la opcion global `--aliados si|no`.
En JSON se guarda tambien `"conAliados": true|false`; la GUI lee ese valor al
seleccionar el escenario y permite confirmarlo o cambiarlo antes de jugar. No se
guardan posiciones ni caracteristicas de aliados personalizables.

Tambien se incluye un escenario completo en
[data/escenario_json/escenario.json](data/escenario_json/escenario.json), compatible
con el juego y con el editor grafico.

## Sistemas tacticos nuevos

Los comandos históricos siguen disponibles. Los nuevos comandos principales son:

```text
recargar [arma]                 estado arma
dar <objeto> <aliado>          pedir <objeto> <aliado>
intercambiar <obj1> <obj2> <aliado>
abrir puerta                   cerrar puerta
hackear terminal               activar interruptor
inspeccionar trampa            desactivar trampa
recetas                        fabricar <resultado>
guardar partida [archivo]      cargar partida [archivo]
estadisticas                    logros
```

El modo procedural se ejecuta, por ejemplo, con:

```bash
java -jar target/the-legend-of-tecla.jar --rapido --modo procedural --seed 12345
```

Un elemento interactivo opcional en `escenario.json` conserva la compatibilidad
porque todos sus campos tienen valores predeterminados:

```json
{
  "fila": 2,
  "columna": 4,
  "descripcion": "Acceso al reactor",
  "transitable": true,
  "elementoTipo": "puerta",
  "elementoId": "reactor-norte",
  "elementoEstado": "BLOQUEADA",
  "referencia": "tarjeta-reactor",
  "resistencia": 30,
  "dificultad": 5
}
```

Los savegames son deliberadamente distintos de los escenarios y comienzan con
`{"version": 1}`. Guardan el mapa modificado, puertas, trampas, coberturas,
turnos, personajes, inventarios, credenciales, componentes, equipamiento,
cargadores, estados, fuego, progresion, estadisticas, logros y celdas
inspeccionadas. Una version desconocida o un JSON corrupto se rechazan con un
error controlado.

La campaña opcional usa `Campana` para encadenar misiones. Su índice, nivel, XP
y habilidades pueden guardarse con `PersistenciaCampana`; el equipo, los aliados
supervivientes y el resto del estado continúan en el savegame de la partida.

La arquitectura y los contratos de ampliacion se detallan en
[docs/ARQUITECTURA_ROGUELIKE.md](docs/ARQUITECTURA_ROGUELIKE.md).
