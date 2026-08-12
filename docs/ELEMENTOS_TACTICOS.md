# Elementos tácticos, trampas y cobertura

Las celdas pueden alojar `ElementoMapa` con identidad única, resistencia y efectos
dinámicos sobre paso, visión, línea de tiro, pathfinding y render ASCII/Swing.
`Puerta`, `Terminal`, `Interruptor`, `Cofre`, `Barricada` y las trampas comparten ese
contrato sin introducir casos especiales en `MotorPartida`.

## Comandos

- `abrir puerta`, `cerrar puerta`, `usar llave` y `usar tarjeta`.
- `hackear terminal` y `activar interruptor`.
- `inspeccionar trampa`, `desactivar mina`, `detonar mina` y `disparar mina`.

Las puertas bloqueadas buscan una `Credencial` compatible en la mochila. Los
terminales e interruptores pueden referenciar por ID otra puerta o interruptor. Toda
interacción genera eventos de dominio y ruido para audio, estadísticas e IA.

Las trampas son recursos finitos. `SistemaTrampas` calcula su radio sobre jugador,
aliados y enemigos, publica daño y muerte, y aplica quemadura, veneno o aturdimiento.
El Zapador recibe una ventaja determinista de detección y desactivación.

La cobertura usa `TipoCobertura` (`NINGUNA`, `MEDIA`, `COMPLETA`) y orientación. La
resolución recibe un `RandomGenerator`, de modo que impacto y flanqueo son
reproducibles. Las barricadas destruidas dejan de proteger y de bloquear.

## JSON

Una celda mantiene los campos históricos y puede añadir opcionalmente:

```json
{
  "fila": 2,
  "columna": 4,
  "descripcion": "Acceso al bunker",
  "transitable": true,
  "elementoTipo": "puerta",
  "elementoId": "puerta-bunker",
  "elementoEstado": "BLOQUEADA",
  "referencia": "tarjeta-roja",
  "resistencia": 30,
  "dificultad": 0
}
```

Tipos admitidos: `puerta`, `terminal`, `interruptor`, `cofre`, `barricada`,
`cobertura`, `mina`, `trampa_fuego`, `trampa_veneno`, `trampa_electrica` y
`alarma`. El validador rechaza IDs duplicados, tipos desconocidos, estados de puerta
inválidos y referencias rotas entre elementos.
