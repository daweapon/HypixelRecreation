package dev.daweapon.type.skyblockgeneric.communityupgrades;

import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneIncomingCalls;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

final class CommunityUpgradeNotifier {
    private CommunityUpgradeNotifier() {
    }

    static void finished(SkyBlockPlayer player, CommunityUpgradeId id, int tier) {
        if (id == CommunityUpgradeId.ISLAND_SIZE) {
            var island = player.getSkyBlockIsland();
            if (island != null) IslandBorder.apply(island, (int) CommunityUpgrades.valueAt(id, tier));
        }
        String scope = id.getScope() == CommunityUpgradeId.Scope.PROFILE ? "Profile" : "Account";
        player.sendMessage("<a>{} {:roman} <7>{} Upgrade has finished! <e>Visit Elizabeth to start the next tier.",
                id.getDisplayName(), tier, scope);
        AbiphoneIncomingCalls.ring(player, "elizabeth", caller -> AbiphoneIncomingCalls.speak(caller, "elizabeth",
                "Hey, " + caller.getUsername() + "! I just wanted to let you know that your " + id.getDisplayName() + " " + scope + " Upgrade has finished!",
                "That's all, bye!"));
    }
}
