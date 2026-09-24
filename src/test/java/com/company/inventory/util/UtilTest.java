package com.company.inventory.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilTest {


    @Test
    void testCompressZLib() {
        String input = "Hello, this is a test string to be compressed using ZLib compression.";
        byte[] compressed = Util.compressZLib(input.getBytes());
        assertNotNull(compressed);
        assertTrue(compressed.length < input.length());
    }

    @Test
    void testCompressZLibEmptyData() {
        //Given
        byte [] data = new byte[0];

        //When
        byte[] compressed = Util.compressZLib(data);

        //Then
        assertNotNull(compressed);
        assertTrue(compressed.length > 0);
    }

    @Test
    void testDecompressZLib() {
        String input = "Hello, this is a test string to be compressed using ZLib compression.";
        byte[] compressed = Util.compressZLib(input.getBytes());
        byte[] decompressed = Util.decompressZLib(compressed);
        assertNotNull(decompressed);
        assertEquals(input, new String(decompressed));
    }

}