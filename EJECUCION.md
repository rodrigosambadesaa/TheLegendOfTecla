# Guia de ejecucion

The Legend of Tecla se puede jugar tanto en interfaz grafica como en consola.
Ambas interfaces utilizan el mismo motor de partida y los mismos cargadores.

## Interfaz grafica completa

### Docker: un solo comando

La GUI Docker es autocontenida: ejecuta Swing sobre Xvfb y publica el escritorio
mediante noVNC. No requiere VcXsrv, XQuartz, WSLg ni configurar `DISPLAY`.

En Windows PowerShell ejecuta:

```powershell
.\run-gui.ps1
```

El script construye e inicia el servicio `gui`, espera a que noVNC responda y
abre automáticamente el navegador. Si PowerShell bloquea scripts locales:

```powershell
powershell -ExecutionPolicy Bypass -File .\run-gui.ps1
```

El comando Docker Compose equivalente, válido en cualquier sistema, es:

```bash
docker compose up --build --detach gui
```

Abre después:

```text
http://localhost:6080/vnc.html?autoconnect=1&resize=scale
```

La publicación está limitada a `127.0.0.1`, por lo que noVNC no queda expuesto a
otros equipos de la red. La primera construcción tarda más porque instala el
escritorio virtual; las siguientes reutilizan la caché de Docker.

Para consultar el estado o detener la GUI:

```bash
docker compose ps gui
docker compose logs gui
docker compose stop gui
```

### Ejecucion local

Requisitos: JDK 17 o posterior y Maven 3.9 o posterior. Compila y abre la ventana:

```bash
mvn clean package
java -jar target/the-legend-of-tecla.jar --gui
```

Desde la pantalla inicial se puede jugar en cualquiera de los tres modos:

- Mapa predeterminado.
- Mapa grande con enemigos y aliados.
- Escenario desde los tres ficheros TXT antiguos o desde `escenario.json`.

Toda la partida se desarrolla en una sola ventana. El mapa utiliza celdas y
formas graficas para representar muros, jugador, objetivo, objetos, aliados y
enemigos visibles. Los controles de movimiento, coger, usar, tirar, equipar,
desequipar, atacar, inventario, estado, ayuda, recorrido y salida estan
disponibles como botones. Las acciones que necesitan un objeto o enemigo abren
un selector contextual, por lo que se puede completar la partida sin escribir
comandos. El campo situado bajo el mapa se mantiene como alternativa.

### Editor grafico de mapas

Abre directamente el editor con:

```bash
java -jar target/the-legend-of-tecla.jar --editor
```

Tambien se puede acceder desde el boton `Editor grafico de mapas` de la pantalla
inicial. El editor permite:

- Crear mapas de 3x3 hasta 60x60 y definir su nombre, descripcion y pasos maximos.
- Personalizar la descripcion y transitabilidad de cada celda.
- Colocar y mover el inicio del jugador y el objetivo.
- Anadir enemigos configurando tipo, nombre, salud, energia y vision.
- Anadir aliados configurando nombre, salud, energia y vision.
- Anadir botiquines, armas, armaduras, binoculares, energia y explosivos con
  peso y atributos especificos.
- Abrir, editar y guardar escenarios completos en `escenario.json`.

Al guardar, selecciona un directorio. El editor crea dentro el archivo
`escenario.json`; ese mismo directorio queda seleccionado en el modo de juego
desde ficheros. Hay un ejemplo listo para abrir en `data/escenario_json`.

El servicio Docker `gui` permite usar también el editor gráfico completo desde
el navegador. El servicio `juego` conserva la imagen ligera de modo consola.

## Opcion 1: Docker Compose (recomendada)

Requisito: Docker Desktop o Docker Engine con el complemento Compose.

Desde la raiz del proyecto, ejecuta:

```bash
docker compose run --rm juego
```

La primera ejecucion construye la imagen. El juego arranca en modo rapido con el
personaje `Tecla`, clase `marine`, mapa por defecto y dificultad normal.

Para utilizar el asistente inicial y elegir cada opcion:

```bash
docker compose run --rm juego --interactivo
```

## Opcion 2: Docker sin Compose

Construye la imagen una vez:

```bash
docker build -t the-legend-of-tecla .
```

Inicia una partida rapida:

```bash
docker run --rm -it the-legend-of-tecla
```

Inicia el asistente interactivo:

```bash
docker run --rm -it the-legend-of-tecla --interactivo
```

`-it` conecta el teclado y la terminal al contenedor. `--rm` elimina el
contenedor cuando termina la partida; la imagen permanece disponible.

### Personalizar una partida

Las opciones se pueden combinar con `--rapido` para evitar preguntas iniciales:

```bash
docker run --rm -it the-legend-of-tecla \
  --rapido \
  --nombre Ada \
  --clase francotirador \
  --modo grande \
  --dificultad dificil \
  --dimensiones 20x30
```

Valores admitidos:

- `--clase`: `marine`, `francotirador` o `zapador`.
- `--modo`: `default`, `grande` o `ficheros`.
- `--dificultad`: `muy_facil`, `facil`, `normal`, `dificil`,
  `muy_dificil`, `pesadilla` o `demente`.
- `--dimensiones`: filas y columnas en formato `12x20`, con un minimo de `3x3`.

Para jugar con el escenario basico incluido en la imagen:

```bash
docker run --rm -it the-legend-of-tecla \
  --rapido --modo ficheros --datos data/escenario_basico
```

Para cargar un escenario propio, monta su directorio en modo de solo lectura.
Este ejemplo funciona en PowerShell desde la raiz del proyecto:

```powershell
docker run --rm -it `
  -v "${PWD}/mi-escenario:/escenario:ro" `
  the-legend-of-tecla `
  --rapido --modo ficheros --datos /escenario
```

El directorio debe contener `mapa.txt`, `objetos.txt` y `enemigos.txt`.

## Opcion 3: ejecucion local con Java

Requisitos: JDK 17 o posterior y Maven 3.9 o posterior.

Compila y empaqueta:

```bash
mvn clean package
```

Ejecuta el JAR:

```bash
java -jar target/the-legend-of-tecla.jar --rapido
```

Para mostrar todas las opciones disponibles:

```bash
java -jar target/the-legend-of-tecla.jar --help
```

## Generar Javadoc HTML

La documentacion de todas las clases, constructores y metodos publicos se puede
generar sin instalar Java ni Maven localmente:

```bash
docker compose run --rm javadoc
```

El resultado se escribe en `target/reports/apidocs/`. Abre
`target/reports/apidocs/index.html` con cualquier navegador.

Si Java 17 y Maven estan instalados, los comandos equivalentes son:

```bash
mvn javadoc:javadoc
mvn verify
```

`mvn verify` ejecuta las pruebas y genera tambien el Javadoc. La configuracion
valida la sintaxis, referencias y estructura de los comentarios, publica solo
la API visible y hace fallar la construccion ante errores o avisos de Javadoc.

## Uso de la consola

Una vez iniciada la partida aparece el indicador `accion>`. Escribe `ayuda` para
ver los comandos del juego. Algunos ejemplos son:

```text
mirar
mover este
inventario
coger botiquin_1
atacar Sectoid_0
recorrido
salir
```

Al escribir `salir`, pulsar `Ctrl+C` o cerrar la entrada estandar finaliza la
sesion. Si la terminal no representa correctamente los colores, ejecuta Docker
con la variable `NO_COLOR`:

```bash
docker run --rm -it -e NO_COLOR=1 the-legend-of-tecla
```
