package dev.daweapon.type.spidersden.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.data.datapoints.DatapointToggles;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.concurrent.ThreadLocalRandom;

// Haymitch, found at the Spider's Den spawn, gives tips about the island.
public class NPCHaymitch extends HypixelNPC {
    private static final int COMBAT_LEVEL_FOR_TIPS = 12;

    public NPCHaymitch() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<f>Haymitch",
                        "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "ZiQozDQddFh5n6/fx2kg3PNFpJdk8Vr063P1KfoYd+aNtwIr3hH2yUjzJgoZiV/75j0EsSn8Iq1hcvGg/KsAr9v1mJEX2/EJiUU86F++Hj+uGu3JZN7BixBD3GOSLsgfzgatuxMDPhrZumq6bg/qmBBG8+QyVVxjju24fCm4/K9pBPrJDh5khSTVOY+01oT1uxg1eW4Yv3Rmd0Wq2Pf12evJPPnj/eTGAdz5peSV/M5O4T/C99PVIZ5yTYfBsX9m7TJWZ980/ERmTFeZS3yJcn6NKwqhHenQjUFS2AnbS52MrX5h6GzvTow394lPMgxN9S0uNgswOh2XLuGdHVfhrhXYUs4jbGRMIxlcTtNI7vmG3wZOxUpKCq2ZuUKimv9zHrlQ+TWE9kjZ8wvlMNjyvWA2QZkSjhMAFB6QMB+inBW5r5BNIiHRu8AAy4I5mdoZe2PCrxt8FWf1kiJcWeFVj+4nso9Lbkx1+NXo8z82DONyxrIKj+Evi65xCOsPmQD0fOivycuryn5BtdnKIVY4xrL8VKJ2L6vAgSL2Y7KBGeCnXe8msdt5yxj+VcGI++OTR4neBrQ3onXd3EKKyEGHmD8x0hm+nkIdje86awaMpB0pmQMWusxxR233gSBTZ8kmHwHUK7yv6uS5MOZecxrE3BerVF7YUrh3+xjtRMN83lM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTk2ODMzNDU1NTksInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2YjlmNTZiODE3Njc2ZDUxMzU1NjU4NGIzMmJiOWM2YWEwZGRkNTEyZDFkMWFhZGZlNmE1OGEyOTA1MmZlZjgifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-202.5, 83, -236.5, 0, 0);
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
        DatapointToggles.Toggles toggles = player.getToggles();
        if (!toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_HAYMITCH)) {
            setDialogue(player, "initial-hello").thenRun(() ->
                    toggles.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_HAYMITCH, true));
            return;
        }
        if (!toggles.get(DatapointToggles.Toggles.ToggleType.HAS_ENTERED_SPIDER_MOUND)) {
            setDialogue(player, "before-mound-" + (1 + ThreadLocalRandom.current().nextInt(2)));
            return;
        }
        if (!toggles.get(DatapointToggles.Toggles.ToggleType.HAS_HEARD_HAYMITCH_MOUND_PRAISE)) {
            setDialogue(player, "mound-praise").thenRun(() ->
                    toggles.set(DatapointToggles.Toggles.ToggleType.HAS_HEARD_HAYMITCH_MOUND_PRAISE, true));
            return;
        }
        if (player.getSkills().getCurrentLevel(SkillCategories.COMBAT) < COMBAT_LEVEL_FOR_TIPS) {
            setDialogue(player, "combat-low");
            return;
        }
        setDialogue(player, "combat-" + (1 + ThreadLocalRandom.current().nextInt(4)));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("initial-hello").lines(
                        "There's lots to do in the <4>Spider's Den<f>.",
                        "Behind me is the <4>Spiders Mound<f>.",
                        "Be careful, though! The <4>Broodmother<f> has made the summit home, and she's quite tenacious!").build(),
                DialogueSet.builder().key("before-mound-1").lines(
                        "Try to reach the top of the <c>Spider Mound<f>.",
                        "Once there, test yourself against the <4>Broodmother<f>, if you're up to the challenge!",
                        "Be careful, though. The <4>Broodmother<f> has made the summit her home, and she's quite tenacious!").build(),
                DialogueSet.builder().key("before-mound-2").lines(
                        "There's lots to do in the <c>Spider's Den<f>.",
                        "Behind me is <c>Spider Mound<f>. Many like to test their skills by trying to reach the top of it.",
                        "Be careful, though. The <4>Broodmother<f> has made the summit her home, and she's quite tenacious!").build(),
                DialogueSet.builder().key("mound-praise").lines(
                        "Wow you're quite strong!",
                        "Unfortunately, there's always bigger fish. Or, in this case, a bigger spider.",
                        "But, for now, you should hone your skills until you reach <a>Combat Skill Level 12<f>, and then travel to <5>The End<f>.").build(),
                DialogueSet.builder().key("combat-low").line(
                        "Hone your skills until you reach <a>Combat Skill Level 12<f>, and then travel to <5>The End<a>!").build(),
                DialogueSet.builder().key("combat-1").line(
                        "The gravel on this island seems to magically reappear when you break it. I don't ask questions.").build(),
                DialogueSet.builder().key("combat-2").line(
                        "Those Dashing Spiders are pretty obnoxious. I find a well-placed arrow can help with that.").build(),
                DialogueSet.builder().key("combat-3").line(
                        "Broodmother, Broodfather, Arachne... There are so many dangerous spiders around here.").build(),
                DialogueSet.builder().key("combat-4").line(
                        "Slimes seem to spawn on the hills when it <b>rains<f>. They're a bit more dangerous during a <6>thunderstorm<f>.").build(),
        };
    }
}
