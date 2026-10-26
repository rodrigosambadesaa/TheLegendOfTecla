# Política de seguridad

## Versiones mantenidas

La rama `main` es la única versión mantenida. Cada cambio se valida con Java 17,
análisis estático, CodeQL, OSV-Scanner y un SBOM CycloneDX. OWASP Dependency
Check se ejecuta además como auditoría profunda independiente.

## Comunicar una vulnerabilidad

No publiques inicialmente detalles explotables en una incidencia pública. Usa la
opción **Security → Report a vulnerability** del repositorio para enviar una
descripción, pasos de reproducción, impacto y, si existe, una corrección propuesta.

Se acusará recibo en un máximo de 72 horas. La evaluación inicial tendrá un plazo
objetivo de siete días y la corrección se coordinará antes de divulgar el problema.
No se solicitan pruebas que destruyan datos, afecten a terceros o degraden servicios.

## Cadena de suministro

Los artefactos de CI incluyen `target/bom.json` en formato CycloneDX. Dependabot
vigila Maven y GitHub Actions. OSV-Scanner comprueba las dependencias nuevas de
cada PR y realiza un análisis completo sobre `main` y cada lunes. Sus
hallazgos se reflejan en los controles y alertas de GitHub.

OWASP Dependency Check conserva un análisis profundo independiente los martes
(o cuando se inicie manualmente); su ejecución falla ante vulnerabilidades con
CVSS igual o superior a 7,0. Requiere descargar e importar la base NVD y puede
fallar por problemas de actualización: un fallo de esa base no demuestra que
las dependencias sean seguras. Las bases H2 incompletas no se guardan en caché.
