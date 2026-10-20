package dev.daweapon.type.skyblockgeneric.foraging;

import dev.daweapon.commons.ServerType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.lore.LoreRegistry;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class FrenzyAxes {
    public static final String LORE_HANDLER = "FRENZY_AXE";

    public record Frenzy(int logsPerSweep, int maxSweep, int foragingLevel) {
    }

    private static final Map<String, Frenzy> AXES = Map.of(
            "FIG_AXE", new Frenzy(2_000, 20, 12),
            "FIGSTONE_AXE", new Frenzy(10_000, 20, 15),
            "HELIX_CHOPPER", new Frenzy(20_000, 40, 20));

    private FrenzyAxes() {
    }

    public static void registerLore() {
        LoreRegistry.register(LORE_HANDLER, new LoreConfig(FrenzyAxes::lore, null, LoreConfig.LoreConfigLocation.AFTER_ABILITY));
    }

    public static Frenzy of(String itemId) {
        return itemId == null ? null : AXES.get(itemId);
    }

    public static int bonusSweep(Frenzy frenzy, int logsCut) {
        return Math.min(frenzy.maxSweep(), Math.max(0, logsCut) / frenzy.logsPerSweep());
    }

    public static double bonusSweep(SkyBlockItem tool) {
        Frenzy frenzy = of(tool.getAttributeHandler().getTypeAsString());
        return frenzy == null ? 0 : bonusSweep(frenzy, tool.getAttributeHandler().getLogsCut());
    }

    public static boolean countsLogsHere() {
        ServerType type = HypixelConst.getTypeLoader().getType();
        return type == ServerType.SKYBLOCK_GALATEA || type == ServerType.SKYBLOCK_TORRHUS_CANYON;
    }

    public static void recordLogs(SkyBlockPlayer player, int slot, int logs) {
        if (logs <= 0 || !countsLogsHere()) return;
        SkyBlockItem held = new SkyBlockItem(player.getInventory().getItemStack(slot));
        if (held.isAir() || of(held.getAttributeHandler().getTypeAsString()) == null) return;
        player.updateItemInSlot(slot, item -> item.getAttributeHandler().setLogsCut(item.getAttributeHandler().getLogsCut() + logs));
    }

    static List<String> lore(SkyBlockItem item, SkyBlockPlayer player) {
        Frenzy frenzy = of(item.getAttributeHandler().getTypeAsString());
        if (frenzy == null) return List.of();
        int logs = item.getAttributeHandler().getLogsCut();
        int bonus = bonusSweep(frenzy, logs);
        NumberFormat format = NumberFormat.getIntegerInstance(Locale.US);
        String next = bonus >= frenzy.maxSweep()
                ? "<7>Next Upgrade: <a>MAXED"
                : "<7>Next Upgrade: <2>+1 <8>(<a>" + format.format(logs - (long) bonus * frenzy.logsPerSweep())
                + "<7>/<c>" + format.format(frenzy.logsPerSweep()) + "<8>)";
        return List.of("", "<7>Current Bonus: <2>+" + bonus, next, "", "<7>Logs Cut: <b>" + format.format(logs),
                "", "<4>❣ <c>Requires <a>Foraging Skill " + frenzy.foragingLevel() + "<c>.");
    }
}
