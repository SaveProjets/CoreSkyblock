# Plugin CoreSkyblock

Plugin core pour toutes les instances du serveur Skyblock.

-----------------

# Convention utilisation du pubsub

## Principe

Le pubsub est un système de publication / souscription. Il permet de faire communiquer des composants entre eux sans
qu'ils aient besoin de se connaitre.

**Channel = "CoreSkyblock"**

## Cas général d'utilisation

Il sera utilisé pour notifier les autres serveurs lorsqu'une action est effectuée sur un joueur ou une île et que les
données doivent être mises à jour.
Chat multi-serveur, gestion des îles, serveur switch, /tpa cross server, etc.
Système de cache et de mise à jour des données.
Autres cas à définir.

-----------------

# Données SKYBLOCK

## Données de SYNC

### Mise en cache redis

coreskyblock:sync:{uuid} → {data}

### Fonctionnement

Mis à jour à chaque fois que le joueur quitte un serveur.
Cette information est récupérée et utilisée par les autres serveurs pour limiter l'accès à la base de données.
S'il n'y a pas de données dans la cache, le serveur doit aller chercher les données dans la base de données.
Le cache est prioritaire sur la base de données et une seule requête est faite, d'abord dans le cache, puis dans la base
de données.
Pas de donnée = données de sync vierges.

**Données corrompues = kick du joueur**

-----------------

# Répartition des îles

Plusieurs facteurs sont pris en compte pour la répartition des îles :

- Nombre d'îles chargées pour chaque serveur
- Quantité théorique de joueurs sur chaque serveur
- Nombre de joueurs connectés sur chaque serveur

-----------------

## Données ISLAND

### Mise en cache redis

coreskyblock → island:{island_uuid}:{data}

### Mise en cache du serveur qui possède l'île chargée (optionnel)

coreskyblock → island:server:{island_uuid}:{SERVER_NAME}

### Mise en cache des membres de l'île

coreskyblock → island:members:{uuid}:{island_uuid}

### Pubsub

#### Data change

coreskyblock → island:pubsub:{island_uuid}

#### Remote create

coreskyblock → island:remote_create:{server_name}:{uuid_owner(uuid)}:{uuid_island(uuid)}

### Fonctionnement

Mis à jour environ toutes les 3-5 minutes et lors d'actions importantes (création, suppression, etc).
Hors du serveur, les données sont en lecture seule pour les autres serveurs pour éviter les problèmes de concurrence.
Le cache est prioritaire sur la base de données et une seule requête est faite, d'abord dans le cache, puis dans la base
de données.
Pas de donnée = Pas d'île.

**Données corrompues = Contacter Farmeurimmo**

-----------------

## Données SKYBLOCK_USER (CACHE EN RÉFLEXION)



