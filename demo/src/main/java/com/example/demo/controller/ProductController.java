package com.example.demo.controller;

import com.example.demo.beans.Product;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
public class ProductController {

    private final MongoTemplate mongoTemplate;

    public ProductController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping
    public List<Product> getProducts() {
        return mongoTemplate.findAll(Product.class);
    }

    @PostMapping
    public void saveProduct(@RequestBody Product product) {
        mongoTemplate.save(product);
    }

}