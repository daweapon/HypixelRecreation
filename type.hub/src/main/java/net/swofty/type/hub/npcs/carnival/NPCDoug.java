package net.swofty.type.hub.npcs.carnival;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.carnival.CarnivalSkin;
import net.swofty.type.hub.gui.carnival.GUISouvenirShop;
import net.swofty.type.skyblockgeneric.carnival.Carnival;
import net.swofty.type.skyblockgeneric.carnival.CarnivalNpc;
import net.swofty.type.skyblockgeneric.carnival.CarnivalProfile;
import net.swofty.type.skyblockgeneric.carnival.Carnivals;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

// Sells Carnival Masks and event perks for Carnival Tokens.
public class NPCDoug extends HypixelNPC {

    public NPCDoug() {
        super(new CarnivalNPCConfiguration("Doug", CarnivalSkin.DOUG, new Pos(-78.5, 73, 22.5, 90, 0)));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player) || !Carnival.isOpen()) return;
        CarnivalProfile profile = Carnivals.profile(player);
        if (profile.hasMet(CarnivalNpc.DOUG)) {
            player.openView(new GUISouvenirShop());
            return;
        }
        setDialogue(player, "introduction").thenRun(() -> {
            CarnivalProfile met = Carnivals.profile(player);
            met.meet(CarnivalNpc.DOUG);
            Carnivals.save(player, met);
            player.openView(new GUISouvenirShop());
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("introduction").lines(
                        "<e>Masks<f>. <a>Event <f>buffs. An <7>Accessory<f>.",
                        "You want them?",
                        "They're yours, my friend.",
                        "As long as you have enough <e>Carnival Tokens<f>.").build()
        };
    }
}
