package tech.terabyte.labs.vendomita.specification.impl;

import tech.terabyte.labs.vendomita.model.Color;
import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.specification.Specification;

public class ColorSpecification implements Specification<Product> {
    private final Color color;

    public ColorSpecification(Color color) {
        this.color = color;
    }

    @Override
    public boolean isSatisfied(Product item) {
        return item.color() == color;
    }
}
