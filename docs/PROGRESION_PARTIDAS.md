# Progresion de experiencia y equilibrio inicial

- Las bajas enemigas otorgan entre 40 y 80 XP al personaje que derrota al enemigo. El jugador y los aliados cercanos obtienen una bonificacion de cooperacion (minimo 8 XP), pero las bajas repetidas no se recompensan. Completar la partida otorga 100 XP al jugador vivo y 60 a cada aliado superviviente.
- El XP necesario para el siguiente nivel es `nivel * 100`. Los niveles durante el juego se limitan a 100; a nivel maximo no se acumula XP adicional. Subir de nivel aumenta las estadisticas maximas (jugador: +3 salud y +4 energia; aliados: +8 salud y +10 energia). La salud de un caido no se restaura.
- Los detalles de nivel y XP aparecen en el panel de estado del jugador y en el resumen de aliados.
- El guardado completo con `guardar partida <archivo>` conserva niveles y XP de jugador y aliados. Se respeta el formato de savegames v1/v2, incluidos saves anteriores sin XP aliada.
- Para nuevas partidas, activar en la GUI **Conservar niveles entre partidas** o en consola pasar `--progreso-local`. El archivo de progreso se almacena en `~/.legendoftecla/progreso.json`, con escritura atomica y validacion de tamano y estructura. El modo es voluntario; no se crea archivo si no se activa. Para empezar desde cero, desactivar la opcion o borrar ese archivo.
- La progresion local solo se aplica cuando coinciden nombre y clase del jugador; los aliados se identifican por sus nombres. El progreso reaparece en partidas posteriores con el mismo personaje y aliados identificables. El inventario/mapa/estado vivo solo se recuperan al cargar un savegame completo.
- Al crear cada partida, los aliados empiezan como maximo **dos niveles** por encima o por debajo del jugador. Se ajustan tambien sus estadisticas. Los enemigos carecen de nivel numerico; su amenaza aumenta suavemente con el nivel del jugador, manteniendo los modificadores de dificultad y la configuracion clasica al nivel 1.

## Comprobaciones previstas
Ejecutar `mvn clean verify` para verificar combate letal unico, experiencia compartida, nivel maximo, recompensas, serializacion de savegames, perfil local y despliegue equilibrado.
