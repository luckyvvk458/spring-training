package com.training.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/products")
public class ProductController {
    @ResponseBody
    @GetMapping
    public String getAllProducts(){
        return "Fetching all products";
    }
}
