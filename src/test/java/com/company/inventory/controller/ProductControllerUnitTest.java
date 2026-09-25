package com.company.inventory.controller;

import com.company.inventory.model.Category;
import com.company.inventory.model.Product;
import com.company.inventory.respnose.ProductResponseRest;
import com.company.inventory.services.IProductService;
import com.company.inventory.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerUnitTest {

    @Mock
    private IProductService productService;

    private ProductRestController controller;

    private static final Long CATEGORY_ID = 3L;
    private static final Long PRODUCT_ID = 7L;
    private static final byte[] IMAGE = "imagen de prueba".getBytes(StandardCharsets.UTF_8);

    @BeforeEach
    void setUp() {
        controller = new ProductRestController(productService);
    }

    @Test
    void s01_crearProductoEnviaLosDatosAlServicio() throws IOException {
        ResponseEntity<ProductResponseRest> expected = response(HttpStatus.OK, List.of(product("Arroz")));
        when(productService.save(any(Product.class), eq(CATEGORY_ID))).thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.save(picture(), "Arroz", 15, 8, CATEGORY_ID);

        ArgumentCaptor<Product> sent = ArgumentCaptor.forClass(Product.class);
        verify(productService).save(sent.capture(), eq(CATEGORY_ID));
        assertEquals("Arroz", sent.getValue().getName());
        assertEquals(15, sent.getValue().getPrice());
        assertEquals(8, sent.getValue().getAccount());
        assertResponse(expected, actual);
    }

    @Test
    void s02_crearProductoComprimeLaImagenSinCambiarSuContenido() throws IOException {
        when(productService.save(any(Product.class), eq(CATEGORY_ID)))
                .thenReturn(response(HttpStatus.OK, List.of(product("Arroz"))));

        controller.save(picture(), "Arroz", 15, 8, CATEGORY_ID);

        ArgumentCaptor<Product> sent = ArgumentCaptor.forClass(Product.class);
        verify(productService).save(sent.capture(), eq(CATEGORY_ID));
        assertArrayEquals(IMAGE, Util.decompressZLib(sent.getValue().getPicture()));
    }

    @Test
    void s03_crearProductoConError404ConservaLaRespuesta() throws IOException {
        checkSaveError(HttpStatus.NOT_FOUND);
    }

    @Test
    void s03_crearProductoConError400ConservaLaRespuesta() throws IOException {
        checkSaveError(HttpStatus.BAD_REQUEST);
    }

    @Test
    void s03_crearProductoConError500ConservaLaRespuesta() throws IOException {
        checkSaveError(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void s04_siNoSePuedeLeerLaImagenNoSeGuardaElProducto() throws IOException {
        MultipartFile brokenPicture = mock(MultipartFile.class);
        when(brokenPicture.getBytes()).thenThrow(new IOException("No se pudo leer la imagen"));

        assertThrows(IOException.class,
                () -> controller.save(brokenPicture, "Arroz", 15, 8, CATEGORY_ID));
        verifyNoInteractions(productService);
    }

    @Test
    void s05_buscarPorIdDevuelveElProductoPedido() {
        ResponseEntity<ProductResponseRest> expected = response(HttpStatus.OK, List.of(product("Arroz")));
        when(productService.searchById(PRODUCT_ID)).thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.searchById(PRODUCT_ID);

        verify(productService).searchById(PRODUCT_ID);
        assertResponse(expected, actual);
        assertEquals("Arroz", actual.getBody().getProduct().getProducts().get(0).getName());
    }

    @Test
    void s06_buscarPorIdConError404ConservaLaRespuesta() {
        checkSearchByIdError(HttpStatus.NOT_FOUND);
    }

    @Test
    void s06_buscarPorIdConError500ConservaLaRespuesta() {
        checkSearchByIdError(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void s07_listarDevuelveLosDosProductos() {
        ResponseEntity<ProductResponseRest> expected = response(HttpStatus.OK,
                List.of(product("Arroz"), product("Leche")));
        when(productService.search()).thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.search();

        verify(productService).search();
        assertResponse(expected, actual);
        assertEquals(2, actual.getBody().getProduct().getProducts().size());
        assertEquals("Arroz", actual.getBody().getProduct().getProducts().get(0).getName());
        assertEquals("Leche", actual.getBody().getProduct().getProducts().get(1).getName());
    }

    @Test
    void s08_listarSinDatosConservaEl404() {
        checkSearchError(HttpStatus.NOT_FOUND);
    }

    @Test
    void s08_listarConErrorConservaEl500() {
        checkSearchError(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void s09_buscarPorNombreEnviaElTextoYDevuelveCoincidencias() {
        ResponseEntity<ProductResponseRest> expected = response(HttpStatus.OK,
                List.of(product("Arroz blanco"), product("Arroz integral")));
        when(productService.searchByName("Arroz")).thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.searchByName("Arroz");

        verify(productService).searchByName("Arroz");
        assertResponse(expected, actual);
        assertEquals(2, actual.getBody().getProduct().getProducts().size());
    }

    @Test
    void s10_buscarPorNombreSinCoincidenciasConservaEl404() {
        checkSearchByNameError(HttpStatus.NOT_FOUND);
    }

    @Test
    void s10_buscarPorNombreConErrorConservaEl500() {
        checkSearchByNameError(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void s11_actualizarProductoEnviaTodosLosDatos() throws IOException {
        ResponseEntity<ProductResponseRest> expected = response(HttpStatus.OK, List.of(product("Leche")));
        when(productService.update(any(Product.class), eq(CATEGORY_ID), eq(PRODUCT_ID)))
                .thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.update(
                picture(), "Leche", 20, 4, CATEGORY_ID, PRODUCT_ID);

        ArgumentCaptor<Product> sent = ArgumentCaptor.forClass(Product.class);
        verify(productService).update(sent.capture(), eq(CATEGORY_ID), eq(PRODUCT_ID));
        assertEquals("Leche", sent.getValue().getName());
        assertEquals(20, sent.getValue().getPrice());
        assertEquals(4, sent.getValue().getAccount());
        assertArrayEquals(IMAGE, Util.decompressZLib(sent.getValue().getPicture()));
        assertResponse(expected, actual);
    }

    @Test
    void s12_actualizarConError404ConservaLaRespuesta() throws IOException {
        checkUpdateError(HttpStatus.NOT_FOUND);
    }

    @Test
    void s12_actualizarConError400ConservaLaRespuesta() throws IOException {
        checkUpdateError(HttpStatus.BAD_REQUEST);
    }

    @Test
    void s12_actualizarConError500ConservaLaRespuesta() throws IOException {
        checkUpdateError(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void s13_siNoSePuedeLeerLaImagenNoSeActualizaElProducto() throws IOException {
        MultipartFile brokenPicture = mock(MultipartFile.class);
        when(brokenPicture.getBytes()).thenThrow(new IOException("No se pudo leer la imagen"));

        assertThrows(IOException.class,
                () -> controller.update(brokenPicture, "Leche", 20, 4, CATEGORY_ID, PRODUCT_ID));
        verifyNoInteractions(productService);
    }

    @Test
    void s14_eliminarProductoDevuelveExito() {
        checkDelete(HttpStatus.OK);
    }

    @Test
    void s14_eliminarProductoConErrorConservaEl500() {
        checkDelete(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void s15_descargarProductosGeneraUnArchivoExcel() throws IOException {
        ResponseEntity<ProductResponseRest> products = response(HttpStatus.OK, List.of(product("Arroz")));
        when(productService.search()).thenReturn(products);
        MockHttpServletResponse download = new MockHttpServletResponse();

        controller.exportToExcel(download);

        verify(productService).search();
        assertEquals("application/octet-stream", download.getContentType());
        assertEquals("attachment; filename=result_product.xlsx",
                download.getHeader("Content-Disposition"));
        assertTrue(download.getContentAsByteArray().length > 0);
    }

    private void checkSaveError(HttpStatus status) throws IOException {
        ResponseEntity<ProductResponseRest> expected = response(status, List.of());
        when(productService.save(any(Product.class), eq(CATEGORY_ID))).thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.save(picture(), "Arroz", 15, 8, CATEGORY_ID);

        verify(productService).save(any(Product.class), eq(CATEGORY_ID));
        assertResponse(expected, actual);
    }

    private void checkSearchByIdError(HttpStatus status) {
        ResponseEntity<ProductResponseRest> expected = response(status, List.of());
        when(productService.searchById(PRODUCT_ID)).thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.searchById(PRODUCT_ID);

        verify(productService).searchById(PRODUCT_ID);
        assertResponse(expected, actual);
    }

    private void checkSearchError(HttpStatus status) {
        ResponseEntity<ProductResponseRest> expected = response(status, List.of());
        when(productService.search()).thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.search();

        verify(productService).search();
        assertResponse(expected, actual);
    }

    private void checkSearchByNameError(HttpStatus status) {
        ResponseEntity<ProductResponseRest> expected = response(status, List.of());
        when(productService.searchByName("Arroz")).thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.searchByName("Arroz");

        verify(productService).searchByName("Arroz");
        assertResponse(expected, actual);
    }

    private void checkUpdateError(HttpStatus status) throws IOException {
        ResponseEntity<ProductResponseRest> expected = response(status, List.of());
        when(productService.update(any(Product.class), eq(CATEGORY_ID), eq(PRODUCT_ID)))
                .thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.update(
                picture(), "Leche", 20, 4, CATEGORY_ID, PRODUCT_ID);

        verify(productService).update(any(Product.class), eq(CATEGORY_ID), eq(PRODUCT_ID));
        assertResponse(expected, actual);
    }

    private void checkDelete(HttpStatus status) {
        ResponseEntity<ProductResponseRest> expected = response(status, List.of());
        when(productService.deleteById(PRODUCT_ID)).thenReturn(expected);

        ResponseEntity<ProductResponseRest> actual = controller.deleteById(PRODUCT_ID);

        verify(productService).deleteById(PRODUCT_ID);
        assertResponse(expected, actual);
    }

    private void assertResponse(ResponseEntity<ProductResponseRest> expected,
                                ResponseEntity<ProductResponseRest> actual) {
        assertEquals(expected.getStatusCode(), actual.getStatusCode());
        assertSame(expected.getBody(), actual.getBody());
    }

    private ResponseEntity<ProductResponseRest> response(HttpStatus status, List<Product> products) {
        ProductResponseRest body = new ProductResponseRest();
        body.getProduct().setProducts(products);
        body.setMetadata(status.is2xxSuccessful() ? "ok" : "error",
                String.valueOf(status.value()), status.getReasonPhrase());
        return new ResponseEntity<>(body, status);
    }

    private Product product(String name) {
        Product product = new Product();
        product.setId(PRODUCT_ID);
        product.setName(name);
        product.setPrice(15);
        product.setAccount(8);
        Category category = new Category();
        category.setId(CATEGORY_ID);
        category.setName("Abarrotes");
        product.setCategory(category);
        return product;
    }

    private MockMultipartFile picture() {
        return new MockMultipartFile("picture", "foto.png", "image/png", IMAGE);
    }
}
