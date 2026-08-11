# Cumplimiento de las especificaciones originales

Este documento separa los requisitos evaluables de los PDF originales de las ampliaciones posteriores. La implementación actual cubre las entregas P1, P2 y P3 y los tres apartados opcionales de P4.

## Matriz de evaluación

| Entrega | Ítems | Cumplimiento y evidencia principal |
|---|---:|---|
| P1 | 1–6 | Modelo con más de seis clases, encapsulación, validación interna, constructores, paquetes y sobrecarga. `ValidacionInternaTest` comprueba getters, setters y límites por reflexión. |
| P1 | 7–9 | Mapa ASCII, estado continuo y nombre configurable en consola. Cubierto por integración y partidas reproducibles. |
| P1 | 10–12 | Movimiento cardinal con obstáculos, `mirar` y `recorrido`. Cubierto por pruebas de comandos y motor. |
| P2 | 14–17 | Coste por peso, armas de una/dos manos, armaduras y final por vida cero. Cubierto por las suites de modelo y combate. |
| P2 | 18–22 | Inventario detallado; coger/tirar; equipar/desequipar; usar; ayuda completa. Se prueba también que tirar deposita el mismo objeto en la celda actual. |
| P2 | 23–24 | `mirar <objeto>` y alcance compacto como `mirar 3e [enemigo]`, respetando la visión y los obstáculos. |
| P2 | 25–28 | Enemigos con estado e inventario, ataque, cadáveres con botín e IA por turnos. La ampliación acordada hace que un ataque afecte a todos los enemigos de la celda objetivo. |
| P2 | 29 | Carga de los tres TXT históricos y de `escenario.json`; selección al arrancar y comando dinámico `cargar <directorio>`. |
| P3 | 30–32 | Marine, francotirador y zapador; cargadores intercambiables; captura de excepciones en el bucle principal. |
| P3 | 33, 38, 40 | Interfaz `Comando`, comandos simples, `ComandoCompuesto` anidable y `ComandoRepetido`. Probados los ejemplos `atacar 4e 2`, `atacar 4e alien_azul 2` y sustitución `equipar nuevo antiguo`. |
| P3 | 34–37 | Agregado `Juego`, jerarquías completas de personajes y objetos, polimorfismo por clase y excepciones propias. El marine puede llevar dos armas de dos manos con coste de movimiento 1,5 veces mayor. |
| P3 | 39, 41 | Interfaces `Consola` y `CargadorJuego`, con implementaciones de texto, gráfica, por defecto y por ficheros. No se duplican reglas entre interfaces. |
| P3 | 42–46 | Uso justificado de `abstract`/`final`, métodos abstractos y heredados, `instanceof` en recogida y constantes centralizadas. |
| P4 opcional | 49 | Javadoc completo generado en `mvn verify`, con portal técnico versionado. |
| P4 opcional | 50 | Juego completo en una sola GUI, mapa gráfico y equivalencia con consola mediante el mismo motor. |
| P4 opcional | 51 | Editor gráfico completo, persistencia JSON y carga directa de sus escenarios. |

## Ampliaciones no exigidas por los PDF

- Siete dificultades, mapas rectangulares y 50 variantes grandes reproducibles.
- Aliados opcionales con IA, estado persistente y dos condiciones de victoria seleccionables.
- Exploración independiente por aliado: los objetos no existen para su decisión hasta inspeccionar la celda.
- Equipamiento autónomo de armas, armaduras y binoculares, con comparación y sustitución; los descartes quedan físicamente en la celda.
- Binoculares consumibles de un solo uso para jugador y aliados, conforme a la aclaración del profesor.
- Acción de descanso con recuperación y riesgo táctico.
- Ayuda aliada con búsqueda de suministros antes de acudir al jugador.
- Formaciones defensiva y ofensiva: acompañamiento permanente, exploración limitada por el aliado
  en mejor estado cuando escasean suministros y respuesta táctica de los enemigos que las detectan.
- Editor JSON, Docker, GUI accesible por navegador con noVNC, CI, cobertura, análisis estático y pruebas GUI headless.
- Ampliación ambiental voluntaria: iluminación y oscuridad, incendios propagables, agua,
  nuevos objetos, registro completo de combate y audio CC0 posicional en el motor común.

## Criterio de máxima calidad

La afirmación de cumplimiento no depende de una partida manual: `mvn clean verify` compila, ejecuta pruebas unitarias e integrales, valida estilo, analiza defectos, exige cobertura y genera Javadoc. `tools/regenerar-pruebas-docs.ps1` añade partidas reales y la matriz de 42 configuraciones. Las extensiones se documentan aparte para no presentarlas como si fueran exigencias originales.
