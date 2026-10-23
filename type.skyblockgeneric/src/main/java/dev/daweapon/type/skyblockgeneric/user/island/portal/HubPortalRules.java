package dev.daweapon.type.skyblockgeneric.user.island.portal;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.island.SkyBlockIsland;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public final class HubPortalRules {
    public static final int REMOVAL_SKILL_LEVEL = 5;
    public static final int REPLACEMENT_COST = 100;

    private HubPortalRules() {
    }

    public static boolean canRemove(SkyBlockPlayer player) {
        return Arrays.stream(SkillCategories.values())
                .anyMatch(skill -> player.getSkills().getCurrentLevel(skill) >= REMOVAL_SKILL_LEVEL);
    }

    public static boolean isHubPortal(PlacedPortal portal) {
        return PortalCatalog.HUB_ITEM_ID.equals(portal.itemId());
    }

    public static @Nullable PlacedPortal placedHubPortal(SkyBlockPlayer player) {
        SkyBlockIsland island = player.getSkyBlockIsland();
        if (island == null || island.getPortals() == null) return null;
        return island.getPortals().all().stream().filter(HubPortalRules::isHubPortal).findFirst().orElse(null);
    }

    public static boolean hasRemovedHubPortal(SkyBlockPlayer player) {
        SkyBlockIsland island = player.getSkyBlockIsland();
        return island != null && island.getPortals() != null && placedHubPortal(player) == null;
    }

    public static void removeFromJerry(SkyBlockPlayer player) {
        PlacedPortal portal = placedHubPortal(player);
        if (portal == null) {
            player.sendMessage("<c>You do not have a Hub Portal on your island!");
            return;
        }
        if (!requireSkill(player)) return;
        player.getSkyBlockIsland().getPortals().remove(player.getInstance(), portal);
        player.sendMessage("<a>Jerry removed the portal to the Hub from your island. You can buy a new one from him for <6>{} coins<a>.", REPLACEMENT_COST);
    }

    public static boolean requireSkill(SkyBlockPlayer player) {
        if (canRemove(player)) return true;
        player.sendMessage("<c>You need a skill at level {:roman} or higher to remove the Hub Portal!", REMOVAL_SKILL_LEVEL);
        return false;
    }

    public static void buyReplacement(SkyBlockPlayer player) {
        if (!hasRemovedHubPortal(player)) {
            player.sendMessage("<c>Jerry only sells a Hub Portal once yours has been removed!");
            return;
        }
        if (player.getCoins() < REPLACEMENT_COST) {
            player.sendMessage("<c>You need {} coins to buy a Portal to the Hub!", REPLACEMENT_COST);
            return;
        }
        player.removeCoins(REPLACEMENT_COST);
        player.addAndUpdateItem(ItemType.HUB_PORTAL);
        player.sendMessage("<a>You bought a Portal to the Hub for <6>{} coins<a>!", REPLACEMENT_COST);
    }
}
