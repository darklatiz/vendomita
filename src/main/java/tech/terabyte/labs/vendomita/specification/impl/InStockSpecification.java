package tech.terabyte.labs.vendomita.specification.impl;

import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.specification.Specification;

public class InStockSpecification implements Specification<Product> {
    @Override
    public boolean isSatisfied(Product item) {
        return item.inStock();
    }
}
