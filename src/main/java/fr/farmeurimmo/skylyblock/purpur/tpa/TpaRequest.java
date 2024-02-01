package fr.farmeurimmo.skylyblock.purpur.tpa;

import java.util.UUID;

public record TpaRequest(UUID sender, UUID receiver, long timestamp, boolean isTpaHere) {

}
