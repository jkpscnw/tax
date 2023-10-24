package com.example.demo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;
    private String name;
    private double price;

    // 사용자 지정 생성자
    public Company(Long id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // getters and setters 생략
}
