# Partidas Probadas

Este archivo resume partidas ejecutadas de forma simulada, incluyendo acciones del jugador y outcome observado para cada accion.

## Referencias de logs completos

- [docs/runs/partida_normal.log](docs/runs/partida_normal.log)
- [docs/runs/partida_con_aliados.log](docs/runs/partida_con_aliados.log)
- [docs/runs/dificultad_matrix/](docs/runs/dificultad_matrix)

## Partida 1: Modo normal (sin aliados)

Configuracion:
- Nombre: Tecla
- Clase: marine
- Modo: `1` (default sin aliados)
- Resultado final: partida finalizada por comando `salir`

Secuencia accion -> outcome:

1. `ayuda` -> `Comandos: mover <dir>, mirar [obj], coger <obj>, tirar <obj>, inventario, usar <obj>, equipar <obj>, desequipar <obj>, atacar <objetivo>, recorrido, ayuda, salir`
2. `mirar` -> `Celda 0,0` + `No hay objetos en esta celda.`
3. `mover este` -> `Te mueves a ESTE.`
4. `coger botiquin_pequeno` -> `Recoges botiquin_pequeno.`
5. `inventario` -> `Mochila: peso 1,00/40.0 kg, espacio restante 9` + `- botiquin_pequeno (Cura 20 de salud, 1.0 kg)`
6. `usar botiquin_pequeno` -> `Usas botiquin_pequeno.`
7. `mover sur` -> `Te mueves a SUR.`
8. `mover este` -> `Te mueves a ESTE.`
9. `mirar` -> `Celda 1,2` + `No hay objetos en esta celda.`
10. `salir` -> `Partida finalizada.`

## Partida 2: Modo grande con aliados

Configuracion:
- Nombre: Tecla
- Clase: marine
- Modo: `2` (mapa 50x50 con aliados)
- Estado inicial observado: `Aliados desplegados: 10`
- Resultado final: partida finalizada por comando `salir`

Secuencia accion -> outcome:

1. `ayuda` -> `Comandos: mover <dir>, mirar [obj], coger <obj>, tirar <obj>, inventario, usar <obj>, equipar <obj>, desequipar <obj>, atacar <objetivo>, recorrido, ayuda, salir`
2. `mirar` -> `Punto de despliegue` + `No hay objetos en esta celda.`
3. `mover este` -> `Te mueves a ESTE.`
4. `mover este` -> `Te mueves a ESTE.`
5. `inventario` -> `Mochila: peso 0,00/40.0 kg, espacio restante 10` + `(vacia)`
6. `mover sur` -> `Te mueves a SUR.`
7. `mirar` -> `Sector 1,2` + `No hay objetos en esta celda.`
8. `mover este` -> `Te mueves a ESTE.`
9. `mover sur` -> `Te mueves a SUR.`
10. `salir` -> `Partida finalizada.`

## Notas

- Los outcomes se tomaron de la salida real de ejecucion (logs enlazados arriba).
- En el modo con aliados se mantiene el comportamiento por turnos y render de mapa grande.

## Matriz de dificultad y tamano global de mapa

Se ejecutaron 42 partidas automatizadas (2 por combinacion):

- Modos: `1`, `2`, `3`
- Dificultades: `muy facil`, `facil`, `normal`, `dificil`, `muy dificil`, `pesadilla`, `demente`
- Perfiles de tamano de mapa:
	- `default` (ENTER)
	- `rectangular` (`22x24`)

Resultados observados:

- 42/42 partidas inician correctamente y finalizan con `salir`.
- En todas aparece el resumen de dificultad aplicada (enemigos/salud/danio) en el arranque.
- El perfil rectangular confirma que el tamano global no tiene que ser cuadrado.
