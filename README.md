# DigiBank - TP Java / Spring Boot sur Red Hat OpenShift

## Présentation

Ce dépôt contient **DigiBank**, une application bancaire Spring Boot modulaire, ainsi que le pipeline **GitHub Actions** qui la construit, la teste et la déploie sur le **Red Hat OpenShift Developer Sandbox**.

Le TP prolonge les ateliers de sécurité précédents (Workshop 3 : analyse dynamique, Workshop 4 : conteneurs, dépendances et artefacts) avec une nouvelle étape : **livrer l'application sur une plateforme Kubernetes/OpenShift de manière automatisée et reproductible**, puis vérifier le déploiement avec des tests d'API.

## Objectifs du TP

- Comprendre les concepts de base d'OpenShift : projet (namespace), BuildConfig, ImageStream, Deployment, Service, Route.
- Déployer une application Spring Boot avec la méthode **S2I (Source-to-Image)** en *binary build*, sans Dockerfile.
- Externaliser la configuration : paramètres non sensibles dans une **ConfigMap**, identifiants dans un **Secret** OpenShift.
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
                    ▲                                   │
         ConfigMap + Secret                   Newman (tests d'API)
```

Le jar est construit dans GitHub Actions, puis envoyé à OpenShift. L'image est assemblée dans le cluster par S2I à partir de l'image builder Java de Red Hat (`registry.access.redhat.com/ubi9/openjdk-21`). Cette image s'exécute avec un utilisateur non-root, comme l'exige OpenShift.

## Prérequis

### 1. Developer Sandbox activé

1. Aller sur <https://developers.redhat.com/developer-sandbox> et cliquer sur **Launch your Developer Sandbox**.
2. Se connecter avec un compte Red Hat et terminer la vérification demandée.
3. Vérifier dans la console, perspective **Developer**, la présence du projet `<utilisateur>-dev`.

Les projets du Sandbox sont créés automatiquement : il n'est pas possible d'en créer soi-même (`oc new-project` est refusé).

### 2. Secret et variables GitHub

Dans le dépôt : **Settings → Secrets and variables → Actions**.

**Onglet Secrets → New repository secret**

| Secret | Description | Exemple |
|---|---|---|
| `OPENSHIFT_TOKEN` | Token de connexion | obtenu via *Copy login command → Display Token* |
| `DB_PASSWORD` (optionnel) | Mot de passe de la base, stocké ensuite dans le Secret OpenShift | vide par défaut (H2 en mémoire l'accepte) |

**Onglet Variables → New repository variable**

| Variable | Description | Exemple |
|---|---|---|
| `OPENSHIFT_SERVER` | URL de l'API du cluster | `https://api.sandbox-xxxx.openshiftapps.com:6443` |
| `OPENSHIFT_NAMESPACE` | Projet de déploiement | `monutilisateur-dev` |

Le serveur et le namespace sont volontairement des **variables** et non des secrets. Ce ne sont pas des données sensibles, et GitHub masque dans les logs et dans les outputs de jobs toute chaîne qui correspond à la valeur d'un secret. Comme l'URL de la route contient le nom du projet (`digibank-<namespace>.apps...`), un namespace stocké en secret rendrait l'URL vide pour le job Newman.

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
| 3 | `deploy` | Build S2I dans OpenShift, création de la ConfigMap et du Secret, déploiement, probes, validation Actuator et Swagger |
| 4 | `newman` | Tests d'API sur l'application déployée |

L'URL de la route est transmise au job `newman` par un artefact (`app-url`) en plus de l'output du job `deploy`.

Les étapes 2 s'exécutent en parallèle. Le déploiement ne démarre que si les trois jobs de test réussissent. Ce pipeline ne contient volontairement ni analyse statique ni scan OWASP ZAP, qui restent dans le pipeline DevSecOps complet.

## Configuration de l'application dans OpenShift

La configuration est séparée en deux objets OpenShift, créés ou mis à jour à chaque run puis injectés dans le pod :

| Objet | Contenu | Source dans le workflow |
|---|---|---|
| ConfigMap `digibank-config` | profil, URL de la base, driver, Flyway, logs, port, mémoire JVM | bloc `APP_ENV` |
| Secret `digibank-secret` | `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` | `DB_USERNAME` (workflow) et secret GitHub `DB_PASSWORD` |

Les noms des deux objets sont définis par `CONFIGMAP_NAME` et `SECRET_NAME` dans le workflow.

```yaml
APP_ENV: |
  SPRING_PROFILES_ACTIVE=dev
  SPRING_DATASOURCE_URL=jdbc:h2:mem:digibank;DB_CLOSE_DELAY=-1
  SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.h2.Driver
  SPRING_FLYWAY_LOCATIONS=classpath:db/migration/h2
  SPRING_FLYWAY_DEFAULT_SCHEMA=PUBLIC
  SPRING_FLYWAY_SCHEMAS=PUBLIC
  ...
```

Déroulement dans le job `deploy` :

1. L'étape « ConfigMap et Secret » écrit `APP_ENV` dans la ConfigMap et les identifiants dans le Secret (`oc create ... --dry-run=client -o yaml | oc apply -f -`, rejouable à chaque run). Les fichiers temporaires sont supprimés ensuite.
2. L'étape « Configurer l'application » retire d'abord les anciennes variables écrites en dur sur le Deployment (elles auraient priorité sur la ConfigMap et le Secret), puis injecte les deux objets avec `oc set env --from`.
3. `oc rollout restart` redémarre le pod : modifier une ConfigMap ou un Secret ne redémarre pas les pods automatiquement.

Points importants :

- **Profil `dev`** : il active Swagger/OpenAPI, nécessaire pour valider le déploiement.
- **H2 à la place de PostgreSQL** : le profil `dev` est écrit pour PostgreSQL. Les variables d'environnement, prioritaires sur les fichiers `application*.yml`, redirigent le driver et les migrations Flyway vers H2, **sans modifier les fichiers de configuration**.
- **Un seul réplica** : la base H2 est en mémoire, chaque pod aurait sa propre base. La base est réinitialisée à chaque déploiement.
- **Probes** : `/actuator/health/readiness` et `/actuator/health/liveness`.
- **Ressources** : requests 100m CPU / 384Mi, limits 500m CPU / 768Mi, adaptées aux quotas du Sandbox.

Pour ajouter un paramètre non sensible, il suffit d'ajouter une ligne dans `APP_ENV`. Pour une valeur sensible, il faut l'ajouter au Secret (ligne `printf` de l'étape « ConfigMap et Secret ») et à la liste de nettoyage de l'étape « Configurer l'application ».

Une clé retirée de `APP_ENV` reste dans la ConfigMap existante : supprimer la ConfigMap (`oc delete configmap digibank-config`) avant de relancer le pipeline.

## Stratégie de déploiement

Le pipeline ne définit pas de stratégie explicite : le Deployment créé par `oc new-app` utilise celle par défaut, **RollingUpdate**.

1. Le build S2I publie une nouvelle image dans l'ImageStream.
2. `oc rollout restart` démarre un nouveau pod à côté de l'ancien.
3. Quand la readiness probe du nouveau pod réussit, le trafic bascule, puis l'ancien pod est arrêté.
4. `oc rollout status` attend la fin de ce processus. Si le nouveau pod ne devient jamais prêt, l'ancien reste en service et le job échoue.

À savoir :

- La base H2 est en mémoire et propre à chaque pod : **chaque déploiement repart d'une base vide**.
- Pendant la bascule, deux pods coexistent brièvement. Si le quota du Sandbox est trop juste, le nouveau pod reste en `Pending`. Dans ce cas, passer en stratégie `Recreate` (courte coupure, une seule instance à la fois) :

```bash
oc patch deployment/digibank -p '{"spec":{"strategy":{"type":"Recreate"}}}'
```

- Il n'y a ni blue/green ni canary : une seule version tourne à la fois.

La stratégie réellement appliquée se vérifie avec `oc get deployment digibank -o jsonpath='{.spec.strategy}'`.

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

oc get configmap digibank-config -o yaml
oc get secret digibank-secret
oc set env deployment/digibank --list
```

## Tests d'API (Newman)

La collection exécute le scénario suivant, avec des assertions sur chaque requête :

1. Création d'un client
2. Lecture du client
3. Création de deux comptes
4. Création d'un transfert

Un test commun vérifie aussi le temps de réponse (moins de 5 secondes) et l'absence de stack trace dans les réponses. Newman est lancé avec `--bail` : il s'arrête à la première requête en échec. L'URL cible est injectée avec `--env-var "baseUrl=<url de la route>"`, ce qui remplace la valeur `localhost` du fichier d'environnement. Les rapports (`newman-report.html` et `newman-report.json`) sont publiés comme artefacts du run.

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
| Secret ou variable manquant(e) | Élément non créé, ou créé dans le mauvais onglet | Créer le secret (`OPENSHIFT_TOKEN`) ou la variable (`OPENSHIFT_SERVER`, `OPENSHIFT_NAMESPACE`) indiqué dans l'erreur |
| Newman : `Invalid URI "http:///api/customers"` | URL vide : le namespace ou le serveur est encore stocké en secret et masqué par GitHub | Les créer comme variables, supprimer les anciens secrets du même nom, relancer |
| Échec au pull de l'image builder | Tag indisponible | Vérifier `oc get is -n openshift \| grep -i openjdk`, puis adapter `BUILDER_IMAGE` |
| Le build utilise une ancienne stratégie | BuildConfig créé lors d'un essai précédent | `oc delete bc/digibank`, puis relancer |
| Une valeur modifiée dans `APP_ENV` n'est pas prise en compte | Ancienne variable littérale sur le Deployment, ou clé obsolète dans la ConfigMap | Vérifier avec `oc set env deployment/digibank --list`, supprimer la ConfigMap et relancer |
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
- Aucun secret n'est stocké dans le dépôt : le token d'accès au cluster est un secret GitHub (le serveur et le namespace, non sensibles, sont des variables), les paramètres non sensibles sont dans une ConfigMap et les identifiants de la base dans un Secret OpenShift. Un Secret n'est qu'encodé en base64 : toute personne ayant accès au projet peut le lire, ce n'est pas du chiffrement.
- Le scan Trivy du pipeline complet analyse l'image construite à partir du Dockerfile, pas l'image assemblée par S2I dans OpenShift. L'image S2I repose sur une image de base Red Hat maintenue et s'exécute en non-root.
- Pour un environnement partagé, repasser Swagger en mode désactivé (profil `qa`) et limiter le health check détaillé.

## Pour aller plus loin

- Utiliser un **ServiceAccount** dédié et un token à longue durée à la place du token personnel.
- Remplacer H2 par une vraie base PostgreSQL déployée sur le Sandbox.
- Ajouter un scan de l'image déployée et un scan OWASP ZAP sur la route OpenShift.
- Décrire le déploiement en manifestes **Helm** ou **Kustomize** versionnés dans le dépôt.
- Passer à OpenShift Pipelines (Tekton) pour exécuter le build directement dans le cluster.
