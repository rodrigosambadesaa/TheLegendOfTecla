# Guia de ejecucion

The Legend of Tecla es un juego de consola. La forma recomendada de ejecutarlo es
con Docker, porque no requiere instalar Java ni Maven en el equipo.

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
