package com.company.inventory.util;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilUnitTest {

    @Test
    void e01_recuperarTextoComprimido() {
        byte[] original = "Texto de prueba".getBytes(StandardCharsets.UTF_8);

        byte[] recovered = Util.decompressZLib(Util.compressZLib(original));

        assertArrayEquals(original, recovered);
    }

    @Test
    void e02_comprimirEntradaVacia() {
        byte[] compressed = Util.compressZLib(new byte[0]);

        assertNotNull(compressed);
        assertArrayEquals(new byte[0], Util.decompressZLib(compressed));
    }

    @Test
    void e03_recuperarDatosBinarios() {
        byte[] original = {0, 1, -1, 0, 127, -128, 42};

        byte[] recovered = Util.decompressZLib(Util.compressZLib(original));

        assertArrayEquals(original, recovered);
    }

    @Test
    void e04_procesarMasDeUnaVueltaDelBuffer() {
        byte[] original = new byte[4096];
        new Random(17).nextBytes(original);

        byte[] compressed = Util.compressZLib(original);
        byte[] recovered = Util.decompressZLib(compressed);

        assertTrue(compressed.length > 1024);
        assertArrayEquals(original, recovered);
    }

    @Test
    void e05_conservarAcentosYEne() {
        byte[] original = "Piña, café y azúcar".getBytes(StandardCharsets.UTF_8);

        byte[] recovered = Util.decompressZLib(Util.compressZLib(original));

        assertArrayEquals(original, recovered);
    }

    @Test
    void e06_noModificarLaEntradaOriginal() {
        byte[] original = {10, 0, 20, -5, 30};
        byte[] copy = original.clone();

        Util.compressZLib(original);

        assertArrayEquals(copy, original);
    }
}
