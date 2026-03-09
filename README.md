# TP4 - Projet Final JEE (Spring Boot)

## 1) Description du projet

Ce repository contient la version finale du projet de gestion de produits realisee avec Spring Boot.
L'application expose une API REST securisee par JWT pour gerer:

- produits ;
- categories ;
- fournisseurs ;
- commandes et lignes de commande ;
- statistiques metier (JPQL).

Le scope couvre les attentes TP2, TP3 et TP4 (persistence JPA, validation, gestion d'erreurs, securite, endpoints avances).

Base URL locale: `http://localhost:8081`

---

## 2) Framework utilise et justification

### Choix

Le projet final utilise **Spring Boot** (pas Jakarta EE/WildFly).

### Justification

- Configuration plus simple pour un projet de TP (demarrage rapide, moins de boilerplate).
- Spring Data JPA accelere la couche repository.
- Securite JWT et OpenAPI/Swagger faciles a integrer.
- Bonne separation controller/service/repository pour une architecture claire.
- Productivite elevee pour livrer TP4 complet avec tests de demo.

---

## 3) Architecture

## 3.1 Architecture en 4 couches

```text
Presentation    -> src/main/java/com/formation/products/controller
                -> src/main/java/com/formation/products/handler

Application     -> src/main/java/com/formation/products/service

Domain          -> src/main/java/com/formation/products/model
                -> src/main/java/com/formation/products/validation
                -> src/main/java/com/formation/products/exception

Infrastructure  -> src/main/java/com/formation/products/repository
                -> src/main/java/com/formation/products/security
                -> src/main/java/com/formation/products/config
```

## 3.2 Flux simplifie

```text
Client HTTP
   |
   v  (validation/erreurs)
Controller (REST) -----------------------.
   |                                     |
   |                                     |
   v  (exceptions metier/techniques)     v
Service (@Transactional) -----> GlobalExceptionHandler --> JSON ErrorResponse
   |
   |
   v
Repository (JPA)
   |
   v
PostgreSQL
```

## 3.3 Modele de donnees (resume)

```text
Category (1) ----< Product (N) >---- (1) Supplier
                         |
                         v
Order (1) ----< OrderItem (N) >---- (1) Product
```

## 3.4 Principes de conception appliques

- **DIP (Dependency Inversion Principle)**:
  - Les services dependent d'abstractions (`*Repository` interfaces Spring Data) et non d'implementations concretes.
  - L'injection se fait par constructeur (pas de `new Repository...` dans les services metier).
- **Relations JPA bidirectionnelles robustes**:
  - Le cote proprietaire est porte par `@ManyToOne` + `@JoinColumn`.
  - Le cote inverse utilise `mappedBy` (`Category.products`, `Supplier.products`, `Order.items`).
  - La coherence en memoire est maintenue via des methodes helper (`add/remove`) et des setters synchronises.

---

## 4) Stack technique

- Java 17
- Spring Boot
- Spring Web
- Spring Data JPA
- Bean Validation (Jakarta Validation)
- Spring Security + JWT (jjwt)
- Springdoc OpenAPI / Swagger UI
- PostgreSQL (Docker)
- Maven

---

## 5) Endpoints disponibles

Versioning URI actif:

- Versionne: `/api/v1/**`

## 5.1 Authentification


| Methode | Endpoint          | Description             |
| ------- | ----------------- | ----------------------- |
| POST    | `/api/v1/auth/login` | Login et generation JWT |


## 5.2 Produits


| Methode | Endpoint                                         | Description                                   |
| ------- | ------------------------------------------------ | --------------------------------------------- |
| GET     | `/api/v1/products`                                  | Liste produits (filtres/pagination possibles) |
| GET     | `/api/v1/products/{id}`                             | Detail produit                                |
| POST    | `/api/v1/products`                                  | Creation produit                              |
| PUT     | `/api/v1/products/{id}`                             | Mise a jour produit                           |
| PATCH   | `/api/v1/products/{id}/stock`                       | Ajustement stock (+/-)                        |
| PATCH   | `/api/v1/products/{id}/decrease-stock?quantity={n}` | Diminution stock controlee                    |
| DELETE  | `/api/v1/products/{id}`                             | Suppression produit (ADMIN)                   |
| GET     | `/api/v1/products/slow`                             | Demo N+1 (lent)                               |
| GET     | `/api/v1/products/fast`                             | Demo optimisee                                |
| GET     | `/api/v1/products/graph/category`                   | EntityGraph partiel                           |
| GET     | `/api/v1/products/graph/full`                       | EntityGraph complet                           |


Filtres/pagination:

- `/api/v1/products?categoryId=1`
- `/api/v1/products?category=Electronics`
- `/api/v1/products?page=0&size=10`

## 5.3 Categories


| Methode | Endpoint                                 | Description                     |
| ------- | ---------------------------------------- | ------------------------------- |
| GET     | `/api/v1/categories`                        | Liste categories                |
| GET     | `/api/v1/categories/{id}`                   | Detail categorie                |
| GET     | `/api/v1/categories/{id}?withProducts=true` | Categorie + produits            |
| POST    | `/api/v1/categories`                        | Creation categorie              |
| PUT     | `/api/v1/categories/{id}`                   | Mise a jour categorie           |
| DELETE  | `/api/v1/categories/{id}`                   | Suppression categorie (si vide) |


## 5.4 Commandes


| Methode | Endpoint                            | Description             |
| ------- | ----------------------------------- | ----------------------- |
| GET     | `/api/v1/orders`                       | Liste commandes         |
| GET     | `/api/v1/orders/{id}`                  | Detail commande         |
| GET     | `/api/v1/orders?customerEmail={email}` | Filtre par email client |
| GET     | `/api/v1/orders?status={status}`       | Filtre par statut       |
| POST    | `/api/v1/orders`                       | Creation commande       |
| PATCH   | `/api/v1/orders/{id}/status`           | Changement statut       |


## 5.5 Statistiques (JPQL)


| Methode | Endpoint                                        | Description                      |
| ------- | ----------------------------------------------- | -------------------------------- |
| GET     | `/api/v1/stats/products-by-category/count`         | Nombre de produits par categorie |
| GET     | `/api/v1/stats/products-by-category/average-price` | Prix moyen par categorie         |
| GET     | `/api/v1/stats/category-stats`                     | Projection DTO CategoryStats     |
| GET     | `/api/v1/stats/top-expensive?limit=10`             | Top N produits chers             |
| GET     | `/api/v1/stats/never-ordered-products`             | Produits jamais commandes        |
| GET     | `/api/v1/stats/categories-min-products?min=1`      | Categories avec min N produits   |
| GET     | `/api/v1/stats/total-revenue`                      | CA total sur commandes DELIVERED |
| GET     | `/api/v1/stats/orders-by-status`                   | Compte commandes par statut      |
| GET     | `/api/v1/stats/most-ordered-products?limit=10`     | Produits les plus commandes      |


## 5.6 Endpoints demo/smoke


| Methode | Endpoint                      | Description                  |
| ------- | ----------------------------- | ---------------------------- |
| POST    | `/api/v1/demo/seed`              | Seed de donnees de base      |
| POST    | `/api/v1/demo/rollback`          | Test rollback transactionnel |
| POST    | `/api/v1/demo/test-repositories` | Test CRUD repository         |


## 5.7 Documentation API

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8081/v3/api-docs`

---

## 6) Securite

### AuthN/AuthZ

- Authentification JWT (`/api/v1/auth/login`).
- Tous les endpoints `/api/v1/**` sont proteges sauf login et docs swagger.
- Regle role:
  - `DELETE /api/v1/**` -> role `ADMIN`.
  - autres endpoints API -> utilisateur authentifie.

### Comptes de demo

- `user` / `user123` -> role USER
- `admin` / `admin123` -> roles USER, ADMIN

### JWT

- Type: Bearer token
- Duree de vie: 1 heure (`3600_000 ms`)
- Header attendu: `Authorization: Bearer <token>`

### Mot de passe

- Hashage via `BCryptPasswordEncoder`.

---

## 7) Validation et gestion d'erreurs

### Validation

- Bean Validation sur entites et DTOs (`@NotBlank`, `@Size`, `@Email`, `@DecimalMin`, etc.).
- Contraintes custom:
  - `@ValidSKU`
  - `@ValidPrice`
  - `@ValidDateRange`

### Erreurs metier (exemples)

- `ProductNotFoundException`
- `DuplicateProductException`
- `InsufficientStockException`
- `CategoryNotFoundException`
- `CategoryNotEmptyException`
- `SupplierNotFoundException`

### Handler global

- `@ControllerAdvice` + `@ExceptionHandler`
- Format uniforme JSON (`ErrorResponse` + `FieldError`)
- Codes correctement mappes (400/401/403/404/409/500).
- Aucune stack trace Java n'est exposee au client HTTP.
- Les details techniques restent dans les logs serveur.

---

## 8) Lancement du projet (Docker + Spring Boot)

## 8.1 Prerequis

- Java 17+
- Maven 3.9+
- Docker + Docker Compose
- `curl` + `jq` (recommande pour les scripts/tests)

## 8.2 Demarrage

Depuis la racine `JEE-TP-01`:

```bash
# 1) Lancer PostgreSQL
docker compose -f docker-compose-db.yml up -d

# 2) Verifier le conteneur
docker ps

# 3) Lancer l'API
cd spring-products-api
mvn spring-boot:run
```

Execution en profil `prod` (recommande pour verification pre-soutenance):

```bash
cd spring-products-api
SPRING_PROFILES_ACTIVE=prod mvn spring-boot:run
```

## 8.3 Arret

```bash
# stop API: Ctrl+C
cd ..
docker compose -f docker-compose-db.yml down
```

## 8.4 Configuration DB locale

- Host: `localhost`
- Port: `5432`
- Database: `productsdb`
- User: `products`
- Password: `products123`

## 8.5 Profil production (`application-prod.properties`)

Un fichier dedie production est fourni:

- `spring-products-api/src/main/resources/application-prod.properties`

Objectifs principaux:

- ne pas exposer les details internes d'erreur (`server.error.include-stacktrace=never`, etc.) ;
- reduire la verbosite des logs (pas de bind SQL en clair) ;
- desactiver Swagger UI par defaut en production ;
- utiliser des variables d'environnement pour la DB.

Variables d'environnement supportees:

- `PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `DB_POOL_MAX_SIZE`
- `DB_POOL_MIN_IDLE`

Exemple lancement JAR:

```bash
java -jar target/spring-products-api-*.jar --spring.profiles.active=prod
```

---

## 9) Conformite TP04 (attendu vs etat courant)

## 9.1 Checklist technique TP04 - 1.1


| Exigence                      | Etat           | Evidence                                                                   |
| ----------------------------- | -------------- | -------------------------------------------------------------------------- |
| 4 couches separees            | OK             | `controller/service/model+validation+exception/repository+security+config` |
| Interfaces repositories       | OK             | Spring Data `JpaRepository`                                                |
| Injection de dependances      | OK             | Injection par constructeur                                                 |
| Principes SOLID               | OK             | Separation des responsabilites claire                                      |
| DIP applique                  | OK             | Services -> abstractions repository                                        |
| Entites JPA + relations       | OK             | `Product`, `Category`, `Supplier`, `Order`, `OrderItem`                    |
| `mappedBy` correctement utilise | OK            | `Category.products`, `Supplier.products`, `Order.items`                    |
| JPQL optimise avec JOIN FETCH | OK             | Requetes `LEFT JOIN FETCH`, EntityGraph                                    |
| Transactions `@Transactional` | OK             | Services transactionnels                                                   |
| Bean Validation               | OK             | Annotations sur entites/DTOs                                               |
| Contrainte personnalisee      | OK             | `@ValidSKU`, `@ValidPrice`, `@ValidDateRange`                              |
| Handler global erreurs        | OK             | `@ControllerAdvice`                                                        |
| Erreurs JSON structurees      | OK             | `ErrorResponse`                                                            |
| Fuite stack trace HTTP        | OK             | Reponses API sans details techniques internes                               |
| Authentication (JWT)          | OK             | `/api/v1/auth/login`, filtre JWT                                            |
| Authorization par roles       | OK             | DELETE reserve ADMIN                                                       |
| Passwords hashes              | OK             | BCrypt                                                                     |
| HTTPS                         | Non            | Non implemente (API exposee en HTTP local uniquement)                      |
| Pagination                    | OK             | `/api/v1/products?page=0&size=10`                                          |
| OpenAPI/Swagger               | OK             | `/swagger-ui.html`, `/v3/api-docs`                                         |
| Versioning API                | OK             | URI versioning via `/api/v1/**`                                             |
| Caching/Rate limiting         | Non implemente | Optionnel TP04                                                             |


Conclusion TP04 REST avance: **OK** (2/4 atteints: pagination + Swagger).

## 9.2 Tests a effectuer TP04 - 1.2

Plan de tests recommande (Yaak/Thunder Client/curl):

1. Login USER et ADMIN -> `200` + token JWT.
2. Acces anonyme a endpoint protege -> `401`.
3. CRUD produit complet:
  - create `201`
  - read `200`
  - update `200`
  - delete USER `403`, delete ADMIN `204`.
4. Validation:
  - payload invalide -> `400` + `errors[]`.
5. Erreurs metier:
  - stock insuffisant -> `400`
  - SKU duplique -> `409`
  - categorie non vide a la suppression -> `409`.
6. Relations/JPA:
  - create produit avec `category.id`
  - lecture sans erreur lazy loading.
7. Pagination:
  - `/api/v1/products?page=0&size=10` -> payload `content/totalElements/totalPages`.
8. Swagger:
  - UI accessible + bouton Authorize.

Script smoke test disponible:

```bash
cd spring-products-api
./pre-soutenance-check.sh
```

Avec URL custom:

```bash
BASE_URL=http://localhost:8081 ./pre-soutenance-check.sh
```

## 9.3 Qualite du code TP04 - 1.3

Actions appliquees:

- cleanup code/commentaires inutiles ;
- imports explicites ;
- renommage methodes/variables ambigues ;
- JavaDoc ajoutee sur methodes publiques complexes ;

Verification:

- `mvn -DskipTests compile` OK
- lints IDE: OK

---

## 10) Difficultes rencontrees et solutions

1. **LazyInitializationException / proxies Hibernate**
  - Cause: relations lazy hors session avec `open-in-view=false`.
  - Solution: `JOIN FETCH`, `EntityGraph`, rechargement entites avant serialization.
2. **Validation a deux niveaux (DTO et entites)**
  - Cause: certaines erreurs captees tardivement.
  - Solution: `@Valid` sur DTOs + gestion conjointe `MethodArgumentNotValidException` et `ConstraintViolationException`.
3. **Homogeneite des erreurs API**
  - Cause: exceptions techniques/metier heterogenes.
  - Solution: exceptions metier dediees + handler global + format `ErrorResponse`.
4. **Securite JWT**
  - Cause: confusion entre erreurs auth attendues et erreurs serveur.
  - Solution: handlers dedies 401 (`BadCredentialsException`, `AuthenticationException`) + configuration stateless.
5. **N+1 et performances JPA**
  - Cause: chargement relationnel non optimise.
  - Solution: endpoints de comparaison (`slow/fast`) + fetch optimise en production.

---

