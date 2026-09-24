package com.company.inventory.respnose;

import java.util.List;

import com.company.inventory.model.Product;

import lombok.Data;

@Data
public class ProductResponse {
	
	List<Product> products;

}
