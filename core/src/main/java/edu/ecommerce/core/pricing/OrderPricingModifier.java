package edu.ecommerce.core.pricing;

import edu.ecommerce.core.entity.Order;

public interface OrderPricingModifier {
    void apply(Order order);
}
