package tech.terabyte.labs.vendomita.specification.utility;

import tech.terabyte.labs.vendomita.specification.Specification;

import java.util.Arrays;

public class SpecsBuilder {

    public static <T> Specification<T> and(Specification<T>... specs) {
        return item -> Arrays.stream(specs)
          .allMatch(spec -> spec.isSatisfied(item));
    }

    public static <T> Specification<T> not(Specification<T> spec) {
        return item -> !spec.isSatisfied(item);
    }

    public static <T> Specification<T> or(Specification<T>... specs) {
        return item -> {
            for (Specification<T> s : specs) {
                if (s.isSatisfied(item)) return true;
            }
            return false;
        };
    }
}
