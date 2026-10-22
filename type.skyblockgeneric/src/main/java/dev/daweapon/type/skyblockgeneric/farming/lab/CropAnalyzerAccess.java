package dev.daweapon.type.skyblockgeneric.farming.lab;

import dev.daweapon.type.skyblockgeneric.gui.inventories.garden.GUICropAnalyzer;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class CropAnalyzerAccess {
    private CropAnalyzerAccess() {
    }

    public static boolean has(SkyBlockPlayer player) {
        return LabQuest.isComplete(player);
    }

    public static void open(SkyBlockPlayer player) {
        if (!has(player)) {
            player.sendMessage("<c>Jake hasn't shared the Crop Analyzer with you yet! Help <e>Beth<c> uncover the Underground Lab first.");
            return;
        }
        player.openView(new GUICropAnalyzer());
    }
}
