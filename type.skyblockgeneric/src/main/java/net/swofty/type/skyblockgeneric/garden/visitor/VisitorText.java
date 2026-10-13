package net.swofty.type.skyblockgeneric.garden.visitor;

import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class VisitorText {
    private VisitorText() {
    }

    public static String fill(String line, SkyBlockPlayer player) {
        return line.replace("{player}", player.getUsername())
                .replace("{item}", "crops")
                .replace("[number]", "many")
                .replace("[player]", player.getUsername())
                .replace("<playername>", player.getUsername());
    }
}
