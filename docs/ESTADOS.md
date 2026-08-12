# Estados temporales

Los efectos temporales se modelan como estrategias `EfectoEstado` administradas por
un `GestorEstados` propio de cada personaje. El gestor controla aplicación,
acumulación, renovación, hooks de comienzo y final de turno, reacción al movimiento,
caducidad y eliminación explícita. `SistemaEstados` coordina el ciclo para jugador,
aliados y enemigos sin trasladar esa lógica a `MotorPartida`.

| Estado | Duración | Acumulable | Efecto principal |
|---|---:|:---:|---|
| Quemado | 3 | Sí | Daño al comienzo de turno; el agua lo elimina |
| Envenenado | 4 | Sí | Daño al final de turno por acumulación |
| Sangrado | 5 | No | Daño al moverse; un botiquín lo detiene |
| Aturdido | 1 | No | Pierde la siguiente acción |
| Cegado | 2 | No | Reduce visión y precisión |
| Mojado | 3 | No | Evita nuevas quemaduras |
| Exhausto | 6 | No | Penaliza el coste energético; descansar lo elimina |
| Asustado | 3 | No | Reduce precisión y puede influir en la IA |
| Inspirado | 3 | No | Mejora temporalmente la precisión |

Las aplicaciones y eliminaciones publican `EstadoAplicado` y `EstadoEliminado` en el
bus de la partida. El daño periódico publica además `PersonajeDanado` y, si procede,
`PersonajeMuerto`. Todos usan el reloj inyectado del bus para que las pruebas y los
replays sean deterministas.

`EstadoActivo` es la vista inmutable y validada usada por consola, Swing y la futura
persistencia de partidas completas. `GestorEstados.restaurar` reconstruye el estado
desde esa vista sin exponer su representación interna.
