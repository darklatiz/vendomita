package tech.terabyte.labs.vendomita.specification.impl;

import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.specification.Specification;

import java.math.BigDecimal;

public class PriceLessThanSpecification implements Specification<Product> {

    private final BigDecimal threshold;

    public PriceLessThanSpecification(BigDecimal threshold) {
        this.threshold = threshold;
    }

    @Override
    public boolean isSatisfied(Product item) {
        return item.price().compareTo(threshold) < 0;
    }
}
