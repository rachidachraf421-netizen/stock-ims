# StockIMS - Gestion de stock avec Spring Boot

Projet des séances 9 et 10 (Java et programmation orientée objet, ESITH, filière IMS).
Application web de gestion de stock : articles, entrées, sorties et stock calculé.

## Participants

| Étudiant | Nom et prénom |
|---|---|
| Étudiant 1 | ABIA Abderrahman |
| Étudiant 2 | RACHID Achraf |

## Technologies

Java 25, Spring Boot 4.1.1 (Spring Web MVC, Spring Data JPA, Validation), MySQL 8.4 local,
Thymeleaf, Lombok, Maven Wrapper. Interface en HTML, CSS et JavaScript.

## Lancer l'application

1. **Démarrer MySQL** (service Windows `MySQL84`, port 3306).
2. **Créer la base et le compte applicatif** dans le client MySQL (une seule fois) :
   ```sql
   CREATE DATABASE stock_ims CHARACTER SET utf8mb4;
   CREATE USER 'stock_app'@'localhost' IDENTIFIED BY 'VOTRE_MOT_DE_PASSE';
   GRANT ALL PRIVILEGES ON stock_ims.* TO 'stock_app'@'localhost';
   ```
3. **Adapter les paramètres MySQL** (voir la section suivante).
4. **(Facultatif) Importer les données de test** : `mysql -u root -p < database/stockims_export.sql`
   (à faire sur une base vide ; sinon l'application crée elle-même les tables vides).
5. **Lancer le projet**, depuis la racine (dossier qui contient `pom.xml`) :
   ```powershell
   .\mvnw.cmd spring-boot:run
   ```
   macOS / Linux : `chmod +x mvnw` puis `./mvnw spring-boot:run`.
   Sous IntelliJ : ouvrir `StockImsApplication.java` et cliquer sur le triangle vert.
6. Ouvrir <http://localhost:8080/>. Arrêter avec `Ctrl + C`.

Lancer les tests (ils n'ont pas besoin de MySQL) : `.\mvnw.cmd test`

## Paramètres MySQL à adapter

Fichier `src/main/resources/application.properties` :

| Paramètre | Valeur par défaut | À adapter si... |
|---|---|---|
| `spring.datasource.url` | `jdbc:mysql://localhost:3306/stock_ims` | le port ou le nom de la base diffère |
| `spring.datasource.username` | `stock_app` | vous utilisez un autre compte |
| `spring.datasource.password` | `VOTRE_MOT_DE_PASSE` | **toujours** : mettez le mot de passe choisi dans `CREATE USER` |

On peut aussi définir les variables d'environnement `DB_URL`, `DB_USER` et `DB_PASSWORD`
au lieu de modifier le fichier (utile pour ne pas publier un mot de passe sur GitHub).
`ddl-auto=update` crée les tables `article`, `entree` et `sortie` au premier démarrage.

Si MySQL répond « Public Key Retrieval is not allowed », ajouter `?allowPublicKeyRetrieval=true`
à la fin de l'URL JDBC.

## Fonctionnalités

- Ajouter des articles et consulter leur liste (avec le stock de chacun).
- Fiche d'un article : stock, historique de ses entrées et sorties.
- Enregistrer une entrée ou une sortie pour un article.
- Historique de tous les mouvements.
- **Stock = somme des entrées − somme des sorties.** Il n'est jamais stocké : il est recalculé
  à partir des mouvements. Un article sans mouvement a un stock de 0.
- Refus (côté serveur) : champs obligatoires vides, référence en double (sans tenir compte de la
  casse), quantité qui n'est pas un entier strictement positif, sortie supérieure au stock.

## Organisation du code

| Package | Contenu |
|---|---|
| `model` | `Article`, `MouvementStock` (classe abstraite, `@MappedSuperclass`), `Entree`, `Sortie` |
| `repository` | `ArticleRepository`, `EntreeRepository`, `SortieRepository` (interfaces Spring Data) |
| `service` | `ArticleService`, `MouvementService`, `StockService` (calcul du stock), `ArticleStock` |
| `exception` | `StockInsuffisantException`, `ReferenceDejaUtiliseeException`, `ArticleIntrouvableException` |
| `web` | `ArticleController`, `MouvementController`, `ArticleForm`, `MouvementForm` |
| racine | `StockImsApplication`, `AccueilController`, `MessageAccueil` |

Choix conformes au guide : attributs `private`, pas de `@Setter` sur les entités, constructeur
métier qui contrôle les valeurs, constructeur vide `protected` pour JPA, classe abstraite
`MouvementStock` avec `quantiteSignee()` redéfinie par `Entree` (+) et `Sortie` (−),
trois tables seulement (`article`, `entree`, `sortie`).

### Comment le stock est calculé

`StockService.stockDe(article)` additionne les quantités de la table `entree` pour l'article, puis
celles de la table `sortie`, et renvoie la différence (0 si une somme est vide).
Pour la liste des articles, `stocksParArticle()` fait le même calcul pour tous les articles en deux
requêtes `GROUP BY`. `MouvementService.enregistrerSortie` compare la quantité demandée au stock
disponible et lève `StockInsuffisantException` si elle le dépasse ; la ligne de l'article est
verrouillée (`PESSIMISTIC_WRITE`) pour que deux sorties simultanées ne dépassent pas le stock.

### Validation à trois niveaux

1. Formulaire (`@Valid`, `ArticleForm`, `MouvementForm`) : champs vides, tailles, entier ≥ 1.
2. Objet métier : les constructeurs de `Article` et `MouvementStock` refusent les valeurs invalides.
3. Base de données : contrainte `UNIQUE` sur `article.reference`, clés étrangères `article_id`.

Les formulaires ont l'attribut `novalidate` : le navigateur n'intercepte rien, c'est toujours le
serveur qui valide, avec ou sans JavaScript.

## Personnalisation de l'interface

**Identité.** L'application s'inspire de l'univers du textile (filière de l'ESITH) :
indigo de teinture pour l'en-tête et les titres, **safran** pour le « point de couture » (le trait
pointillé sous l'en-tête et dans le logo), **vert-de-gris** pour les entrées et **garance**
(rouge) pour les sorties et les erreurs. Le fond est un gris toile clair. Les encadrés
« Calcul du stock » (accueil) et « en stock » (fiche article) imitent des étiquettes cousues
(bord pointillé).

**HTML (Thymeleaf).**
- Un fragment commun `fragments/layout.html` (en-tête, navigation, messages, pied de page) évite
  de répéter le code ; la page active est signalée dans le menu.
- Formulaires avec `label` associés, aides de saisie, messages d'erreur sous chaque champ
  (`th:errors`), valeurs conservées après une erreur.
- Tableaux avec en-têtes, page de détail d'un article, pages d'erreur 404 et générale en français.
- Message de confirmation après chaque enregistrement (attribut flash).

**CSS** (`static/css/style.css`).
- Variables de couleurs et de polices en tête de fichier ; titres en serif (Palatino/Georgia),
  texte en sans-serif système (aucune ressource externe : fonctionne hors ligne).
- Boutons (indigo, vert pour une entrée, rouge pour une sortie), tableaux, étiquettes de stock
  (rouge quand le stock est nul), messages de succès et d'erreur, champs invalides entourés en rouge.
- **Responsive** : sous 760 px, le menu se replie derrière un bouton et chaque ligne de tableau
  devient une carte (le libellé de chaque colonne est repris via `data-label`).
- Focus clavier visible, contrastes soignés, animations désactivées si le système le demande.

**JavaScript** (`static/js/app.js`, sans bibliothèque). Quatre interactions, toutes facultatives :
1. **Filtre de recherche** dans la liste des articles et dans l'historique (insensible à la casse et
   aux accents, avec compteur et message « aucun résultat »).
2. **Confirmation avant une sortie** : une boîte de dialogue rappelle l'article et la quantité.
3. **Stock disponible affiché** dès qu'on choisit un article dans le formulaire ; pour une sortie,
   un avertissement apparaît si la quantité saisie dépasse le stock (le serveur reste seul juge).
4. **Menu replié** sur petit écran.

Sans JavaScript, tout fonctionne : les validations sont faites par le serveur.

## Vérifications à faire (scénario du sujet)

1. Créer l'article `TEST-001` (ou utiliser `TIS-001` d'une base vide) : stock **0**.
2. Entrée de **20** : stock **20**.
3. Sortie de **7** : stock **13**.
4. Sortie de **15** : **refusée** (« Sortie refusée : le stock disponible est de 13, la quantité
   demandée est de 15. »), le stock reste **13**.
5. Quantité **0**, **−4**, **2,5** ou **abc** : refusée avec un message.
6. Référence déjà utilisée (même en minuscules) : refusée.
7. Arrêter puis relancer l'application : les articles et le stock sont conservés (données dans MySQL).

Les tests unitaires (`src/test`) couvrent les règles métier : constructeurs, signe des mouvements,
calcul du stock, refus de la sortie de 15 quand le stock est 13.

## Captures d'écran

Dossier `docs/captures/` : `01-interface.png` (interface personnalisée), `02-stock-calcule.png`
(fiche article avec stock 13 et historique), `03-message-validation.png` (sortie refusée).

## Structure du dépôt

```
stock-ims/
├── pom.xml, mvnw, mvnw.cmd, .mvn/        Maven et Maven Wrapper
├── database/stockims_export.sql          Structure et données de test
├── docs/captures/                        Captures d'écran
├── src/main/java/ma/esith/stockims/      Code Java
├── src/main/resources/
│   ├── application.properties            Paramètres MySQL
│   ├── messages.properties               Messages d'erreur de saisie
│   ├── templates/                        Pages Thymeleaf
│   └── static/css, static/js             Feuille de style et script
└── src/test/java/                        Tests unitaires
```
