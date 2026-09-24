package com.company.inventory.dao;

import com.company.inventory.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ICategoryDaoTest {

    @Autowired
    ICategoryDao categoryDao;

    @Test
    void testFindById(){
        Optional<Category> category = categoryDao.findById(1L);
        assertTrue(category.isPresent(), "Category should be present");
        assertEquals("Electronics", category.get().getName(), "Category name should match");
    }

    @Test
    void testFindAll(){
        List<Category> category = (List<Category>) categoryDao.findAll();
        assertFalse(category.isEmpty(), "Category list should not be empty");
        assertEquals(3, category.size(), "There should be 3 categories");
    }

    @Test
    void testFindByIdThrowsException(){
        Optional<Category> category = categoryDao.findById(999L);
        assertThrows(NoSuchElementException.class, category::orElseThrow);
        assertFalse(category.isPresent());
    }

    @Test
    void testSaveCategory() {
        Category newCategory = new Category();
        newCategory.setName("Books");
        newCategory.setDescription("All kinds of books");

        Category savedCategory = categoryDao.save(newCategory);
        assertNotNull(savedCategory.getId(), "Saved category should have an ID");
        assertEquals("Books", savedCategory.getName(), "Saved category name should match");
    }

    @Test
    void testUpdateCategory() {
        Optional<Category> category = categoryDao.findById(1L);
        assertTrue(category.isPresent(), "Category should be present for update");

        Category existingCategory = category.get();
        existingCategory.setName("Updated Electronics");

        Category updatedCategory = categoryDao.save(existingCategory);
        assertEquals("Updated Electronics", updatedCategory.getName(), "Updated category name should match");
    }

    @Test
    void testDeleteCategory() {
        Optional<Category> category = categoryDao.findById(1L);
        assertTrue(category.isPresent(), "Category should be present for deletion");

        categoryDao.delete(category.get());
        Optional<Category> deletedCategory = categoryDao.findById(1L);
        assertFalse(deletedCategory.isPresent(), "Deleted category should not be found");
    }

    @Test
    void testDeleteById() {
        Long idToDelete = 2L;
        Optional<Category> category = categoryDao.findById(idToDelete);
        assertTrue(category.isPresent(), "Category should be present for deletion");

        categoryDao.deleteById(idToDelete);
        Optional<Category> deletedCategory = categoryDao.findById(idToDelete);
        assertFalse(deletedCategory.isPresent(), "Deleted category should not be found");
    }

}