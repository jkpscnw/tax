package com.example.demo.service;

import java.util.Arrays;
import java.util.List;

import com.example.demo.TbConstants;
import com.example.demo.dto.UserDto;
import com.example.demo.model.Role;
import com.example.demo.model.User;
import com.example.demo.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Product;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> getAllProducts() {
        return  productRepository.findAll();
    }

    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    @Override
    public void updateProduct(Long id, Product updateProduct) {
        Product existingProduct = productRepository.findById(id).orElse(null);
        if (existingProduct != null) {
            existingProduct.setName(updateProduct.getName());
            existingProduct.setPrice(updateProduct.getPrice());
            productRepository.save(existingProduct);
        }
    }

    @Transactional
    @Override
    public Product addProduct(Product product) {

//        Product saveProduct = productRepository.save(product);
//        return saveProduct;

        try {
            System.out.println("serviceImpl-addproduct-start");
            Product saveProduct = productRepository.save(product);

            System.out.println("saveProduct = " + saveProduct);

            product.setId(product.getId());
            System.out.println("product.getTid() = " + product.getTid());
            productRepository.save(product);
            System.out.println("serviceImpl-addproduct-end");


            System.out.println("return-saveproduct");
            return saveProduct;
        } catch (RuntimeException e) {
            System.out.println("e = " + e);
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }

        System.out.println("return-product");
        return product;
    }

}
