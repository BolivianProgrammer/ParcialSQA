package com.company.inventory.services;

import com.company.inventory.dao.ICategoryDao;
import com.company.inventory.model.Category;
import com.company.inventory.respnose.CategoryResponseRest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceUnitTest {

    @Mock
    private ICategoryDao categoryDao;

    @InjectMocks
    private CategoryServiceImpl service;

    private Category category(long id, String name, String description) {
        return new Category(id, name, description);
    }

    private void assertMetadata(ResponseEntity<CategoryResponseRest> result, HttpStatus status, String message) {
        assertEquals(status, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(message, result.getBody().getMetadata().get(0).get("date"));
    }

    @Test
    void j02_emptyListReturnsOkAndStaysEmpty() {
        when(categoryDao.findAll()).thenReturn(List.of());

        var result = service.search();

        assertMetadata(result, HttpStatus.OK, "Respuesta exitosa");
        assertTrue(result.getBody().getCategoryResponse().getCategory().isEmpty());
        verify(categoryDao).findAll();
    }

    @Test
    void j04_existingIdReturnsTheCategory() {
        Category found = category(7L, "Bebidas", "Frías");
        when(categoryDao.findById(7L)).thenReturn(Optional.of(found));

        var result = service.searchById(7L);

        assertMetadata(result, HttpStatus.OK, "Categoria encontrada");
        assertEquals(List.of(found), result.getBody().getCategoryResponse().getCategory());
        verify(categoryDao).findById(7L);
    }

    @Test
    void j05_unknownIdReturnsNotFound() {
        when(categoryDao.findById(8L)).thenReturn(Optional.empty());

        var result = service.searchById(8L);

        assertMetadata(result, HttpStatus.NOT_FOUND, "Categoria no encontrada");
        verify(categoryDao).findById(8L);
    }

    @Test
    void j06_lookupFailureReturnsServerError() {
        when(categoryDao.findById(8L)).thenThrow(new RuntimeException("falló la consulta"));

        var result = service.searchById(8L);

        assertMetadata(result, HttpStatus.INTERNAL_SERVER_ERROR, "Error al consultar por id");
        verify(categoryDao).findById(8L);
    }

    @Test
    void j10_updateKeepsIdAndSavesNewFields() {
        Category stored = category(4L, "Anterior", "Descripción anterior");
        Category changes = category(99L, "Nueva", "Descripción nueva");
        when(categoryDao.findById(4L)).thenReturn(Optional.of(stored));
        when(categoryDao.save(any(Category.class))).thenAnswer(call -> call.getArgument(0));

        var result = service.update(changes, 4L);

        assertMetadata(result, HttpStatus.OK, "Categoria actualizada");
        ArgumentCaptor<Category> saved = ArgumentCaptor.forClass(Category.class);
        verify(categoryDao).save(saved.capture());
        assertSame(stored, saved.getValue());
        assertEquals(4L, saved.getValue().getId());
        assertEquals("Nueva", saved.getValue().getName());
        assertEquals("Descripción nueva", saved.getValue().getDescription());
        assertEquals(List.of(stored), result.getBody().getCategoryResponse().getCategory());
        verify(categoryDao).findById(4L);
    }

    @Test
    void j11_updateUnknownIdDoesNotSave() {
        when(categoryDao.findById(4L)).thenReturn(Optional.empty());

        var result = service.update(category(4L, "Nueva", "Descripción"), 4L);

        assertMetadata(result, HttpStatus.NOT_FOUND, "Categoria no encontrada");
        verify(categoryDao).findById(4L);
        verify(categoryDao, never()).save(any(Category.class));
    }

    @Test
    void j12_updateWithoutSavedResultReturnsBadRequest() {
        Category stored = category(4L, "Anterior", "Descripción anterior");
        when(categoryDao.findById(4L)).thenReturn(Optional.of(stored));
        when(categoryDao.save(any(Category.class))).thenReturn(null);

        var result = service.update(category(4L, "Nueva", "Descripción nueva"), 4L);

        assertMetadata(result, HttpStatus.BAD_REQUEST, "Categoria no actualizada");
        verify(categoryDao).save(stored);
    }

    @Test
    void j13_updateLookupFailureDoesNotSave() {
        when(categoryDao.findById(4L)).thenThrow(new RuntimeException("falló la consulta"));

        var result = service.update(category(4L, "Nueva", "Descripción"), 4L);

        assertMetadata(result, HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar categoria");
        verify(categoryDao, never()).save(any(Category.class));
    }

    @Test
    void j14_updateSaveFailureReturnsServerError() {
        Category stored = category(4L, "Anterior", "Descripción anterior");
        when(categoryDao.findById(4L)).thenReturn(Optional.of(stored));
        when(categoryDao.save(any(Category.class))).thenThrow(new RuntimeException("falló el guardado"));

        var result = service.update(category(4L, "Nueva", "Descripción nueva"), 4L);

        assertMetadata(result, HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar categoria");
        verify(categoryDao).save(stored);
    }

    @Test
    void j15_deleteCallsDaoOnceWithTheId() {
        var result = service.deleteById(4L);

        assertMetadata(result, HttpStatus.OK, "Registro eliminado");
        verify(categoryDao, times(1)).deleteById(4L);
    }

    @Test
    void j16_deleteFailureReturnsServerError() {
        doThrow(new RuntimeException("falló el borrado")).when(categoryDao).deleteById(4L);

        var result = service.deleteById(4L);

        assertMetadata(result, HttpStatus.INTERNAL_SERVER_ERROR, "Error al eliminar");
        verify(categoryDao).deleteById(4L);
    }
}
