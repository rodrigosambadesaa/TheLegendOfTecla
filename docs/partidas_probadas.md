# Pruebas reproducibles de consola y GUI

Última regeneración: 8 de agosto de 2026.

Las evidencias de esta carpeta proceden de la versión actual del juego. Se regeneran con:

```powershell
powershell -ExecutionPolicy Bypass -File .\tools\regenerar-pruebas-docs.ps1
```

El script ejecuta la suite completa antes de crear los registros. Si una prueba o una partida falla, termina con error y no presenta el proceso como válido.

## Resultado verificado

- Suite Maven completa: sin fallos, con Checkstyle, SpotBugs, JaCoCo y Javadoc.
- Comandos compuestos: movimiento repetido, ataque repetido con y sin nombre, sustitución de equipo y árboles de comandos anidados.
- Consola y GUI: ambas entradas llaman al mismo `MotorPartida`; los escenarios controlados verifican los mismos efectos sobre el modelo.
- Ataque múltiple: nombrar un enemigo identifica la celda, pero el golpe afecta a todos los enemigos vivos que contiene.
- Objetos: inspección previa, coger, usar, equipar, desequipar y tirar sobre la celda actual.
- Aliados: inspeccionan antes de descubrir objetos; comparan y sustituyen armas, armaduras y binoculares; todo descarte queda en su celda.
- Formaciones: se comprueban órdenes defensiva/ofensiva, acompañamiento, explorador sano dentro
  del radio del grupo, consumo táctico del binocular y reacción de enemigos con visión directa.
- Binoculares: cada unidad solo puede activarse una vez y desaparece de la mochila o del equipo al usarla.
- Carga durante la partida: `cargar <directorio>` sustituye el escenario activo tanto desde consola como desde el campo de comandos de la GUI.
- Descanso: recupera salud y energía sin movimiento y permite que los enemigos se acerquen en su turno.
- Victoria con aliados: se prueban `solo_jugador` y `jugador_y_aliados`, incluido cualquier orden de llegada.

## Evidencias

- [Suite completa](runs/suite_completa.log)
- [Comandos compuestos en consola y GUI](runs/comandos_compuestos_consola_gui.log)
- [Partida de consola sin aliados](runs/partida_normal.log)
- [Partida de consola con aliados](runs/partida_con_aliados.log)
- [Matriz de dificultades y modos](runs/dificultad_matrix/)

La matriz contiene 42 arranques reales: tres modos (`default`, `grande`, `ficheros`), siete dificultades y dos perfiles de dimensiones (10x10 y 22x24). Todos llegan al bucle principal y terminan limpiamente con `salir`.

Las pruebas GUI se ejecutan de forma determinista y sin pantalla mediante Swing en el EDT. Además generan capturas de humo en `target/gui-smoke/`; `target` no se versiona porque es un resultado reproducible.
