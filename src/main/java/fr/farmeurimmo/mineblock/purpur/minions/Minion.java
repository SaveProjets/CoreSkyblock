package fr.farmeurimmo.mineblock.purpur.minions;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.UUID;

public class Minion {

    private final UUID id;
    private final UUID islandId;
    private final MinionType type;
    private Location minionLocation;
    private BlockFace blockFace;
    private Block chestBlock;
    private boolean autoSmelt;
    private int level;

    public Minion(UUID id, UUID islandId, MinionType type, Location minionLocation, BlockFace blockFace,
                  Block chestBlock, boolean autoSmelt, int level) {
        this.id = id;
        this.islandId = islandId;
        this.type = type;
        this.minionLocation = minionLocation;
        this.blockFace = blockFace;
        this.chestBlock = chestBlock;
        this.autoSmelt = autoSmelt;
        this.level = level;
    }

    public UUID getId() {
        return id;
    }

    public UUID getIslandId() {
        return islandId;
    }

    public MinionType getType() {
        return type;
    }

    public Location getMinionLocation() {
        return minionLocation;
    }

    public void setMinionLocation(Location minionLocation) {
        this.minionLocation = minionLocation;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    public void setBlockFace(BlockFace blockFace) {
        this.blockFace = blockFace;
    }

    public Block getChestBlock() {
        return chestBlock;
    }

    public void setChestBlock(Block chestBlock) {
        this.chestBlock = chestBlock;
    }

    public boolean isAutoSmelt() {
        return autoSmelt;
    }

    public void setAutoSmelt(boolean autoSmelt) {
        this.autoSmelt = autoSmelt;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

}
