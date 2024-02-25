package fr.farmeurimmo.coreskyblock.purpur.chests;

import fr.farmeurimmo.coreskyblock.purpur.islands.IslandsManager;
import fr.farmeurimmo.coreskyblock.storage.islands.Island;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class Chest {

    private final UUID uuid;
    private final UUID islandId;
    private final ChestType type;
    private final Location block;
    private ItemStack itemToBuySell;
    private double price;
    private boolean isSell;
    private boolean activeSellOrBuy;
    private long amountOfStackedBlocks;

    public Chest(UUID uuid, UUID islandId, ChestType type, Location block, ItemStack itemToBuySell, double price,
                 boolean isSell, boolean activeSellOrBuy, long amountOfStackedBlocks) {
        this.uuid = uuid;
        this.islandId = islandId;
        this.type = type;
        this.block = block;
        this.itemToBuySell = itemToBuySell;
        this.price = price;
        this.isSell = isSell;
        this.activeSellOrBuy = activeSellOrBuy;
        this.amountOfStackedBlocks = amountOfStackedBlocks;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getIslandId() {
        return islandId;
    }

    public ChestType getType() {
        return type;
    }

    public Location getBlock() {
        if (block.getWorld() == null) {
            Island island = IslandsManager.INSTANCE.getIslandByUUID(islandId);
            if (island != null && island.isLoaded()) {
                block.setWorld(IslandsManager.INSTANCE.getIslandWorld(island.getIslandUUID()));
            }
        }
        return block;
    }

    public ItemStack getItemToBuySell() {
        return itemToBuySell;
    }

    public void setItemToBuySell(ItemStack itemToBuySell) {
        this.itemToBuySell = itemToBuySell;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isSell() {
        return isSell;
    }

    public void setSell(boolean sell) {
        isSell = sell;
    }

    public boolean isActiveSellOrBuy() {
        return activeSellOrBuy;
    }

    public String getSellOrBuy() {
        return isSell ? "§aVente" : "§cAchat";
    }

    public String getActiveSellOrBuy() {
        return activeSellOrBuy ? "§aActivé" : "§cDésactivé";
    }

    public void setActiveSellOrBuy(boolean activeSellOrBuy) {
        this.activeSellOrBuy = activeSellOrBuy;
    }

    public String getActiveSellOrBuyColor() {
        return activeSellOrBuy ? "§a" : "§c";
    }

    public long getAmountOfStackedBlocks() {
        return amountOfStackedBlocks;
    }

    public void setAmountOfStackedBlocks(long amountOfStackedBlocks) {
        this.amountOfStackedBlocks = amountOfStackedBlocks;
    }


}
