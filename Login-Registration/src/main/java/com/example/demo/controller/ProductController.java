package com.example.demo.controller;

import com.example.demo.dto.ProductDto;
import com.example.demo.dto.UserDto;
import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String productList(Model model) {
        System.out.println("products 진입");
        List<Product> products = productService.getAllProducts();
        model.addAttribute("products", products);
        return "productList";
    }

    @GetMapping("/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        if (product != null) {
            model.addAttribute("product", product);
            return "editProduct";
        } else {
            return "redirect:/products";
        }
    }

    @PostMapping("/{id}/edit")
    public String updateProduct(@PathVariable Long id, Product updateProduct) {
        productService.updateProduct(id, updateProduct);
        return "redirect:/products";
    }

    @GetMapping("/add")
    public String addProductForm() {
        return "addProduct";
    }

    @PostMapping("/add")
    public String addProduct(@ModelAttribute Product newProduct) {
        System.out.println("newProduct : "  + newProduct);
        Product product = productService.addProduct(newProduct);
        return "redirect:/products";
    }

    /*@GetMapping("/inicis-payment")
    public String inicisPayment() {
        System.out.println("inicisPayment 진입");
        return "INIstdpay_pc_req";
    }*/
}
