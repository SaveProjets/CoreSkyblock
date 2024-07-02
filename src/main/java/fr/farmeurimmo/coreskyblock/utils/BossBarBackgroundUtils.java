package fr.farmeurimmo.coreskyblock.utils;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;

public class BossBarBackgroundUtils {

    // good example: §f䧼䧼䧼䧼䧼CoreSkyblock

    public static BossBar getBossBarWithText(String text) {
        return BossBar.bossBar(Component.text(applyBackGround(text)), 1.0f, BossBar.Color.WHITE, BossBar.Overlay.PROGRESS);
    }

    public static String applyBackGround(String text) {
        StringBuilder builder = new StringBuilder();

        String[] texts = text.split("\\|");

        for (String txt : texts) {
            builder.append("\uF801䧼".repeat(Math.max(0, (txt.length() + 1) / 2)));
            builder.append("\uF806".repeat(txt.length()));
            builder.append(txt);
            builder.append(" ".repeat(4));
        }

        return builder.toString();

        /*String[] texts = text.split("\\|");
        StringBuilder builder = new StringBuilder();
        int it = 0;
        String lastBack = "";
        for (String txt : texts) {
            if (it > 0 && it < texts.length) {
                builder.append(" ".repeat(lastBack.length()));
            }
            if (txt.length() < 16) {
                builder.append("䧼\uF801".repeat((int) (txt.length() / 1.7)));
                builder.append("\uF808".repeat((int) (txt.length() / 1.33)));
                lastBack = txt;
            } else if (text.length() < 42) {
                builder.append("䧼\uF801".repeat(txt.length() / 2));
                builder.append("\uF808".repeat((int) (txt.length() / 1.37)));
                lastBack = txt;
            } else {
                builder.append("䧼\uF801".repeat((int) (txt.length() / 2.1)));
                builder.append("\uF808".repeat((int) (txt.length() / 1.46)));
                lastBack = txt;
            }
            //we need  per 2 characters

            builder.append(txt);
            builder.append("\uF808\uF808\uF808");
            it++;
        }

        return builder.toString();*/
    }
}
