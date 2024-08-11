package fr.farmeurimmo.coreskyblock.purpur.items.sacs;

import fr.mrmicky.fastinv.FastInv;
import fr.mrmicky.fastinv.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SacsAdminInv extends FastInv {

    private int amountInIt = 0;

    public SacsAdminInv() {
        super(6 * 9, "§0Liste des sacs");

        update();
    }

    private void update() {
        int slot = 0;
        for (SacsType sacsType : SacsType.values()) {
            setItem(slot, SacsManager.INSTANCE.createSacs(sacsType, amountInIt), e -> {
                Player p = (Player) e.getWhoClicked();
                if (p.getInventory().firstEmpty() == -1) {
                    p.sendMessage("§cVous n'avez pas de place dans votre inventaire.");
                } else {
                    p.getInventory().addItem(SacsManager.INSTANCE.createSacs(sacsType, amountInIt));
                    p.sendMessage("§aVous avez reçu un sac de " + sacsType.getName().toLowerCase() + ".");
                }
            });
            slot++;
        }

        setItem(45, new ItemBuilder(Material.RED_WOOL).amount(1).name("§cRetirer un item").build(), e -> {
            if (amountInIt > 0) {
                amountInIt--;
                update();
            }
        });
        setItem(46, new ItemBuilder(Material.RED_WOOL).amount(10).name("§cRetirer 10 items").build(), e -> {
            if (amountInIt > 10) {
                amountInIt -= 10;
                update();
            } else {
                amountInIt = 0;
                update();
            }
        });
        setItem(47, new ItemBuilder(Material.RED_WOOL).amount(32).name("§cRetirer 100 items").build(), e -> {
            if (amountInIt > 100) {
                amountInIt -= 100;
                update();
            } else {
                amountInIt = 0;
                update();
            }
        });
        setItem(48, new ItemBuilder(Material.RED_WOOL).amount(64).name("§cRetirer 1000 items").build(), e -> {
            if (amountInIt > 1000) {
                amountInIt -= 1000;
                update();
            } else {
                amountInIt = 0;
                update();
            }
        });

        setItem(53, new ItemBuilder(Material.GREEN_WOOL).amount(1).name("§aAjouter un item").build(), e -> {
            if (amountInIt < SacsManager.MAX_AMOUNT) {
                amountInIt++;
                update();
            }
        });
        setItem(52, new ItemBuilder(Material.GREEN_WOOL).amount(10).name("§aAjouter 10 items").build(), e -> {
            if (amountInIt < SacsManager.MAX_AMOUNT - 10) {
                amountInIt += 10;
                update();
            } else {
                amountInIt = SacsManager.MAX_AMOUNT;
                update();
            }
        });
        setItem(51, new ItemBuilder(Material.GREEN_WOOL).amount(32).name("§aAjouter 100 items").build(), e -> {
            if (amountInIt < SacsManager.MAX_AMOUNT - 100) {
                amountInIt += 100;
                update();
            } else {
                amountInIt = SacsManager.MAX_AMOUNT;
                update();
            }
        });
        setItem(50, new ItemBuilder(Material.GREEN_WOOL).amount(64).name("§aAjouter 1000 items").build(), e -> {
            if (amountInIt < SacsManager.MAX_AMOUNT - 1000) {
                amountInIt += 1000;
                update();
            } else {
                amountInIt = SacsManager.MAX_AMOUNT;
                update();
            }
        });

        setItem(49, new ItemBuilder(Material.BARRIER).name("§cFermer").build(), e -> e.getWhoClicked().closeInventory());
    }
}
