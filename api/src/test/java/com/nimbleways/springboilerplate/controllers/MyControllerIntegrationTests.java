package com.nimbleways.springboilerplate.controllers;

import com.nimbleways.springboilerplate.entities.Order;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.OrderRepository;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.services.implementations.NotificationService;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class MyControllerIntegrationTests {
    private static final LocalDate TODAY = LocalDate.of(2024, 6, 15);
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private Clock clock;

    @MockBean
    private NotificationService notificationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        when(clock.instant()).thenReturn(TODAY.atStartOfDay(ZONE_ID).toInstant());
        when(clock.getZone()).thenReturn(ZONE_ID);
    }

    @Test
    public void processOrderAppliesBusinessRulesToEveryProduct() throws Exception {
        List<Product> allProducts = createProducts();
        Set<Product> orderItems = new HashSet<>(allProducts);
        Order order = createOrder(orderItems);
        productRepository.saveAll(allProducts);
        order = orderRepository.save(order);

        mockMvc.perform(post("/orders/{orderId}/processOrder", order.getId())
                .contentType("application/json"))
                .andExpect(status().isOk());

        Order resultOrder = orderRepository.findById(order.getId()).get();
        assertEquals(order.getId(), resultOrder.getId());
        assertEquals(29, availableQuantity("USB Cable"));
        assertEquals(0, availableQuantity("USB Dongle"));
        assertEquals(29, availableQuantity("Butter"));
        assertEquals(0, availableQuantity("Milk"));
        assertEquals(29, availableQuantity("Watermelon"));
        assertEquals(30, availableQuantity("Grapes"));
        verify(notificationService).sendDelayNotification(10, "USB Dongle");
        verify(notificationService).sendExpirationNotification("Milk", TODAY.minusDays(2));
        verify(notificationService).sendOutOfStockNotification("Grapes");
    }

    private Integer availableQuantity(String productName) {
        return productRepository.findFirstByName(productName).get().getAvailable();
    }

    private static Order createOrder(Set<Product> products) {
        Order order = new Order();
        order.setItems(products);
        return order;
    }

    private static List<Product> createProducts() {
        List<Product> products = new ArrayList<>();
        products.add(new Product(null, 15, 30, ProductType.NORMAL, "USB Cable", null, null, null));
        products.add(new Product(null, 10, 0, ProductType.NORMAL, "USB Dongle", null, null, null));
        products.add(new Product(null, 15, 30, ProductType.EXPIRABLE, "Butter", TODAY.plusDays(26), null, null));
        products.add(new Product(null, 90, 6, ProductType.EXPIRABLE, "Milk", TODAY.minusDays(2), null, null));
        products.add(new Product(null, 15, 30, ProductType.SEASONAL, "Watermelon", null, TODAY.minusDays(2),
                TODAY.plusDays(58)));
        products.add(new Product(null, 15, 30, ProductType.SEASONAL, "Grapes", null, TODAY.plusDays(180),
                TODAY.plusDays(240)));
        return products;
    }
}
