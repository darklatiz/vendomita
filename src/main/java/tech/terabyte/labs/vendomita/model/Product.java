package tech.terabyte.labs.vendomita.model;

import java.math.BigDecimal;

public record Product(String name, Color color, Size size, BigDecimal price, boolean inStock) {
}
