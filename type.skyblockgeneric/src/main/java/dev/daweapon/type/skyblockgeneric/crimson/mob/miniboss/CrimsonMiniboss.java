package dev.daweapon.type.skyblockgeneric.crimson.mob.miniboss;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.always;
import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.chance;
import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.cyclamen;
import static dev.daweapon.type.skyblockgeneric.crimson.mob.MobDrop.pet;

/**
 * The four Crimson Isle minibosses and the Magma Boss (wiki boss pages): where they spawn, how long until they
 * return, and their shared-loot tables. Drops ignore Magic Find except Cyclamen Dye. Kuudra Keys drop once the
 * key items exist. The wiki disagrees on Bladesoul (2 vs 5 min) and the Magma Boss (2 vs 5 min); the newer boss
 * pages win. Spawn spots without published coordinates are estimates.
 */
@Getter
public enum CrimsonMiniboss {
    BLADESOUL("Bladesoul", "<8><l>", new Pos(-295.5, 82, -518.5), Duration.ofMinutes(5), 6, 4, List.of(
            always("HALLOWED_SKULL", 1), always("COAL", 20), chance("COAL", 10, 70), chance("COAL", 5, 50),
            chance("ENCHANTED_COAL", 50), chance("ENCHANTED_COAL", 50), chance("RAGNAROCK_AXE", 0.5))),
    MAGE_OUTLAW("Mage Outlaw", "<5><l>", new Pos(-185.5, 105, -865.5), Duration.ofMinutes(2), 6, 4, List.of(
            always("SPELL_POWDER", 1), always("GLOWSTONE_DUST", 20), chance("GLOWSTONE_DUST", 10, 70),
            chance("GLOWSTONE_DUST", 5, 50), chance("ENCHANTED_GLOWSTONE_DUST", 50),
            chance("ENCHANTED_GLOWSTONE_DUST", 50), chance("FIRE_FREEZE_STAFF", 0.5), chance("WAND_OF_STRENGTH", 0.5))),
    BARBARIAN_DUKE_X("Barbarian Duke X", "<8><l>", new Pos(-527.5, 104, -923.5), Duration.ofMinutes(2), 6, 4, List.of(
            always("LEATHER_CLOTH", 1), always("RAW_PORKCHOP", 20), chance("RAW_PORKCHOP", 10, 70),
            chance("RAW_PORKCHOP", 5, 50), chance("ENCHANTED_PORK", 50), chance("ENCHANTED_PORK", 50),
            chance("FLAMING_FIST", 0.5))),
    ASHFANG("Ashfang", "<8><l>", new Pos(-484.5, 135, -1015.5), Duration.ofMinutes(2), 6, 4, List.of(
            always("LUMINO_FIBER", 1), always("BLAZE_ROD", 20), chance("BLAZE_ROD", 10, 70), chance("BLAZE_ROD", 5, 50),
            chance("ENCHANTED_BLAZE_POWDER", 50), chance("ENCHANTED_BLAZE_POWDER", 50), chance("FIRE_VEIL_WAND", 0.5))),
    MAGMA_BOSS("Magma Boss", "<4><l>", new Pos(-367.5, 63, -778.5), Duration.ofMinutes(2), 12, 8, List.of(
            always("COMPACT_OOZE", 1), always("REKINDLED_EMBER_FRAGMENT", 1), always("MAGMA_CREAM", 20),
            chance("MAGMA_CREAM", 10, 70), chance("MAGMA_CREAM", 5, 50), chance("ENCHANTED_MAGMA_CREAM", 50),
            chance("ENCHANTED_MAGMA_CREAM", 50), pet("MAGMA_CUBE_PET", dev.daweapon.commons.skyblock.item.Rarity.EPIC, 1.5),
            chance("FIRE_FURY_STAFF", 0.5), chance("MOLTEN_CUBE", 0.5),
            pet("MAGMA_CUBE_PET", dev.daweapon.commons.skyblock.item.Rarity.LEGENDARY, 0.4))),
    ;

    public static final double LOOT_SHARE_DAMAGE = 250_000;
    public static final int DAILY_REPUTATION = 20;

    private final String displayName;
    private final String nameColour;
    private final Pos spawn;
    private final Duration respawn;
    private final List<MobDrop> drops;

    CrimsonMiniboss(String displayName, String nameColour, Pos spawn, Duration respawn, double keyChance,
                    double hotKeyChance, List<MobDrop> uniqueDrops) {
        this.displayName = displayName;
        this.nameColour = nameColour;
        this.spawn = spawn;
        this.respawn = respawn;
        List<MobDrop> drops = new ArrayList<>();
        uniqueDrops.forEach(drop -> drops.add(drop.noMagicFind()));
        drops.add(chance("MAGMA_URCHIN", 2).noMagicFind());
        drops.add(chance("KUUDRA_TIER_KEY", keyChance).noMagicFind());
        drops.add(chance("KUUDRA_HOT_TIER_KEY", hotKeyChance).noMagicFind());
        drops.add(cyclamen(0.0004));
        this.drops = List.copyOf(drops);
    }

    public String coloured() {
        return nameColour + displayName;
    }
}
