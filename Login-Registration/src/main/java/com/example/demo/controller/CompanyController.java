package com.example.demo.controller;

import com.example.demo.model.Company;
import com.example.demo.service.CompanyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CompanyController {

    @Autowired
    private CompanyService companyService;

    @GetMapping("/company")
    public String companyList(Model model) {
        System.out.println("company 진입");
        List<Company> company = companyService.getAllCompany();
        model.addAttribute("company", company);
        return "companyList";
    }


}
