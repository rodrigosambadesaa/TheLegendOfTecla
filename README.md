# The Legend of Tecla (Java)

Implementacion en Java del proyecto de POO por entregas (P1, P2, P3) y con ampliaciones voluntarias:

- Mapa ASCII con celdas transitables/no transitables.
- Jerarquia de personajes y objetos con herencia/polimorfismo.
- Inventario con peso y capacidad.
- Equipar/desequipar, usar objetos, atacar NPCs, recorrido.
- Interfaz de comandos y comandos compuestos/repetidos.
- Cargador por defecto y cargador por ficheros.
- Modo de mapa grande (50x50) con aliados y mas enemigos.
- Bucle principal con captura de excepciones.
- Interfaz grafica completa en una unica ventana para los tres modos.
- Mapa grafico con celdas, jugador, objetivo, objetos, aliados y enemigos.
- Botones contextuales para coger, usar, tirar, equipar, desequipar y atacar.
- Editor grafico de escenarios completos con persistencia JSON.

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

La GUI permite seleccionar los modos predeterminado, grande y desde ficheros. La
version de consola sigue disponible y utiliza el mismo motor de partida.

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

Genera en Docker la documentacion HTML de toda la API publica:

```bash
docker compose run --rm javadoc
```

Abre despues `target/reports/apidocs/index.html`. La misma documentacion se
genera localmente con `mvn javadoc:javadoc` y tambien durante `mvn verify`.

## Ejecutar localmente

Con JDK 17+ y Maven 3.9+:

```bash
mvn clean package
java -jar target/the-legend-of-tecla.jar --rapido
```

Al iniciar el juego, el modo se elige con:

- `1`: mapa por defecto, sin aliados.
- `2`: mapa grande 50x50, con mas enemigos y aliados.
- `3`: carga desde ficheros.

## Formato basico de ficheros (modo ficheros)

### mapa.txt

```text
6x6
0,0
5,5
```

### objetos.txt

```text
# nombre;tipo;fila;columna
botiquin_A;botiquin;1;1
rifle_A;arma;2;3
```

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

Nota: al cargar un mapa de ficheros mayor de `20x20`, el juego genera aliados automaticamente para ayudar al jugador.

Tambien se incluye un escenario completo en
[data/escenario_json/escenario.json](data/escenario_json/escenario.json), compatible
con el juego y con el editor grafico.
