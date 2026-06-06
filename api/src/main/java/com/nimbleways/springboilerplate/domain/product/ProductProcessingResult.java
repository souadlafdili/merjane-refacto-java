package com.nimbleways.springboilerplate.domain.product;

public class ProductProcessingResult {
    private final boolean shouldSave;
    private final ProductNotificationType notificationType;

    private ProductProcessingResult(boolean shouldSave, ProductNotificationType notificationType) {
        this.shouldSave = shouldSave;
        this.notificationType = notificationType;
    }

    public static ProductProcessingResult withoutChange() {
        return new ProductProcessingResult(false, ProductNotificationType.NONE);
    }

    public static ProductProcessingResult savedWithoutNotification() {
        return new ProductProcessingResult(true, ProductNotificationType.NONE);
    }

    public static ProductProcessingResult savedWithNotification(ProductNotificationType notificationType) {
        return new ProductProcessingResult(true, notificationType);
    }

    public boolean shouldSave() {
        return shouldSave;
    }

    public ProductNotificationType getNotificationType() {
        return notificationType;
    }
}
