# Probabilidades empíricas de una partida autónoma

Estos porcentajes proceden de **6400 partidas completas ejecutadas por `MotorPartida`**, no del antiguo modelo agregado de cohortes. Cada una de las 32 configuraciones usa 200 semillas independientes.

## Cómo leerlos

- `Misión`: el jugador sobrevive y satisface la condición de evacuación elegida.
- `Bando humano`: también incluye una evacuación aliada posterior a la muerte del jugador.
- `IC 95 %`: intervalo Wilson; expresa incertidumbre muestral, no diferencias entre estrategias humanas.
- Se cambia una sola variable respecto a la referencia: Normal, mapa 25x25, 10 aliados de nivel 10, jugador nivel 10 y evacuación solo del jugador.

La política automática usa formación defensiva, consume suministros por debajo de los umbrales de seguridad, ataca amenazas visibles y avanza por la ruta más corta. Por tanto son probabilidades reales **para esa política**, no una garantía para cualquier jugador.

## Resultados

| Variable | Valor | Entidades A/E | Misión (IC 95 %) | Bando humano (IC 95 %) | Turnos medios | Censuradas |
|---|---:|---:|---:|---:|---:|---:|
|dificultad|MUY_FACIL|10.0/5.0|87.0% (81.6–91.0)|88.0% (82.8–91.8)|145.7|0|
|dificultad|FACIL|10.0/8.0|56.0% (49.1–62.7)|56.5% (49.6–63.2)|266.1|0|
|dificultad|NORMAL|10.0/10.0|25.0% (19.5–31.4)|27.5% (21.8–34.1)|373.0|0|
|dificultad|DIFICIL|10.0/11.0|21.0% (15.9–27.2)|23.0% (17.7–29.3)|358.8|0|
|dificultad|MUY_DIFICIL|10.0/11.0|15.5% (11.1–21.2)|17.5% (12.9–23.4)|416.4|0|
|dificultad|PESADILLA|10.0/11.0|10.5% (7.0–15.5)|13.0% (9.0–18.4)|485.0|0|
|dificultad|DEMENTE|10.0/11.0|14.0% (9.9–19.5)|14.5% (10.3–20.0)|525.4|0|
|poblacion|0|0.0/1.0|90.5% (85.6–93.8)|90.5% (85.6–93.8)|57.1|0|
|poblacion|1|1.0/2.0|35.0% (28.7–41.8)|79.0% (72.8–84.1)|109.6|0|
|poblacion|5|5.0/5.0|45.5% (38.7–52.4)|47.0% (40.2–53.9)|376.6|0|
|poblacion|10|10.0/10.0|34.0% (27.8–40.8)|37.5% (31.1–44.4)|339.7|0|
|poblacion|25|25.0/25.0|13.0% (9.0–18.4)|14.0% (9.9–19.5)|333.7|0|
|poblacion|50|50.0/50.0|3.5% (1.7–7.0)|3.5% (1.7–7.0)|276.1|0|
|poblacion|100|100.0/100.0|1.0% (0.3–3.6)|1.0% (0.3–3.6)|185.9|0|
|poblacion|250|250.0/250.0|0.0% (0.0–1.9)|0.0% (0.0–1.9)|217.0|0|
|mapa|10x10|10.0/10.0|34.0% (27.8–40.8)|37.5% (31.1–44.4)|62.5|0|
|mapa|15x25|10.0/10.0|28.5% (22.7–35.1)|30.0% (24.1–36.7)|207.1|0|
|mapa|30x30|10.0/10.0|32.5% (26.4–39.3)|34.0% (27.8–40.8)|565.9|0|
|mapa|50x50|10.0/11.0|24.5% (19.1–30.9)|25.0% (19.5–31.4)|1937.3|0|
|nivel_aliados|1|10.0/10.0|21.0% (15.9–27.2)|22.5% (17.3–28.8)|314.2|0|
|nivel_aliados|5|10.0/10.0|21.5% (16.4–27.7)|22.0% (16.8–28.2)|356.6|0|
|nivel_aliados|10|10.0/10.0|29.0% (23.2–35.6)|29.5% (23.6–36.2)|360.8|0|
|nivel_aliados|25|10.0/10.0|42.0% (35.4–48.9)|45.0% (38.3–51.9)|367.2|0|
|nivel_aliados|50|10.0/10.0|41.0% (34.4–47.9)|45.5% (38.7–52.4)|394.5|0|
|nivel_aliados|100|10.0/10.0|45.0% (38.3–51.9)|47.5% (40.7–54.4)|384.8|0|
|nivel_jugador|1|10.0/10.0|22.5% (17.3–28.8)|25.5% (20.0–32.0)|357.6|0|
|nivel_jugador|10|10.0/10.0|30.0% (24.1–36.7)|34.5% (28.3–41.3)|364.5|0|
|nivel_jugador|25|10.0/10.0|42.0% (35.4–48.9)|44.0% (37.3–50.9)|273.1|0|
|nivel_jugador|50|10.0/10.0|57.0% (50.1–63.7)|60.5% (53.6–67.0)|213.7|0|
|nivel_jugador|100|10.0/10.0|70.0% (63.3–75.9)|71.0% (64.4–76.8)|200.3|0|
|condicion|SOLO_JUGADOR|10.0/10.0|25.5% (20.0–32.0)|28.0% (22.2–34.6)|358.7|0|
|condicion|JUGADOR_Y_ALIADOS|10.0/10.0|2.5% (1.1–5.7)|2.5% (1.1–5.7)|101.7|0|

## Hallazgos principales

- La misión cae de 87.0 % en Muy fácil a 25.0 % en Normal y 10.5 % en Pesadilla. Las pequeñas inversiones entre categorías adyacentes deben leerse con sus intervalos, que se solapan.
- Con esta estrategia, aumentar la población no compensa el fuego concentrado: 90.5 % sin aliados, 1.0 % con 100 y 0.0 % con 250. Esto no compara composiciones o formaciones alternativas.
- Subir aliados de nivel 1 a 100 eleva la misión de 21.0 % a 45.0 %; subir al jugador de nivel 1 a 100 la eleva de 22.5 % a 70.0 %.
- Exigir a todo el escuadrón reduce el éxito de 25.5 % a 2.5 % en la referencia.
- Partidas censuradas: 0. Todas las semillas tienen un desenlace observado.

Son análisis de sensibilidad de una variable cada vez; no deben multiplicarse para predecir combinaciones no ejecutadas.

## Reproducción y trazabilidad

Semilla raíz: `2026081401`. `samples.csv.gz` contiene una fila por partida; `summary.csv` y `statistics.json` contienen los agregados. La primera ejecución de cada configuración se conserva en `trace-*.log.gz`, incluyendo comandos y acciones emitidas por jugador, aliados y enemigos. `manifest.sha256` permite comprobar que los artefactos no han cambiado.

Ejemplo de regeneración:

```powershell
mvn -q -DskipTests package
java -cp target/classes com.legendoftecla.tools.GeneradorProbabilidadesReales --runs=200 --seed=2026081401 --threads=6 --output=docs/runs/monte-carlo-real-20260814
```
