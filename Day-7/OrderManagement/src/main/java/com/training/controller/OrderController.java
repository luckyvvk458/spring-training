package com.training.controller;

import com.training.model.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/order")
public class OrderController {
    @GetMapping
    @ResponseBody
    public String getAllOrders() {
        return "Fetching all orders";
    }

    @GetMapping("/{id}")
    @ResponseBody
    public String getOrderById(@PathVariable int id) {
        return "Fetching order by id" + id;
    }

    @PostMapping("/save")
    @ResponseBody
    public String saveOrder(@ModelAttribute Order order) {
        return "Id: " + order.getId() + " customer name from model attribute: " + order.getCustomerName() +
                " customer city from model attribute: " + order.getCustomerCity();
    }

    @PostMapping("/saveOrderReqBody")
    @ResponseBody
    public String saveOrderUsingRequestBody(@RequestBody Order order) {
        return "Id: " + order.getId() + " customer name from reqBody: "
                + order.getCustomerName() + " customer city from reqBody: " + order.getCustomerCity();
    }

    @PutMapping("/{id}")
    @ResponseBody
    public String updateOrderById(@PathVariable int id) {
        return "updating order for id: " + id;
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public String deleteOrder(@PathVariable int id) {
        return "Deleting user with id = " + id;
    }

}
