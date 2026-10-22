package dev.daweapon.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.NPCOption;
import net.kyori.adventure.text.format.NamedTextColor;
import java.util.ArrayList;
import java.util.List;

public class NPCLoras extends HypixelNPC {
    private static final String OPTIONS = "loras";


    public NPCLoras() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Loras", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "m4+5XcihPvzB9ui4Putmn7dSHlIR7Fm0LRA7dB2vC+6ZgD9LHVp+JB6W5xlET0XuJKt+fbRdD/QYpkxtfpcIQOYdQgUFlpHsxhM9qyUtStNh3XdaJmInojmgWAVpbPLcVbaAwC5vKs3VRaygMwIwDkRmIZah2JDODFQr341D6FWIzE2WW+3qi2cd8FAcBaWLcwZxcFcVB6c7Q/W3tu38rUl39LlWvC2ETzH8iwfQzpbJ4tyYFAglFQjwtvP1X7yex5Yw+4CsZPQHS8GQzUCGAuQJgsVRCg0ZFM/3C1PrOU9pS7qYTj8zRks2yja7z0D3MYtR2o47Tl7sM2f1QEcPEBUCFFZA0+03dze/sxHwiDhSAKGaqU8uQWjDcaT2+dtJepzzYqvg3o7Aa9okrARpfelWSuACEnl8Zv3SVOAs3HS98KUjQ0HysZhMfq6SlYKxxhSIu3hOcRSnE5NE8M8xR+eBvCde+nmKs0uhhDi/yZ2+WN3S81LwECSrPAGsGtQjvhRi21ITnx6wGYXwhRgecy76UVTN3qaf9UvJbCV+tQq7PykIOs7n8mnHNKkW/j4gd1RRu9SqOG+2Ntym21TpYmjXVdvUWkMaFZjMV9N9FryDUfq83Fc3XoztonjpEyD8lrwsX3Fa5ypAmmpM9hcK59dkA8H624yReTx0TZ1XDuA=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxOTMyOTA0MDEzMSwKICAicHJvZmlsZUlkIiA6ICJiOThjNGFkOGM2MjY0NGYxOWUzYzhhZTE0ZThhMDI1OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5cnRlZGR5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2IzZjFhNGYwMGNiYjBiMDRhNzgwZjQ3MjBmZGM1Y2NjNzY4MjhhMTI5YmU5NTBkNTZlNWY3OWI1NDQ1M2ZlNTEiCiAgICB9CiAgfQp9";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<b>Loras");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-593.5, 118, 14.5, -90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    private record Step(String[] lines, String option) {
    }

    private static final List<Step> STEPS = List.of(
            new Step(new String[]{
                    "Greetings, traveler.",
                    "I'm Loras, a historian dedicated to uncovering the secrets of the <5>Precursors<f>.",
                    "Sadly, their history is shrouded in mystery, with scarce records surviving.",
                    "I've had to piece together their story through fieldwork and ancient relics.",
                    "My dream? To pen a comprehensive tome about them, but the pieces are still falling into place.",
                    "Interested in what I've uncovered so far?"}, "Yes"),
            new Step(new String[]{
                    "Ah, a fellow history enthusiast! Excellent.",
                    "Through my research, I've deducted that the Precursors were this world's first inhabitants.",
                    "Remarkably, they lived under the watch of multiple entities called <9>Starborns<f>!",
                    "I've identified at least eight Starborns, but I suspect there were more.",
                    "Each Starborn had a unique domain and wielded immense power."}, "What kind of domain?"),
            new Step(new String[]{
                    "From the Starborn of the Forests or the Sea, even a Starborn of Life!",
                    "It's unclear whether the Precursors were creations of these Starborns, but they certainly revered them.",
                    "My studies led me to distinguish two categories: Old and New Starborns."}, "Not very original."),
            new Step(new String[]{
                    "Ah, but the names reflect a theory.",
                    "The Old Starborns seemen indifferent to mortal affairs, transcending earthly desires.",
                    "Conversely, the New Starborns engaged actively with the Precursors, reveling in their advancements and guiding their progress.",
                    "I believe that immortals who maintain worldly interests and desire could be considered 'young' in a divine sense."}, "It's an interesting theory"),
            new Step(new String[]{
                    "Much appreciated.",
                    "The New Starborns were particularly invested, almost as proud mentors observing their wards' milestones.",
                    "They even intervened directly at times, teaching essential lessons like substainable living and the imporance of nature's balance."}, "So the Starborns were spectating them"),
            new Step(new String[]{
                    "Precisely, but also guiding.",
                    "For instance, they introduced magic to the Precursors, a decision with unforeseen consequences.",
                    "The Precursors ended up excelling in both magic and science, eventually blending the two to create wonders."}, "I see where this is going"),
            new Step(new String[]{
                    "Their ambition grew boundless.",
                    "They sought more: longer life, greater strength, boundless energy.",
                    "Later generations of Precursors, born into a world of accomplishments, yearned for purpose and legacy.",
                    "For those young generations of Precursors, it became impossible to define their own purpose in the shadow of monumental achievements.",
                    "Something had to happen, the wheel of progress had to keep turning.",
                    "So one day, a bold claim arose: the Precursors had surpassed the Starborns.",
                    "They saw the Starborns' teachings as shackles, hindering their true potential."}, "Those Precursors are very human after all"),
            new Step(new String[]{
                    "Indeed... But this hubris marked the beginning of the end.",
                    "The Precursors harnessed their tools and magic to overpower the Starborns.",
                    "Adding to this, the Starborns' power was intrinsically tied to the belief of their followers.",
                    "As the Precursors' faith diminished, so too did the might of the Starborns, weakening them in this crucial confrontation."}, "Can Starborns even die?"),
            new Step(new String[]{
                    "That remains a mystery. But it's clear that their influence faded.",
                    "As for the Precursors, they vanished without a trace, a puzzling paradox."}, "So how does the story end?"),
            new Step(new String[]{
                    "Here's my theory: aware of their impending doom, the Starborn of Time, perhaps the most powerful due to universal belief, made a desperate move.",
                    "He propelled the Precursors far into the future, hoping that by then, others could stand against them."}, "The future? When exactly?"),
            new Step(new String[]{
                    "That's the unknown.",
                    "But it suggests that the Precursors might reemerge one day, a thought both fascinating and daunting."}, "So the Precursors will just appear out of nowhere one day?"),
            new Step(new String[]{
                    "It's a possibility we cannot ignore.",
                    "We must be prepared for whatever comes.",
                    "Thank you for lending an ear to my life's work.",
                    "Now, I must return to my studies."}, null));

    @Override
    public void onClick(NPCInteractEvent event) {
        advance(event.player(), 0);
    }

    private void advance(HypixelPlayer player, int index) {
        if (isInDialogue(player)) return;
        setDialogue(player, "step-" + index).thenRun(() -> {
            String option = STEPS.get(index).option();
            if (option == null) return;
            List<NPCOption.Option> options = new ArrayList<>();
            options.add(new NPCOption.Option("next", NamedTextColor.GREEN, true, option, chosen -> advance(chosen, index + 1)));
            if (index == 0) options.add(new NPCOption.Option("later", NamedTextColor.RED, true, "Later, maybe", chosen -> setDialogue(chosen, "later")));
            NPCOption.sendOption(player, OPTIONS, true, options);
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        List<DialogueSet> sets = new ArrayList<>();
        for (int index = 0; index < STEPS.size(); index++) sets.add(DialogueSet.builder().key("step-" + index).lines(STEPS.get(index).lines()).build());
        sets.add(DialogueSet.builder().key("later").lines(
                "Understandable. History has a way of capturing us when we least expect it.",
                "Feel free to return if curiosity strikes.").build());
        return sets.toArray(DialogueSet[]::new);
    }
}
