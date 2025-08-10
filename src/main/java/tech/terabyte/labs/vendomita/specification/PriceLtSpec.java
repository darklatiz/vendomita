package tech.terabyte.labs.vendomita.specification;

import java.math.BigDecimal;

public record PriceLtSpec(BigDecimal price) implements SpecDto {
}
