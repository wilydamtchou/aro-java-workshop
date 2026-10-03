# DigiBank - TP Java / Spring Boot sur Red Hat OpenShift

## Présentation

Ce dépôt contient **DigiBank**, une application bancaire Spring Boot modulaire, ainsi que le pipeline **GitHub Actions** qui la construit, la teste et la déploie sur le **Red Hat OpenShift Developer Sandbox**.

Le TP prolonge les ateliers de sécurité précédents (Workshop 3 : analyse dynamique, Workshop 4 : conteneurs, dépendances et artefacts) avec une nouvelle étape : **livrer l'application sur une plateforme Kubernetes/OpenShift de manière automatisée et reproductible**, puis vérifier le déploiement avec des tests d'API.

## Objectifs du TP

- Comprendre les concepts de base d'OpenShift : projet (namespace), BuildConfig, ImageStream, Deployment, Service, Route.
- Déployer une application Spring Boot avec la méthode **S2I (Source-to-Image)** en *binary build*, sans Dockerfile.
- Externaliser la configuration de l'application dans des variables d'environnement.
- Automatiser build, tests et déploiement avec GitHub Actions, avec un lancement manuel et le choix de la branche.
- Valider le déploiement : health check Actuator, Swagger/OpenAPI, tests d'API Newman.

## Stack technique

| Élément | Version / outil |
|---|---|
| Java | 21 (Temurin) |
| Spring Boot | 3.4.3 |
| Build | Maven (projet multi-modules) |
| Base de données (déploiement) | H2 en mémoire + Flyway |
| Documentation d'API | springdoc-openapi (Swagger UI) |
| Supervision | Spring Boot Actuator |
| Tests | JUnit 5, Cucumber, JaCoCo |
| Tests d'API | Postman / Newman |
| Plateforme | Red Hat OpenShift Developer Sandbox |
| CI/CD | GitHub Actions |

## Structure du dépôt

```text
digibank-parent/
├── common-module/
├── customer-module/
├── account-module/
├── transfer-module/
├── digibank-web/                  # application exécutable (jar Spring Boot)
├── dast/
│   └── postman/                   # collection et environnement Newman
├── .github/
│   └── workflows/
│       └── deploy-openshift.yml   # pipeline build + tests + déploiement + Newman
├── pom.xml
└── README.md
```

Le `Dockerfile` et le `docker-compose.yml` des ateliers précédents restent utilisables en local (par exemple pour PostgreSQL), mais **ils ne sont pas utilisés par le déploiement OpenShift**.

## Architecture du déploiement

```text
GitHub Actions
   │  1. build du jar (Maven)
   │  2. tests unitaires, Cucumber, JaCoCo
   ▼
oc start-build (binary build S2I)
   │  jar -> image builder ubi9/openjdk-21
   ▼
ImageStream  ->  Deployment (1 pod)  ->  Service  ->  Route HTTPS
                                                        │
                                              Newman (tests d'API)
```

Le jar est construit dans GitHub Actions, puis envoyé à OpenShift. L'image est assemblée dans le cluster par S2I à partir de l'image builder Java de Red Hat (`registry.access.redhat.com/ubi9/openjdk-21`). Cette image s'exécute avec un utilisateur non-root, comme l'exige OpenShift.

## Prérequis

### 1. Developer Sandbox activé

1. Aller sur <https://developers.redhat.com/developer-sandbox> et cliquer sur **Launch your Developer Sandbox**.
2. Se connecter avec un compte Red Hat et terminer la vérification demandée.
3. Vérifier dans la console, perspective **Developer**, la présence du projet `<utilisateur>-dev`.

Les projets du Sandbox sont créés automatiquement : il n'est pas possible d'en créer soi-même (`oc new-project` est refusé).

### 2. Secrets GitHub

Dans le dépôt : **Settings → Secrets and variables → Actions → New repository secret**.

| Secret | Description | Exemple |
|---|---|---|
| `OPENSHIFT_SERVER` | URL de l'API du cluster | `https://api.sandbox-xxxx.openshiftapps.com:6443` |
| `OPENSHIFT_TOKEN` | Token de connexion | obtenu via *Copy login command → Display Token* |
| `OPENSHIFT_NAMESPACE` | Projet de déploiement | `monutilisateur-dev` |

Le token du Sandbox expire après environ 24 heures. Quand le login échoue avec *Unauthorized*, il faut le régénérer et mettre le secret à jour.

### 3. Fichiers Newman

Les deux fichiers doivent exister à ces chemins :

- `dast/postman/DigiBank-DAST-Collection.postman_collection.json`
- `dast/postman/DigiBank-Local.postman_environment.json`

L'environnement doit contenir la variable `baseUrl`. Le pipeline la remplace par l'URL de la route OpenShift.

## Lancer le pipeline

Le pipeline se lance **uniquement à la main** :

1. Ouvrir l'onglet **Actions** du dépôt.
2. Choisir le workflow **DigiBank - Build, Tests & Deploy OpenShift Sandbox**.
3. Cliquer sur **Run workflow**, saisir la **branche** à déployer (par défaut `main`), puis valider.

Le menu « Use workflow from » désigne la branche d'où provient le fichier du workflow. Le code construit et déployé est celui de la branche saisie dans le champ `branch`.

### Étapes du pipeline

| Étape | Job | Rôle |
|---|---|---|
| 1 | `build` | Compile et empaquette le projet (`mvn package -DskipTests`) et publie le jar |
| 2 | `unit-tests` | Tests JUnit (hors Cucumber) |
| 2 | `cucumber-tests` | Scénarios Cucumber |
| 2 | `jacoco` | Rapport de couverture de code |
| 3 | `deploy` | Build S2I dans OpenShift, déploiement, probes, validation Actuator et Swagger |
| 4 | `newman` | Tests d'API sur l'application déployée |

Les étapes 2 s'exécutent en parallèle. Le déploiement ne démarre que si les trois jobs de test réussissent. Ce pipeline ne contient volontairement ni analyse statique ni scan OWASP ZAP, qui restent dans le pipeline DevSecOps complet.

## Configuration de l'application dans OpenShift

Les variables du pod sont définies à un seul endroit, dans le bloc `APP_ENV` du workflow :

```yaml
APP_ENV: |
  SPRING_PROFILES_ACTIVE=dev
  SPRING_DATASOURCE_URL=jdbc:h2:mem:digibank;DB_CLOSE_DELAY=-1
  SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver
  SPRING_DATASOURCE_USERNAME=sa
  SPRING_DATASOURCE_PASSWORD=
  SPRING_FLYWAY_LOCATIONS=classpath:db/migration/h2
  SPRING_FLYWAY_DEFAULT_SCHEMA=PUBLIC
  SPRING_FLYWAY_SCHEMAS=PUBLIC
  ...
```

Points importants :

- **Profil `dev`** : il active Swagger/OpenAPI, nécessaire pour valider le déploiement.
- **H2 à la place de PostgreSQL** : le profil `dev` est écrit pour PostgreSQL. Les variables d'environnement, prioritaires sur les fichiers `application*.yml`, redirigent le driver et les migrations Flyway vers H2, **sans modifier les fichiers de configuration**.
- **Un seul réplica** : la base H2 est en mémoire, chaque pod aurait sa propre base. La base est réinitialisée à chaque déploiement.
- **Probes** : `/actuator/health/readiness` et `/actuator/health/liveness`.
- **Ressources** : requests 100m CPU / 384Mi, limits 500m CPU / 768Mi, adaptées aux quotas du Sandbox.

Pour ajouter ou modifier une variable, il suffit d'éditer `APP_ENV`. Supprimer une ligne ne retire pas la variable du cluster : utiliser `oc set env deployment/digibank NOM_VAR-`.

## Vérifier le déploiement

À la fin du job `deploy`, le résumé du run affiche l'URL de l'application. Les vérifications automatiques sont :

- `GET /actuator/health` doit répondre avec succès.
- `GET /v3/api-docs` doit retourner une spécification OpenAPI valide.
- `GET /swagger-ui.html` doit être accessible.

Vérifications manuelles avec la CLI `oc` :

```bash
oc login --token=<token> --server=<serveur>
oc project <utilisateur>-dev

oc get pods
oc logs -f deployment/digibank
oc get route digibank
oc describe deployment/digibank
```

## Tests d'API (Newman)

La collection exécute le scénario suivant, avec des assertions sur chaque requête :

1. Création d'un client
2. Lecture du client
3. Création de deux comptes
4. Création d'un transfert

Un test commun vérifie aussi le temps de réponse (moins de 5 secondes) et l'absence de stack trace dans les réponses. Newman est lancé avec `--bail` : il s'arrête à la première requête en échec. Les rapports (`newman-report.html` et `newman-report.json`) sont publiés comme artefacts du run.

Lancer la collection en local :

```bash
newman run dast/postman/DigiBank-DAST-Collection.postman_collection.json \
  -e dast/postman/DigiBank-Local.postman_environment.json \
  --env-var "baseUrl=http://localhost:8080"
```

## Exécution en local

```bash
mvn clean install
SPRING_PROFILES_ACTIVE="" mvn spring-boot:run -pl digibank-web
```

Sans profil, l'application démarre avec H2 en mémoire. Pour PostgreSQL en local, utiliser le `docker-compose.yml` avec le profil `dev` ou `qa`.

## Dépannage

| Symptôme | Cause probable | Solution |
|---|---|---|
| `Unauthorized` au login | Token expiré | Régénérer le token et mettre à jour `OPENSHIFT_TOKEN` |
| Secret manquant | Secret non créé | Ajouter le secret indiqué dans l'erreur |
| Échec au pull de l'image builder | Tag indisponible | Vérifier `oc get is -n openshift \| grep -i openjdk`, puis adapter `BUILDER_IMAGE` |
| Le build utilise une ancienne stratégie | BuildConfig créé lors d'un essai précédent | `oc delete bc/digibank`, puis relancer |
| Pod en `Pending` | Quota du Sandbox dépassé | Réduire les ressources ou supprimer les anciennes applications |
| Pod redémarré en boucle au démarrage | Démarrage lent, CPU limité | Augmenter les délais des probes |
| `/v3/api-docs` répond 401 ou 403 | Spring Security bloque Swagger | Autoriser `/v3/api-docs/**`, `/swagger-ui/**` et `/swagger-ui.html` |
| Newman cible `localhost` | Nom de variable différent | Aligner `NEWMAN_BASE_URL_VAR` avec le fichier d'environnement |
| Application endormie | Inactivité du Sandbox | `oc scale deployment/digibank --replicas=1` ou relancer le pipeline |

## Limites du Developer Sandbox

- Environnement temporaire (environ 30 jours, renouvelable), non destiné à la production.
- Ressources et stockage limités.
- Pods mis en veille après une période d'inactivité.
- Aucun droit d'administration du cluster : pas de création de projets, pas d'opérateurs.

## Notes de sécurité

- Le profil `dev` expose Swagger, affiche les détails du health check et peut renvoyer des stack traces. L'URL de la route est publique : ne pas la partager et ne pas y déposer de données réelles.
- Aucun secret n'est stocké dans le dépôt : l'accès au cluster passe par les secrets GitHub, et les variables du pod ne contiennent aucun mot de passe sensible (base H2 en mémoire).
- Le scan Trivy du pipeline complet analyse l'image construite à partir du Dockerfile, pas l'image assemblée par S2I dans OpenShift. L'image S2I repose sur une image de base Red Hat maintenue et s'exécute en non-root.
- Pour un environnement partagé, repasser Swagger en mode désactivé (profil `qa`) et limiter le health check détaillé.

## Pour aller plus loin

- Utiliser un **ServiceAccount** dédié et un token à longue durée à la place du token personnel.
- Remplacer H2 par une vraie base PostgreSQL déployée sur le Sandbox.
- Ajouter un scan de l'image déployée et un scan OWASP ZAP sur la route OpenShift.
- Décrire le déploiement en manifestes **Helm** ou **Kustomize** versionnés dans le dépôt.
- Passer à OpenShift Pipelines (Tekton) pour exécuter le build directement dans le cluster.
