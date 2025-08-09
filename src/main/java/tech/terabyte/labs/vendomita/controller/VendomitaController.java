package tech.terabyte.labs.vendomita.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.terabyte.labs.vendomita.filter.CorrelationIdFilter;
import tech.terabyte.labs.vendomita.model.ApiResponse;
import tech.terabyte.labs.vendomita.model.Color;
import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.model.Size;
import tech.terabyte.labs.vendomita.specification.Filter;
import tech.terabyte.labs.vendomita.specification.Specification;
import tech.terabyte.labs.vendomita.specification.factory.SpecNode;
import tech.terabyte.labs.vendomita.specification.factory.SpecParser;
import tech.terabyte.labs.vendomita.specification.utility.ProductGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class VendomitaController {

    private List<Product> products;
    private List<Product> lastFiltered;
    private final SpecParser specParser;

    public VendomitaController(SpecParser specParser) {
        this.products = new ArrayList<>();
        this.lastFiltered = new ArrayList<>();
        this.specParser = specParser;
    }

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Map<String, ?>>> generate(@RequestParam(defaultValue = "50") int count) {
        this.products = ProductGenerator.generate(count);
        this.lastFiltered = new ArrayList<>();
        var meta = Map.of("count", products.size());
        return ResponseEntity.ok(ApiResponse.success("Catalog generated", meta, meta));
    }

    @GetMapping("/specs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAvailableSpecs() {
        var data = Map.of(
          "colors", Color.values(),
          "sizes", Size.values(),
          "examplePriceThreshold", 1000,
          "flags", List.of("inStock", "notColor")
        );
        return ResponseEntity.ok(ApiResponse.success("Available specifications", data));
    }

    @PostMapping("/filter")
    public ResponseEntity<ApiResponse<List<Product>>> filterProducts(
      @RequestBody SpecNode root,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {

        if (products == null || products.isEmpty()) {
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
              .body(ApiResponse.error("No products available. Generate the catalog first with POST /api/products/generate"));
        }

        Specification<Product> spec = specParser.fromNode(root);
        Filter<Product> filter = (items, s) -> items.stream().filter(s::isSatisfied);

        List<Product> filtered = filter.filter(products, spec).toList();

        int total = filtered.size();
        if (size <= 0) size = 20;
        if (page < 0) page = 0;

        int from = Math.min(page * size, total);
        int to = Math.min(from + size, total);
        List<Product> pageContent = filtered.subList(from, to);

        int totalPages = (int) Math.ceil(total / (double) size);
        boolean hasNext = page + 1 < totalPages;
        boolean hasPrev = page > 0;

        String cid = org.slf4j.MDC.get(CorrelationIdFilter.MDC_KEY);

        var meta = java.util.Map.of(
          "total", total,
          "page", page,
          "size", size,
          "returned", pageContent.size(),
          "totalPages", totalPages,
          "hasNext", hasNext,
          "hasPrev", hasPrev,
          "correlationId", cid
        );

        return ResponseEntity.ok(ApiResponse.success("Filter executed", pageContent, meta));
    }


    @PostMapping("/download")
    public ResponseEntity<Resource> downloadFromSpec(@RequestBody SpecNode root) {
        if (products == null || products.isEmpty()) {
            var msg = "No products available. Generate the catalog first with POST /api/products/generate";
            var r = new ByteArrayResource(msg.getBytes());
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
              .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=info.txt")
              .contentType(MediaType.TEXT_PLAIN)
              .body(r);
        }

        Specification<Product> spec = specParser.fromNode(root);
        var filtered = products.stream().filter(spec::isSatisfied).toList();

        StringBuilder content = new StringBuilder("Filtered Products:\n\n");
        if (filtered.isEmpty()) {
            content.append("No matches.\n");
        } else {
            filtered.forEach(p -> content.append("%s | %s | %s | $%s | %s%n"
              .formatted(p.name(), p.color(), p.size(), p.price(), p.inStock() ? "IN" : "OUT")));
        }

        ByteArrayResource r = new ByteArrayResource(content.toString().getBytes());
        return ResponseEntity.ok()
          .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=filtered-products.txt")
          .contentType(MediaType.TEXT_PLAIN)
          .body(r);
    }
}