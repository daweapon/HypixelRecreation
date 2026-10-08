package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.gathering;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import net.swofty.type.skyblockgeneric.fishing.FishingMedium;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootRoll;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Where the gathering pets drop: the Flying Fish from The Loch Emperor and the Squid from water
 * catches. Each source rolls its rarities rarest first and gives at most one pet.
 */
public final class GatheringPetDrops {
    private static final String LOCH_EMPEROR = "THE_SEA_EMPEROR";
    private static final Map<Rarity, Double> FLYING_FISH = rarestFirst(
            Rarity.LEGENDARY, 0.5, Rarity.EPIC, 2, Rarity.RARE, 4);
    private static final Map<Rarity, Double> SQUID = rarestFirst(
            Rarity.LEGENDARY, 0.0017, Rarity.EPIC, 0.0035, Rarity.RARE, 0.007,
            Rarity.UNCOMMON, 0.0105, Rarity.COMMON, 0.0139);

    private GatheringPetDrops() {
    }

    public static void mobKilled(SkyBlockPlayer player, SkyBlockMob mob) {
        if (!(mob instanceof SeaCreatureMob seaCreature) || !LOCH_EMPEROR.equals(seaCreature.getSeaCreatureId())) return;
        SkyBlockItem pet = roll(player, ItemType.FLYING_FISH_PET, FLYING_FISH);
        if (pet == null) return;
        player.addAndUpdateItem(pet);
        player.sendMessage("<6><l>RARE DROP!</l> " + pet.getDisplayName());
    }

    public static void fishCaught(SkyBlockPlayer player, FishingMedium medium, CatchPayload payload) {
        if (medium != FishingMedium.WATER || payload instanceof CatchPayload.SeaCreature) return;
        if (player.getRegion() != null && player.getRegion().getType() == RegionType.TOMB_FLOODWAY) return;
        SkyBlockItem pet = roll(player, ItemType.SQUID_PET, SQUID);
        if (pet == null) return;
        player.addAndUpdateItem(pet);
        boolean outstanding = pet.getAttributeHandler().getRarity().isAtLeast(Rarity.EPIC);
        player.sendMessage((outstanding ? "<d><l>OUTSTANDING CATCH!</l>" : "<5><l>GREAT CATCH!</l>")
                + " <b>You found a <f>" + pet.getDisplayName() + "<b>.");
    }

    private static @Nullable SkyBlockItem roll(SkyBlockPlayer player, ItemType type, Map<Rarity, Double> chances) {
        for (Map.Entry<Rarity, Double> entry : chances.entrySet()) {
            if (!LootRoll.roll(player.getUuid(), type, entry.getValue())) continue;
            SkyBlockItem pet = new SkyBlockItem(type);
            pet.getAttributeHandler().setRarity(entry.getKey());
            return pet;
        }
        return null;
    }

    private static Map<Rarity, Double> rarestFirst(Object... pairs) {
        Map<Rarity, Double> chances = new LinkedHashMap<>();
        for (int i = 0; i < pairs.length; i += 2) chances.put((Rarity) pairs[i], ((Number) pairs[i + 1]).doubleValue());
        return chances;
    }
}
