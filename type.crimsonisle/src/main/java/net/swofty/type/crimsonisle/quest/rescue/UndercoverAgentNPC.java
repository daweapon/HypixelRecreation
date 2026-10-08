package net.swofty.type.crimsonisle.quest.rescue;

import lombok.Getter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crimson.quest.RescueStage;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

/**
 * The Undercover Agent at the edge of the enemy venue. Briefs the player (wiki dialogue) and starts the
 * infiltration; while it runs, offers to call it off. The "too dangerous" button label is an estimate.
 */
@Getter
public class UndercoverAgentNPC extends HypixelNPC {
    private final RescueSite site;

    public UndercoverAgentNPC(RescueSite site) {
        super((site == RescueSite.MAGE ? RescueAppearance.MAGE_AGENT : RescueAppearance.PIGLIN).configure(
                new String[]{"<e>Undercover Agent", "<e><l>CLICK"}, "<e>Undercover Agent",
                _ -> site.getAgent(), player -> {
                    RescueState state = RescueService.state(player);
                    return state != null && state.recruited(site);
                }, true, false));
        this.site = site;
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        RescueState state = RescueService.state(player);
        if (state == null || !state.recruited(site)) return;
        if (state.stage() == RescueStage.RECRUITED) {
            setDialogue(player, "brief").thenRun(() -> RescueService.begin(player));
            return;
        }
        NPCOption.sendOption(player, "undercover_agent", true, List.of(
                new NPCOption.Option("cancel", NamedTextColor.RED, false, "It's too dangerous",
                        p -> setDialogue(p, "cancel").thenRun(() -> RescueService.abandon((SkyBlockPlayer) p, null)))));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        String name = player == null ? "" : player.getUsername();
        return new DialogueSet[]{
                DialogueSet.builder().key("brief").lines(
                        "Pst, hey " + name + "! You must be the one they sent to get our guy back.",
                        "Here's what you need to do.",
                        "You need to sneak through this area and avoid the guards.",
                        "You need to stay out of their <e>sight<f>.",
                        "They also have remarkable <e>hearing<f>. <e>Sneaking <f>will be much harder for them to detect than <e>sprinting<f>.",
                        "If you're observant, you might be able to <e>distract <f>some of them.",
                        "If it's too dangerous for you, tell me. If you leave the area, we'll assume you've abandoned the mission and find someone else instead.",
                        "Good luck out there!").build(),
                DialogueSet.builder().key("cancel").lines(
                        "It's too dangerous for you?",
                        "Oh well, we'll find someone else. Leave before you're captured too.").build()
        };
    }
}
