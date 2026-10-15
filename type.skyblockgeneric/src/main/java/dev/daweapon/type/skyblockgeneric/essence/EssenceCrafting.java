package dev.daweapon.type.skyblockgeneric.essence;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Blacksmith essence crafting: star upgrades and salvaging. Salvaging refunds half of the
 * resources spent on stars (rounded down), as the wiki states.
 */
public final class EssenceCrafting {
    private EssenceCrafting() {
    }

    public enum UpgradeResult {UPGRADED, NOT_UPGRADABLE, MAXED, MISSING_ITEMS_IN_SERVER, TOO_POOR}

    public record SalvageResult(EssenceType essence, long amount, Map<ItemType, Integer> items) {
    }

    public static UpgradeResult check(SkyBlockPlayer player, SkyBlockItem item) {
        Optional<EssenceUpgrades.Upgrade> upgrade = EssenceUpgrades.upgradeFor(item.getAttributeHandler().getPotentialType());
        if (upgrade.isEmpty() || item.getAmount() != 1) return UpgradeResult.NOT_UPGRADABLE;
        int stars = item.getAttributeHandler().getUpgradeLevel();
        if (stars >= upgrade.get().maxStars()) return UpgradeResult.MAXED;
        EssenceUpgrades.Star next = upgrade.get().star(stars + 1);
        if (next.hasUnknownItems()) return UpgradeResult.MISSING_ITEMS_IN_SERVER;
        if (Essences.get(player, upgrade.get().essence()) < next.essence() || player.getCoins() < next.coins()) {
            return UpgradeResult.TOO_POOR;
        }
        for (Map.Entry<String, Integer> entry : next.items().entrySet()) {
            if (player.countItem(ItemType.get(entry.getKey())) < entry.getValue()) return UpgradeResult.TOO_POOR;
        }
        return UpgradeResult.UPGRADED;
    }

    public static UpgradeResult upgrade(SkyBlockPlayer player, SkyBlockItem item) {
        UpgradeResult result = check(player, item);
        if (result != UpgradeResult.UPGRADED) return result;
        EssenceUpgrades.Upgrade upgrade = EssenceUpgrades.upgradeFor(item.getAttributeHandler().getPotentialType()).orElseThrow();
        int stars = item.getAttributeHandler().getUpgradeLevel();
        EssenceUpgrades.Star next = upgrade.star(stars + 1);
        if (!Essences.take(player, upgrade.essence(), next.essence())) return UpgradeResult.TOO_POOR;
        if (next.coins() > 0) player.removeCoins(next.coins());
        next.items().forEach((id, amount) -> player.takeItem(ItemType.get(id), amount));
        item.getAttributeHandler().setUpgradeLevel(stars + 1);
        return UpgradeResult.UPGRADED;
    }

    public static boolean canSalvage(SkyBlockItem item) {
        return EssenceUpgrades.salvageFor(item.getAttributeHandler().getPotentialType()).isPresent();
    }

    public static Optional<SalvageResult> preview(SkyBlockPlayer player, SkyBlockItem item) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        Optional<EssenceUpgrades.Salvage> salvage = EssenceUpgrades.salvageFor(type);
        if (salvage.isEmpty()) return Optional.empty();
        long bonusPercent = Math.round(EssencePerks.value(player, IceEssencePerk.COOLED_FORGES));
        long essence = salvage.get().amount() + salvage.get().amount() * bonusPercent / 100;
        Map<ItemType, Integer> items = new HashMap<>();
        salvage.get().items().forEach((id, amount) -> {
            ItemType itemType = ItemType.get(id);
            if (itemType != null) items.merge(itemType, amount * item.getAmount(), Integer::sum);
        });
        essence *= item.getAmount();
        EssenceType starEssence = salvage.get().essence();
        Optional<EssenceUpgrades.Upgrade> upgrade = EssenceUpgrades.upgradeFor(type);
        int stars = item.getAttributeHandler().getUpgradeLevel();
        if (upgrade.isPresent() && stars > 0) {
            long spent = 0;
            Map<ItemType, Integer> spentItems = new HashMap<>();
            for (int level = 1; level <= Math.min(stars, upgrade.get().maxStars()); level++) {
                EssenceUpgrades.Star star = upgrade.get().star(level);
                spent += star.essence();
                star.items().forEach((id, amount) -> {
                    ItemType itemType = ItemType.get(id);
                    if (itemType != null) spentItems.merge(itemType, amount, Integer::sum);
                });
            }
            if (upgrade.get().essence() == starEssence) essence += spent / 2;
            spentItems.forEach((itemType, amount) -> items.merge(itemType, amount / 2, Integer::sum));
        }
        items.values().removeIf(amount -> amount <= 0);
        return Optional.of(new SalvageResult(starEssence, essence, items));
    }

    public static Optional<SalvageResult> salvage(SkyBlockPlayer player, SkyBlockItem item) {
        Optional<SalvageResult> result = preview(player, item);
        result.ifPresent(salvaged -> {
            Essences.add(player, salvaged.essence(), salvaged.amount());
            salvaged.items().forEach((type, amount) -> player.addAndUpdateItem(new SkyBlockItem(type, amount)));
        });
        return result;
    }
}
