package net.swofty.type.skyblockgeneric.user.pets;

import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * The part of a held Pet Item that reacts to play, such as the Reaper Gem's Ferocity for a few
 * seconds after each kill. It runs alongside the summoned pet's own abilities.
 */
public final class PetItemAbility implements PetAbility {
    private final PetItemComponent item;

    public PetItemAbility(PetItemComponent item) {
        this.item = item;
    }

    @Override
    public String getName() {
        return "Held Item";
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        return item.effectLines();
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet) {
        if (item.isInactive() || player == null) return ItemStatistics.empty();
        AbilityRuntime runtime = player.getPetData().getAbilityRuntime(this);
        return System.currentTimeMillis() < runtime.getBuffUntil() ? item.getKillBuffStatistics() : ItemStatistics.empty();
    }

    @Override
    public void onEvent(PetEvent event, SkyBlockItem pet) {
        if (item.isInactive() || !(event instanceof PetEvent.Kill kill)) return;
        AbilityRuntime runtime = kill.player().getPetData().getAbilityRuntime(this);
        runtime.setBuffUntil(System.currentTimeMillis() + (long) (item.getKillBuffSeconds() * 1000));
    }
}
