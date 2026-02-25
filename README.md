# TP1 - API REST avec Architecture en Couches

## Auteur
Fethi SEDJAI

## Description
Ce projet contient deux implémentations d'une API REST de gestion de produits :
- Jakarta EE déployée sur WildFly 31 avec PostgreSQL
- Spring Boot avec Tomcat embarqué

Les deux applications suivent une architecture en 4 couches (Presentation, Application, Infrastructure, Domain) et exposent les mêmes endpoints REST.

## Prérequis
- Java 17+
- Maven 3.9+
- Docker et Docker Compose

## Lancement Jakarta EE

```bash
cd jakarta-products-api
mvn clean package -DskipTests
docker compose up -d --build
```

Le démarrage de WildFly prend environ 15-20 secondes.

URL : http://localhost:8080/products-api/api/products

Pour arrêter :
```bash
docker compose down
```

## Lancement Spring Boot

```bash
cd spring-products-api
mvn spring-boot:run
```

Le démarrage prend 2-3 secondes.

URL : http://localhost:8081/api/products

Pour arrêter : Ctrl+C

## Endpoints REST

| Méthode | Chemin | Description | Code retour |
|---------|--------|-------------|-------------|
| GET | /api/products | Liste tous les produits | 200 |
| GET | /api/products?category=X | Filtre par catégorie | 200 |
| GET | /api/products/{id} | Récupère un produit | 200 ou 404 |
| POST | /api/products | Crée un produit | 201 |
| PUT | /api/products/{id} | Met à jour un produit | 200 ou 404 |
| PATCH | /api/products/{id}/stock | Ajuste le stock | 200 ou 404 |
| DELETE | /api/products/{id} | Supprime un produit | 204 |

Note : Pour Jakarta EE, préfixer les chemins avec /products-api

## Exemple de requête POST

```json
{
  "name": "MacBook Pro 14",
  "description": "Apple M3 Pro, 18GB RAM",
  "price": 2499.99,
  "category": "Electronics",
  "stock": 15
}
```

## Structure du projet

```
JEE-TP-01/
├── jakarta-products-api/
│   ├── src/main/java/com/formation/products/
│   │   ├── model/           # Couche Domain
│   │   ├── repository/      # Couche Infrastructure
│   │   ├── service/         # Couche Application
│   │   └── resource/        # Couche Presentation
│   ├── Dockerfile
│   └── docker-compose.yml
│
├── spring-products-api/
│   ├── src/main/java/com/formation/products/
│   │   ├── model/
│   │   ├── repository/
│   │   ├── service/
│   │   └── controller/
│   └── src/main/resources/
│
├── captures/
├── ANALYSE.md
└── README.md
```

## Difficultés rencontrées

1. Configuration de la datasource WildFly : Le module PostgreSQL nécessite des dépendances spécifiques (java.sql, java.management). La configuration du module.xml et du CLI script a demandé plusieurs ajustements.

2. DevTools Spring Boot : Le rechargement automatique surveille les fichiers .class compilés, pas les sources .java. Il faut soit activer la compilation automatique dans l'IDE, soit recompiler manuellement.

3. Qualifiers CDI pour Jakarta EE : Pour basculer entre les implémentations InMemory et JPA, il faut créer des annotations @Qualifier personnalisées, ce qui est plus verbeux que les @Profile de Spring.

## Points clés appris

1. L'architecture en couches permet de changer l'implémentation du stockage (InMemory → JPA) sans modifier les couches supérieures.

2. L'injection d'interface plutôt que d'implémentation concrète est essentielle pour le découplage et la testabilité.

3. Jakarta EE et Spring Boot permettent la même architecture avec des annotations différentes. Le choix dépend du contexte projet.

4. Les principes SOLID se traduisent concrètement dans l'organisation du code et facilitent l'évolution de l'application.

## Mode JPA (Bonus)

Jakarta EE : Le docker-compose inclut PostgreSQL. La datasource est configurée automatiquement au démarrage.

```bash
cd jakarta-products-api
docker compose up -d --build
```

Spring Boot : Lancer d'abord PostgreSQL puis l'application avec le profil JPA.

```bash
docker compose -f docker-compose-db.yml up -d
cd spring-products-api
mvn spring-boot:run -Dspring-boot.run.profiles=jpa
```

Connexion PostgreSQL :
- Host: localhost
- Port: 5432
- Database: productsdb
- User: products
- Password: products123
