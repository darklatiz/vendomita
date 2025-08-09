package tech.terabyte.labs.vendomita.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.terabyte.labs.vendomita.model.Color;
import tech.terabyte.labs.vendomita.model.FilterRequest;
import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.model.Size;
import tech.terabyte.labs.vendomita.specification.Filter;
import tech.terabyte.labs.vendomita.specification.Specification;
import tech.terabyte.labs.vendomita.specification.factory.SpecificationFactory;
import tech.terabyte.labs.vendomita.specification.utility.ProductGenerator;
import tech.terabyte.labs.vendomita.specification.utility.SpecsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class VendomitaController {

    private final SpecificationFactory specificationFactory;
    private List<Product> products;
    private List<Product> lastFiltered;


    public VendomitaController(SpecificationFactory specificationFactory) {
        this.specificationFactory = specificationFactory;
        this.products = new ArrayList<>();
        this.lastFiltered = new ArrayList<>();
    }

    @GetMapping("/generate")
    public ResponseEntity<String> generate(@RequestParam(defaultValue = "50") int count) {
        this.products = ProductGenerator.generate(count);
        return ResponseEntity.ok("✅ Generated " + products.size() + " products.");
    }

    @GetMapping("/specs")
    public Map<String, Object> getAvailableSpecs() {
        return Map.of(
          "colors", Color.values(),
          "sizes", Size.values(),
          "examplePriceThreshold", 1000,
          "flags", List.of("inStock", "notColor")
        );
    }

    @PostMapping("/filter")
    public ResponseEntity<List<Product>> filterProducts(@RequestBody FilterRequest request) {
        List<Specification<Product>> specs = specificationFactory.buildSpecs(request);
        Specification<Product> finalSpec = SpecsBuilder.and(specs.toArray(new Specification[0]));

        Filter<Product> filter = (items, spec) -> items.stream().filter(spec::isSatisfied);

        this.lastFiltered = filter.filter(this.products, finalSpec).toList();
        return ResponseEntity.ok(this.lastFiltered);
    }

    @GetMapping("/download")
    public ResponseEntity<Resource> downloadFiltered() {
        if (lastFiltered.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        StringBuilder content = new StringBuilder("Filtered Products:\n\n");
        for (Product p : lastFiltered) {
            content.append("%s | %s | %s | $%s | %s%n".formatted(
              p.name(), p.color(), p.size(), p.price(), p.inStock() ? "✔ in stock" : "❌ out of stock"
            ));
        }

        ByteArrayResource resource = new ByteArrayResource(content.toString().getBytes());

        return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=filtered-products.txt")
          .contentType(MediaType.TEXT_PLAIN)
          .body(resource);
    }
}
