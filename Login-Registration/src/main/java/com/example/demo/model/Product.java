package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@Entity
@NoArgsConstructor
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String name;
    private int price;
    private String tid;

    // 사용자 지정 생성자
    public Product(Long id, String name, int price, String tid) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.tid = tid;
    }

    // getters and setters 생략
}
