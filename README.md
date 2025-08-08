# 🧠 Product Filter – Specification Pattern in Java 21

[![Java 21](https://img.shields.io/badge/java-21-blue.svg)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Build](https://img.shields.io/badge/build-passing-brightgreen)](#)
[![Test Coverage](https://img.shields.io/badge/tests-100%25-success)](#)

This project demonstrates a clean and scalable implementation of the **Specification Pattern** in **Java 21**, applying the **Open-Closed Principle** (from the SOLID principles) to filter a dynamic product catalog of over 1500 items.

It’s built with **Gradle**, features functional composition (AND, NOT), and includes a dynamic, adaptive test suite using **JUnit 5**.

---

## 📦 Features

- ✅ Java 21 + Gradle
- ✅ Custom `Specification<T>` interface
- ✅ Dynamic filtering by:
  - Color
  - Size
  - Price thresholds
  - Stock availability
- ✅ Functional-style composition (AND / NOT)
- ✅ Fluent utility `Specs` builder
- ✅ 1500+ randomly generated products
- ✅ Adaptive and resilient unit tests with JUnit 5

---

## 📁 Project Structure

```

vendomita/
├── build.gradle
├── settings.gradle
├── README.md
├── src/
│   ├── main/
│   │   └── java/com/terabyte/specfilter/
│   │       ├── App.java
│   │       ├── model/
│   │       ├── spec/
│   │       └── util/
│   └── test/
│       └── java/com/terabyte/specfilter/
│           └── ProductFilterTest.java

````

---

## 🚀 How to Run

### Requirements

- Java 21
- Gradle 8.x (or use the wrapper `./gradlew`)

### Build and Run

```bash
git clone https://github.com/darklatiz/vendomita.git
cd vendomita
./gradlew build
./gradlew test
````

> The main app will generate 15000 random products and filter them using composed specifications (e.g., in stock AND price < 1000).

If you get a “no main class” error, make sure your `build.gradle` contains:

```groovy
application {
    mainClass = 'com.terabyte.specfilter.App'
}
```

---

## 🧪 Running Tests

This project uses JUnit 5 with adaptive assertions (no hardcoded values) based on the generated data.

```bash
./gradlew test
```

All test cases will pass dynamically even as the dataset changes with every execution.

---

## 🧠 Why Specification Pattern?

This pattern helps you **decouple business rules** from filtering logic and supports:

* Composability (`AND`, `OR`, `NOT`)
* Testability (unit test each rule independently)
* Reusability (use rules in different contexts: APIs, batch, etc.)
* Extensibility (**Open-Closed Principle** in action)

Instead of writing dozens of `filterByXAndYAndZ()` methods, you can combine small specs dynamically.

---

## 📘 Example

```java
Specification<Product> spec = Specs.and(
    new InStockSpecification(),
    new PriceLessThanSpecification(BigDecimal.valueOf(1000)),
    new SizeSpecification(Size.MEDIUM)
);

productFilter.filter(products, spec)
    .forEach(p -> System.out.println("MATCH: " + p.name()));
```

---

## 👨‍💻 Author

**Laboratorios Terabyte**
Built with ❤️ by [@darklatiz](https://github.com/darklatiz)

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
