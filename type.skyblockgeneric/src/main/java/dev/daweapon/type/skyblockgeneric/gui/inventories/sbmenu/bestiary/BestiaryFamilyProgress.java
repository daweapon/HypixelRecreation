package dev.daweapon.type.skyblockgeneric.gui.inventories.sbmenu.bestiary;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryData;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointBestiary;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public record BestiaryFamilyProgress(int found, int completed, int total) {
    private static final BestiaryData BESTIARY = new BestiaryData();
    private static final String BAR = "─────────────────";

    public static BestiaryFamilyProgress of(DatapointBestiary.PlayerBestiary bestiary, BestiaryEntry[] entries) {
        return of(bestiary, Arrays.asList(entries));
    }

    public static BestiaryFamilyProgress of(DatapointBestiary.PlayerBestiary bestiary, Collection<? extends BestiaryEntry> entries) {
        int found = 0;
        int completed = 0;
        for (BestiaryEntry entry : entries) {
            int kills = bestiary.getAmount(entry.getMobs());
            if (kills <= 0) continue;
            found++;
            if (kills >= BESTIARY.getTotalKillsForMaxTier(entry.getMobs().getFirst())) completed++;
        }
        return new BestiaryFamilyProgress(found, completed, entries.size());
    }

    public List<Text> lore() {
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(found == total ? "<7>Families Found: <a>{}%" : "<7>Families Found: <e>{}%", percent(found)));
        lore.add(bar(found));
        lore.add(Text.empty());
        lore.add(Text.of(completed == total ? "<7>Families Completed: <a>{}%" : "<7>Families Completed: <e>{}%", percent(completed)));
        lore.add(bar(completed));
        return lore;
    }

    static String percent(int amount, int total) {
        if (total == 0) return "0";
        return BigDecimal.valueOf(amount * 100.0 / total)
                .setScale(1, RoundingMode.HALF_UP)
                .stripTrailingZeros()
                .toPlainString();
    }

    private String percent(int amount) {
        return percent(amount, total);
    }

    private Text bar(int amount) {
        int filled = total == 0 ? 0 : (int) Math.round((double) amount / total * BAR.length());
        filled = Math.min(filled, BAR.length());
        return Text.of("<3><m>{}<f>{}<r> <b>{:,}<3>/<b>{:short}",
                BAR.substring(0, filled), BAR.substring(filled), amount, total);
    }
}
