package dev.daweapon.type.skyblockgeneric.communityupgrades;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import dev.daweapon.type.generic.utility.ScheduleUtility;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneNPC;
import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
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
        if (hasElizabethContact(player)) {
            ring(player);
            player.sendMessage("<e>[NPC] Elizabeth<f>: <b>✆ <f>Hey, {}! I just wanted to let you know that your {} {} Upgrade has finished!",
                    player.getUsername(), id.getDisplayName(), scope);
            player.sendMessage("<e>[NPC] Elizabeth<f>: <b>✆ <f>That's all, bye!");
        }
    }

    private static void ring(SkyBlockPlayer player) {
        player.sendMessage("<b>✆ <d>Elizabeth <f>is calling...");
        for (int pulse = 0; pulse < 3; pulse++) {
            ScheduleUtility.delay(() -> player.playSound(Sound.sound(Key.key("block.note_block.pling"),
                    Sound.Source.PLAYER, 1f, 1.6f), Sound.Emitter.self()), pulse * 8);
        }
    }

    private static boolean hasElizabethContact(SkyBlockPlayer player) {
        AbiphoneNPC elizabeth = AbiphoneRegistry.getFromId("elizabeth");
        if (elizabeth == null) return false;
        for (SkyBlockItem item : player.getAllInventoryItems()) {
            if (item.hasComponent(AbiphoneComponent.class) && item.getAttributeHandler().hasAbiphoneNPC(elizabeth)) return true;
        }
        return false;
    }
}
