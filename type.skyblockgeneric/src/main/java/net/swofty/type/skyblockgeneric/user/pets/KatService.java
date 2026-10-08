package net.swofty.type.skyblockgeneric.user.pets;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointKat;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.KatUpgrade;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

/**
 * Kat's pet sitting: one pet at a time is upgraded to its next rarity after a real-time wait, for
 * coins (0.3% cheaper per pet level) and items, if the player's Taming is high enough.
 */
public final class KatService {
    private KatService() {
    }

    public static @Nullable Rarity nextRarity(SkyBlockItem pet) {
        if (!pet.hasComponent(PetComponent.class)) return null;
        return pet.getComponent(PetComponent.class).nextRarity(pet.getAttributeHandler().getRarity());
    }

    public static @Nullable KatUpgrade upgradeFor(SkyBlockItem pet) {
        Rarity next = nextRarity(pet);
        return next == null ? null : pet.getComponent(PetComponent.class).katUpgradeTo(next);
    }

    public static int requiredTaming(Rarity target) {
        return switch (target) {
            case UNCOMMON -> 1;
            case RARE -> 5;
            case EPIC -> 10;
            case LEGENDARY -> 20;
            case MYTHIC -> 25;
            default -> 0;
        };
    }

    public static long coins(SkyBlockItem pet, KatUpgrade upgrade) {
        return upgrade.discountedCoins(PetLevels.level(pet));
    }

    public static @Nullable String problem(SkyBlockPlayer player, SkyBlockItem pet) {
        KatUpgrade upgrade = upgradeFor(pet);
        if (upgrade == null) return "Kat can't upgrade this pet any further!";
        Rarity next = nextRarity(pet);
        if (player.getSkills().getCurrentLevel(SkillCategories.TAMING) < requiredTaming(next)) {
            return "You need Taming " + requiredTaming(next) + " for Kat to upgrade this pet!";
        }
        if (player.getCoins() < coins(pet, upgrade)) return "You don't have enough Coins!";
        for (KatUpgrade.ItemCost cost : upgrade.items()) {
            if (player.getAmountInInventory(cost.item()) + player.getSackItems().getAmount(cost.item()) < cost.amount()) {
                return "You don't have the required items!";
            }
        }
        return null;
    }

    public static boolean start(SkyBlockPlayer player, SkyBlockItem pet) {
        if (problem(player, pet) != null || player.getKatData().getPet() != null) return false;
        KatUpgrade upgrade = upgradeFor(pet);
        player.removeCoins(coins(pet, upgrade));
        for (KatUpgrade.ItemCost cost : upgrade.items()) player.removeItemFromPlayer(cost.item(), cost.amount());
        player.getKatData().set(System.currentTimeMillis() + upgrade.timeMillis(), pet);
        return true;
    }

    public static @Nullable SkyBlockItem collect(SkyBlockPlayer player) {
        DatapointKat.PlayerKat kat = player.getKatData();
        SkyBlockItem pet = kat.getPet();
        if (pet == null || kat.getFinishTime() > System.currentTimeMillis()) return null;
        Rarity next = nextRarity(pet);
        if (next != null) pet.getAttributeHandler().setRarity(next);
        kat.clear();
        return pet;
    }
}
