package tech.terabyte.labs.vendomita.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record FilterRequest(
  @JsonProperty("color")
  String color,

  @JsonProperty("not_color")
  String notColor,

  @JsonProperty("size")
  String size,

  @JsonProperty("priceless_than")
  BigDecimal priceLessThan,

  @JsonProperty("in_stock")
  Boolean inStock
) {
}
