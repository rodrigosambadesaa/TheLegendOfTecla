# Misiones, campaña, progresión y crafting

El modo normal conserva su condición de victoria histórica. Cuando `Juego` recibe
una `Mision`, la victoria delega exclusivamente en su objetivo principal. Los
objetivos secundarios se evalúan aparte y no bloquean la finalización.

Objetivos disponibles:

- alcanzar la salida;
- eliminar un enemigo o jefe identificado;
- rescatar o escoltar un aliado;
- recuperar un objeto;
- activar un terminal;
- sobrevivir una cantidad de turnos;
- apagar un incendio;
- no perder aliados;
- completar otro objetivo sin disparar.

`Campana` mantiene una secuencia opcional de misiones y una
`ProgresionPersonaje`. `PersistenciaCampana` guarda un documento versionado con el
índice, nivel, XP y habilidades. El equipo, inventario y aliados supervivientes se
conservan mediante el savegame completo de la campaña.

La experiencia usa un umbral incremental (`nivel * 100`). Cada clase dispone de
un árbol pequeño creado por `CatalogoHabilidades`:

- Marine: resistencia, fuego de supresión y doble arma pesada.
- Francotirador: disparo preciso, silenciador y visión avanzada.
- Zapador: desactivación avanzada, demolición y reutilización de explosivos.

Las habilidades tienen ID estable, nivel mínimo, prerrequisito y efecto. El
silenciador reduce el ruido de disparo, la supresión aplica miedo y la
desactivación avanzada incrementa las ventajas del Zapador.

## Crafting

Comandos:

```text
recetas
fabricar
fabricar botiquin
fabricar mina
fabricar antorcha
fabricar kit reparacion
```

La fabricación valida todos los ingredientes antes de modificar la mochila,
agrupa requisitos repetidos y restaura el inventario original si el resultado no
cabe. No crea recursos en caso de error.

## Destrucción

`SistemaDestruccion` daña elementos por identidad y conserva el orden del
escenario. Puertas destructibles, barricadas, coberturas y `ParedDebil` actualizan
automáticamente paso, visión y línea de tiro porque el mapa consulta su estado en
cada operación. Explosivos, munición pesada y fuego producen daño estructural; un
suelo de madera consumido por el incendio queda convertido en terreno quemado no
inflamable.
