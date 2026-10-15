package dev.daweapon.type.skyblockgeneric.experiments;

import dev.daweapon.type.skyblockgeneric.consumable.TimedConsumables;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointExperiments;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import dev.daweapon.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public final class MetaphysicalSerum {
    private static final double XP_PER_DOSE = 5;
    private static final double XP_AFTER_THIRD = 15;

    private MetaphysicalSerum() {
    }

    public static void registerInteractables() {
        InteractableRegistry.register("METAPHYSICAL_SERUM", InteractableItemConfig.builder()
                .rightClickHandler((player, item) -> {
                    DatapointExperiments.ExperimentsData data = ExperimentTable.data(player);
                    if (!ExperimentTable.drinkSerum(data)) {
                        player.sendMessage("<c>The serum tastes far too abstract. You can't drink any more!");
                        return;
                    }
                    TimedConsumables.consumeHeld(player, item);
                    ExperimentTable.save(player);
                    syncXp(player);
                    int drunk = ExperimentTable.serums(data);
                    if (drunk >= ExperimentTable.SERUM_LIMIT) {
                        player.sendMessage("<d>You drank the last dose! Your Superpairs gain <a>+{} <d>permanent clicks.", ExperimentTable.SERUM_BONUS_CLICKS);
                    } else {
                        player.sendMessage("<d>You drank the serum! Add-on chains are now <a>{} <d>shorter ({}/{}).", drunk, drunk, ExperimentTable.SERUM_LIMIT);
                    }
                }).build());
    }

    public static void syncXp(SkyBlockPlayer player) {
        int drunk = ExperimentTable.serums(ExperimentTable.data(player));
        double xp = drunk >= ExperimentTable.SERUM_LIMIT ? XP_AFTER_THIRD : drunk * XP_PER_DOSE;
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.METAPHYSICAL_SERUM, xp);
    }
}
