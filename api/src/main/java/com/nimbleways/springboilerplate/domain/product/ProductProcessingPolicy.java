package com.nimbleways.springboilerplate.domain.product;

import java.time.LocalDate;

import com.nimbleways.springboilerplate.entities.Product;
import com.nimbleways.springboilerplate.entities.ProductType;

public class ProductProcessingPolicy {

    public ProductProcessingResult process(Product product, LocalDate today) {
        if (product.getType() == null) {
            return ProductProcessingResult.withoutChange();
        }

        return process(product.getType(), product, today);
    }

    private ProductProcessingResult process(ProductType productType, Product product, LocalDate today) {
        switch (productType) {
            case NORMAL:
                return processNormalProduct(product);
            case SEASONAL:
                return processSeasonalProduct(product, today);
            case EXPIRABLE:
                return processExpirableProduct(product, today);
            default:
                return ProductProcessingResult.withoutChange();
        }
    }

    private ProductProcessingResult processNormalProduct(Product product) {
        if (product.getAvailable() > 0) {
            decrementAvailable(product);
            return ProductProcessingResult.savedWithoutNotification();
        }

        if (product.getLeadTime() > 0) {
            return ProductProcessingResult.savedWithNotification(ProductNotificationType.DELAY);
        }

        return ProductProcessingResult.withoutChange();
    }

    private ProductProcessingResult processSeasonalProduct(Product product, LocalDate today) {
        if (isDuringSeason(product, today) && product.getAvailable() > 0) {
            decrementAvailable(product);
            return ProductProcessingResult.savedWithoutNotification();
        }

        return handleUnavailableSeasonalProduct(product, today);
    }

    private ProductProcessingResult handleUnavailableSeasonalProduct(Product product, LocalDate today) {
        if (today.plusDays(product.getLeadTime()).isAfter(product.getSeasonEndDate())) {
            product.setAvailable(0);
            return ProductProcessingResult.savedWithNotification(ProductNotificationType.OUT_OF_STOCK);
        }

        if (product.getSeasonStartDate().isAfter(today)) {
            return ProductProcessingResult.savedWithNotification(ProductNotificationType.OUT_OF_STOCK);
        }

        return ProductProcessingResult.savedWithNotification(ProductNotificationType.DELAY);
    }

    private ProductProcessingResult processExpirableProduct(Product product, LocalDate today) {
        if (product.getAvailable() > 0 && product.getExpiryDate().isAfter(today)) {
            decrementAvailable(product);
            return ProductProcessingResult.savedWithoutNotification();
        }

        product.setAvailable(0);
        return ProductProcessingResult.savedWithNotification(ProductNotificationType.EXPIRATION);
    }

    private boolean isDuringSeason(Product product, LocalDate today) {
        return today.isAfter(product.getSeasonStartDate()) && today.isBefore(product.getSeasonEndDate());
    }

    private void decrementAvailable(Product product) {
        product.setAvailable(product.getAvailable() - 1);
    }
}