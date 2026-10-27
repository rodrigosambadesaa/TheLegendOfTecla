package com.legendoftecla.persistence;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Limita memoria y tamano de archivos JSON de progreso y estadisticas.
 * Los savegames y los replays mantienen sus propias politicas de tamano.
 */
final class ArchivoJsonAcotado {
    static final int MAX_BYTES = 1024 * 1024;

    private ArchivoJsonAcotado() { }

    static String leer(Path archivo) throws IOException {
        Objects.requireNonNull(archivo, "Archivo");
        try (InputStream entrada = Files.newInputStream(archivo)) {
            byte[] datos = entrada.readNBytes(MAX_BYTES + 1);
            if (datos.length > MAX_BYTES) {
                throw new IOException("El archivo JSON supera el limite de 1 MiB.");
            }
            // El decodificador estricto no sustituye bytes UTF-8 malformados.
            return StandardCharsets.UTF_8.newDecoder()
                    .decode(ByteBuffer.wrap(datos)).toString();
        }
    }

    static void escribir(Path archivo, String json) throws IOException {
        Objects.requireNonNull(json, "JSON");
        if (json.getBytes(StandardCharsets.UTF_8).length > MAX_BYTES) {
            throw new IOException("El archivo JSON supera el limite de 1 MiB.");
        }
        EscrituraAtomica.escribir(archivo, json);
    }
}
