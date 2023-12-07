package com.example.demo.service;

import com.example.demo.model.Product;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    Product getProductById(Long id);

    void updateProduct(Long id, Product updateProduct);

    Product addProduct(Product product);

}
