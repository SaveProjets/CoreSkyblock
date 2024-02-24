package fr.farmeurimmo.coreskyblock.purpur.tpa;

import java.util.UUID;

public record TpaRequest(UUID sender, UUID receiver, long timestamp, boolean isTpaHere) {

}
