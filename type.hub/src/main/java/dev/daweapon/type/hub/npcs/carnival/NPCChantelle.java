package dev.daweapon.type.hub.npcs.carnival;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.hub.carnival.CarnivalSkin;
import dev.daweapon.type.hub.gui.carnival.GUICarnivalGoals;
import dev.daweapon.type.skyblockgeneric.carnival.Carnival;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalNpc;
import dev.daweapon.type.skyblockgeneric.carnival.CarnivalProfile;
import dev.daweapon.type.skyblockgeneric.carnival.Carnivals;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

// Tracks Carnival Goals and game stats.
public class NPCChantelle extends HypixelNPC {

    public NPCChantelle() {
        super(new CarnivalNPCConfiguration("Chantelle", CarnivalSkin.CHANTELLE, new Pos(-83.5, 72, 11.5, 180, 0)));
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player) || !Carnival.isOpen()) return;
        CarnivalProfile profile = Carnivals.profile(player);
        if (profile.hasMet(CarnivalNpc.CHANTELLE)) {
            player.openView(new GUICarnivalGoals());
            return;
        }
        setDialogue(player, "introduction").thenRun(() -> {
            CarnivalProfile met = Carnivals.profile(player);
            met.meet(CarnivalNpc.CHANTELLE);
            Carnivals.save(player, met);
            player.openView(new GUICarnivalGoals());
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("introduction").lines(
                        "Ever feel like you're being watched?",
                        "Like your every action is being recorded and monitored?",
                        "Well, that sure is the case here at <e>The Carnival<f>...",
                        "...but it's just so we can track your <6>stats <f>and let you complete <6>challenges <f>for <a>rewards<f>!",
                        "Don't think too hard about it.").build()
        };
    }
}
