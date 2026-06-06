package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.repositories.OrderRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderProcessingService {
    private final OrderRepository orderRepository;
    private final ProductService productService;

    public OrderProcessingService(OrderRepository orderRepository, ProductService productService) {
        this.orderRepository = orderRepository;
        this.productService = productService;
    }

    @Transactional
    public ProcessOrderResponse processOrder(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        order.getItems().forEach(productService::processProduct);
        return new ProcessOrderResponse(order.getId());
    }
}
