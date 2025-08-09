package tech.terabyte.labs.vendomita.specification.factory;

import org.springframework.stereotype.Component;
import tech.terabyte.labs.vendomita.model.Color;
import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.model.Size;
import tech.terabyte.labs.vendomita.specification.Specification;
import tech.terabyte.labs.vendomita.specification.impl.ColorSpecification;
import tech.terabyte.labs.vendomita.specification.impl.InStockSpecification;
import tech.terabyte.labs.vendomita.specification.impl.PriceLessThanSpecification;
import tech.terabyte.labs.vendomita.specification.impl.SizeSpecification;
import tech.terabyte.labs.vendomita.specification.utility.SpecsBuilder;

import java.math.BigDecimal;

@Component
public class SpecParser {
    public Specification<Product> fromNode(SpecNode node) {
        return switch (node.op().toUpperCase()) {
            case "AND" -> SpecsBuilder.and(
              node.children().stream()
                .map(this::fromNode)
                .toArray(Specification[]::new)
            );
            case "OR" -> SpecsBuilder.or(
              node.children().stream()
                .map(this::fromNode)
                .toArray(Specification[]::new)
            );
            case "NOT" -> SpecsBuilder.not(fromNode(node.children().get(0)));
            case "LEAF" -> leafToSpec(node.field(), node.value());
            default -> throw new IllegalArgumentException("Unknown op: " + node.op());
        };
    }

    private Specification<Product> leafToSpec(String field, String value) {
        return switch (field.toLowerCase()) {
            case "color" -> new ColorSpecification(Color.valueOf(value.toUpperCase()));
            case "size" -> new SizeSpecification(Size.valueOf(value.toUpperCase()));
            case "instock" -> "true".equalsIgnoreCase(value) ? new InStockSpecification() : SpecsBuilder.not(new InStockSpecification());
            case "pricelessthan" -> new PriceLessThanSpecification(new BigDecimal(value));
            default -> throw new IllegalArgumentException("Unsupported field: " + field);
        };
    }
}
