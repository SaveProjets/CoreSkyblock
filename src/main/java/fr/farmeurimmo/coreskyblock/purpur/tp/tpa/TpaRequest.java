package fr.farmeurimmo.coreskyblock.purpur.tp.tpa;

import java.util.UUID;

public record TpaRequest(UUID sender, String senderName, UUID receiver, String receiverName, long timestamp,
                         boolean isTpaHere) {

}
