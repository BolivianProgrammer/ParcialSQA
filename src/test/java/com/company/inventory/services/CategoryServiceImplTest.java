package com.company.inventory.services;

import com.company.inventory.dao.ICategoryDao;
import com.company.inventory.model.Category;
import com.company.inventory.respnose.CategoryResponseRest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceImplTest {

    @InjectMocks
    CategoryServiceImpl service;

    @Mock
    ICategoryDao categoryDao;

    List<Category> list = new ArrayList<Category>();

    @BeforeEach
    public void init(){
        MockitoAnnotations.openMocks(this);
        this.chargeList();
    }

    /**
     * Test de la busqueda de categorias
     * Se espera que retorne una lista con 2 categorias
     * y el estado de la respuesta HTTP sea OK
     */
    @Test
    void testSearchSucess() {

        //Given
        when(categoryDao.findAll()).thenReturn(list);

        //When
        ResponseEntity<CategoryResponseRest> response = service.search();

        //Then
        assertEquals(2, response.getBody().getCategoryResponse().getCategory().size());
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respueesta HTTP debe ser OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Abarrotes", response.getBody().getCategoryResponse().getCategory().get(0).getName(), "El nombre de la primera categoria debe ser Abarrotes");
        assertEquals("Lacteos", response.getBody().getCategoryResponse().getCategory().get(1).getName());
        assertEquals("00", response.getBody().getMetadata().get(0).get("code"));

        //Optional
        verify(categoryDao, times(1)).findAll();

    }

    /**
     * Test de la busqueda de categorias
     * Se espera que retorne un error al consultar
     * y el estado de la respuesta HTTP sea INTERNAL_SERVER_ERROR
     */
    @Test
    void testSearchException() {

        //Given
        when(categoryDao.findAll()).thenThrow(new RuntimeException("Error al consultar"));

        //When
        ResponseEntity<CategoryResponseRest> response = service.search();

        //Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"), "El tipo de respuesta debe ser Respuesta nok");
        assertEquals("Error al consultar", response.getBody().getMetadata().get(0).get("date"));

        //Optional
        verify(categoryDao, times(1)).findAll();
    }

    /**
     * Test para probar guardar una categoria
     */
    @Test
    void testSaveCategorySuccess() {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintas tipos de bebidas");

        when(categoryDao.save(ArgumentMatchers.any())).thenReturn(category);

        // When
        ResponseEntity<CategoryResponseRest> response = service.save(category);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode(), "El estado de la respuesta HTTP debe ser OK");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Bebidas", response.getBody().getCategoryResponse().getCategory().get(0).getName(), "El nombre de la categoria guardada debe ser Bebidas");
        assertEquals(3L, response.getBody().getCategoryResponse().getCategory().get(0).getId());
        assertEquals("Distintas tipos de bebidas", response.getBody().getCategoryResponse().getCategory().get(0).getDescription());
        assertEquals("00", response.getBody().getMetadata().get(0).get("code"));

        verify(categoryDao, times(1)).save(category);
    }

    /**
     * Test para probar retorno nullo al guardar una categoria
     */
    @Test
    void testSaveCategoryDaoReturnsNull() {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintas tipos de bebidas");

        when(categoryDao.save(ArgumentMatchers.any())).thenReturn(null);

        // When
        ResponseEntity<CategoryResponseRest> response = service.save(category);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode(), "El estado de la respuesta HTTP debe ser BAD_REQUEST");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"), "El tipo de respuesta debe ser Respuesta nok");
        assertEquals("Categoria no guardada", response.getBody().getMetadata().get(0).get("date"));

        verify(categoryDao, times(1)).save(ArgumentMatchers.any());
    }

    /**
     * Test para probar una excepción al guardar una categoria
     */
    @Test
    void testSaveCategoryException() {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintas tipos de bebidas");

        when(categoryDao.save(ArgumentMatchers.any())).thenThrow(new RuntimeException("Error al guardar"));

        // When
        ResponseEntity<CategoryResponseRest> response = service.save(category);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), "El estado de la respuesta HTTP debe ser INTERNAL_SERVER_ERROR");
        assertNotNull(response.getBody(), "El cuerpo de la respuesta no debe ser nulo");
        assertEquals("Respuesta nok", response.getBody().getMetadata().get(0).get("type"), "El tipo de respuesta debe ser Respuesta nok");
        assertEquals("Error al grabar categoria", response.getBody().getMetadata().get(0).get("date"));

        verify(categoryDao, times(1)).save(ArgumentMatchers.any());
    }


    /**
     * Método que agrega datos a la lista de categorias
     */
    public void chargeList() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Abarrotes");
        category.setDescription("Distintos tipos de abarrotes");
        list.add(category);

        category = new Category();
        category.setId(2L);
        category.setName("Lacteos");
        category.setDescription("Distintos tipos de lacteos");
        list.add(category);
    }
}