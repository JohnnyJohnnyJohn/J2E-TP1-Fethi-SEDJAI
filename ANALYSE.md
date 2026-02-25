# Analyse Comparative Jakarta EE vs Spring Boot

## 4.1 Tableau de Comparaison

| Critère | Jakarta EE | Spring Boot | Observations |
|---------|------------|-------------|--------------|
| Configuration | beans.xml + annotations | application.properties + annotations | Spring Boot privilégie la convention sur la configuration |
| Annotations | @ApplicationScoped, @Inject, @Path, @GET | @Service, @Repository, @GetMapping | Concepts identiques, syntaxe différente |
| Démarrage | Serveur d'applications (WildFly) | Serveur embarqué (Tomcat) | Spring Boot démarre en ~3s contre ~15s pour WildFly |
| Packaging | WAR déployé sur serveur externe | JAR exécutable autonome | Le JAR Spring facilite le déploiement |
| Serveur | WildFly, Payara, GlassFish | Tomcat embarqué (ou Jetty, Undertow) | Jakarta EE offre plus de choix de serveurs certifiés |
| Hot Reload | Redéploiement WAR nécessaire | DevTools avec auto-restart | DevTools surveille les fichiers .class compilés |
| Simplicité | Configuration explicite | Auto-configuration | Spring Boot plus accessible pour débuter |

## 4.2 Questions de Réflexion

### 1. Architecture : Les deux applications ont-elles la même structure en couches ?

Oui, les deux applications suivent exactement la même architecture en 4 couches :

```
Presentation Layer  →  ProductResource.java / ProductController.java
Application Layer   →  ProductService.java
Infrastructure Layer →  IProductRepository.java + InMemoryProductRepository.java
Domain Layer        →  Product.java
```

Cela s'explique par le fait que l'architecture en couches est un pattern de conception indépendant du framework. Les frameworks fournissent les outils (annotations, injection) mais ne dictent pas l'organisation du code.

### 2. Dépendances : Qu'avez-vous injecté dans le Service ?

J'ai injecté l'interface `IProductRepository`, pas l'implémentation concrète `InMemoryProductRepository`.

```java
// Jakarta EE
@Inject
public ProductService(@JpaRepository IProductRepository productRepository)

// Spring Boot
public ProductService(IProductRepository productRepository)
```

Cette approche est importante pour plusieurs raisons :
- Le Service ne connaît pas l'implémentation concrète (découplage)
- On peut injecter un mock pour les tests unitaires
- On peut changer d'implémentation (InMemory → JPA) sans modifier le Service
- Elle respecte le principe d'inversion des dépendances (DIP)

### 3. SOLID : Comment chaque principe est-il respecté ?

**SRP (Single Responsibility Principle)**

Chaque classe a une responsabilité unique :
- `Product` : représente les données d'un produit
- `InMemoryProductRepository` : gère la persistance des produits
- `ProductService` : contient la logique métier (validation, règles)
- `ProductController` : gère les requêtes HTTP

**OCP (Open/Closed Principle)**

Le code est ouvert à l'extension mais fermé à la modification. J'ai pu ajouter `JpaProductRepository` sans modifier `ProductService` ni les implémentations existantes.

**LSP (Liskov Substitution Principle)**

`InMemoryProductRepository` et `JpaProductRepository` sont interchangeables car ils implémentent tous deux `IProductRepository`. Le Service fonctionne avec n'importe quelle implémentation.

**ISP (Interface Segregation Principle)**

L'interface `IProductRepository` contient uniquement les méthodes nécessaires pour les opérations sur les produits. Elle n'est pas surchargée de méthodes inutiles.

**DIP (Dependency Inversion Principle)**

Les modules de haut niveau (Service) dépendent d'abstractions (IProductRepository), pas d'implémentations concrètes. L'injection est gérée par le conteneur (CDI ou Spring).

### 4. Tests : Comment testeriez-vous ProductService sans base de données ?

Grâce à l'injection de l'interface, on peut utiliser un mock :

```java
@Test
void shouldCreateProduct() {
    IProductRepository mockRepository = mock(IProductRepository.class);
    ProductService service = new ProductService(mockRepository);
    
    Product product = new Product("Test", new BigDecimal("10.00"), "Category");
    when(mockRepository.save(any())).thenReturn(product);
    
    Product created = service.createProduct(product);
    
    assertNotNull(created);
    verify(mockRepository).save(product);
}
```

Le test est rapide et isolé, sans dépendance à une base de données.

### 5. Évolution : Si demain vous devez ajouter JPA, quelles classes devrez-vous...

**Modifier :**
- `Product.java` : ajouter les annotations @Entity, @Id, @Column
- `pom.xml` : ajouter les dépendances JPA et PostgreSQL

**Créer :**
- `JpaProductRepository.java` : nouvelle implémentation avec EntityManager
- `persistence.xml` (Jakarta EE) ou `application-jpa.properties` (Spring)

**Ne pas toucher :**
- `IProductRepository` : l'interface reste identique
- `ProductService` : dépend de l'interface, pas de l'implémentation
- `ProductResource` / `ProductController` : aucun changement nécessaire

---

## BONUS : Migration vers JPA

### Fichiers créés

| Application | Fichier | Description |
|-------------|---------|-------------|
| Jakarta EE | JpaProductRepository.java | Implémentation JPA avec EntityManager |
| Jakarta EE | persistence.xml | Configuration de l'unité de persistance |
| Jakarta EE | @JpaRepository, @InMemory | Qualifiers CDI pour choisir l'implémentation |
| Spring Boot | SpringDataProductRepository.java | Interface Spring Data JPA |
| Spring Boot | JpaProductRepositoryAdapter.java | Adapter entre notre interface et Spring Data |
| Spring Boot | application-jpa.properties | Configuration du profil JPA |

Total : 7 fichiers créés

### Fichiers modifiés

| Application | Fichier | Modification |
|-------------|---------|--------------|
| Les deux | Product.java | Annotations @Entity, @Table, @Id, @Column |
| Jakarta EE | ProductService.java | Changement du qualifier CDI |
| Jakarta EE | pom.xml | Dépendance PostgreSQL |
| Spring Boot | InMemoryProductRepository.java | Ajout @Profile("!jpa") |
| Spring Boot | pom.xml | Spring Data JPA + PostgreSQL |

Total : 6 fichiers modifiés

### Les couches supérieures ont-elles changé ?

Non. Le `ProductController` et la logique métier du `ProductService` n'ont pas été modifiés. Seule la couche Infrastructure a été étendue avec une nouvelle implémentation.

### Ce que j'ai appris sur l'architecture en couches

L'ajout de JPA a démontré concrètement l'intérêt de cette architecture :

1. Le découplage via l'interface permet d'avoir plusieurs implémentations interchangeables
2. Le principe OCP est respecté : j'ai étendu le système sans modifier le code existant
3. Les couches supérieures (Presentation, Application) sont protégées des changements techniques
4. Le basculement entre InMemory et JPA se fait via la configuration (qualifiers CDI ou profils Spring)

Le nombre de fichiers à modifier reste limité (6) et la logique métier n'a pas été impactée.
