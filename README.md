# Plugin MineBlock

# Convention utilisation du pubsub

## Principe

Le pubsub est un système de publication / souscription. Il permet de faire communiquer des composants entre eux sans
qu'ils aient besoin de se connaitre.

**Channel = "Mineblock"**

## SkyblockUser

### Après une mise à jour de celui-ci

    skyblockuser:<from>:update:<uuid>

### Après une création de celui-ci

    skyblockuser:<from>:create:<uuid>

## SkyblockIsland

### Après une mise à jour de celle-ci

    skyblockisland:<from>:update:<uuid>

### Après une création de celle-ci

    skyblockisland:<from>:create:<uuid>

En rédaction...

