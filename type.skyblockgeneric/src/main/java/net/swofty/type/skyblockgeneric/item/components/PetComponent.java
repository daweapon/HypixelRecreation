package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.particle.Particle;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointPetData;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.lore.LoreConfig;
import net.swofty.type.skyblockgeneric.item.handlers.pet.KatUpgrade;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.ConfiguredPetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetNumbers;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetStatTable;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.PetType;
import net.swofty.type.skyblockgeneric.item.handlers.pet.definition.RarityNumbers;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.PetItems;
import net.swofty.type.skyblockgeneric.user.pets.PetLevels;
import net.swofty.type.skyblockgeneric.user.pets.PetScore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * A pet, defined entirely in item YAML: its rarities, level cap, stats and abilities. See
 * {@code PET_FORMAT.md}.
 */
@Getter
public class PetComponent extends SkyBlockItemComponent {
    private final String petName;
    private final PetType petType;
    private final List<Rarity> rarities;
    private final int maxLevel;
    private final int statsBaseLevel;
    private final @Nullable Integer fixedXpOffset;
    private final boolean fixedLevel;
    private final boolean countsForPetScore;
    private final boolean passive;
    private final RarityNumbers georgePrices;
    private final EnumMap<Rarity, KatUpgrade> katUpgrades;
    private final PetStatTable statistics;
    private final List<ConfiguredPetAbility> abilities;
    private final Particle particleId;
    private final String skullTexture;

    public PetComponent(String petName, PetType petType, List<Rarity> rarities, int maxLevel, int statsBaseLevel,
                        @Nullable Integer fixedXpOffset, boolean fixedLevel, boolean countsForPetScore, boolean passive,
                        RarityNumbers georgePrices, EnumMap<Rarity, KatUpgrade> katUpgrades,
                        PetStatTable statistics, List<ConfiguredPetAbility> abilities,
                        Particle particleId, String skullTexture) {
        this.petName = petName;
        this.petType = petType;
        this.rarities = List.copyOf(rarities);
        this.maxLevel = maxLevel;
        this.statsBaseLevel = statsBaseLevel;
        this.fixedXpOffset = fixedXpOffset;
        this.fixedLevel = fixedLevel;
        this.countsForPetScore = countsForPetScore;
        this.passive = passive;
        this.georgePrices = georgePrices;
        this.katUpgrades = katUpgrades;
        this.statistics = statistics;
        this.abilities = List.copyOf(abilities);
        this.particleId = particleId;
        this.skullTexture = skullTexture;

        addInheritedComponent(new SkullHeadComponent((item) -> skullTexture));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new InteractableComponent(this::interact, this::interact, null));
        addInheritedComponent(new LoreUpdateComponent(
                new LoreConfig((item, player) -> getAbsoluteLore(player, item), (item, player) -> {
                    Rarity rarity = item.getAttributeHandler().getRarity();
                    return Text.of("<7>[Lvl {}] <color:{}>{}", PetLevels.level(item), rarity.getColor(), petName)
                            .serialize();
                }), true)
        );
    }

    public Rarity getMinRarity() {
        return rarities.getFirst();
    }

    public Rarity getMaxRarity() {
        return rarities.getLast();
    }

    public @Nullable Rarity nextRarity(Rarity current) {
        for (Rarity rarity : rarities) if (rarity.ordinal() > current.ordinal()) return rarity;
        return null;
    }

    public @Nullable SkillCategories getSkillCategory() {
        return petType.getSkill();
    }

    public int xpOffset(Rarity rarity) {
        return fixedXpOffset != null ? fixedXpOffset : ItemAttributePetData.rarityOffset(rarity);
    }

    public int georgePrice(Rarity rarity) {
        return (int) georgePrices.get(rarity);
    }

    public @Nullable KatUpgrade katUpgradeTo(Rarity target) {
        return katUpgrades.get(target);
    }

    public boolean isEggForm(int level) {
        return level < statsBaseLevel;
    }

    public ItemStatistics statisticsAt(Rarity rarity, int level) {
        if (isEggForm(level)) return ItemStatistics.empty();
        return statistics.at(rarity, level, statsBaseLevel);
    }

    public List<PetAbility> abilitiesFor(SkyBlockItem pet) {
        Rarity rarity = pet.getAttributeHandler().getRarity();
        if (isEggForm(PetLevels.level(pet))) return List.of();
        List<PetAbility> unlocked = new ArrayList<>();
        for (ConfiguredPetAbility ability : abilities) if (ability.appliesAt(rarity)) unlocked.add(ability);
        return unlocked;
    }

    private void interact(SkyBlockPlayer player, SkyBlockItem item) {
        DatapointPetData.UserPetData petData = player.getPetData();
        if (petData.isFull()) {
            player.sendMessage("<c>Your Pets menu is full! You can hold up to {} pets.", petData.capacity());
            return;
        }

        petData.addPet(item);
        PetScore.syncSkyBlockXP(player);
        player.setItemInHand(null);
        player.sendMessage("<a>Successfully added {} <a>to your pet menu!", item.getDisplayName());
        player.playSound(Sound.sound()
                .type(Key.key("minecraft", "entity.experience_orb.pickup"))
                .volume(1f)
                .pitch(1f)
                .build());
    }

    public List<String> getAbsoluteLore(@Nullable SkyBlockPlayer player, SkyBlockItem item) {
        List<String> lore = new ArrayList<>();
        Rarity rarity = item.getAttributeHandler().getRarity();
        int level = PetLevels.level(item);

        lore.add("<8>" + petType.getLabel() + " Pet");
        lore.add(" ");

        ItemStatistics statistics = PetItems.statistics(item);
        boolean anyStat = false;
        for (ItemStatistic stat : ItemStatistic.values()) {
            double value = statistics.getOverall(stat);
            if (value == 0) continue;
            anyStat = true;
            lore.add("<7>" + stat.getDisplayName() + ": <color:" + stat.getDisplayColor().asHexString() + ">"
                    + (value >= 0 ? "+" : "") + PetNumbers.floor(value) + (stat.getIsPercentage() ? "%" : ""));
        }
        if (isEggForm(level)) {
            lore.add("<7>This pet hatches at level <a>" + statsBaseLevel + "<7>.");
            anyStat = true;
        }

        boolean first = true;
        for (PetAbility ability : abilitiesFor(item)) {
            if (!first || anyStat) lore.add(" ");
            first = false;
            lore.add("<6>" + ability.getName());
            lore.addAll(ability.getDescription(item));
        }

        ItemType heldItem = item.getAttributeHandler().getPetHeldItem();
        PetItemComponent held = PetItems.held(item);
        if (heldItem != null && held != null) {
            Rarity heldRarity = new SkyBlockItem(heldItem).getAttributeHandler().getRarity();
            lore.add(" ");
            lore.add("<6>Held Item: <color:" + heldRarity.getColor().asHexString() + ">" + heldItem.getDisplayName());
            lore.addAll(held.getDescription());
        }

        lore.add(" ");
        if (fixedLevel || level >= maxLevel) {
            lore.add("<b><l>MAX LEVEL");
        } else {
            double experience = PetLevels.experienceIntoLevel(item);
            long needed = PetLevels.experienceToNextLevel(item);
            lore.add(progressText("Progress to Level " + (level + 1), experience, needed));
            lore.add(Text.of("<bar:{}:{}>", experience, needed).serialize());
        }

        lore.add(" ");
        lore.add(Text.of("<rarity:{}>", rarity.name()).serialize());

        return lore;
    }

    private static String progressText(String label, double current, double max) {
        double percent = max != 0 ? (current / max) * 100.0 : 0.0;
        percent = StringUtility.roundTo(percent, 1);
        return percent < 100.0
                ? "<7>" + label + ": <e>" + StringUtility.commaify(percent) + "<6>%"
                : "<7>" + label + ": <a>100.0%";
    }
}
