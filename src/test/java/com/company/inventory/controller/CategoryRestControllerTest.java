package com.company.inventory.controller;

import com.company.inventory.model.Category;
import com.company.inventory.respnose.CategoryResponseRest;
import com.company.inventory.services.ICategoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CategoryRestControllerTest {

    private MockMvc mockMvc;

    ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    CategoryRestController categoryRestController;

    @Mock
    private ICategoryService service;

    List<Category> list = new ArrayList<Category>();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(categoryRestController).build();
        this.chargeList();
    }

    /**
     * Test para probar controlador de obtener todas las categorias
     */
    @Test
    void testSearchCategories() throws Exception {
        // Given
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.getCategoryResponse().setCategory(list);
        categoryResponseRest.setMetadata("Respuesta ok", "00", "Respuesta exitosa");

        when(service.search()).thenReturn(new ResponseEntity<CategoryResponseRest>(categoryResponseRest, HttpStatus.OK));

        // When
        this.mockMvc.perform(get("/api/v1/categories")
                // Then
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoryResponse").exists())
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Abarrotes"))
                .andExpect(jsonPath("$.categoryResponse.category.length()").value(2))
                .andExpect(jsonPath("$.categoryResponse.category[0].id").value(1))
                .andExpect(jsonPath("$.categoryResponse.category[0].description").value("Distintos tipos de abarrotes"))
                .andExpect(jsonPath("$.categoryResponse.category[1].id").value(2))
                .andExpect(jsonPath("$.categoryResponse.category[1].name").value("Lacteos"))
                .andExpect(jsonPath("$.categoryResponse.category[1].description").value("Distintos tipos de lacteos"))
                .andExpect(status().isOk());

        verify(service).search();
        verifyNoMoreInteractions(service);
    }

    /**
     * Test para probar error en controlador de obtener todas las categorias
     */
    @Test
    void testSearchCategoriesError() throws Exception {
        // Given
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Error", "01", "Error al consultar categorias");

        when(service.search()).thenReturn(
                new ResponseEntity<CategoryResponseRest>(categoryResponseRest, HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        this.mockMvc.perform(get("/api/v1/categories")
            // Then)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.metadata[0].code").value("01"))
                .andExpect(jsonPath("$.metadata[0].type").value("Error"))
                .andExpect(status().isInternalServerError());

        verify(service).search();
        verifyNoMoreInteractions(service);
    }

    /**
     * Test para probar controlador de obtener categoria por id
     */
    @Test
    void testSearchCategoriesById() throws Exception {
        // Given
        Long id = 1L;
        Category category = list.get(0);
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.getCategoryResponse().setCategory(List.of(category));
        categoryResponseRest.setMetadata("Respuesta ok", "00", "Respuesta exitosa");

        when(service.searchById(id)).thenReturn(new ResponseEntity<CategoryResponseRest>(categoryResponseRest, HttpStatus.OK));

        // When
        this.mockMvc.perform(get("/api/v1/categories/{id}", id)
                // Then
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoryResponse").exists())
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Abarrotes"))
                .andExpect(jsonPath("$.categoryResponse.category.length()").value(1))
                .andExpect(jsonPath("$.categoryResponse.category[0].id").value(1))
                .andExpect(jsonPath("$.categoryResponse.category[0].description").value("Distintos tipos de abarrotes"))
                .andExpect(jsonPath("$.metadata[0].code").value("00"))
                .andExpect(status().isOk());

        verify(service).searchById(id);
        verifyNoMoreInteractions(service);
    }

    /**
     * Test para probar error en controlador de obtener categoria por id
     */
    @Test
    void testSearchCategoriesByIdError() throws Exception {
        // Given
        Long id = 1L;
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Error", "01", "Error al consultar categoria por id");

        when(service.searchById(id)).thenReturn(
                new ResponseEntity<CategoryResponseRest>(categoryResponseRest, HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        this.mockMvc.perform(get("/api/v1/categories/{id}", id)
            // Then
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.metadata[0].code").value("01"))
                .andExpect(jsonPath("$.metadata[0].type").value("Error"))
                .andExpect(status().isInternalServerError());

        verify(service).searchById(id);
        verifyNoMoreInteractions(service);
    }

    /**
     * Test para guardar una categoria
     */
    @Test
    void testSaveCategory() throws Exception {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintos tipos de bebidas");

        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.getCategoryResponse().setCategory(List.of(category));
        categoryResponseRest.setMetadata("Respuesta ok", "00", "Categoria guardada exitosamente");

        when(service.save(category)).thenReturn(
                new ResponseEntity<CategoryResponseRest>(categoryResponseRest, HttpStatus.OK));

        // When
        this.mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category))
                .accept(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(jsonPath("$.categoryResponse").exists())
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Bebidas"))
                .andExpect(jsonPath("$.categoryResponse.category.length()").value(1))
                .andExpect(jsonPath("$.categoryResponse.category[0].id").value(3))
                .andExpect(jsonPath("$.categoryResponse.category[0].description").value("Distintos tipos de bebidas"))
                .andExpect(jsonPath("$.metadata[0].code").value("00"))
                .andExpect(status().isOk());

        verify(service).save(category);
        verifyNoMoreInteractions(service);
    }

    /**
     * Test para probar error al guardar una categoria
     */
    @Test
    void testSaveCategoryError() throws Exception {
        // Given
        Category category = new Category();
        category.setId(3L);
        category.setName("Bebidas");
        category.setDescription("Distintos tipos de bebidas");

        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Error", "01", "Error al guardar la categoria");

        when(service.save(category)).thenReturn(
                new ResponseEntity<CategoryResponseRest>(categoryResponseRest, HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        this.mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category))
                .accept(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(jsonPath("$.metadata[0].code").value("01"))
                .andExpect(jsonPath("$.metadata[0].type").value("Error"))
                .andExpect(status().isInternalServerError());

        verify(service).save(category);
        verifyNoMoreInteractions(service);
    }

    /**
     * Test para actualizar una categoria
     */
    @Test
    void testUpdateCategory() throws Exception {
        // Given
        Long id = 1L;
        Category category = new Category();
        category.setId(id);
        category.setName("Abarrotes Actualizado");
        category.setDescription("Descripcion actualizada");

        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.getCategoryResponse().setCategory(List.of(category));
        categoryResponseRest.setMetadata("Respuesta ok", "00", "Categoria actualizada exitosamente");

        when(service.update(category, id)).thenReturn(
                new ResponseEntity<CategoryResponseRest>(categoryResponseRest, HttpStatus.OK));

        // When
        this.mockMvc.perform(put("/api/v1/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category))
                .accept(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(jsonPath("$.categoryResponse").exists())
                .andExpect(jsonPath("$.categoryResponse.category[0].name").value("Abarrotes Actualizado"))
                .andExpect(jsonPath("$.categoryResponse.category.length()").value(1))
                .andExpect(jsonPath("$.categoryResponse.category[0].id").value(1))
                .andExpect(jsonPath("$.categoryResponse.category[0].description").value("Descripcion actualizada"))
                .andExpect(jsonPath("$.metadata[0].code").value("00"))
                .andExpect(status().isOk());

        verify(service).update(category, id);
        verifyNoMoreInteractions(service);
    }

    /**
     * Test para probar error al actualizar una categoria
     */
    @Test
    void testUpdateCategoryError() throws Exception {
        // Given
        Long id = 1L;
        Category category = new Category();
        category.setId(id);
        category.setName("Abarrotes Actualizado");
        category.setDescription("Descripcion actualizada");

        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Error", "01", "Error al actualizar la categoria");

        when(service.update(category, id)).thenReturn(
                new ResponseEntity<CategoryResponseRest>(categoryResponseRest, HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        this.mockMvc.perform(put("/api/v1/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(category))
                .accept(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(jsonPath("$.metadata[0].code").value("01"))
                .andExpect(jsonPath("$.metadata[0].type").value("Error"))
                .andExpect(status().isInternalServerError());

        verify(service).update(category, id);
        verifyNoMoreInteractions(service);
    }

    /**
     * Test para eliminar una categoria
     */
    @Test
    void testDeleteCategory() throws Exception {
        // Given
        Long id = 1L;
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Respuesta ok", "00", "Categoria eliminada exitosamente");

        when(service.deleteById(id)).thenReturn(
                new ResponseEntity<CategoryResponseRest>(categoryResponseRest, HttpStatus.OK));

        // When
        this.mockMvc.perform(delete("/api/v1/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(jsonPath("$.metadata[0].code").value("00"))
                .andExpect(status().isOk());

        verify(service).deleteById(id);
        verifyNoMoreInteractions(service);
    }

    /**
     * Test para probar error al eliminar una categoria
     */
    @Test
    void testDeleteCategoryError() throws Exception {
        // Given
        Long id = 1L;
        CategoryResponseRest categoryResponseRest = new CategoryResponseRest();
        categoryResponseRest.setMetadata("Error", "01", "Error al eliminar la categoria");

        when(service.deleteById(id)).thenReturn(
                new ResponseEntity<CategoryResponseRest>(categoryResponseRest, HttpStatus.INTERNAL_SERVER_ERROR));

        // When
        this.mockMvc.perform(delete("/api/v1/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                // Then
                .andExpect(jsonPath("$.metadata[0].code").value("01"))
                .andExpect(jsonPath("$.metadata[0].type").value("Error"))
                .andExpect(status().isInternalServerError());

        verify(service).deleteById(id);
        verifyNoMoreInteractions(service);
    }


    /**
     * MÃ©todo que agrega datos a la lista de categorias
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