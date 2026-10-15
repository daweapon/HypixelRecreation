package dev.daweapon.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.gui.lucius.GUILucius;
import dev.daweapon.type.hub.gui.lucius.LuciusTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCLucius extends HypixelNPC {

    public NPCLucius() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Lucius", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "rXiabawGlB76QvS4asOzdGVmgi3FNkjYA5E00KV9W4mZthyMdJuDNRD3ozvtKXPVUmW2vbHgitViegTlW9UQxvg5dtwsfFXLZ9/II7mehp24FI1GDoInABpqEWVAuz9es+z5sDvmayvhjcRdvExskD7YO2CrYdwo4PZ0+l/axDAs18hVzpZWRq+LswBYbANOo7P0gl3HG4lrpv3kuu4HQsotahfryt1+y+cN+T3FbCljuhL3yn672L9DfUDyW/BFWadWYPuw4SHNYhUCcYUh8aT7+3dYwzl4dz0RymlVU3ghJkljwNq3cvtnvNRCH/nui0waHKEhbUN+fx6n7OmyEIjrRocCJtIpoEKpmXEsHw8Kqa8x1KdPt7fIJsrViEaXuJl5F62oiqINjz5qGClVpzagjhe47a3mgzQFZi6W/BchksWjJml244qWF6Hy41V1viekc3aladLdfNz0oIoQD9rNacRIlnmS6AYGcvWc8xHwPGVYeOK1GEL0Xp/RllVPD6xDzWaN7R8IJ3zsDqo+v+/HX9P1xKR/EZ7wDcCRne76cASMQeROqMsSSJnpkxvUcW7VjnNkSDqx7jcSnucHJDBypcL0ZjwU7sRHm6BBabIG/HRRrGslYCWYyf/6eSt1rgs4t66ez7AVHa+Nc6dRFLRS+7bXuojto2s38FTNDjk=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NzUyNTE4NDcwMTUsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U0ZDg5NTFjZDIwOTc2OGRiNTcxNWJkYzQ0MzYyMWE1NDUyZDI1ZjRlYjViNmExYzBlNzI4NmIxODQyOTQxZjkifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(125, 73, 165, 30, 0);
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
        if (isInDialogue(player)) return;

        int purchases = LuciusTier.darkAuctionPurchases(player);
        if (purchases == 0) {
            setDialogue(player, "introduction");
            return;
        }

        LuciusTier next = LuciusTier.next(player);
        if (next == null) {
            setDialogue(player, "family").thenRun(() -> player.openView(new GUILucius()));
            return;
        }
        if (purchases < next.getRequiredPurchases()) {
            setDialogue(player, "come-back");
            return;
        }
        player.openView(new GUILucius());
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        if (!(player instanceof SkyBlockPlayer skyBlockPlayer)) {
            return new DialogueSet[]{DialogueSet.ofTranslation("introduction", "npcs_hub.lucius.dialogue.introduction")};
        }
        int purchases = LuciusTier.darkAuctionPurchases(skyBlockPlayer);
        LuciusTier next = LuciusTier.next(skyBlockPlayer);
        int target = next == null ? purchases : next.getRequiredPurchases();
        return new DialogueSet[]{
                DialogueSet.ofTranslation("introduction", "npcs_hub.lucius.dialogue.introduction"),
                DialogueSet.ofTranslation("come-back", "npcs_hub.lucius.dialogue.come_back", purchases, target),
                DialogueSet.ofTranslation("family", "npcs_hub.lucius.dialogue.family", purchases)
        };
    }
}
