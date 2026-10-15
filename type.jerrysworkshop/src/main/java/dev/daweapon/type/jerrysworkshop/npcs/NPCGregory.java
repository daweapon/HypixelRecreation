package dev.daweapon.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.jerrysworkshop.gui.GUIShopGregory;
import dev.daweapon.type.skyblockgeneric.fishing.winter.ReindrakeMob;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Gregory the Opportunist stands at Jerry Pond only while a Reindrake is alive and leaves when it
// dies. His Dragon's Nest appearance during Ender Dragon fights isn't built (no dragon fight here).
public class NPCGregory extends HypixelNPC {
    private final Set<UUID> greeted = ConcurrentHashMap.newKeySet();

    public NPCGregory() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Gregory", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgyMmQ4ZTc1MWM4ZjJmZDRjODk0MmM0NGJkYjJmNWNhNGQ4YWU4ZTU3NWVkM2ViMzRjMThhODZlOTNiIn19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-72.5, 78, 28.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return ReindrakeMob.anyAlive();
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        if (!ReindrakeMob.anyAlive() || isInDialogue(player)) return;
        if (greeted.add(player.getUuid())) {
            setDialogue(player, "intro").thenRun(() -> player.openView(new GUIShopGregory()));
            return;
        }
        player.openView(new GUIShopGregory());
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("intro").lines(
                        "Hey, I'm an opportunist!",
                        "I sell Arrows and Bows for a truly overpriced amount!"
                ).build()
        };
    }
}
