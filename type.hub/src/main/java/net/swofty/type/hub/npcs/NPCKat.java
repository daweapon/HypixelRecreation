package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.StringUtility;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.kat.GUIKat;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointKat;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.KatComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.pets.KatService;

public class NPCKat extends HypixelNPC {

    public NPCKat() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Kat", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "vPok0YXlbGxwdgMYI8WExOL1dfYlF47ISzPRMUr5g9nvtiHdTktB45gT7hTB45VwsAScDIgKOHl2fo/jlLCW01xtsHlczuvjurLvtyrQj8ipjfFr/RZTWUF1ZdXM2C5fRWa0etz4UQeQwcmKOnD+NUOKux3GJfhN7el7xYI2fp6nRDCqIcyAW/MIL7UhqdoRCUDIFgymYBQ04AVDIZ59nZjaqdBS+5oUQ8AkJkcZSThx0CpCdj24/xeyNlTgx9I0jwvIrf9AP+cc8hxrEIVhy9sMH9lHmIhbsdiGYlgMBFjfvdp9KOrFyWSjUyeqHstNXZ8C5v2ieWDy4u+FYtG485vylOQPo5CRitRdNBBbiMsL5rroVumLKroh0gyZGLlYHYAq0CmWST6wbizPpcE3tAAvYdiPCEMHpTYWiPAuKckBeSWaVL3t1MZuQoRM5aV8QeDGoe6QhYBsXVxrPDxRX05nZ5sDII8Uht6r8u0uk21ejvswnSGXH9Rpjoy85YKTDLtSzXYmzk/YmpfPUWB9YnXeARCJYO+kNN6QciDFTt+e5XtSKz4o9ejbvWu3Y74CXnWYcEs6Vqu/zztYfq47roCdTP/TVGD311pRxZvjS5PaQdEbrkLCFblOhGOcxg0IFbs/zlz8bemBdQZyJIzR3IwgIH1tcv4GEMs20rTrP9k=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU4ODcyMTU5ODM4OCwKICAicHJvZmlsZUlkIiA6ICI5ZThiODc0N2EzMTc0NGU2OGE0ODcxMzM0MDNiNGQzNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJFdmlsRGN0ciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lYWFlODEyNzc1NzA2YjU2NTk2NjgzNDg2YWJlYWY4NTdlYTFjMDY4Y2IzNGE4YmMxZGU5YTU3YzYzOGNmNDExIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(10.5, 72, -53.5, 45, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        DatapointKat.PlayerKat katData = player.getKatData();
        SkyBlockItem heldItem = new SkyBlockItem(player.getItemInMainHand());
        boolean waiting = katData.getPet() != null && katData.getFinishTime() > System.currentTimeMillis();
        if (heldItem.hasComponent(KatComponent.class) && waiting) {
            long reducedTime = (long) heldItem.getComponent(KatComponent.class).getReducedDays() * 86400000;
            katData.setFinishedTime(katData.getFinishTime() - reducedTime);
            player.setItemInMainHand(heldItem.getAmount() > 1
                    ? player.getItemInMainHand().withAmount(heldItem.getAmount() - 1) : ItemStack.AIR);
            player.sendMessage("<a>Kat will be done with your pet sooner!");
        } else if (katData.getPet() != null) {
            if (waiting) {
                player.sendMessage("<e>[NPC] Kat<f>: I'm still taking care of your {}<f>! Come back in {}.",
                        katData.getPet().getDisplayName(),
                        StringUtility.formatTimeLeftWrittenOut(katData.getFinishTime() - System.currentTimeMillis()));
            } else {
                SkyBlockItem pet = KatService.collect(player);
                if (pet == null) return;
                player.addAndUpdateItem(pet);
                player.sendMessage("<e>[NPC] Kat<f>: Here's your {}<f>, all grown up!", pet.getDisplayName());
            }
        } else {
            new GUIKat().open(player);
        }
    }
}
