package com.example.demo.beans;

import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document
public class Product {

    @Id
    private String id;
    private String name;

    public Product(String id, String name) {
        this.id = id;
        this.name = name;
    }

}
