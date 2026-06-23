package com.training.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
public class OrderController {

    @GetMapping
    @ResponseBody
    public String getAllOrders() {
        return "Fetching all orders ";
    }

    @GetMapping("/getOrder/{id}")
    @ResponseBody
    public String getOrderById(@PathVariable int id) {
        System.out.println("In get Order By id method ");
        return "fetching order id " + id;
    }

    @GetMapping("/getOrder")
    @ResponseBody
    public String getOrderByIdUsingRequestParam(@RequestParam int id) {
        System.out.println("In get Order By id method ");
        return "fetching order id from request param " + id;
    }

    @GetMapping("/form")
    public String showForm() {
        return "form";
    }

    @PostMapping("/save")
    @ResponseBody
    public String addStudent(@RequestParam String name,
                             @RequestParam String email,
                             @RequestParam String city) {

        return "Order Details: Name " + name + " Email: " + email + " City:" + city;
    }

    @PutMapping
    @ResponseBody
    public String updateOrderById(@RequestParam int id) {
        return "updating order for id: " + id;
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteOrderById(@PathVariable int id){
        return "Deleting order for id: "+id;
    }
}
