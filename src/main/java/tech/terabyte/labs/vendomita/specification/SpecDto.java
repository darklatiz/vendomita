package tech.terabyte.labs.vendomita.specification;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
  @JsonSubTypes.Type(value = AndNode.class, name = "AND"),
  @JsonSubTypes.Type(value = OrNode.class,  name = "OR"),
  @JsonSubTypes.Type(value = NotNode.class, name = "NOT"),
  @JsonSubTypes.Type(value = ColorSpec.class, name = "ColorSpecification"),
  @JsonSubTypes.Type(value = SizeSpec.class,  name = "SizeSpecification"),
  @JsonSubTypes.Type(value = PriceLtSpec.class, name = "PriceLessThanSpecification"),
  @JsonSubTypes.Type(value = InStockSpec.class, name = "InStockSpecification")
})
public sealed interface SpecDto permits AndNode, OrNode, NotNode, ColorSpec, SizeSpec, PriceLtSpec, InStockSpec {
}

