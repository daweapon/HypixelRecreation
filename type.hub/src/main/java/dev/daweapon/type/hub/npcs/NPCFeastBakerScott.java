package dev.daweapon.type.hub.npcs;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.shop.catalogue.CatalogueShopView;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCFeastBakerScott extends HypixelNPC {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTU1NGY2YzRmODFiMGEwYzg5MzUzMDYyOTk1Mjk0YmI2YWE3NTViMzJmZDE2NjI5NDk3N2I4ZDZkNDVhNDliYSJ9fX0=";
    private static final String SPOKEN = "npc.feast_baker_scott.spoken";

    public NPCFeastBakerScott() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Feast Baker Scott", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(77.5, 72, -83.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (ChapterTriggers.done(player, SPOKEN)) {
            player.openView(new CatalogueShopView("grand_bakery"));
            return;
        }
        setDialogue(player, "hello").thenRun(() -> {
            ChapterTriggers.fire(player, SPOKEN);
            player.openView(new CatalogueShopView("grand_bakery"));
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{DialogueSet.builder().key("hello").lines(
                "Welcome to the <c>Grand Bakery<f>!",
                "Whenever you help season the <e>Communal Stew <f>during a <d>Grand Feast<f>, you'll earn a <e>Kernel<f>.",
                "Those <e>Kernels <f>can be baked here into a variety of items!").build()};
    }
}
