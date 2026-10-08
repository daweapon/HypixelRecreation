package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.pets.PetItems;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class PetItemComponent extends SkyBlockItemComponent {
    private final ItemStatistics statistics;
    private final Map<ItemStatistic, Double> petStatisticBoosts;
    private final double allPetStatisticsBoost;
    private final boolean knockbackImmune;
    private final double petExpBoost;
    private final @Nullable SkillCategories petExpSkill;
    private final @Nullable ItemType onlyFor;
    private final List<String> description;

    public PetItemComponent(ItemStatistics statistics, Map<ItemStatistic, Double> petStatisticBoosts,
                            double allPetStatisticsBoost, boolean knockbackImmune, double petExpBoost,
                            @Nullable SkillCategories petExpSkill, @Nullable ItemType onlyFor,
                            List<String> description) {
        this.statistics = statistics;
        this.petStatisticBoosts = Map.copyOf(petStatisticBoosts);
        this.allPetStatisticsBoost = allPetStatisticsBoost;
        this.knockbackImmune = knockbackImmune;
        this.petExpBoost = petExpBoost;
        this.petExpSkill = petExpSkill;
        this.onlyFor = onlyFor;
        this.description = List.copyOf(description);

        addInheritedComponent(new ExtraRarityComponent("PET ITEM"));
        addInheritedComponent(new ExtraUnderNameComponent("Consumed on use"));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new InteractableComponent(PetItems::give, null, null));
        if (!description.isEmpty()) {
            addInheritedComponent(new LoreUpdateComponent(new LoreConfig((item, player) -> lore(description), null), false));
        }
    }

    private List<String> lore(List<String> description) {
        List<String> lore = new ArrayList<>();
        if (onlyFor == null) {
            lore.add("<a>Pet Items <7>can boost pets in many");
            lore.add("<7>powerful ways! A pet can only hold");
            lore.add("<7>one <a>Pet Item<7>, but you can <e>swap <7>it at");
            lore.add("<7>any time!");
            lore.add("<8>The pet must be visible to apply the item!");
        } else {
            lore.add("<7>This pet item may only be used on a");
            lore.add("<6>" + StringUtility.toNormalCase(onlyFor.name().replace("_PET", "")) + " <7>pet.");
        }
        lore.add("");
        lore.addAll(description);
        lore.add("");
        lore.add("<e>Right-click on your summoned pet to");
        lore.add("<e>give it this item!");
        return lore;
    }

    public boolean appliesTo(ItemType pet) {
        return onlyFor == null || onlyFor == pet;
    }
}
