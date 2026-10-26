package com.legendoftecla.persistence;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Escritura de archivos de usuario mediante reemplazo seguro.
 *
 * <p>El archivo existente no se trunca mientras se serializa el contenido:
 * primero se escribe y sincroniza un temporal en el mismo directorio. Si el
 * sistema de archivos no admite un movimiento atomico, se usa el reemplazo
 * convencional como mejor esfuerzo. El temporal se elimina incluso si falla
 * el reemplazo.
 */
final class EscrituraAtomica {
    private EscrituraAtomica() {
    }

    static void escribir(Path archivo, String contenido) throws IOException {
        Objects.requireNonNull(archivo, "Archivo");
        Objects.requireNonNull(contenido, "Contenido");
        Path destino = archivo.toAbsolutePath().normalize();
        Path directorio = destino.getParent();
        Files.createDirectories(directorio);
        Path temporal = Files.createTempFile(directorio, ".tecla-save-", ".tmp");
        try {
            Files.writeString(temporal, contenido, StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            try (FileChannel canal = FileChannel.open(temporal, StandardOpenOption.WRITE)) {
                canal.force(true);
            }
            try {
                Files.move(temporal, destino, StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException noSoportado) {
                Files.move(temporal, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
