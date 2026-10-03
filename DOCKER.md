# Docker - DigiBank

## 🚀 Démarrage rapide avec H2 (par défaut)

```bash
docker-compose up --build
```

- **Port**: 8080
- **Base de données**: H2 en mémoire
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **API Docs**: http://localhost:8080/v3/api-docs

Les données sont perdues au redémarrage du conteneur.

## 📊 Avec PostgreSQL (optionnel)

### 1. Activer PostgreSQL dans `.env`

```bash
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/digibankdb
SPRING_DATASOURCE_USERNAME=digibank
SPRING_DATASOURCE_PASSWORD=digibank123
```

### 2. Décommenter postgres dans `docker-compose.yml`

Ligne 20: enlever `#` du commentaire de `depends_on`

### 3. Démarrer

```bash
docker-compose up --build
```

PostgreSQL démarre automatiquement et persiste les données dans le volume `postgres_data`.

## 📝 Fichiers

| Fichier | Description |
|---------|-------------|
| **Dockerfile** | Image Spring Boot (Java 21) |
| **docker-compose.yml** | Orchestration: App + PostgreSQL optionnel |
| **.env** | Configuration des profils et base de données |
| **DOCKER.md** | Cette documentation |

## 🔧 Configuration

### Profils disponibles:
- **Vide (défaut)**: H2 en mémoire
- **dev**: PostgreSQL avec logs détaillés
- **qa**: PostgreSQL en mode production

### Variables d'environnement clés:
- `SPRING_PROFILES_ACTIVE`: Profil Spring à utiliser
- `SPRING_DATASOURCE_URL`: URL JDBC
- `SPRING_DATASOURCE_USERNAME`: Utilisateur DB
- `SPRING_DATASOURCE_PASSWORD`: Mot de passe DB

## 📦 Commandes utiles

```bash
# Démarrer en arrière-plan
docker-compose up -d --build

# Voir les logs
docker-compose logs -f app

# Arrêter
docker-compose down

# Supprimer les données PostgreSQL
docker-compose down -v

# Reconstruire
docker-compose build --no-cache
```

## ✅ Vérification

```bash
# Health check
curl http://localhost:8080/actuator/health

# API Customer
curl http://localhost:8080/api/customers/1
```

## 🐘 Notes PostgreSQL

- **Host**: postgres (interne au conteneur)
- **Port**: 5432 (exposé sur localhost:5432)
- **User**: digibank (configurable dans .env)
- **Password**: digibank123 (configurable dans .env)
- **Database**: digibankdb (configurable dans .env)
