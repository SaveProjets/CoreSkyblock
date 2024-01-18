package fr.farmeurimmo.skylyblock.purpur.trade;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.UUID;

public class Trade {

    private final UUID emitter;
    private final UUID receiver;
    private final ArrayList<ItemStack> emitterItems;
    private final ArrayList<ItemStack> receiverItems;
    private double emitterMoney;
    private double receiverMoney;
    private boolean emitterReady;
    private boolean receiverReady;

    public Trade(UUID emitter, UUID receiver) {
        this.emitter = emitter;
        this.receiver = receiver;
        this.emitterMoney = 0;
        this.receiverMoney = 0;
        this.emitterReady = false;
        this.receiverReady = false;
        this.emitterItems = new ArrayList<>();
        this.receiverItems = new ArrayList<>();
    }

    public UUID getEmitter() {
        return emitter;
    }

    public UUID getReceiver() {
        return receiver;
    }

    public double getEmitterMoney() {
        return emitterMoney;
    }

    public void setEmitterMoney(double emitterMoney) {
        this.emitterMoney = emitterMoney;
    }

    public double getReceiverMoney() {
        return receiverMoney;
    }

    public void setReceiverMoney(double receiverMoney) {
        this.receiverMoney = receiverMoney;
    }

    public boolean isEmitterReady() {
        return emitterReady;
    }

    public void setEmitterReady(boolean emitterReady) {
        this.emitterReady = emitterReady;
    }

    public boolean isReceiverReady() {
        return receiverReady;
    }

    public void setReceiverReady(boolean receiverReady) {
        this.receiverReady = receiverReady;
    }

    public ArrayList<ItemStack> getEmitterItems() {
        return emitterItems;
    }

    public ArrayList<ItemStack> getReceiverItems() {
        return receiverItems;
    }

    public void addItem(UUID uuid, ItemStack item) {
        if (uuid.equals(emitter)) {
            emitterItems.add(item);
        } else if (uuid.equals(receiver)) {
            receiverItems.add(item);
        }
    }

    public void removeItem(UUID uuid, ItemStack item) {
        if (uuid.equals(emitter)) {
            emitterItems.remove(item);
        } else if (uuid.equals(receiver)) {
            receiverItems.remove(item);
        }
    }
}
