# Política de seguridad

## Versiones mantenidas

La rama `main` es la única versión mantenida. Cada cambio se valida con Java 17,
análisis estático, CodeQL, OWASP Dependency Check y un SBOM CycloneDX.

## Comunicar una vulnerabilidad

No publiques inicialmente detalles explotables en una incidencia pública. Usa la
opción **Security → Report a vulnerability** del repositorio para enviar una
descripción, pasos de reproducción, impacto y, si existe, una corrección propuesta.

Se acusará recibo en un máximo de 72 horas. La evaluación inicial tendrá un plazo
objetivo de siete días y la corrección se coordinará antes de divulgar el problema.
No se solicitan pruebas que destruyan datos, afecten a terceros o degraden servicios.

## Cadena de suministro

Los artefactos de CI incluyen `target/bom.json` en formato CycloneDX. Dependabot
vigila Maven y GitHub Actions; el job de OWASP bloquea vulnerabilidades con CVSS
igual o superior a 7,0.
