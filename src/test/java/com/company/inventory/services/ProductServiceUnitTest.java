package com.company.inventory.services;

import com.company.inventory.dao.ICategoryDao;
import com.company.inventory.dao.IProductDao;
import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import com.company.inventory.respnose.ProductResponseRest;
import com.company.inventory.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceUnitTest {

    private static final Long CATEGORY_ID = 3L;
    private static final Long PRODUCT_ID = 7L;

    @Mock
    private ICategoryDao categoryDao;

    @Mock
    private IProductDao productDao;

    private ProductServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ProductServiceImpl(categoryDao, productDao);
    }

    @Test
    void v01_guardarProductoConCategoriaValida() {
        Category category = category();
        Product incoming = product("Arroz", null, image("nueva"));
        Product saved = product("Arroz", PRODUCT_ID, image("nueva"));
        saved.setCategory(category);
        when(categoryDao.findById(CATEGORY_ID)).thenReturn(Optional.of(category));
        when(productDao.save(any(Product.class))).thenReturn(saved);

        ResponseEntity<ProductResponseRest> result = service.save(incoming, CATEGORY_ID);

        ArgumentCaptor<Product> sent = ArgumentCaptor.forClass(Product.class);
        verify(categoryDao).findById(CATEGORY_ID);
        verify(productDao).save(sent.capture());
        assertSame(category, sent.getValue().getCategory());
        assertEquals("Arroz", sent.getValue().getName());
        assertEquals(15, sent.getValue().getPrice());
        assertEquals(8, sent.getValue().getAccount());
        assertArrayEquals(image("nueva"), Util.decompressZLib(sent.getValue().getPicture()));
        assertSuccess(result);
        assertSame(saved, products(result).get(0));
    }

    @Test
    void v02_guardarConCategoriaInexistente() {
        when(categoryDao.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        ResponseEntity<ProductResponseRest> result = service.save(newProduct(), CATEGORY_ID);

        assertError(result, HttpStatus.NOT_FOUND);
        verifyNoInteractions(productDao);
    }

    @Test
    void v03_falloAlBuscarCategoriaParaGuardar() {
        when(categoryDao.findById(CATEGORY_ID)).thenThrow(new RuntimeException("falló la búsqueda"));

        ResponseEntity<ProductResponseRest> result = service.save(newProduct(), CATEGORY_ID);

        assertError(result, HttpStatus.INTERNAL_SERVER_ERROR);
        verifyNoInteractions(productDao);
    }

    @Test
    void v04_guardadoSinResultado() {
        when(categoryDao.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
        when(productDao.save(any(Product.class))).thenReturn(null);

        ResponseEntity<ProductResponseRest> result = service.save(newProduct(), CATEGORY_ID);

        assertError(result, HttpStatus.BAD_REQUEST);
        verify(productDao).save(any(Product.class));
    }

    @Test
    void v05_falloAlGuardarProducto() {
        when(categoryDao.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
        when(productDao.save(any(Product.class))).thenThrow(new RuntimeException("falló el guardado"));

        ResponseEntity<ProductResponseRest> result = service.save(newProduct(), CATEGORY_ID);

        assertError(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void v06_consultarProductoExistenteRecuperaSuImagen() {
        byte[] original = image("consulta");
        Product found = product("Arroz", PRODUCT_ID, original);
        when(productDao.findById(PRODUCT_ID)).thenReturn(Optional.of(found));

        ResponseEntity<ProductResponseRest> result = service.searchById(PRODUCT_ID);

        verify(productDao).findById(PRODUCT_ID);
        assertSuccess(result);
        assertSame(found, products(result).get(0));
        assertEquals("Arroz", products(result).get(0).getName());
        assertArrayEquals(original, products(result).get(0).getPicture());
    }

    @Test
    void v07_consultarProductoInexistente() {
        when(productDao.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        ResponseEntity<ProductResponseRest> result = service.searchById(PRODUCT_ID);

        assertError(result, HttpStatus.NOT_FOUND);
    }

    @Test
    void v08_falloAlConsultarPorId() {
        when(productDao.findById(PRODUCT_ID)).thenThrow(new RuntimeException("falló la consulta"));

        ResponseEntity<ProductResponseRest> result = service.searchById(PRODUCT_ID);

        assertError(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void v09_listarDosProductosRecuperaSusImagenes() {
        byte[] firstImage = image("arroz");
        byte[] secondImage = image("leche");
        Product first = product("Arroz", PRODUCT_ID, firstImage);
        Product second = product("Leche", 8L, secondImage);
        when(productDao.findAll()).thenReturn(List.of(first, second));

        ResponseEntity<ProductResponseRest> result = service.search();

        verify(productDao).findAll();
        assertSuccess(result);
        assertEquals(2, products(result).size());
        assertEquals("Arroz", products(result).get(0).getName());
        assertEquals("Leche", products(result).get(1).getName());
        assertArrayEquals(firstImage, products(result).get(0).getPicture());
        assertArrayEquals(secondImage, products(result).get(1).getPicture());
    }

    @Test
    void v10_listaDeProductosVaciaDevuelve404() {
        when(productDao.findAll()).thenReturn(List.of());

        ResponseEntity<ProductResponseRest> result = service.search();

        assertError(result, HttpStatus.NOT_FOUND);
    }

    @Test
    void v11_falloAlListarProductos() {
        when(productDao.findAll()).thenThrow(new RuntimeException("falló el listado"));

        ResponseEntity<ProductResponseRest> result = service.search();

        assertError(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void v12_buscarPorNombreDevuelveCoincidenciasEImagenes() {
        byte[] firstImage = image("blanco");
        byte[] secondImage = image("integral");
        Product first = product("Arroz blanco", PRODUCT_ID, firstImage);
        Product second = product("Arroz integral", 8L, secondImage);
        when(productDao.findByNameContainingIgnoreCase("arroz"))
                .thenReturn(List.of(first, second));

        ResponseEntity<ProductResponseRest> result = service.searchByName("arroz");

        verify(productDao).findByNameContainingIgnoreCase("arroz");
        assertSuccess(result);
        assertEquals(2, products(result).size());
        assertEquals("Arroz blanco", products(result).get(0).getName());
        assertEquals("Arroz integral", products(result).get(1).getName());
        assertArrayEquals(firstImage, products(result).get(0).getPicture());
        assertArrayEquals(secondImage, products(result).get(1).getPicture());
    }

    @Test
    void v13_buscarSinCoincidenciasDevuelve404() {
        when(productDao.findByNameContainingIgnoreCase("arroz")).thenReturn(List.of());

        ResponseEntity<ProductResponseRest> result = service.searchByName("arroz");

        assertError(result, HttpStatus.NOT_FOUND);
    }

    @Test
    void v14_falloAlBuscarPorNombre() {
        when(productDao.findByNameContainingIgnoreCase("arroz"))
                .thenThrow(new RuntimeException("falló la búsqueda"));

        ResponseEntity<ProductResponseRest> result = service.searchByName("arroz");

        assertError(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void v15_eliminarProducto() {
        ResponseEntity<ProductResponseRest> result = service.deleteById(PRODUCT_ID);

        verify(productDao).deleteById(PRODUCT_ID);
        assertSuccess(result);
    }

    @Test
    void v16_falloAlEliminarProducto() {
        doThrow(new RuntimeException("falló el borrado"))
                .when(productDao).deleteById(PRODUCT_ID);

        ResponseEntity<ProductResponseRest> result = service.deleteById(PRODUCT_ID);

        verify(productDao).deleteById(PRODUCT_ID);
        assertError(result, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void v17_actualizarProductoYCategoriaExistentes() {
        Category newCategory = category();
        Product existing = product("Arroz", PRODUCT_ID, image("vieja"));
        Product newData = product("Leche", null, image("nueva"));
        newData.setPrice(20);
        newData.setAccount(4);
        when(categoryDao.findById(CATEGORY_ID)).thenReturn(Optional.of(newCategory));
        when(productDao.findById(PRODUCT_ID)).thenReturn(Optional.of(existing));
        when(productDao.save(any(Product.class))).thenAnswer(call -> call.getArgument(0));

        ResponseEntity<ProductResponseRest> result = service.update(newData, CATEGORY_ID, PRODUCT_ID);

        ArgumentCaptor<Product> sent = ArgumentCaptor.forClass(Product.class);
        verify(categoryDao).findById(CATEGORY_ID);
        verify(productDao).findById(PRODUCT_ID);
        verify(productDao).save(sent.capture());
        assertSame(existing, sent.getValue());
        assertEquals(PRODUCT_ID, sent.getValue().getId());
        assertEquals("Leche", sent.getValue().getName());
        assertEquals(20, sent.getValue().getPrice());
        assertEquals(4, sent.getValue().getAccount());
        assertSame(newCategory, sent.getValue().getCategory());
        assertArrayEquals(image("nueva"), Util.decompressZLib(sent.getValue().getPicture()));
        assertSuccess(result);
        assertSame(existing, products(result).get(0));
    }

    @Test
    void v18_actualizarConCategoriaInexistente() {
        when(categoryDao.findById(CATEGORY_ID)).thenReturn(Optional.empty());

        ResponseEntity<ProductResponseRest> result = service.update(newProduct(), CATEGORY_ID, PRODUCT_ID);

        assertError(result, HttpStatus.NOT_FOUND);
        verifyNoInteractions(productDao);
    }

    @Test
    void v19_actualizarProductoInexistente() {
        when(categoryDao.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
        when(productDao.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        ResponseEntity<ProductResponseRest> result = service.update(newProduct(), CATEGORY_ID, PRODUCT_ID);

        assertError(result, HttpStatus.NOT_FOUND);
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    void v20_actualizacionSinResultado() {
        when(categoryDao.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
        when(productDao.findById(PRODUCT_ID))
                .thenReturn(Optional.of(product("Arroz", PRODUCT_ID, image("vieja"))));
        when(productDao.save(any(Product.class))).thenReturn(null);

        ResponseEntity<ProductResponseRest> result = service.update(newProduct(), CATEGORY_ID, PRODUCT_ID);

        assertError(result, HttpStatus.BAD_REQUEST);
    }

    @Test
    void v21_falloAlBuscarCategoriaParaActualizar() {
        when(categoryDao.findById(CATEGORY_ID)).thenThrow(new RuntimeException("falló la categoría"));

        ResponseEntity<ProductResponseRest> result = service.update(newProduct(), CATEGORY_ID, PRODUCT_ID);

        assertError(result, HttpStatus.INTERNAL_SERVER_ERROR);
        verifyNoInteractions(productDao);
    }

    @Test
    void v22_falloAlBuscarProductoParaActualizar() {
        when(categoryDao.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
        when(productDao.findById(PRODUCT_ID)).thenThrow(new RuntimeException("falló el producto"));

        ResponseEntity<ProductResponseRest> result = service.update(newProduct(), CATEGORY_ID, PRODUCT_ID);

        assertError(result, HttpStatus.INTERNAL_SERVER_ERROR);
        verify(productDao, never()).save(any(Product.class));
    }

    @Test
    void v23_falloAlGuardarLaActualizacion() {
        when(categoryDao.findById(CATEGORY_ID)).thenReturn(Optional.of(category()));
        when(productDao.findById(PRODUCT_ID))
                .thenReturn(Optional.of(product("Arroz", PRODUCT_ID, image("vieja"))));
        when(productDao.save(any(Product.class))).thenThrow(new RuntimeException("falló el guardado"));

        ResponseEntity<ProductResponseRest> result = service.update(newProduct(), CATEGORY_ID, PRODUCT_ID);

        assertError(result, HttpStatus.INTERNAL_SERVER_ERROR);
        verify(productDao).save(any(Product.class));
    }

    private void assertSuccess(ResponseEntity<ProductResponseRest> result) {
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertFalse(result.getBody().getMetadata().isEmpty());
        assertEquals("00", result.getBody().getMetadata().get(0).get("code"));
    }

    private void assertError(ResponseEntity<ProductResponseRest> result, HttpStatus status) {
        assertEquals(status, result.getStatusCode());
        assertFalse(result.getBody().getMetadata().isEmpty());
        assertEquals("respuesta nok", result.getBody().getMetadata().get(0).get("type"));
        assertEquals("-1", result.getBody().getMetadata().get(0).get("code"));
    }

    private List<Product> products(ResponseEntity<ProductResponseRest> result) {
        return result.getBody().getProduct().getProducts();
    }

    private Category category() {
        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("Abarrotes");
        return category;
    }

    private Product newProduct() {
        return product("Arroz", null, image("nueva"));
    }

    private Product product(String name, Long id, byte[] originalImage) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(15);
        product.setAccount(8);
        product.setPicture(Util.compressZLib(originalImage));
        return product;
    }

    private byte[] image(String name) {
        return ("imagen " + name).getBytes(StandardCharsets.UTF_8);
    }
}
