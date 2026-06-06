package com.nimbleways.springboilerplate.services.implementations;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.nimbleways.springboilerplate.domain.product.ProductNotificationType;
import com.nimbleways.springboilerplate.domain.product.ProductProcessingPolicy;
import com.nimbleways.springboilerplate.domain.product.ProductProcessingResult;
import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.repositories.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final NotificationService notificationService;
    private final ProductProcessingPolicy productProcessingPolicy;
    private final Clock clock;

    public ProductService(ProductRepository productRepository, NotificationService notificationService,
            ProductProcessingPolicy productProcessingPolicy, Clock clock) {
        this.productRepository = productRepository;
        this.notificationService = notificationService;
        this.productProcessingPolicy = productProcessingPolicy;
        this.clock = clock;
    }

    public void notifyDelay(int leadTime, Product p) {
        p.setLeadTime(leadTime);
        productRepository.save(p);
        notificationService.sendDelayNotification(leadTime, p.getName());
    }

    public void processProduct(Product product) {
        ProductProcessingResult result = productProcessingPolicy.process(product, today());
        if (result.shouldSave()) {
            productRepository.save(product);
        }
        sendNotification(result.getNotificationType(), product);
    }

    public void handleSeasonalProduct(Product p) {
        ProductProcessingResult result = productProcessingPolicy.process(p, today());
        if (result.shouldSave()) {
            productRepository.save(p);
        }
        sendNotification(result.getNotificationType(), p);
    }

    public void handleExpiredProduct(Product p) {
        ProductProcessingResult result = productProcessingPolicy.process(p, today());
        if (result.shouldSave()) {
            productRepository.save(p);
        }
        sendNotification(result.getNotificationType(), p);
    }

    private void sendNotification(ProductNotificationType notificationType, Product product) {
        switch (notificationType) {
            case DELAY:
                notificationService.sendDelayNotification(product.getLeadTime(), product.getName());
                break;
            case OUT_OF_STOCK:
                notificationService.sendOutOfStockNotification(product.getName());
                break;
            case EXPIRATION:
                notificationService.sendExpirationNotification(product.getName(), product.getExpiryDate());
                break;
            case NONE:
            default:
                break;
        }
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }
}
