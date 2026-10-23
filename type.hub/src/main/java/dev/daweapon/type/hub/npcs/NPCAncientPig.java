package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.AnimalConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.technoblade.ProfileOnce;
import dev.daweapon.type.skyblockgeneric.technoblade.ShrineDialogue;
import dev.daweapon.type.skyblockgeneric.technoblade.TechnobladeShrine;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCAncientPig extends HypixelNPC {
    public NPCAncientPig() {
        super(new AnimalConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Ancient Pig", "<e><l>CLICK"};
            }

            @Override
            public EntityType entityType() {
                return EntityType.PIG;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return TechnobladeShrine.config().ancientPig();
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return TechnobladeShrine.ancientPigPresent();
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        ShrineDialogue.ancientPig(TechnobladeShrine.activeEffect()).forEach(player::sendMessage);
        if (ProfileOnce.FLYING_PIG.done(player)) return;
        ProfileOnce.FLYING_PIG.record(player);
        player.addAndUpdateItem(new SkyBlockItem(ItemType.PET_ITEM_FLYING_PIG));
        player.sendMessage("<e>[NPC] <6>Ancient Pig<f>: Take this <9>Flying Pig<f>, a gift for the one who helped the shrine.");
    }
}
