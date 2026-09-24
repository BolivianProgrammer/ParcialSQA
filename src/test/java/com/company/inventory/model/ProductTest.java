package com.company.inventory.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductTest {

    @Test
    void testProduct(){
        Category category = new Category(1L, "Abarrotes", "Categoria de abarrotes");
        byte[] image = new byte[]{1, 2, 3, 4, 5};

        Product product = new Product();
        product.setId(10L);
        product.setName("Arroz");
        product.setPrice(100);
        product.setAccount(50);
        product.setCategory(category);
        product.setPicture(image);

        assertEquals(10L, product.getId());
        assertEquals("Arroz", product.getName());
        assertEquals(100, product.getPrice());
        assertEquals(50, product.getAccount());
        assertEquals(category, product.getCategory());
        assertArrayEquals(image, product.getPicture(), "La imagen del producto debe ser igual a la imagen proporcionada");

    }

}