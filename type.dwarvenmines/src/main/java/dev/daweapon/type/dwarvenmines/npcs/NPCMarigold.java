package dev.daweapon.type.dwarvenmines.npcs;

import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterTriggers;
import dev.daweapon.type.skyblockgeneric.essence.EssenceType;
import dev.daweapon.type.skyblockgeneric.essence.Essences;
import dev.daweapon.type.skyblockgeneric.gui.inventories.essence.GUIEssenceShop;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;

public class NPCMarigold extends HypixelNPC {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2NiMDU5NzU4MmY1YTZjMmY0NDVkOGRhNjk3MWIzZjNlOGJiZDljYzI5NjhmZDYzMDc1ZDM0YWJiMDJjMWNiMyJ9fX0=";
    private static final String GOBLIN_ACHIEVEMENT = "skyblock.goblin_killer";
    private static final String SPOKEN = "npc.marigold.spoken";
    private static final String THANKED = "npc.marigold.thanked";
    private static final int THANKS_ESSENCE = 25;

    public NPCMarigold() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Marigold", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "<6>Marigold";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(181.5, 150, 60.5, 90, 0);
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
        if (isInDialogue(player)) return;
        boolean goblinSlain = player.getAchievementHandler().getProgress(GOBLIN_ACHIEVEMENT) > 0;
        if (ChapterTriggers.done(player, THANKED)) {
            player.openView(new GUIEssenceShop(EssenceType.GOLD));
            return;
        }
        boolean firstTime = !ChapterTriggers.done(player, SPOKEN);
        ChapterTriggers.fire(player, SPOKEN);
        if (!goblinSlain) {
            setDialogue(player, firstTime ? "first" : "waiting");
            return;
        }
        setDialogue(player, firstTime ? "first-slain" : "slain").thenRun(() -> {
            if (ChapterTriggers.done(player, THANKED)) return;
            ChapterTriggers.fire(player, THANKED);
            Essences.add(player, EssenceType.GOLD, THANKS_ESSENCE);
            player.openView(new GUIEssenceShop(EssenceType.GOLD));
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        String intro1 = "Hey! I'm <6>Marigold<f>! I don't recall seeing you around these parts before...";
        String intro2 = "Now normally, I run a humble little shop that allows you to upgrade some useful perks in exchange for <6>Gold Essence<f>!";
        String intro3 = "However, a pesky <6>Golden Goblin <f>just came by and snatched a bunch of my <6>Gold Essence<f>! How can I operate safely with <c><o>thieves <r><f>like that running around?";
        String request = "Speak to me again after killing a <6>Golden Goblin <f>and I'll show you my shop! I heard <6>Golden Goblins <f>have a small chance to spawn when <b>mining ores <f>or throwing <b>Goblin Eggs <f>in the <b>Dwarven Mines <f>or <5>Crystal Hollows<f>.";
        String gift = "As a token of my gratitude, here's another <6>25 Gold Essence <f>to get you started at my shop! It's not a lot, but a little goes a long way!";
        String show = "Let me show you the <6>Gold Essence Shop<f>!";
        return new DialogueSet[]{
                DialogueSet.builder().key("first").lines(intro1, intro2, intro3, request).build(),
                DialogueSet.builder().key("waiting").lines(request).build(),
                DialogueSet.builder().key("first-slain").lines(intro1, intro2, intro3,
                        "Speak to me again after killing a-",
                        "wait... your <a>Bestiary <f>shows that you've already killed a <6>Golden Goblin<f>!",
                        "I can't thank you enough! " + gift, show).build(),
                DialogueSet.builder().key("slain").lines(
                        "<a>Amazing! <f>Thanks for the help - those pesky <6>Golden Goblins <f>seem to appear all the time!",
                        gift, show).build()
        };
    }
}
