package fr.farmeurimmo.mineblock.purpur.tpa;

import java.util.UUID;

public record TpaRequest(UUID sender, UUID receiver, long timestamp, boolean isTpaHere) {

}
