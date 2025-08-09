package tech.terabyte.labs.vendomita.specification.factory;

import java.util.List;

public record SpecNode(
  String op, // AND | OR | NOT | LEAF
  String field, // color, size, priceLessThan, inStock
  String value, // RED, LARGE, etc
  List<SpecNode> children) { }
