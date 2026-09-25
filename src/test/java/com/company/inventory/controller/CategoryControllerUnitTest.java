package com.company.inventory.controller;

import com.company.inventory.model.Category;
import com.company.inventory.respnose.CategoryResponseRest;
import com.company.inventory.services.ICategoryService;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerUnitTest {

    @Mock
    private ICategoryService service;

    @InjectMocks
    private CategoryRestController controller;

    @Test
    @DisplayName("T02 - devuelve la lista vacia recibida del servicio")
    void searchCategoriesReturnsEmptyList() {
        ResponseEntity<CategoryResponseRest> expected = response(HttpStatus.OK, List.of());
        when(service.search()).thenReturn(expected);

        ResponseEntity<CategoryResponseRest> actual = controller.searchCategories();

        assertSame(expected, actual);
        assertEquals(HttpStatus.OK, actual.getStatusCode());
        assertTrue(actual.getBody().getCategoryResponse().getCategory().isEmpty());
        verify(service).search();
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("T05 - conserva el 404 al consultar una categoria inexistente")
    void searchCategoriesByIdReturnsNotFound() {
        ResponseEntity<CategoryResponseRest> expected = response(HttpStatus.NOT_FOUND, null);
        when(service.searchById(91L)).thenReturn(expected);

        ResponseEntity<CategoryResponseRest> actual = controller.searchCategoriesById(91L);

        assertErrorResponse(expected, actual, HttpStatus.NOT_FOUND);
        verify(service).searchById(91L);
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("T07 - conserva el rechazo 400 al guardar una categoria")
    void saveReturnsBadRequest() {
        Category input = new Category(null, "Bebidas", "Bebidas frias");
        ResponseEntity<CategoryResponseRest> expected = response(HttpStatus.BAD_REQUEST, null);
        when(service.save(input)).thenReturn(expected);

        ResponseEntity<CategoryResponseRest> actual = controller.save(input);

        assertErrorResponse(expected, actual, HttpStatus.BAD_REQUEST);
        verify(service).save(same(input));
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("T09 - conserva el 404 al actualizar una categoria inexistente")
    void updateReturnsNotFound() {
        Category input = new Category(null, "Nombre nuevo", "Descripcion nueva");
        ResponseEntity<CategoryResponseRest> expected = response(HttpStatus.NOT_FOUND, null);
        when(service.update(input, 91L)).thenReturn(expected);

        ResponseEntity<CategoryResponseRest> actual = controller.update(input, 91L);

        assertErrorResponse(expected, actual, HttpStatus.NOT_FOUND);
        verify(service).update(same(input), eq(91L));
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("T09 - conserva el rechazo 400 al actualizar una categoria")
    void updateReturnsBadRequest() {
        Category input = new Category(null, "Nombre nuevo", "Descripcion nueva");
        ResponseEntity<CategoryResponseRest> expected = response(HttpStatus.BAD_REQUEST, null);
        when(service.update(input, 42L)).thenReturn(expected);

        ResponseEntity<CategoryResponseRest> actual = controller.update(input, 42L);

        assertErrorResponse(expected, actual, HttpStatus.BAD_REQUEST);
        verify(service).update(same(input), eq(42L));
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("T12 - entrega un XLSX de categorias con encabezados de descarga")
    void exportToExcelReturnsWorkbookAndDownloadHeaders() throws Exception {
        List<Category> categories = List.of(
                new Category(42L, "Bebidas", "Bebidas frias"),
                new Category(77L, "Lacteos", "Productos refrigerados"));
        when(service.search()).thenReturn(response(HttpStatus.OK, categories));
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        controller.exportToExcel(servletResponse);

        assertEquals("application/octet-stream", servletResponse.getContentType());
        assertEquals("attachment; filename=result_category.xlsx",
                servletResponse.getHeader("Content-Disposition"));
        byte[] content = servletResponse.getContentAsByteArray();
        assertTrue(content.length > 0);
        // Abrir el libro demuestra que no es solo un arreglo de bytes cualquiera.
        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(content))) {
            assertNotNull(workbook.getSheet("Resultado"));
            assertEquals(3, workbook.getSheet("Resultado").getPhysicalNumberOfRows());
            assertEquals("Bebidas", workbook.getSheet("Resultado").getRow(1).getCell(1).getStringCellValue());
            assertEquals("Lacteos", workbook.getSheet("Resultado").getRow(2).getCell(1).getStringCellValue());
        }
        verify(service).search();
        verifyNoMoreInteractions(service);
    }

    @Test
    @DisplayName("T13 - exporta una lista vacia como un libro con encabezados")
    void exportToExcelReturnsHeaderOnlyWorkbookForEmptyList() throws Exception {
        when(service.search()).thenReturn(response(HttpStatus.OK, List.of()));
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        controller.exportToExcel(servletResponse);

        assertEquals("application/octet-stream", servletResponse.getContentType());
        assertEquals("attachment; filename=result_category.xlsx",
                servletResponse.getHeader("Content-Disposition"));
        try (XSSFWorkbook workbook = new XSSFWorkbook(
                new ByteArrayInputStream(servletResponse.getContentAsByteArray()))) {
            assertNotNull(workbook.getSheet("Resultado"));
            assertEquals(1, workbook.getSheet("Resultado").getPhysicalNumberOfRows());
            assertEquals("ID", workbook.getSheet("Resultado").getRow(0).getCell(0).getStringCellValue());
        }
        verify(service).search();
        verifyNoMoreInteractions(service);
    }

    private ResponseEntity<CategoryResponseRest> response(HttpStatus status, List<Category> categories) {
        CategoryResponseRest body = new CategoryResponseRest();
        body.getCategoryResponse().setCategory(categories);
        body.setMetadata(status.is2xxSuccessful() ? "Respuesta ok" : "Respuesta nok",
                status.is2xxSuccessful() ? "00" : "-1", "Respuesta preparada para la prueba");
        return new ResponseEntity<>(body, status);
    }

    private void assertErrorResponse(ResponseEntity<CategoryResponseRest> expected,
                                     ResponseEntity<CategoryResponseRest> actual,
                                     HttpStatus status) {
        assertSame(expected, actual);
        assertEquals(status, actual.getStatusCode());
        assertEquals("-1", actual.getBody().getMetadata().get(0).get("code"));
        assertEquals("Respuesta preparada para la prueba", actual.getBody().getMetadata().get(0).get("date"));
        assertNull(actual.getBody().getCategoryResponse().getCategory());
    }
}
