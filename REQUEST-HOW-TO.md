# Vendomita API – Usage Guide (Generate → Filter → Download)

Este documento explica cómo usar la API paso a paso, con **requests listos** (cURL) y una breve descripción de lo que hace cada endpoint. El flujo es:

1. **Generar** catálogo (en memoria)
2. **Filtrar** usando un árbol de especificaciones tipadas (`SpecDto`: AND / OR / NOT + hojas)
3. **Descargar** el resultado como `.txt`

> Todas las respuestas JSON llevan el formato unificado `ApiResponse<T>`:
>
> ```json
> {
>   "status": "success|error",
>   "message": "texto humano",
>   "data": { ... } | [ ... ] | null,
>   "meta": { ... } | null
> }
> ```
>
> Además, cada respuesta incluye el header `X-Correlation-Id` para trazabilidad.

---

## 0) Base URL

```
http://localhost:8081/api/products
```

---

## 1) Generar catálogo

Genera `N` productos aleatorios en memoria (default 50). Reinicia el último resultado filtrado.

**Request**

```bash
curl -X POST 'http://localhost:8081/api/products/generate?count=50'
```

**Response (200)**

```json
{
  "status": "success",
  "message": "Catalog generated",
  "data": { "count": 50 },
  "meta": { "count": 50 }
}
```

**Errores típicos**

* N/A (si `count` < 1, la app podría generar 0; no es error, pero no podrás filtrar útilmente).

---

## 2) Filtrar (SpecDto + paginación)

Recibe un **árbol de especificaciones** tipadas (polimorfismo por `type`).
Tipos compuestos: `AND`, `OR`, `NOT`.
Hojas disponibles:

* `ColorSpecification` (`color`: `RED|GREEN|BLUE|BLACK`)
* `SizeSpecification` (`size`: `SMALL|MEDIUM|LARGE`)
* `PriceLessThanSpecification` (`price`: número)
* `InStockSpecification` (sin parámetros)

Parámetros de paginación por query:

* `page` (default 0)
* `size` (default 20)

### 2.1 Ejemplo A – AND simple

**Regla:** color GREEN **y** size LARGE.

**Request**

```bash
curl -X POST 'http://localhost:8081/api/products/filter?page=0&size=20' \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "AND",
    "children": [
      { "type": "ColorSpecification", "color": "GREEN" },
      { "type": "SizeSpecification",  "size":  "LARGE" }
    ]
  }'
```

**Response (200) – ejemplo**

```json
{
  "status": "success",
  "message": "Filter executed",
  "data": [
    {
      "name": "T-Shirt-7342",
      "color": "GREEN",
      "size": "LARGE",
      "price": 899,
      "inStock": true
    }
  ],
  "meta": {
    "total": 7,
    "page": 0,
    "size": 20,
    "returned": 7,
    "totalPages": 1,
    "hasNext": false,
    "hasPrev": false
  }
}
```

---

### 2.2 Ejemplo B – AND + NOT (precio ≥ 1000)

Usamos `NOT PriceLessThanSpecification(1000)` para expresar “precio **≥ 1000**”, sin crear otra spec.

**Regla:** size LARGE **y** en stock **y** precio ≥ 1000.

**Request**

```bash
curl -X POST 'http://localhost:8081/api/products/filter?page=0&size=10' \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "AND",
    "children": [
      { "type": "SizeSpecification",  "size": "LARGE" },
      { "type": "InStockSpecification" },
      { "type": "NOT", "child": { "type": "PriceLessThanSpecification", "price": 1000 } }
    ]
  }'
```

---

### 2.3 Ejemplo B – AND + NOT (In Stock)

**Regla:** size LARGE **y** en no en stock **y** precio ≥ 1000.

**Request**

```bash
curl -X POST 'http://localhost:8081/api/products/filter?page=0&size=10' \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "AND",
    "children": [
      { "type": "SizeSpecification",  "size": "LARGE" },
      { "type": "NOT", "child": { "type": "PriceLessThanSpecification", "price": 1000 } },
      { "type": "NOT", "child": { "type": "InStockSpecification" } }
    ]
  }'
```

---

### 2.4 Ejemplo C – OR al nivel raíz

**Regla:** (color BLUE) **o** (en stock).

**Request**

```bash
curl -X POST 'http://localhost:8081/api/products/filter?page=1&size=10' \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "OR",
    "children": [
      { "type": "ColorSpecification", "color": "BLUE" },
      { "type": "InStockSpecification" }
    ]
  }'
```

---

### 2.5 Ejemplo D – Mixto (OR dentro de AND)

**Regla:** (color GREEN **o** BLUE) **y** size LARGE **y** en stock **y** precio < 2000.

**Request**

```bash
curl -X POST 'http://localhost:8081/api/products/filter?page=0&size=20' \
  -H 'Content-Type: application/json' \
  -d '{
    "type": "AND",
    "children": [
      {
        "type": "OR",
        "children": [
          { "type": "ColorSpecification", "color": "GREEN" },
          { "type": "ColorSpecification", "color": "BLUE" }
        ]
      },
      { "type": "SizeSpecification",  "size": "LARGE" },
      { "type": "InStockSpecification" },
      { "type": "PriceLessThanSpecification", "price": 2000 }
    ]
  }'
```

**Errores típicos**

* `412 PRECONDITION_FAILED`: no has generado el catálogo aún.
* `400 BAD REQUEST`: árbol inválido (p.ej., `NOT` sin `child`, o `ColorSpecification` sin `color`).
* Valores fuera de rango o enums inválidos (ej. `color: "PURPLE"`) → `400`.

---

## 3) Descargar como TXT

Misma estructura de `SpecDto`, pero el endpoint devuelve **archivo `.txt`** con las coincidencias.
Si no hay catálogo, responde `412` con un `.txt` explicativo.

**Request**

```bash
curl -X POST 'http://localhost:8081/api/products/download' \
  -H 'Content-Type: application/json' \
  -o filtered-products.txt \
  -d '{
    "type": "AND",
    "children": [
      { "type": "ColorSpecification", "color": "GREEN" },
      { "type": "SizeSpecification",  "size":  "LARGE" }
    ]
  }'
```

**Response (200) – contenido del `.txt`**

```
Filtered Products:

T-Shirt-7342 | GREEN | LARGE | $899 | IN
Pants-1021   | GREEN | LARGE | $1200 | OUT
...
```

---

## 4) Headers útiles

* **X-Correlation-Id**: presente en todas las respuestas (y en logs).
  Puedes propagarlo enviándolo también en la petición:

  ```
  X-Correlation-Id: <uuid>
  ```

  Si no lo envías, el servidor genera uno nuevo.

---

## 5) Notas de diseño (para devs)

* El filtro implementa **Open/Closed**: agregar una spec nueva requiere:

    1. Crear un `record` que implemente `SpecDto`.
    2. Registrar el tipo en `@JsonSubTypes` del `SpecDto`.
    3. Agregar el `case` correspondiente en el `SpecParser.fromDto(...)`.
* Paginación se aplica **después** de evaluar la spec (`stream().filter(...)`) para mantener simpleza en memoria.
  Si más adelante usas DB, la spec debería traducirse a query (Criteria/JPA) y paginar en DB.

---

## 6) Troubleshooting rápido

* **“No products available”** → Ejecuta `POST /generate` primero.
* **“Unsupported spec type”** → Revisa el campo `type`.
* **Enums inválidos** → Usa valores exactos (`RED|GREEN|BLUE|BLACK`, `SMALL|MEDIUM|LARGE`).
* **`NOT` con varios hijos** → No es válido. Debe tener exactamente **uno**.
