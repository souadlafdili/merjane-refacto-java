package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.dto.product.ProcessOrderResponse;
import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class OrderProcessingServiceTests {
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductService productService;

    private OrderProcessingService orderProcessingService;

    @BeforeEach
    public void setUp() {
        orderProcessingService = new OrderProcessingService(orderRepository, productService);
    }

    @Test
    public void processOrderProcessesEveryProductAndReturnsOrderId() {
        Product firstProduct = new Product();
        Product secondProduct = new Product();
        Order order = new Order(42L, Set.of(firstProduct, secondProduct));
        when(orderRepository.findById(42L)).thenReturn(Optional.of(order));

        ProcessOrderResponse response = orderProcessingService.processOrder(42L);

        assertEquals(42L, response.id());
        verify(productService).processProduct(firstProduct);
        verify(productService).processProduct(secondProduct);
    }
}
