package tech.terabyte.labs.vendomita.runner;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tech.terabyte.labs.vendomita.model.Color;
import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.model.Size;
import tech.terabyte.labs.vendomita.specification.Filter;
import tech.terabyte.labs.vendomita.specification.Specification;
import tech.terabyte.labs.vendomita.specification.impl.ColorSpecification;
import tech.terabyte.labs.vendomita.specification.impl.InStockSpecification;
import tech.terabyte.labs.vendomita.specification.impl.PriceLessThanSpecification;
import tech.terabyte.labs.vendomita.specification.impl.SizeSpecification;
import tech.terabyte.labs.vendomita.specification.utility.ProductGenerator;
import tech.terabyte.labs.vendomita.specification.utility.SpecsBuilder;

import java.math.BigDecimal;
import java.util.List;

@Component
public class DemoRunner implements CommandLineRunner {

    @Override
    public void run(String... args) throws Exception {
        List<Product> productList = ProductGenerator.generate(50);
        Filter<Product> productFilter = (items, spec) -> items.stream().filter(spec::isSatisfied);

        System.out.println("📦 Total products: " + productList.size());
        System.out.println("RED products:");

        productFilter.filter(productList, new ColorSpecification(Color.RED))
          .forEach(p -> System.out.println(" - " + p.name() + " | " + p.color()));

        System.out.println("\n📏 LARGE BLACK products in stock:");
        Specification<Product> combo = SpecsBuilder.and(
          new SizeSpecification(Size.LARGE),
          new ColorSpecification(Color.BLACK),
          new InStockSpecification()
        );

        productFilter.filter(productList, combo)
          .forEach(p -> System.out.println(" - " + p.name() + " | " + p.size() + " | " + p.color() + " | stock: " + p.inStock()));

        System.out.println("💸 Products cheaper than $500 and in stock:");
        Specification<Product> combo2 = SpecsBuilder.and(
          new PriceLessThanSpecification(BigDecimal.valueOf(500)),
          new InStockSpecification()
        );

        productFilter.filter(productList, combo2)
          .forEach(p -> System.out.println(" - " + p.name() + " | " + p.size() + " | " + p.color() + " | stock: " + p.inStock()));




    }
}
