package tech.terabyte.labs.vendomita.specification;

import java.util.List;

public record AndNode(List<SpecDto> children) implements SpecDto {
}
