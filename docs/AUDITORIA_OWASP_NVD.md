# Auditoría OWASP / NVD (las tres versiones de Tecla)

La auditoría completa de dependencias funciona de forma independiente de la CI de
las pull requests y se ejecuta por programación semanal o `workflow_dispatch`.
**No se considera exitosa mientras Dependency-Check no termine correctamente.**
Las comprobaciones de OSV y CodeQL no sustituyen la auditoría OWASP.

## Configuración del acceso a NVD

1. Solicitar una clave personal de API en
   https://nvd.nist.gov/developers/request-an-api-key
2. En cada uno de los tres repositorios de GitHub, abrir
   **Settings → Secrets and variables → Actions → New repository secret**.
3. Crear un secreto **`NVD_API_KEY`** con el valor recibido. Nunca colocar
   la clave en el repositorio, el POM, los argumentos de Maven, los comentarios
   de una PR, un fichero versionado ni los logs.
4. Ejecutar **Actions → OWASP full dependency audit → Run workflow**, o esperar
   a la ejecución semanal; comprobar que se ha generado el informe y que la
   ejecución termina correctamente.

La acción expone el secreto solo como variable de entorno durante la auditoría.
El POM usa `nvdApiKeyEnvironmentVariable`, evitando pasar su valor a Maven
mediante `-DnvdApiKey`. Si el secreto no está configurado, se emite una
advertencia sin mostrar credenciales. La carga sin clave puede ser limitada
por NVD (HTTP 429).

La configuración aumenta el intervalo entre llamadas a NVD a 12 segundos y
el máximo de reintentos a 20, también para inicializaciones sin caché.
**Esto reduce la probabilidad de limitación; no garantiza evitarla.**
La auditoría sigue rechazando vulnerabilidades de puntuación CVSS 7 o mayor
(`failBuildOnCVSS=7.0`). Los errores de red y actualización también producen
un fallo de la acción: no se activa `failOnError=false` ni se ignoran hallazgos.

## Gestión de caché

La base de datos solo se almacena en caché después de una auditoría
satisfactoria. No es correcto conservar una base incompleta producida por una
actualización fallida. La clave de caché se rota semanalmente; en ejecuciones
posteriores se restaura el último estado validado.

Si reaparece HTTP 429, comprobar que el secreto existe en **ese repositorio**
y que la clave NVD está activada. Si se observan errores de integridad H2,
investigar la caché existente antes de reutilizarla. No se deben compartir
bases de datos parcialmente inicializadas entre variantes.

## Verificación

Los tres proyectos mantienen los controles de compilación, pruebas,
cobertura, GUI Docker, OSV y CodeQL. Para revisar el problema concreto de NVD,
la prueba pertinente es la ejecución completa **OWASP full dependency audit**:
una CI Maven/OSV verde no demuestra por sí misma que la descarga NVD haya
terminado satisfactoriamente.
