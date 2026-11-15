# Contrato de paridad: The Legend of Tecla

Este proyecto se mantiene en tres repositorios independientes:

- **Original (referencia):** `rodrigosambadesaa/TheLegendOfTecla`.
- **Alonso/Sambade Reforged:** `rodrigosambadesaa/TheLegendOfTecla-Alonso-Sambade-Reforged`.
- **Mago Reforged:** `rodrigosambadesaa/TheLegendOfTecla-Mago-Reforged`.

## Regla de sincronización

1. Toda funcionalidad de producción del original debe estar presente en las dos
   reconstrucciones. Esto incluye `src/main/java` y `src/main/resources`.
2. En Alonso, `src/main` debe ser idéntico al del original.
3. En Mago solo se autorizan las clases nuevas `Mago`, `Guerrero` y
   `Alquimista` y las adaptaciones necesarias para integrarlas en
   configuración, arranque, selección de clases, cargadores y persistencia.
   La lista exacta de excepciones está en
   `tools/check_upstream_parity.py`. No autoriza eliminar otras funciones.
4. Los materiales `historico/`, la autoría y las particularidades de las
   reconstrucciones no deben reemplazarse por archivos de otro repositorio.
5. Los tests propios de cada edición deben mantenerse; los tests comunes del
   original son contratos de comportamiento que también deben superar las
   reconstrucciones.

## Comprobaciones automáticas

- El original ejecuta Maven, pruebas, cobertura, CodeQL y OSV.
- Las reconstrucciones ejecutan Maven, pruebas, comprobaciones de la GUI Docker
  y OSV. Su CI compara el árbol de producción con `main` del original y
  rechaza divergencias no autorizadas.
- La tarea de regresión ascendente de las reconstrucciones añade temporalmente
  las pruebas del original que no existen en la edición local. Se ejecutan
  durante CI, **sin sobrescribir ni eliminar** los tests locales.
- El análisis OWASP Dependency-Check completo continúa por separado: los
  problemas de acceso/limitación de la base NVD no deben silenciarse ni
  interpretarse como ausencia de vulnerabilidades.

## Procedimiento ante un cambio

1. Desarrollar la función o corrección en el original y acompañarla de pruebas.
2. Tras integrarla, trasladar los cambios comunes a ambos Reforged.
3. Revisar manualmente las diferencias permitidas de Mago, especialmente
   creación de personajes, cargadores y restauración de partidas.
4. Ejecutar `mvn clean verify`, el control automático de paridad, las pruebas
   ascendentes y el smoke test GUI antes de fusionar las PR.
5. Mantener los tres historiales separados; no hacer fuerza sobre `main`
   ni suprimir comprobaciones para conseguir una CI aparentemente verde.

**Alcance:** comparar archivos idénticos demuestra paridad de implementación,
pero las adaptaciones autorizadas requieren además pruebas funcionales propias.
