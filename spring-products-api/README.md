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


## Tests Effectues

- CRUD complet sur toutes les entites
- Relations bidirectionnelles fonctionnelles
- Transactions avec rollback (Livrable 4)
- Requetes d'agregation (Livrable 6)
- Optimisation N+1 demontree (Livrable 7)
- DTOs pour projections JPQL (CategoryStats, OrderStatusCount, MostOrderedProduct)
- @BatchSize sur Category.products
- @NamedEntityGraph (Product.withCategory, Product.full)

## Captures (dossier `captures/`)


| Fichier                             | Livrable                           |
| ----------------------------------- | ---------------------------------- |
| `TP2-livrable1-demarrage-jpa.png`   | Demarrage + profil jpa + Hibernate |
| `TP2-livrable2-tables-postgres.png` | Tables PostgreSQL + FK             |
| `TP2-livrable3-requete-join.png`    | Logs SQL avec JOIN FETCH           |
| `TP2-livrable4-rollback.png`        | Demo rollback transactionnel       |
| `TP2-livrable5-orders.png`          | Tables orders + order_items        |
| `TP2-livrable6-aggregation.png`     | Resultats requetes d'agregation    |
| `TP2-livrable7-nplus1.png`          | Logs N+1 vs 1 requete              |


## Difficultes Rencontrees

1. **ByteBuddyInterceptor / LazyInitializationException** : Jackson ne sait pas serialiser les proxies Hibernate lazy. Resolu avec `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` sur les entites.
2. **Probleme N+1** : sans JOIN FETCH, chaque acces a `product.getCategory()` declenche un SELECT supplementaire. Resolu avec des requetes JPQL utilisant `LEFT JOIN FETCH`.
3. **Serialisation circulaire** : les relations bidirectionnelles Order/OrderItem causent une boucle infinie en JSON. Resolu avec `@JsonIgnore` sur `OrderItem.order`.

## Points Cles Appris

1. **JOIN FETCH** est essentiel pour eviter le probleme N+1 en JPA.
2. **@Transactional** garantit l'atomicite : en cas d'exception, tout est annule (rollback).
3. **@NamedEntityGraph** permet de definir des profils de chargement reutilisables sans dupliquer les requetes JPQL.
4. **DTO projections** avec `SELECT NEW` evitent de retourner des `Object[]` et offrent un typage fort.
5. **@BatchSize** reduit le nombre de requetes lors du chargement lazy de collections (@OneToMany).

