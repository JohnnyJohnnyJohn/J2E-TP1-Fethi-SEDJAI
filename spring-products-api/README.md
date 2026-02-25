# TP2 - Persistence avec JPA

## Auteur

Fethi Sedjai

## Choix technologique : Spring Boot au lieu de Jakarta EE / WildFly

Le sujet du TP2 est prevu pour **Jakarta EE** (persistence.xml, EntityManager, WildFly, JTA).  
J'ai choisi d'utiliser **Spring Boot** pour les raisons suivantes :

- **Spring Data JPA** simplifie enormement la couche repository (interfaces au lieu d'implementations manuelles avec `EntityManager`).
- **Spring Boot auto-configuration** remplace `persistence.xml` et la configuration manuelle de datasource WildFly par un simple `application-jpa.properties`.
- **@Transactional de Spring** remplace la gestion JTA de WildFly, avec le meme comportement (rollback automatique sur RuntimeException).
- **Demarrage rapide** : pas besoin de deployer un WAR sur un serveur d'application ; un simple `mvn spring-boot:run` suffit.
- Les concepts JPA restent identiques (entites, relations, JPQL, JOIN FETCH, N+1, EntityGraph) quel que soit le framework.

## Description

API REST Spring Boot avec persistence JPA complete incluant :

- 5 entites JPA (Product, Category, Supplier, Order, OrderItem)
- Relations complexes (@ManyToOne, @OneToMany avec cascade)
- Requetes JPQL optimisees (JOIN FETCH, agregations, sous-requetes, DTO projections)
- Transactions gerees avec @Transactional (rollback teste)
- Optimisations : @BatchSize, @NamedEntityGraph, demonstration du probleme N+1

## Modele de Donnees

```
categories (1) ──< products >── (1) suppliers
                      │
                      │ (via order_items)
                      │
orders (1) ──< order_items >── (N) products
```

- **Category** `1 ──< N` **Product** (`@OneToMany` / `@ManyToOne`)
- **Supplier** `1 ──< N` **Product** (`@OneToMany` / `@ManyToOne`)
- **Order** `1 ──< N` **OrderItem** (`@OneToMany`, cascade ALL, orphanRemoval)
- **OrderItem** `N >── 1` **Product** (`@ManyToOne`)

## Lancement

```bash
# 1. Demarrer PostgreSQL
docker compose -f ../docker-compose-db.yml up -d

# 2. Lancer l'application
mvn spring-boot:run
```

L'application demarre sur `http://localhost:8081`.

## Endpoints

### Products


| Methode | URL                             | Description                      |
| ------- | ------------------------------- | -------------------------------- |
| GET     | `/api/products`                 | Liste optimisee (JOIN FETCH)     |
| GET     | `/api/products?categoryId={id}` | Filtrer par categorie            |
| GET     | `/api/products?category={name}` | Filtrer par nom de categorie     |
| GET     | `/api/products/{id}`            | Obtenir un produit               |
| POST    | `/api/products`                 | Creer un produit                 |
| PUT     | `/api/products/{id}`            | Modifier un produit              |
| PATCH   | `/api/products/{id}/stock`      | Modifier le stock                |
| PATCH   | `/api/products/{id}/decrease-stock?quantity=N` | Diminuer le stock     |
| DELETE  | `/api/products/{id}`            | Supprimer un produit             |
| GET     | `/api/products/slow`            | Demo N+1 (sans JOIN FETCH)       |
| GET     | `/api/products/fast`            | Demo optimisee (JOIN FETCH)      |
| GET     | `/api/products/graph/category`  | EntityGraph Product.withCategory |
| GET     | `/api/products/graph/full`      | EntityGraph Product.full         |


### Categories


| Methode | URL                                      | Description                              |
| ------- | ---------------------------------------- | ---------------------------------------- |
| GET     | `/api/categories`                        | Lister les categories                    |
| GET     | `/api/categories/{id}`                   | Obtenir une categorie                    |
| GET     | `/api/categories/{id}?withProducts=true` | Categorie avec ses produits (JOIN FETCH) |
| POST    | `/api/categories`                        | Creer une categorie                      |
| PUT     | `/api/categories/{id}`                   | Modifier une categorie                   |
| DELETE  | `/api/categories/{id}`                   | Supprimer une categorie                  |


### Orders


| Methode | URL                                 | Description          |
| ------- | ----------------------------------- | -------------------- |
| GET     | `/api/orders`                       | Lister les commandes |
| GET     | `/api/orders?customerEmail={email}` | Filtrer par email    |
| GET     | `/api/orders?status={status}`       | Filtrer par statut   |
| GET     | `/api/orders/{id}`                  | Obtenir une commande |
| POST    | `/api/orders`                       | Creer une commande   |
| PATCH   | `/api/orders/{id}/status`           | Modifier le statut   |


### Stats (agregations JPQL)


| Methode | URL                                             | Description                         |
| ------- | ----------------------------------------------- | ----------------------------------- |
| GET     | `/api/stats/products-by-category/count`         | Nombre de produits par categorie    |
| GET     | `/api/stats/products-by-category/average-price` | Prix moyen par categorie            |
| GET     | `/api/stats/category-stats`                     | DTO CategoryStats (projection JPQL) |
| GET     | `/api/stats/top-expensive?limit=10`             | Top N produits les plus chers       |
| GET     | `/api/stats/never-ordered-products`             | Produits jamais commandes           |
| GET     | `/api/stats/categories-min-products?min=1`      | Categories avec min N produits      |
| GET     | `/api/stats/total-revenue`                      | Chiffre d'affaires (DELIVERED)      |
| GET     | `/api/stats/orders-by-status`                   | Commandes par statut                |
| GET     | `/api/stats/most-ordered-products?limit=10`     | Produits les plus commandes         |


### Demo


| Methode | URL                           | Description                                           |
| ------- | ----------------------------- | ----------------------------------------------------- |
| POST    | `/api/demo/seed`              | Creer donnees de base (Category + Supplier + Product) |
| POST    | `/api/demo/rollback`          | Tester le rollback transactionnel                     |
| POST    | `/api/demo/test-repositories` | Tester CRUD sur chaque repository                     |


## Tests Effectues (TP2)

- CRUD complet sur toutes les entites
- Relations bidirectionnelles fonctionnelles
- Transactions avec rollback (Livrable 4)
- Requetes d'agregation (Livrable 6)
- Optimisation N+1 demontree (Livrable 7)
- DTOs pour projections JPQL (CategoryStats, OrderStatusCount, MostOrderedProduct)
- @BatchSize sur Category.products
- @NamedEntityGraph (Product.withCategory, Product.full)

## Captures TP2 (dossier `captures/`)


| Fichier                             | Livrable                           |
| ----------------------------------- | ---------------------------------- |
| `TP2-livrable1-demarrage-jpa.png`   | Demarrage + profil jpa + Hibernate |
| `TP2-livrable2-tables-postgres.png` | Tables PostgreSQL + FK             |
| `TP2-livrable3-requete-join.png`    | Logs SQL avec JOIN FETCH           |
| `TP2-livrable4-rollback.png`        | Demo rollback transactionnel       |
| `TP2-livrable5-orders.png`          | Tables orders + order_items        |
| `TP2-livrable6-aggregation.png`     | Resultats requetes d'agregation    |
| `TP2-livrable7-nplus1.png`          | Logs N+1 vs 1 requete             |


## Difficultes Rencontrees (TP2)

1. **ByteBuddyInterceptor / LazyInitializationException** : Jackson ne sait pas serialiser les proxies Hibernate lazy. Resolu avec `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` sur les entites.
2. **Probleme N+1** : sans JOIN FETCH, chaque acces a `product.getCategory()` declenche un SELECT supplementaire. Resolu avec des requetes JPQL utilisant `LEFT JOIN FETCH`.
3. **Serialisation circulaire** : les relations bidirectionnelles Order/OrderItem causent une boucle infinie en JSON. Resolu avec `@JsonIgnore` sur `OrderItem.order`.

## Points Cles Appris (TP2)

1. **JOIN FETCH** est essentiel pour eviter le probleme N+1 en JPA.
2. **@Transactional** garantit l'atomicite : en cas d'exception, tout est annule (rollback).
3. **@NamedEntityGraph** permet de definir des profils de chargement reutilisables sans dupliquer les requetes JPQL.
4. **DTO projections** avec `SELECT NEW` evitent de retourner des `Object[]` et offrent un typage fort.
5. **@BatchSize** reduit le nombre de requetes lors du chargement lazy de collections (@OneToMany).

---

# TP3 - Validation et Gestion des Erreurs

## Framework utilise

Spring Boot (Spring MVC + Bean Validation)

## Architecture ajoutee

```
src/main/java/com/formation/products/
├── validation/
│   ├── ValidSKU.java              (annotation custom)
│   ├── ValidSKUValidator.java
│   ├── ValidPrice.java
│   ├── ValidPriceValidator.java
│   ├── ValidDateRange.java
│   └── ValidDateRangeValidator.java
├── exception/
│   ├── ErrorResponse.java
│   ├── FieldError.java
│   ├── ProductNotFoundException.java
│   ├── DuplicateProductException.java
│   ├── InsufficientStockException.java
│   ├── CategoryNotFoundException.java
│   └── CategoryNotEmptyException.java
└── handler/
    └── GlobalExceptionHandler.java (@ControllerAdvice)
```

## Validation Implementee

### Contraintes Standards

- **Product** : `@NotBlank`, `@Size(2-200)`, `@NotNull`, `@DecimalMin("0.01")`, `@Digits(8,2)`, `@Min(0)`
- **Category** : `@NotBlank`, `@Size(2-100)`, `@Column(unique=true)`
- **Order** : `@NotBlank`, `@Size(2-100)`, `@Email`, `@PastOrPresent`
- **OrderItem** : `@NotNull`, `@Min(1)`, `@Max(1000)`, `@DecimalMin("0.01")`

### Contraintes Custom

- **@ValidSKU** : Format `^[A-Z]{3}\d{3}$` (ex: ABC123, XYZ789)
- **@ValidPrice** : Verifie max 2 decimales (rejette 99.999)
- **@ValidDateRange** : Contrainte au niveau classe sur Order, verifie `deliveryDate >= orderDate`

## Gestion des Erreurs

### @ControllerAdvice (GlobalExceptionHandler)

| Exception                          | Code HTTP | Type        |
| ---------------------------------- | --------- | ----------- |
| `MethodArgumentNotValidException`  | 400       | Validation  |
| `ConstraintViolationException`     | 400       | Validation  |
| `InsufficientStockException`       | 400       | Metier      |
| `IllegalArgumentException`         | 400       | Metier      |
| `ProductNotFoundException`         | 404       | Not Found   |
| `CategoryNotFoundException`        | 404       | Not Found   |
| `DuplicateProductException`        | 409       | Conflict    |
| `CategoryNotEmptyException`        | 409       | Conflict    |
| `Exception` (fallback)             | 500       | Generique   |

### Format de reponse uniforme

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "timestamp": "2026-02-25T17:45:10.164049",
  "path": "/api/products",
  "errors": [
    {
      "field": "price",
      "message": "Le prix doit être d'au moins 0.01",
      "rejectedValue": -10
    }
  ]
}
```

## Validations Metier (Partie 6)

- **decreaseStock** : verifie que le stock est suffisant avant de diminuer, sinon `InsufficientStockException`
- **existsBySku** : verifie l'unicite du SKU avant creation, sinon `DuplicateProductException`
- **deleteCategory** : verifie que la categorie n'a pas de produits, sinon `CategoryNotEmptyException`

## Tests Realises (TP3)

- [x] Validation echouee sur chaque champ (nom vide, prix negatif, email invalide)
- [x] Contraintes custom fonctionnelles (@ValidSKU, @ValidPrice, @ValidDateRange)
- [x] Reponses structurees (JSON avec ErrorResponse + FieldError)
- [x] Codes HTTP appropries (400, 404, 409, 500)
- [x] Messages clairs en francais
- [x] Exceptions metier gerees (stock insuffisant, doublon SKU, categorie non vide)

## Captures TP3 (dossier `captures/`)


| Fichier                                  | Livrable                              |
| ---------------------------------------- | ------------------------------------- |
| `TP3-livrable1-validation-errors.png`    | Erreurs de validation Bean            |
| `TP3-livrable2-custom-constraints.png`   | Contraintes custom (SKU, prix, dates) |
| `TP3-livrable3-structured-errors.png`    | Erreurs structurees (400/404/409/500) |
| `TP3-livrable4-business-validation.png`  | Validations metier                    |


## Difficultes Rencontrees (TP3)

1. **Validation du DTO vs entite** : la validation `@ValidDateRange` est au niveau de l'entite `Order`, mais la creation passe par un DTO `CreateOrderRequest`. Il a fallu propager les dates optionnelles du DTO vers l'entite pour que Hibernate valide la contrainte au moment du persist.
2. **ConstraintViolationException vs MethodArgumentNotValidException** : Spring MVC lance `MethodArgumentNotValidException` pour les `@RequestBody`, tandis que Hibernate Validator lance `ConstraintViolationException` pour les entites au moment du persist. Il faut gerer les deux dans le `GlobalExceptionHandler`.

## Points Cles Appris (TP3)

1. **Bean Validation** est declaratif : les annotations sur les champs suffisent, pas besoin de code de validation manuelle.
2. **@ControllerAdvice** centralise la gestion des erreurs en un seul endroit au lieu de try/catch dans chaque controller.
3. **Contraintes custom** (`@Constraint` + `ConstraintValidator`) permettent de valider des regles metier complexes (format SKU, precision prix, coherence de dates).
4. **Separation des responsabilites** : la validation declarative (@NotBlank, @Size) sur les entites, la validation metier (stock, doublons) dans les services, et la mise en forme des erreurs dans le handler.
5. **Ne jamais exposer de stack traces** : le handler generique renvoie un message generique pour les erreurs 500.
