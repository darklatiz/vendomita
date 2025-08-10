package tech.terabyte.labs.vendomita.specification.factory;

import org.springframework.stereotype.Component;
import tech.terabyte.labs.vendomita.model.Color;
import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.model.Size;
import tech.terabyte.labs.vendomita.specification.AndNode;
import tech.terabyte.labs.vendomita.specification.ColorSpec;
import tech.terabyte.labs.vendomita.specification.InStockSpec;
import tech.terabyte.labs.vendomita.specification.NotNode;
import tech.terabyte.labs.vendomita.specification.OrNode;
import tech.terabyte.labs.vendomita.specification.PriceLtSpec;
import tech.terabyte.labs.vendomita.specification.SizeSpec;
import tech.terabyte.labs.vendomita.specification.SpecDto;
import tech.terabyte.labs.vendomita.specification.Specification;
import tech.terabyte.labs.vendomita.specification.utility.SpecsBuilder;

@Component
public class SpecParser {
    public Specification<Product> fromDto(SpecDto dto) {
        return switch (dto) {
            case AndNode a -> SpecsBuilder.and(a.children().stream().map(this::fromDto).toArray(Specification[]::new));
            case OrNode o -> SpecsBuilder.or(o.children().stream().map(this::fromDto).toArray(Specification[]::new));
            case NotNode n -> SpecsBuilder.not(fromDto(n.child()));
            case ColorSpec c -> p -> p.color() == Color.valueOf(c.color().toUpperCase());
            case SizeSpec s -> p -> p.size() == Size.valueOf(s.size().toUpperCase());
            case PriceLtSpec pr -> p -> p.price().compareTo(pr.price()) < 0;
            case InStockSpec ignored -> Product::inStock;
        };
    }
}
