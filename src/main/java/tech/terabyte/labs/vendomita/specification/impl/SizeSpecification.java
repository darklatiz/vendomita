package tech.terabyte.labs.vendomita.specification.impl;

import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.model.Size;
import tech.terabyte.labs.vendomita.specification.Specification;

public class SizeSpecification implements Specification<Product> {
    private final Size size;

    public SizeSpecification(Size size) {
        this.size = size;
    }

    @Override
    public boolean isSatisfied(Product item) {
        return item.size() == size;
    }
}
