package com.nimbleways.springboilerplate.services.implementations;

import com.nimbleways.springboilerplate.domain.product.ProductProcessingPolicy;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;
import com.nimbleways.springboilerplate.repositories.ProductRepository;
import com.nimbleways.springboilerplate.utils.Annotations.UnitTest;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
@UnitTest
public class MyUnitTests {
    private static final LocalDate TODAY = LocalDate.of(2024, 6, 15);
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Mock
    private NotificationService notificationService;

    @Mock
    private ProductRepository productRepository;

    private ProductService productService;

    @BeforeEach
    public void setUp() {
        Clock clock = Clock.fixed(TODAY.atStartOfDay(ZONE_ID).toInstant(), ZONE_ID);
        productService = new ProductService(productRepository, notificationService, new ProductProcessingPolicy(),
                clock);
    }

    @Test
    public void notifyDelaySavesProductAndSendsDelayNotification() {
        Product product = product(15, 0, ProductType.NORMAL, "RJ45 Cable");

        productService.notifyDelay(product.getLeadTime(), product);

        assertEquals(0, product.getAvailable());
        assertEquals(15, product.getLeadTime());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(product.getLeadTime(), product.getName());
    }

    @Test
    public void processNormalProductWithStockDecrementsAvailableQuantity() {
        Product product = product(15, 30, ProductType.NORMAL, "USB Cable");

        productService.processProduct(product);

        assertEquals(29, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    public void processNormalProductWithoutStockSendsDelayWhenLeadTimeIsPositive() {
        Product product = product(10, 0, ProductType.NORMAL, "USB Dongle");

        productService.processProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(10, "USB Dongle");
    }

    @Test
    public void processNormalProductWithoutStockDoesNothingWhenLeadTimeIsNotPositive() {
        Product product = product(0, 0, ProductType.NORMAL, "USB Dongle");

        productService.processProduct(product);

        assertEquals(0, product.getAvailable());
        verifyNoInteractions(productRepository, notificationService);
    }

    @Test
    public void processSeasonalProductWithStockDuringSeasonDecrementsAvailableQuantity() {
        Product product = seasonalProduct(15, 30, TODAY.minusDays(2), TODAY.plusDays(58));

        productService.processProduct(product);

        assertEquals(29, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    public void processSeasonalProductWithoutStockSendsDelayWhenRestockHappensDuringSeason() {
        Product product = seasonalProduct(15, 0, TODAY.minusDays(2), TODAY.plusDays(58));

        productService.processProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(15, "Watermelon");
    }

    @Test
    public void processSeasonalProductWithoutStockMarksProductUnavailableWhenRestockExceedsSeason() {
        Product product = seasonalProduct(90, 0, TODAY.minusDays(2), TODAY.plusDays(58));

        productService.processProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendOutOfStockNotification("Watermelon");
        verify(notificationService, never()).sendDelayNotification(90, "Watermelon");
    }

    @Test
    public void processSeasonalProductBeforeSeasonSendsOutOfStockNotification() {
        Product product = seasonalProduct(15, 30, TODAY.plusDays(180), TODAY.plusDays(240));

        productService.processProduct(product);

        assertEquals(30, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendOutOfStockNotification("Watermelon");
    }

    @Test
    public void processSeasonalProductOnSeasonStartUsesExistingDelayRule() {
        Product product = seasonalProduct(15, 30, TODAY, TODAY.plusDays(58));

        productService.processProduct(product);

        assertEquals(30, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendDelayNotification(15, "Watermelon");
    }

    @Test
    public void processExpirableProductWithStockBeforeExpiryDecrementsAvailableQuantity() {
        Product product = expirableProduct(15, 30, TODAY.plusDays(26));

        productService.processProduct(product);

        assertEquals(29, product.getAvailable());
        verify(productRepository).save(product);
        verifyNoInteractions(notificationService);
    }

    @Test
    public void processExpirableProductAfterExpiryMarksProductUnavailableAndSendsNotification() {
        Product product = expirableProduct(90, 6, TODAY.minusDays(2));

        productService.processProduct(product);

        assertEquals(0, product.getAvailable());
        verify(productRepository).save(product);
        verify(notificationService).sendExpirationNotification("Butter", TODAY.minusDays(2));
    }

    @Test
    public void processProductWithoutTypeDoesNothing() {
        Product product = product(15, 30, null, "Gift Card");

        productService.processProduct(product);

        assertEquals(30, product.getAvailable());
        verifyNoInteractions(productRepository, notificationService);
    }

    private static Product product(Integer leadTime, Integer available, ProductType type, String name) {
        return new Product(null, leadTime, available, type, name, null, null, null);
    }

    private static Product seasonalProduct(Integer leadTime, Integer available, LocalDate seasonStartDate,
            LocalDate seasonEndDate) {
        return new Product(null, leadTime, available, ProductType.SEASONAL, "Watermelon", null, seasonStartDate,
                seasonEndDate);
    }

    private static Product expirableProduct(Integer leadTime, Integer available, LocalDate expiryDate) {
        return new Product(null, leadTime, available, ProductType.EXPIRABLE, "Butter", expiryDate, null, null);
    }
}
