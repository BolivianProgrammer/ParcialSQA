package com.company.inventory.util;

import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.CellType;
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

class ProductExcelExporterUnitTest {

    @Test
    void e11_encabezadosDeProductos() throws IOException {
        try (XSSFWorkbook workbook = export(products())) {
            var sheet = workbook.getSheet("Resultado");
            assertNotNull(sheet);
            var header = sheet.getRow(0);
            assertEquals("ID", header.getCell(0).getStringCellValue());
            assertEquals("Nombre", header.getCell(1).getStringCellValue());
            assertEquals("Precio", header.getCell(2).getStringCellValue());
            assertEquals("Cantidad", header.getCell(3).getStringCellValue());
            assertEquals("Categoría", header.getCell(4).getStringCellValue());
            assertEquals(5, header.getPhysicalNumberOfCells());
        }
    }

    @Test
    void e12_contenidoDeProductos() throws IOException {
        try (XSSFWorkbook workbook = export(products())) {
            var sheet = workbook.getSheet("Resultado");
            assertEquals(3, sheet.getPhysicalNumberOfRows());

            var first = sheet.getRow(1);
            assertEquals("7", first.getCell(0).getStringCellValue());
            assertEquals("Arroz", first.getCell(1).getStringCellValue());
            assertEquals(CellType.NUMERIC, first.getCell(2).getCellType());
            assertEquals(15, first.getCell(2).getNumericCellValue());
            assertEquals(CellType.NUMERIC, first.getCell(3).getCellType());
            assertEquals(8, first.getCell(3).getNumericCellValue());
            assertEquals("Abarrotes", first.getCell(4).getStringCellValue());

            var second = sheet.getRow(2);
            assertEquals("8", second.getCell(0).getStringCellValue());
            assertEquals("Leche", second.getCell(1).getStringCellValue());
            assertEquals(CellType.NUMERIC, second.getCell(2).getCellType());
            assertEquals(20, second.getCell(2).getNumericCellValue());
            assertEquals(CellType.NUMERIC, second.getCell(3).getCellType());
            assertEquals(4, second.getCell(3).getNumericCellValue());
            assertEquals("Bebidas", second.getCell(4).getStringCellValue());
            assertNull(sheet.getRow(3));
        }
    }

    @Test
    void e13_exportarProductosVacios() throws IOException {
        try (XSSFWorkbook workbook = export(List.of())) {
            var sheet = workbook.getSheet("Resultado");
            assertNotNull(sheet);
            assertEquals("ID", sheet.getRow(0).getCell(0).getStringCellValue());
            assertEquals("Nombre", sheet.getRow(0).getCell(1).getStringCellValue());
            assertEquals("Precio", sheet.getRow(0).getCell(2).getStringCellValue());
            assertEquals("Cantidad", sheet.getRow(0).getCell(3).getStringCellValue());
            assertEquals("Categoría", sheet.getRow(0).getCell(4).getStringCellValue());
            assertEquals(1, sheet.getPhysicalNumberOfRows());
            assertNull(sheet.getRow(1));
        }
    }

    @Test
    void e14_propagarErrorDeSalida() throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getOutputStream()).thenThrow(new IOException("sin salida"));

        IOException error = assertThrows(IOException.class,
                () -> new ProductExcelExporter(products()).export(response));

        assertEquals("sin salida", error.getMessage());
    }

    private XSSFWorkbook export(List<Product> products) throws IOException {
        MockHttpServletResponse response = new MockHttpServletResponse();
        new ProductExcelExporter(products).export(response);
        return new XSSFWorkbook(new ByteArrayInputStream(response.getContentAsByteArray()));
    }

    private List<Product> products() {
        Category groceries = new Category(1L, "Abarrotes", "Alimentos");
        Category drinks = new Category(2L, "Bebidas", "Frías");
        return List.of(
                product(7L, "Arroz", 15, 8, groceries),
                product(8L, "Leche", 20, 4, drinks));
    }

    private Product product(Long id, String name, int price, int account, Category category) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setAccount(account);
        product.setCategory(category);
        return product;
    }
}
