# 1.000 partidas autonomas de gran poblacion

Lote reproducible generado por `GeneradorRunsAutomaticos` sin intervencion humana. El modelo `headless-cohort-v1` procesa combatientes por cohortes y conserva un evento por turno para mantener el consumo de memoria acotado.

- Partidas completas: 1000
- Aliados: 100 a 4999
- Victoria solo jugador: 500
- Victoria jugador + aliados: 500
- Victorias humanas: 500
- Victorias enemigas: 500
- Acciones individuales registradas: 131562514
- Tamano comprimido de logs: 1250913 bytes
- Semilla raiz: 104372539623444

`index.csv` permite analizar el lote; `statistics.json` contiene los agregados; `manifest.sha256` permite comprobar la integridad de cada evidencia. Los logs de acciones usan rangos sin perdida: cada selector incluye una accion por cada ID y `tools/expandir-log-acciones.ps1` puede materializar todas las lineas.
