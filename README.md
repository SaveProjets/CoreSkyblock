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

# Cross server JOUEUR

## Pubsub

### Liste des joueurs connectés (toutes les 15s)

coreskyblock → player_list{server_name}:{PlayerUUID1;PlayerName1,PlayerUUID2;PlayerName2,...}

### Tpa request

coreskyblock → tpa_request:{sender_uuid}:{sender_name}:{receiver_uuid}:{receiver_name}:{timestamp}:{is_tpa_here}:
{server_name}

### Tpa accept/yes (retour du serveur où le joueur qui a reçu la demande est connecté)

coreskyblock → tpa_accept:{tpa_type}:{sender_uuid}:{receiver_uuid}:{server_name}

### Tpa deny/no (retour du serveur où le joueur qui a reçu la demande est connecté)

coreskyblock → tpa_deny:{tpa_type}:{sender_uuid}:{receiver_uuid}:{server_name}

-----------------

# Données SKYBLOCK

## Données de SYNC

### Mise en cache données de sync

coreskyblock:sync:{uuid} → {data}

### Mise en cache données utilisateurs

coreskyblock:user:{uuid} → {data}

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

### Mise en cache d'un warp d'île

coreskyblock → island:warp:{island_uuid}

### Mise en cache du BYPASS d'un joueur sur les îles

coreskyblock → island:bypass:{uuid}

### Mise en cache du SPY d'un joueur sur les îles

coreskyblock → island:spy:{uuid}

### Pubsub

#### Data change

coreskyblock → island:pubsub:{island_uuid}:{server_name}

#### Remote create

coreskyblock → island:remote_create:{server_name}:{uuid_owner(uuid)}:{uuid_island(uuid)}

#### Remote load

coreskyblock → island:remote_load:{island_uuid}:{server_name}

#### Teleport

coreskyblock → island:teleport:{uuid}:{island_uuid}

#### Island chat to everyone

coreskyblock → island:chat_message:{island_uuid}:{server_name}:{message...}

#### Island chat to certain ranks with permission

coreskyblock → island:chat_message_with_perms:{island_uuid}:{server_name}:{permission}:{message...}

#### Island notify player

coreskyblock → island:to_player_chat:{uuid}:{server_name}:{message...}

#### Island warp update

coreskyblock → island:warp_update:{island_uuid}

#### Island coop check

coreskyblock → island:coop_check:{uuid}:{server_name}

#### Island coop check response

coreskyblock → island:coop_check_response:{uuid}:{server_name}

### Fonctionnement

Mis à jour environ toutes les 3-5 minutes et lors d'actions importantes (création, suppression, etc).
Hors du serveur, les données sont en lecture seule pour les autres serveurs pour éviter les problèmes de concurrence.
Le cache est prioritaire sur la base de données et une seule requête est faite, d'abord dans le cache, puis dans la base
de données.
Pas de donnée = Pas d'île.

**Données corrompues = Contacter Farmeurimmo**

-----------------

## Données SKYBLOCK_USERS

### Mise en cache redis

coreskyblock:user:{uuid}:{data}

-----------------

## Données AUCTIONS

### Pubsub

#### Auction create

coreskyblock → auction:create:{auction_uuid}:{server_name}

#### Auction buying

coreskyblock → auction:buy:{auction_uuid}:{buyer_uuid}:{timestamp}:{server_name}

#### Auction remove

coreskyblock → auction:remove:{auction_uuid}:{server_name}

#### Auction givemoney

Déclenché lorsqu'un joueur achète un item à l'enchéreur sur un serveur différent

coreskyblock → auction:givemoney:{player}:{auction_price}:{server_name}



