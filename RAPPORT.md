# Rapport de Projet - Spring Products API

## 1. Presentation

Ce projet correspond a la realisation d'une API REST complete de gestion commerciale, developpee avec Spring Boot.  
L'application permet de gerer les produits, categories, fournisseurs, commandes et statistiques metier, avec persistance PostgreSQL via JPA.

L'objectif etait de mettre en pratique les acquis des TP1 a TP4: architecture en couches, principes SOLID, modelisation relationnelle, validation des donnees, gestion propre des erreurs, securisation des endpoints et fonctionnalites REST avancees.

Le resultat est une API professionnelle, testable et maintenable, avec:

- une separation claire des responsabilites (Controller, Service, Repository, Domain),
- des relations JPA robustes (avec `mappedBy` et synchronisation bidirectionnelle),
- un format d'erreur JSON standardise,
- une authentification JWT et une authorization par roles,
- une documentation OpenAPI/Swagger exploitable en demonstration.

## 2. Architecture Technique

### 2.1 Framework Utilise

Le framework retenu est **Spring Boot** (Spring MVC + Spring Data JPA + Spring Security).

Ce choix a ete fait pour:

- accelerer la mise en place d'une API REST complete,
- beneficier d'une injection de dependances simple (injection par constructeur),
- exploiter un ecosysysteme coherent pour JPA, validation, securite et documentation API.

### 2.2 Couches de l'Application

Le projet est organise en 4 couches:

- **Presentation**: controleurs REST (`controller`) et gestionnaire d'exceptions global (`handler`)
- **Application**: logique metier transactionnelle (`service`)
- **Domain**: entites JPA, DTOs, validations et exceptions metier (`model`, `validation`, `exception`)
- **Infrastructure**: acces aux donnees, securite et configuration technique (`repository`, `security`, `config`)

Flux type:

`HTTP Request -> Controller -> Service (@Transactional) -> Repository (JPA) -> PostgreSQL`

En cas d'erreur:

`Exception -> GlobalExceptionHandler -> reponse JSON ErrorResponse`

### 2.3 Technologies Utilisees

- Java 17
- Spring Boot
- Spring Web (Spring MVC)
- Spring Data JPA (Hibernate)
- Bean Validation (Jakarta Validation)
- Spring Security + JWT
- Springdoc OpenAPI / Swagger UI
- PostgreSQL
- Docker / Docker Compose
- Maven

### 2.4 Modele de Donnees

Entites principales:

- `Product`
- `Category`
- `Supplier`
- `Order`
- `OrderItem`

Relations:

- `Category (1) -> (N) Product`
- `Supplier (1) -> (N) Product`
- `Order (1) -> (N) OrderItem`
- `OrderItem (N) -> (1) Product`

Points techniques importants:

- relations JPA avec `@ManyToOne`, `@OneToMany`, `@JoinColumn`, `mappedBy`,
- cote proprietaire porte par les `@ManyToOne`,
- coherence bidirectionnelle maintenue en memoire via des methodes helper (`add/remove`) et des setters synchronises dans les entites.

## 3. Fonctionnalites Implementees

### 3.1 CRUD

CRUD complet sur les ressources principales:

- Produits: `/api/v1/products`
- Categories: `/api/v1/categories`
- Commandes: `/api/v1/orders`

Exemples:

- `POST /api/v1/products` -> creation produit (`201 Created`)
- `GET /api/v1/products/{id}` -> lecture detail (`200`)
- `PUT /api/v1/products/{id}` -> mise a jour (`200`)
- `DELETE /api/v1/products/{id}` -> suppression (`204`, role ADMIN requis)

### 3.2 Validation

Validation appliquee sur entites et DTOs:

- contraintes standards: `@NotBlank`, `@Size`, `@Email`, `@DecimalMin`, `@NotNull`, etc.
- contraintes personnalisees: `@ValidSKU`, `@ValidPrice`, `@ValidDateRange`

Exemples de cas couverts:

- payload invalide -> `400 Bad Request` avec details `errors[]`
- champs metier invalides (ex. SKU non conforme, intervalle de dates invalide)

### 3.3 Gestion des Erreurs

Gestion centralisee avec:

- `@ControllerAdvice`
- `@ExceptionHandler`
- format uniforme `ErrorResponse` (+ `FieldError` pour erreurs de validation)

Codes renvoyes selon la nature du probleme:

- `400`: validation/arguments invalides
- `401`: authentification requise ou invalide
- `403`: acces refuse
- `404`: ressource inexistante
- `409`: conflit metier (ex. SKU duplique)
- `500`: erreur interne inattendue

Point securite important:

- aucune stack trace Java n'est exposee au client HTTP,
- les details techniques sont journalises cote serveur.

### 3.4 Fonctionnalites Avancees

Fonctionnalites REST avancees implementees:

- **Pagination**: `GET /api/v1/products?page=0&size=10`
- **Swagger/OpenAPI**: `/swagger-ui.html` et `/v3/api-docs`
- **Versioning API (URI)**: coexistence `/api/`** et `/api/v1/**`
- **Securite JWT** (option valorisee):
  - login: `/api/v1/auth/login`
  - token Bearer requis sur endpoints proteges
  - regles de roles (`DELETE` reserve ADMIN)

Bonne pratique production ajoutee:

- profil `prod` avec configuration dediee (`application-prod.properties`)
- masquage des details d'erreurs HTTP et logs plus stricts.

## 4. Difficultes Rencontrees et Solutions

### Difficulte 1: Lazy loading et risque de N+1

**Probleme**: certaines lectures relationnelles pouvaient declencher des acces SQL multiples ou des erreurs hors contexte transactionnel.  
**Solution**: utilisation de `JOIN FETCH`, `EntityGraph`, et rechargement cible des entites selon les cas d'usage.

### Difficulte 2: Homogeneite des erreurs API

**Probleme**: les exceptions techniques et metier produisaient des reponses heterogenes.  
**Solution**: mise en place d'un handler global unique avec mapping explicite des exceptions vers des statuts HTTP et un schema JSON stable.

### Difficulte 3: Coherence des relations bidirectionnelles

**Probleme**: risque d'etat incoherent entre les deux cotes d'une relation (`Product.category` vs `Category.products`, idem `Supplier`).  
**Solution**: implementation de methodes helper et de setters synchronises pour maintenir automatiquement les deux cotes sans recursion.

### Difficulte 4: Separation dev/prod

**Probleme**: configuration de developpement trop verbeuse pour un contexte production (logs SQL, exposition potentielle d'informations).  
**Solution**: creation d'un profil `prod` dedie avec regles de securite d'erreurs HTTP, logs durcis et parametres externalises via variables d'environnement.

## 5. Points d'Amelioration

Avec plus de temps, les evolutions prioritaires seraient:

- ajouter une suite de tests automatises plus complete (unitaires + integration),
- introduire une migration schema versionnee (Flyway/Liquibase),
- implementer un mecanisme de rate limiting (fonctionnalite REST avancee supplementaire),
- renforcer l'observabilite (metrics, traces, dashboard),
- separer plus fortement les modeles API et les entites de persistence (DTOs systematiques).

## 6. Conclusion

Ce projet m'a permis de consolider une approche professionnelle de construction d'API REST avec Spring Boot, en allant au-dela du simple CRUD:

- architecture en couches lisible et evolutive,
- maitrise de la persistence JPA et des relations,
- validation metier fiable,
- gestion d'erreurs propre et securisee,
- securisation JWT et bonnes pratiques de configuration.

L'apprentissage principal est qu'une API de qualite ne repose pas seulement sur des endpoints qui "fonctionnent", mais sur la coherence globale: design, robustesse, securite, maintenabilite et capacite de demonstration claire en soutenance.