package dev.daweapon.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.moonglade.flames.BlueFlamesTrial;
import dev.daweapon.type.skyblockgeneric.moonglade.flames.BlueFlamesTrials;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import java.util.ArrayList;
import java.util.List;

public class NPCBrian extends HypixelNPC {

    public NPCBrian() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<3>Brian", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "WMwIZxxswxyvaQNLOE9y4OwBoaBPiy9wu8CZ9zmYfC8ZspOR7JJTTSJc4ujkHzlijTqC9tLmPKzFKX0gpQj+pDnr3Wm9ZdC/AYhvpAjy+msQmOUba5RU5qQgtI7NaYn88bWcpThyfILRZuHNfF1HXF/j1XkeV1xALV99Qe0EepthYytFu2tz5E0WvLn0q92037cwDjKDTQDp1sgRXnuHtpVIaorSAzOhdYQwPu5kJy7Ksd+VN/f+0mL5jyERfO9Nh4YJDn6JWomrHtq7vtGFUD7M8K/k2nJN2nk4nyE0yR/hVS/gUnOuAmop5ayQOOJCiSdjnT5cS+mLyotCb1xa6WZjK3AcB/1dJg0vtALxVZ5hdk/4bKpqccNR8uiP+kpW7aKF1LdpvQSshXlKMkrBCz2pvbgtOOy98OXnTXeF6cTYsu4pqaVQHmpILwCHx6fBxBaR7+nEr2AWIsNkt0iWnSWusKLzkw7fEeNiUnWlClB/LCO8/AOcdg3G6Zxn9vUKceG+1BcOOsJ9ObUL3ds4KQYkBUUzTfkBBJSZb/A6X0oBdSWpwmYirJEc5B45kx83Iz1ebS93FM8Iypmetd1lCr+gDrOe540HH5W1fLpnZTmIRtwx5788lFcpBVhwjJ1dXIwWI8Fr5DoY/3seBbdq0zLDmY33P3hNXly028PNqDQ=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0MTA5ODk1NjU1MSwKICAicHJvZmlsZUlkIiA6ICJlNzdlMDRhNmMyMzg0MGVmODZlOGViOWY4NGQ3OWVjNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJMT1JJU0pUMTYiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNlNTI0ZDE0ZjZmYmU3NjRmODdmYjNhMjUxMDkzMzVlNDk0NjA2ZjlkZGQxMTkzZWIwNGIwMWJlOWQ5MWYxNyIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<3>Brian");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-628.5, 117, 47.5, -45, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
        BlueFlamesTrial.start();
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (!BlueFlamesTrial.met(player)) {
            setDialogue(player, "first-interaction").thenRun(() -> BlueFlamesTrial.markMet(player));
            return;
        }
        if (BlueFlamesTrial.badgePending(player)) {
            setDialogue(player, switch (BlueFlamesTrial.claimBadge(player)) {
                case GIVEN_FIRST -> "given-first";
                case UPGRADED -> "upgraded";
                case NEEDS_BADGE_IN_HAND -> "hold-badge";
                case NEEDS_MATERIALS -> "materials";
                case NOTHING_PENDING -> "idle";
            });
            return;
        }
        int done = BlueFlamesTrial.completed(player);
        if (done == 1 && !ChapterTriggers.done(player, BlueFlamesTrial.TIER_ONE_TALK_TRIGGER)) {
            setDialogue(player, "after-first").thenRun(() -> ChapterTriggers.fire(player, BlueFlamesTrial.TIER_ONE_TALK_TRIGGER));
            return;
        }
        setDialogue(player, done >= BlueFlamesTrials.TRIAL_COUNT ? "all-complete" : "idle");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
        List<DialogueSet> sets = new ArrayList<>();
        sets.add(DialogueSet.builder().key("first-interaction").lines(
                "Hey! I'm the leader of the <3>Soul Campfire Cult<f>!",
                "Survive <a>10s <f>in the <c>campfire <f>to earn one!").build());
        sets.add(DialogueSet.builder().key("after-first").lines(
                "Wow! You survived the <3>soul campfire<f>!",
                "The soul campfire isn't quite as strong as a regular <c>campfire<f>. It respects your <a>❈ Defense<f>!",
                "Try to survive the <3>Trial of Blue Flames II<f>, and I'll give you the Soul Campfire Initiate Badge I!").build());
        sets.add(DialogueSet.builder().key("given-first").lines(
                "Wow! That campfire seems to scare off most foragers, but not you!",
                "Here's your Soul Campfire Initiate Badge I!",
                "Complete the <3>Trial of Blue Flames III<f>, and I'll upgrade your badge!").build());
        sets.add(DialogueSet.builder().key("hold-badge").lines(
                "<c>I need you to hold your <f>Soul Campfire Badge <c>so I can upgrade it!").build());
        sets.add(DialogueSet.builder().key("all-complete").lines(
                "Congratulations on beating every single <3>Trial of Blue Flames<f>! You're the real fire guy, hotshot!").build());
        sets.add(upgraded(skyBlockPlayer));
        sets.add(materials(skyBlockPlayer));
        sets.add(idle(skyBlockPlayer));
        return sets.toArray(DialogueSet[]::new);
    }

    private static DialogueSet upgraded(SkyBlockPlayer player) {
        int done = BlueFlamesTrial.completed(player);
        int level = BlueFlamesTrial.claimed(player);
        DialogueSet.DialogueSetBuilder builder = DialogueSet.builder().key("upgraded")
                .line("<a>Congrats! <f>Your Soul Campfire Badge was upgraded to <a>{} {}<f>!", BlueFlamesTrials.rarityOf(level), StringUtility.getAsRomanNumeral(level));
        if (done >= BlueFlamesTrials.TRIAL_COUNT) return builder.line("Congratulations on beating every single <3>Trial of Blue Flames<f>! You're the real fire guy, hotshot!").build();
        builder.line("Try the <3>Trial of Blue Flames {} <f>for a greater challenge!", StringUtility.getAsRomanNumeral(done + 1));
        int until = BlueFlamesTrials.trialsUntilRarityUp(done);
        if (until == 1) builder.line("The rarity of your item will be upgraded on the next trial!");
        else if (until > 1) builder.line("The rarity of your item will be upgraded in <a>{} <f>trials!", until);
        return builder.build();
    }

    private static DialogueSet materials(SkyBlockPlayer player) {
        int done = BlueFlamesTrial.completed(player);
        BlueFlamesTrials.Upgrade cost = BlueFlamesTrials.upgradeInto(BlueFlamesTrial.claimed(player) + 1);
        DialogueSet.DialogueSetBuilder builder = DialogueSet.builder().key("materials")
                .line("<a>Congrats on completing tier {}!", done)
                .line("You are ready for the next badge rarity!");
        if (cost != null) builder.line("To receive it, we require <a>{} <f>{}!", cost.amount(), StringUtility.toNormalCase(cost.itemId()));
        return builder.build();
    }

    private static DialogueSet idle(SkyBlockPlayer player) {
        int next = Math.min(BlueFlamesTrials.TRIAL_COUNT, BlueFlamesTrial.completed(player) + 1);
        return DialogueSet.builder().key("idle")
                .line("Next step is the <3>Trial of Blue Flames {}<f>! <7>({} damage)", StringUtility.getAsRomanNumeral(next), StringUtility.commaify((int) BlueFlamesTrials.totalDamage(next)))
                .line("Step into the campfire to start the trial and survive for <6>10s<f>!").build();
    }
}
