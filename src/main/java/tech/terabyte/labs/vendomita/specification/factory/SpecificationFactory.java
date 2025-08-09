package tech.terabyte.labs.vendomita.specification.factory;

import org.springframework.stereotype.Component;
import tech.terabyte.labs.vendomita.model.Color;
import tech.terabyte.labs.vendomita.model.FilterRequest;
import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.model.Size;
import tech.terabyte.labs.vendomita.specification.Specification;
import tech.terabyte.labs.vendomita.specification.impl.ColorSpecification;
import tech.terabyte.labs.vendomita.specification.impl.InStockSpecification;
import tech.terabyte.labs.vendomita.specification.impl.PriceLessThanSpecification;
import tech.terabyte.labs.vendomita.specification.impl.SizeSpecification;
import tech.terabyte.labs.vendomita.specification.utility.SpecsBuilder;

import java.util.ArrayList;
import java.util.List;

@Component
public class SpecificationFactory {

    public List<Specification<Product>> buildSpecs(FilterRequest request) {
        List<Specification<Product>> specs = new ArrayList<>();

        if (request.color() != null) {
            specs.add(new ColorSpecification(Color.valueOf(request.color())));
        }

        if (request.notColor() != null) {
            specs.add(SpecsBuilder.not(new ColorSpecification(Color.valueOf(request.notColor()))));
        }

        if (request.size() != null) {
            specs.add(new SizeSpecification(Size.valueOf(request.size())));
        }

        if (request.priceLessThan() != null) {
            specs.add(new PriceLessThanSpecification(request.priceLessThan()));
        }

        if (Boolean.TRUE.equals(request.inStock())) {
            specs.add(new InStockSpecification());
        }

        return specs;
    }

}
