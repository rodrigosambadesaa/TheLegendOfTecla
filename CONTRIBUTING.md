# Contribuir

## Preparación

- JDK 17 o posterior y Maven 3.9 o posterior.
- Rama corta derivada de `main`.
- No mezclar refactorizaciones ajenas con una corrección funcional.

## Puerta de entrega

Antes de enviar cambios ejecuta:

```bash
mvn clean verify
docker compose build juego gui javadoc-web
docker compose run --rm juego --help
```

La entrega debe mantener el umbral JaCoCo, las reglas de arquitectura, Checkstyle,
SpotBugs, Javadoc y todas las pruebas. Las reglas nuevas requieren tests unitarios;
los cambios de persistencia necesitan round-trip y compatibilidad hacia atrás.

## Diseño

- Consola y Swing sólo coordinan casos de uso; las reglas viven fuera de la GUI.
- El dominio y el motor no dependen de Swing.
- `BusEventos` se inyecta a través de `Juego`; nunca es un singleton global.
- RNG y semillas se pasan explícitamente cuando afectan al resultado.
- Ningún archivo Java puede superar 1.700 líneas.

## Commits

Usa commits cohesivos y mensajes imperativos, por ejemplo
`feat: añadir cobertura destructible` o `test: cubrir migración de partidas`.
