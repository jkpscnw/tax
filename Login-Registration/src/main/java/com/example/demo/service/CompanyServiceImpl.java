package com.example.demo.service;

import com.example.demo.model.Company;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyServiceImpl implements CompanyService {

    @Autowired
    private EntityManager entityManager;

    @Override
    public List<Company> getAllCompany() {
        String jpql = "SELECT c FROM Company c";
        Query query = entityManager.createQuery(jpql, Company.class);
        return query.getResultList();
    }
}
