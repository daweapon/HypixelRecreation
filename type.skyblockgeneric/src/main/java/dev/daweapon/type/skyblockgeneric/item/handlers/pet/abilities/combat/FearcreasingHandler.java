package dev.daweapon.type.skyblockgeneric.item.handlers.pet.abilities.combat;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearProgress;
import dev.daweapon.type.skyblockgeneric.greatspook.PrimalFearRules;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.definition.PetNumbers;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityContext;
import dev.daweapon.type.skyblockgeneric.item.handlers.pet.registry.PetAbilityHandler;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public final class FearcreasingHandler implements PetAbilityHandler {
    public static final int KILL_CAP = 150;

    @Override
    public ItemStatistics statistics(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        if (player == null || player.getSkyblockDataHandler() == null) return ItemStatistics.empty();
        double fear = PrimalFearRules.creasingFear(context.value(0), PrimalFearProgress.kills(player));
        if (fear <= 0) return ItemStatistics.empty();
        return ItemStatistics.builder().withBase(ItemStatistic.FEAR, fear).build();
    }

    @Override
    public List<String> description(PetAbilityContext context) {
        SkyBlockPlayer player = context.player();
        int kills = player == null || player.getSkyblockDataHandler() == null ? 0 : PrimalFearProgress.kills(player);
        return List.of(
                "<7>Gives <a>+" + PetNumbers.format(context.value(0)) + " <5>Fear <7>for every <a>10 <c>Primal",
                "<c>Fears <7>killed, up to <a>" + KILL_CAP + " <c>Primal Fears<7>.",
                "<c>Primal Fear Kills<7>: (<a>" + kills + "<7>/<a>" + KILL_CAP + "<7>)");
    }
}
