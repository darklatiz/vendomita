package tech.terabyte.labs.vendomita.specification.utility;

import tech.terabyte.labs.vendomita.model.Color;
import tech.terabyte.labs.vendomita.model.Product;
import tech.terabyte.labs.vendomita.model.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ProductGenerator {
    private static final Random random = new Random();
    private static final String[] BASE_NAMES = {
      "T-Shirt", "Hat", "Shoes", "Pants", "Backpack", "Watch", "Glasses", "Sweater", "Socks", "Jacket"
    };

    public static List<Product> generate(int count) {
        List<Product> products = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String name = generateName();
            Color color = Color.values()[random.nextInt(Color.values().length)];
            Size size = Size.values()[random.nextInt(Size.values().length)];
            BigDecimal price = BigDecimal.valueOf(100 + random.nextInt(4901)); // $100 - $5000
            boolean inStock = random.nextBoolean();

            products.add(new Product(name, color, size, price, inStock));
        }
        return products;
    }

    private static String generateName() {
        String base = BASE_NAMES[random.nextInt(BASE_NAMES.length)];
        int suffix = 1000 + random.nextInt(9000);
        return base + "-" + suffix;
    }
}
