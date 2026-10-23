package dev.daweapon.type.skyblockgeneric.dante;

import lombok.NonNull;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockActionBar;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class PercentManaCost extends RegisteredAbility.AbilityCost {
    private final double percent;

    public PercentManaCost(double percent) {
        this.percent = percent;
    }

    public static int costOf(double maxMana, double percent) {
        return (int) Math.round(maxMana * percent / 100D);
    }

    private int effectiveCost(SkyBlockPlayer player) {
        return RegisteredAbility.effectiveManaCost(player, costOf(player.getMaxMana(), percent));
    }

    @Override
    public boolean canUse(@NonNull SkyBlockPlayer player) {
        return player.getMana() >= effectiveCost(player);
    }

    @Override
    public void onUse(@NonNull SkyBlockPlayer player, @NonNull RegisteredAbility ability) {
        int spent = effectiveCost(player);
        SkyBlockActionBar.getFor(player).addReplacement(
                SkyBlockActionBar.BarSection.MANA,
                Text.of("<b>-{} (<6>{}</6>)", spent, ability.getName()),
                20,
                2);
        player.setMana(player.getMana() - spent);
    }

    @Override
    public void onFail(@NonNull SkyBlockPlayer player) {
        SkyBlockActionBar.getFor(player).addReplacement(
                SkyBlockActionBar.BarSection.MANA,
                Text.of("<c><l>NOT ENOUGH MANA"),
                20 * 2,
                2);
    }

    @Override
    public String getLoreDisplay() {
        return "<8>Mana Cost: <3>" + (percent == Math.rint(percent) ? String.valueOf((int) percent) : String.valueOf(percent)) + "%";
    }
}
