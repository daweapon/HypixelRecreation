package dev.daweapon.type.skyblockgeneric.foraging;

import dev.daweapon.commons.StringUtility;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointHOTF;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class SweepDetails {
    private SweepDetails() {
    }

    public static boolean enabled(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler() != null && HotfService.data(player).isSweepDetails();
    }

    public static boolean toggle(SkyBlockPlayer player) {
        DatapointHOTF.PlayerHOTFData data = HotfService.data(player);
        data.setSweepDetails(!data.isSweepDetails());
        HotfService.save(player, data);
        return data.isSweepDetails();
    }

    public static void report(SkyBlockPlayer player, String tree, String part, double sweep, double toughness,
                              boolean wrongOrder, int logs) {
        if (!enabled(player)) return;
        for (String line : lines(tree, part, sweep, toughness, wrongOrder, logs)) player.sendMessage(line);
    }

    static String[] lines(String tree, String part, double sweep, double toughness, boolean wrongOrder, int logs) {
        String effective = StringUtility.decimalify(wrongOrder ? sweep / 2 : sweep, 1);
        return new String[]{
                "<2>Sweep Details <8>(" + capitalize(tree) + " " + capitalize(part) + ")",
                "<7>Sweep: <2>" + StringUtility.decimalify(sweep, 1) + (wrongOrder ? " <7>→ <c>" + effective + " <7>(wrong order, <c>halved<7>)" : ""),
                "<7>Toughness: <c>" + StringUtility.decimalify(toughness, 1),
                "<7>Logs cut: <6>" + logs
        };
    }

    private static String capitalize(String value) {
        return value.charAt(0) + value.substring(1).toLowerCase();
    }
}
