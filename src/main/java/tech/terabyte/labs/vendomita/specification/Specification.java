package tech.terabyte.labs.vendomita.specification;

@FunctionalInterface
public interface Specification<T> {
    boolean isSatisfied(T item);
}
