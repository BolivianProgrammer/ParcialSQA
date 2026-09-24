package com.company.inventory.model;


import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryTest {

    @Test
    void testCategory(){
       Category category = new Category(4L, "Electronicas", "Categoria de electronicos");

       assertThat(category.getId()).isEqualTo(4L);
       assertThat(category.getName()).isEqualTo("Electronicas");
       assertThat(category.getDescription()).isEqualTo("Categoria de electronicos");
   }

}