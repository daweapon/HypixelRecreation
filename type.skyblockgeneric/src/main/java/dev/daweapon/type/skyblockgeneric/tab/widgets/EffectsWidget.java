package dev.daweapon.type.skyblockgeneric.tab.widgets;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.potion.GodPotionService;
import dev.daweapon.type.skyblockgeneric.tab.TabLocation;
import dev.daweapon.type.skyblockgeneric.tab.TabWidget;
import dev.daweapon.type.skyblockgeneric.tab.TabWidgetContext;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryStatistic;
import dev.daweapon.type.skyblockgeneric.potion.CookieBuffService;
import net.minestom.server.item.Material;

import java.util.ArrayList;
import java.util.List;

public final class EffectsWidget implements TabWidget {
    public static final String ID = "effects";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Active Effects";
    }

    @Override
    public Material icon() {
        return Material.BREWING_STAND;
    }

    @Override
    public int defaultPriority() {
        return 50;
    }

    @Override
    public boolean enabledByDefault(TabLocation location) {
        return false;
    }

    @Override
    public List<Text> lines(SkyBlockPlayer player, TabWidgetContext context) {
        List<TemporaryStatistic> effects = new ArrayList<>(player.getStatistics().getDisplayableActiveEffects());
        effects.removeIf(CookieBuffService::isCookieBuff);
        long godPotionMs = effects.stream().filter(GodPotionService::isGodPotionEffect)
                .mapToLong(TemporaryStatistic::getRemainingMs).max().orElse(0);
        if (godPotionMs > 0) effects.removeIf(GodPotionService::isGodPotionEffect);

        List<Text> lines = new ArrayList<>();
        lines.add(Text.of("<a><l>Active Effects: <e>({})", effects.size() + (godPotionMs > 0 ? 1 : 0)));
        if (godPotionMs > 0) lines.add(Text.of(" <c>God Potion <f>{}", TabDurations.clock(godPotionMs)));
        for (TemporaryStatistic effect : effects) {
            String color = effect.getDisplayColor() != null ? effect.getDisplayColor() : "<7>";
            lines.add(Text.of(" " + color + "{} <f>{}", effect.getDisplayName(), TabDurations.clock(effect.getRemainingMs())));
        }
        return lines;
    }
}
