package tech.terabyte.labs.vendomita;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tech.terabyte.labs.vendomita.model.Color;
import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.model.Size;
import tech.terabyte.labs.vendomita.specification.Filter;
import tech.terabyte.labs.vendomita.specification.Specification;
import tech.terabyte.labs.vendomita.specification.impl.AndSpecification;
import tech.terabyte.labs.vendomita.specification.impl.ColorSpecification;
import tech.terabyte.labs.vendomita.specification.impl.InStockSpecification;
import tech.terabyte.labs.vendomita.specification.impl.PriceLessThanSpecification;
import tech.terabyte.labs.vendomita.specification.impl.SizeSpecification;
import tech.terabyte.labs.vendomita.specification.utility.ProductGenerator;
import tech.terabyte.labs.vendomita.specification.utility.SpecsBuilder;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductFilterTest {

    private List<Product> products;
    private Filter<Product> productFilter;

    @BeforeEach
    void setup() {
        products = ProductGenerator.generate(15000);
        productFilter = (items, spec) -> items.stream().filter(spec::isSatisfied);
    }

    @Test
    @DisplayName("Filter Products By Color Red")
    void filterByColor() {
        Color colorToTest = Color.RED;
        Specification<Product> spec = new ColorSpecification(colorToTest);

        List<Product> expected = products.stream()
          .filter(p -> p.color() == colorToTest)
          .toList();

        List<Product> result = productFilter.filter(products, spec).toList();

        assertEquals(expected.size(), result.size());
        assertTrue(result.containsAll(expected));
    }

    @Test
    void filterBySize() {
        Size sizeToTest = Size.MEDIUM;
        Specification<Product> spec = new SizeSpecification(sizeToTest);

        List<Product> expected = products.stream()
          .filter(p -> p.size() == sizeToTest)
          .toList();

        List<Product> result = productFilter.filter(products, spec).toList();

        assertEquals(expected.size(), result.size());
    }

    @Test
    void filterByPriceLessThan() {
        BigDecimal threshold = BigDecimal.valueOf(1500);
        Specification<Product> spec = new PriceLessThanSpecification(threshold);

        List<Product> expected = products.stream()
          .filter(p -> p.price().compareTo(threshold) < 0)
          .toList();

        List<Product> result = productFilter.filter(products, spec).toList();

        assertEquals(expected.size(), result.size());
    }

    @Test
    void filterByInStock() {
        Specification<Product> spec = new InStockSpecification();

        List<Product> expected = products.stream()
          .filter(Product::inStock)
          .toList();

        List<Product> result = productFilter.filter(products, spec).toList();

        assertEquals(expected.size(), result.size());
    }

    @Test
    void filterByColorAndSize() {
        Color color = Color.BLACK;
        Size size = Size.LARGE;
        Specification<Product> spec = new AndSpecification<>(
          new ColorSpecification(color),
          new SizeSpecification(size)
        );

        List<Product> expected = products.stream()
          .filter(p -> p.color() == color && p.size() == size)
          .toList();

        List<Product> result = productFilter.filter(products, spec).toList();

        assertEquals(expected.size(), result.size());
    }

    @Test
    void filterByThreeConditions() {
        BigDecimal price = BigDecimal.valueOf(1500);
        Specification<Product> spec = SpecsBuilder.and(
          new InStockSpecification(),
          new PriceLessThanSpecification(price),
          new SizeSpecification(Size.MEDIUM)
        );

        List<Product> expected = products.stream()
          .filter(p -> p.inStock()
            && p.price().compareTo(price) < 0
            && p.size() == Size.MEDIUM)
          .toList();

        List<Product> result = productFilter.filter(products, spec).toList();

        assertEquals(expected.size(), result.size());
    }

    @Test
    void filterWithNotSpecification() {
        Specification<Product> spec = SpecsBuilder.not(new ColorSpecification(Color.GREEN));

        List<Product> expected = products.stream()
          .filter(p -> p.color() != Color.GREEN)
          .toList();

        List<Product> result = productFilter.filter(products, spec).toList();

        assertEquals(expected.size(), result.size());
        assertTrue(result.stream().noneMatch(p -> p.color() == Color.GREEN));
    }

    @Test
    void filterWith_AND_NotSpecification() {
        BigDecimal price = BigDecimal.valueOf(1150);
        Specification<Product> specNotGreenColor = SpecsBuilder.not(new ColorSpecification(Color.GREEN));
        Specification<Product> specNotInStock = SpecsBuilder.not(new InStockSpecification());
        Specification<Product> specGreaterThanPrice = SpecsBuilder.not(new PriceLessThanSpecification(price));
        Specification<Product> notGreenAndNotInStock = SpecsBuilder.and(specNotGreenColor, specNotInStock, specGreaterThanPrice);

        List<Product> expected = products.stream()
          .filter(p -> p.color() != Color.GREEN && !p.inStock() && p.price().compareTo(price) >= 0)
          .toList();

        List<Product> result = productFilter.filter(products, notGreenAndNotInStock).toList();

        assertEquals(expected.size(), result.size());
        assertTrue(result.stream().noneMatch(p -> p.color() == Color.GREEN && p.inStock() && p.price().compareTo(price) >= 0));
    }


}
