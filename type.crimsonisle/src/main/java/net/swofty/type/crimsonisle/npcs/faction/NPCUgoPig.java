package net.swofty.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.crimsonisle.faction.CrimsonNPC;
import net.swofty.type.skyblockgeneric.crimson.questline.BarbarianStep;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import static net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene.ELLE;
import static net.swofty.type.skyblockgeneric.crimson.questline.CrimsonScene.UGO;

// The lost Piglin hiding in a Burning Desert cave: Ugo, turned back into a pig for lack of Sulphur.
public class NPCUgoPig extends CrimsonNPC {
    public NPCUgoPig() {
        super("<d><k>----", EntityType.PIG, new Pos(-508.5, 98, -633.5));
    }

    @Override
    protected boolean visible(SkyBlockPlayer player) {
        BarbarianStep step = CrimsonQuestlines.barbarian(player);
        return step == BarbarianStep.FIND_THE_NOISE || step == BarbarianStep.FEED_THE_PIG;
    }

    @Override
    protected String[] holograms(SkyBlockPlayer player) {
        return new String[]{"<d><k>----"};
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (CrimsonQuestlines.barbarian(player) != BarbarianStep.FEED_THE_PIG
                || player.getAmountInInventory(ItemType.SULPHUR) < 1) {
            say(player, "Oink");
            return;
        }
        if (!CrimsonQuestlines.advance(player, BarbarianStep.FEED_THE_PIG, BarbarianStep.ESCORT_UGO, 0)) return;
        player.removeItemFromPlayer(ItemType.SULPHUR, 1);
        CrimsonScene.create()
                .raw("<c>-1 Sulphur")
                .say(UGO, "You saved me!", "Memory is failing me but I think I remember getting attacked.",
                        "My leg got hurt and I had to hide here, the rest is blurry...")
                .say(ELLE, "How could you leave <c>Dragontail <f>without <2>Sulphur <f>on you!")
                .say(UGO, "I know, I was careless, I was on a mission for...", "SULPHUR! My father is going to KILL ME!!")
                .say(ELLE, "Let's not exaggerate.")
                .say(UGO, "You don't understand, my father is the <c>Chief<f>!")
                .say(ELLE, "You were not exaggerating.")
                .say(UGO, "Please help me, I have to present myself to him.")
                .say(ELLE, "Of course.", "I will shoulder you to the <c>Chief's Hut<f>.",
                        "<player> make sure monsters don't attack us on the way there.")
                .play(player);
    }
}
