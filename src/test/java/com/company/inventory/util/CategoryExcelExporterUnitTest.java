package com.company.inventory.util;

import com.company.inventory.model.Category;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryExcelExporterUnitTest {

    @Test
    void e07_encabezadosDeCategorias() throws IOException {
        try (XSSFWorkbook workbook = export(categories())) {
            var sheet = workbook.getSheet("Resultado");
            assertNotNull(sheet);
            var header = sheet.getRow(0);
            assertEquals("ID", header.getCell(0).getStringCellValue());
            assertEquals("Nombre", header.getCell(1).getStringCellValue());
            assertEquals("Descripción", header.getCell(2).getStringCellValue());
            assertEquals(3, header.getPhysicalNumberOfCells());
        }
    }

    @Test
    void e08_contenidoDeCategorias() throws IOException {
        try (XSSFWorkbook workbook = export(categories())) {
            var sheet = workbook.getSheet("Resultado");
            assertEquals(3, sheet.getPhysicalNumberOfRows());
            assertEquals("1", sheet.getRow(1).getCell(0).getStringCellValue());
            assertEquals("Abarrotes", sheet.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Alimentos", sheet.getRow(1).getCell(2).getStringCellValue());
            assertEquals("2", sheet.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Bebidas", sheet.getRow(2).getCell(1).getStringCellValue());
            assertEquals("Frías", sheet.getRow(2).getCell(2).getStringCellValue());
            assertNull(sheet.getRow(3));
        }
    }

    @Test
    void e09_exportarCategoriasVacias() throws IOException {
        try (XSSFWorkbook workbook = export(List.of())) {
            var sheet = workbook.getSheet("Resultado");
            assertNotNull(sheet);
            assertEquals("ID", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Nombre", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("Descripción", sheet.getRow(0).getCell(2).getStringCellValue());
            assertEquals(1, sheet.getPhysicalNumberOfRows());
            assertNull(sheet.getRow(1));
        }
    }

    @Test
    void e10_propagarErrorDeSalida() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new IOException("sin salida"));

        IOException error = assertThrows(IOException.class,
                () -> new CategoryExcelExporter(categories()).export(response));

        assertEquals("sin salida", error.getMessage());
    }

    private XSSFWorkbook export(List<Category> categories) throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new CategoryExcelExporter(categories).export(response);
        return new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()));
    }

    private List<Category> categories() {
        return List.of(
                new Category(1L, "Abarrotes", "Alimentos"),
                new Category(2L, "Bebidas", "Frías"));
    }
}
