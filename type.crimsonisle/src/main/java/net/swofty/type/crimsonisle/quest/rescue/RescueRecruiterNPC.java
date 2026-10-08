package net.swofty.type.crimsonisle.quest.rescue;

import lombok.Getter;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.crimson.quest.RescueStage;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/** The Rescue Recruiter outside each faction's Community Center, present while a Rescue Mission is open (wiki dialogue). */
@Getter
public class RescueRecruiterNPC extends HypixelNPC {
    private final RescueSite site;

    public RescueRecruiterNPC(RescueSite site) {
        super((site == RescueSite.MAGE ? RescueAppearance.MAGE_RECRUITER : RescueAppearance.PIGLIN).configure(
                new String[]{"<e>Rescue Recruiter", "<e><l>CLICK"}, "<e>Rescue Recruiter",
                _ -> site.getRecruiter(), player -> RescueService.state(player) != null, true, false));
        this.site = site;
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        RescueState state = RescueService.state(player);
        if (state == null) return;
        if (!CrimsonReputationService.isMember(player, site.getFaction())) {
            setDialogue(player, "suspicious");
            return;
        }
        if (state.site() != site) {
            setDialogue(player, "enemy");
            return;
        }
        if (state.stage() == RescueStage.ASSIGNED) {
            setDialogue(player, "start").thenRun(() -> RescueService.setStage(player, RescueStage.RECRUITED));
            return;
        }
        setDialogue(player, "started");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        String destination = site == RescueSite.MAGE
                ? "<2>The Bastion <f>area in <c>Barbarian <f>territory"
                : "<5>Cathedral <f>area in <5>Mage territory";
        return new DialogueSet[]{
                DialogueSet.builder().key("start").lines(
                        "You want to help us rescue a hostage from those filthy brutes over there?",
                        "Perfect! Go see our <e>Undercover Agent <f>at the " + destination + " <f>to get started.").build(),
                DialogueSet.builder().key("started").lines(
                        "Thank you for your help! Go see the <e>Undercover Agent <f>for details.").build(),
                DialogueSet.builder().key("suspicious").lines("You look suspicious. I have my eyes on you...").build(),
                DialogueSet.builder().key("enemy").lines("You have a rescue with the enemy? I KNEW they were spying on us!").build()
        };
    }
}
