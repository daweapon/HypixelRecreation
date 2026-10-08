package net.swofty.type.crimsonisle.npcs.kuudra;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crimson.kuudra.KuudraAvailability;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerProgress;
import net.swofty.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * The Weird Sailor lives in Kuudra's Hollow, which this map doesn't have, so he stands on the
 * nearest open ground to his wiki spot (-131, 93, -177): the netherrack ledge north of the
 * Stronghold. Fishing up the Wet Napkin beside him needs a won Kuudra run, so it stays pending.
 * His skin isn't documented; a fisherman villager stands in.
 */
public class NPCWeirdSailor extends HypixelNPC {
    private static final Pos POSITION = new Pos(-276.5, 115, -454.5);

    public NPCWeirdSailor() {
        super(new VillagerConfiguration() {
            @Override
            public VillagerProfession profession() {
                return VillagerProfession.FISHERMAN;
            }

            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Weird Sailor", "<e><l>CLICK"};
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<e>Weird Sailor");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return POSITION;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return player instanceof SkyBlockPlayer skyBlockPlayer && KuudraFollowerRules.npcsVisible(skyBlockPlayer);
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        setDialogue(player, "talk").thenRun(() -> {
            if (KuudraFollowerProgress.isBlessed(player)) player.sendMessage(KuudraAvailability.UNAVAILABLE_CHAT);
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{DialogueSet.builder().key("talk").lines(lines(player)).build()};
    }

    private static String[] lines(HypixelPlayer viewer) {
        SkyBlockPlayer player = viewer instanceof SkyBlockPlayer skyBlockPlayer ? skyBlockPlayer : null;
        List<String> lines = new ArrayList<>();
        lines.add("hi");
        if (player != null && KuudraFollowerRules.wearingElegantTuxedo(player)) lines.add("you have elegant taste in fashion");
        lines.add("u want to know a secret?");
        lines.add("...");
        lines.add("i can hold my breath under lava for 15 seconds!");
        if (player != null && KuudraFollowerRules.wearingRespirationHelmet(player)) lines.add("seems like u can too!");
        lines.add("...");
        lines.add("i heard a legend there was a big fish in these waters");
        lines.add("mayb u could fish it up?");
        if (player != null && KuudraFollowerRules.holding(player, "ICE_BAIT")) lines.add("try using dat bait u hav");
        lines.add("...");
        lines.add("anyway... i have a train to catch");
        lines.add("im goin 2 da circus");
        if (player != null && KuudraFollowerRules.wearingBonzoMask(player)) lines.add("u can join me if u like");
        lines.add("bai (⌐■_■)\\/");
        return lines.toArray(String[]::new);
    }
}
