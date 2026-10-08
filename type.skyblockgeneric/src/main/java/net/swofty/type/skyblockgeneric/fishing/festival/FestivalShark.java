package net.swofty.type.skyblockgeneric.fishing.festival;

import net.swofty.commons.skyblock.item.Rarity;
import lombok.Getter;
import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureBehaviour;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureProfile;

import java.util.List;

/**
 * The four Fishing Festival Sharks with their wiki stats, catch weights, drops and Bestiary
 * entries (wiki: Nurse Shark, Blue Shark, Tiger Shark, Great White Shark; game update 0.27.2).
 * Fishing level requirements and Fishing XP for catch resolution live in sea_creatures.yml.
 */
@Getter
public enum FestivalShark {
    NURSE_SHARK("Nurse Shark", 22, 2_500, 80, 2_875, 220, 26, 22,
            "A tiny fin emerges from the water, you've caught a Nurse Shark.",
            List.of(MobType.AQUATIC, MobType.ANIMAL),
            2, ItemType.NURSE_SHARK_TOOTH,
            List.of(new SharkDrop(ItemType.CARNIVAL_TICKET, "Carnival Ticket", 1, 0.1),
                    new SharkDrop(null, "Aquamarine Dye", 1, 0.00002)),
            new Bestiary("Still studying to become a doctor.", 4, 15),
            "2067ccefba5d811f47e3e18438556b704393aafcafccedd5d0981999286f598a"),
    BLUE_SHARK("Blue Shark", 52, 25_000, 150, 1_500, 650, 67, 65,
            "You spot a fin as blue as the water it came from, it's a Blue Shark.",
            List.of(MobType.AQUATIC, MobType.ANIMAL),
            4, ItemType.BLUE_SHARK_TOOTH,
            List.of(new SharkDrop(ItemType.CARNIVAL_TICKET, "Carnival Ticket", 1, 0.15),
                    new SharkDrop(null, "Aquamarine Dye", 1, 0.00004)),
            new Bestiary("Camouflages well with his environment.", 6, 20),
            "381e1d06e5f0654a682a3264905b5dc4b8e7b613ab6697ac45f2e0da3bc9b4fd"),
    TIGER_SHARK("Tiger Shark", 99, 250_000, 300, 500, 1_485, 149, 148,
            "A striped beast bounds from the depths, the wild Tiger Shark!",
            List.of(MobType.AQUATIC, MobType.ANIMAL),
            8, ItemType.TIGER_SHARK_TOOTH,
            List.of(new SharkDrop(ItemType.MEGALODON_PET, Rarity.EPIC, "Epic Megalodon Pet", 1, 1),
                    new SharkDrop(ItemType.CARNIVAL_TICKET, "Carnival Ticket", 1, 0.25),
                    new SharkDrop(null, "Aquamarine Dye", 1, 0.00004)),
            new Bestiary("Like a regular tiger, but swims.", 7, 20),
            "ea575977e6bd0c7add94e2d8fdcc2af77e36c44d6b4c67788862a94000be6399"),
    GREAT_WHITE_SHARK("Great White Shark", 160, 1_500_000, 750, 100, 3_200, 401, 320,
            "Hide no longer, a Great White Shark has tracked your scent and thirsts for your blood!",
            List.of(MobType.AQUATIC, MobType.ANIMAL, MobType.ELUSIVE),
            16, ItemType.GREAT_WHITE_SHARK_TOOTH,
            List.of(new SharkDrop(ItemType.MEGALODON_PET, Rarity.LEGENDARY, "Legendary Megalodon Pet", 1, 1),
                    new SharkDrop(ItemType.CARNIVAL_TICKET, "Carnival Ticket", 1, 0.5),
                    new SharkDrop(null, "Aquamarine Dye", 1, 0.002)),
            new Bestiary("Most famous of all sharks.", 6, 15),
            "a94ae433b301c7fb7c68cba625b0bd36b0b14190f20e34a7c8ee0d9de06d53b9"),
    ;

    public static final double BASE_TOOTH_CHANCE = 10D;
    private static final double SPEED = 110D;

    private final String displayName;
    private final int level;
    private final double health;
    private final double damage;
    private final int weight;
    private final long fishingXp;
    private final int xpOrbs;
    private final int coins;
    private final String catchMessage;
    private final List<MobType> mobTypes;
    private final int finAmount;
    private final ItemType tooth;
    private final List<SharkDrop> extraDrops;
    private final Bestiary bestiary;
    private final String headTexture;
    private final SeaCreatureProfile profile;

    FestivalShark(String displayName, int level, double health, double damage, int weight,
                  long fishingXp, int xpOrbs, int coins, String catchMessage, List<MobType> mobTypes,
                  int finAmount, ItemType tooth, List<SharkDrop> extraDrops, Bestiary bestiary,
                  String headTexture) {
        this.displayName = displayName;
        this.level = level;
        this.health = health;
        this.damage = damage;
        this.weight = weight;
        this.fishingXp = fishingXp;
        this.xpOrbs = xpOrbs;
        this.coins = coins;
        this.catchMessage = catchMessage;
        this.mobTypes = mobTypes;
        this.finAmount = finAmount;
        this.tooth = tooth;
        this.extraDrops = extraDrops;
        this.bestiary = bestiary;
        this.headTexture = headTexture;
        this.profile = SeaCreatureProfile.builder(name())
                .displayName(displayName)
                .level(level)
                .entityType(EntityType.DOLPHIN)
                .health(health).damage(damage).speed(SPEED)
                .behaviour(SeaCreatureBehaviour.aggressive(1.8, 20, 20))
                .mobTypes(mobTypes.toArray(MobType[]::new))
                .damageCooldownMs(400)
                .fishingXpReward(fishingXp).xpOrbs(xpOrbs)
                .catchMessage(catchMessage)
                .build();
    }

    public record Bestiary(String description, int bracket, int maxTier) {
    }
}
