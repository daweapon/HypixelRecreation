package dev.daweapon.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class NPCLoneAdventurerNest extends AbstractLoneAdventurer {
    public NPCLoneAdventurerNest() {
        super(new Pos(-588.5, 22, -270.5, 0, 0), stage -> !stage.atFirstStand());
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = skyBlock(event.player());
        if (isInDialogue(player)) return;
        switch (LoneAdventurerProgress.stage(player)) {
            case ENDER_ARMOR -> onEnderArmor(player);
            case SHORTBOW -> onShortbow(player);
            case PEARL_DEALER_REWARD -> setDialogue(player, "visit_pearl_dealer");
            case GUIDE_WAIT -> onGuide(player);
            case GUIDE_READY -> setDialogue(player, "guide_unavailable");
            default -> { }
        }
    }

    private void onEnderArmor(SkyBlockPlayer player) {
        if (!EnderArmorSet.missing(wornArmor(player), ownedAccessories(player)).isEmpty()) {
            setDialogue(player, "armor_hints");
            return;
        }
        setDialogue(player, "dragon_story")
                .thenRun(() -> LoneAdventurerProgress.setStage(player, LoneAdventurerStage.SHORTBOW));
    }

    private void onShortbow(SkyBlockPlayer player) {
        if (!player.removeItemFromPlayer(ItemType.DRAGON_SHORTBOW, 1)) {
            setDialogue(player, "seers");
            return;
        }
        setDialogue(player, "shortbow_handed_in")
                .thenRun(() -> LoneAdventurerProgress.setStage(player, LoneAdventurerStage.PEARL_DEALER_REWARD));
    }

    private void onGuide(SkyBlockPlayer player) {
        int combat = player.getSkills().getCurrentLevel(SkillCategories.COMBAT);
        if (!LoneAdventurerStage.GUIDE_WAIT.guideUnlocked(combat, LoneAdventurerProgress.millisSinceShortbow(player))) {
            setDialogue(player, "guide_wait");
            return;
        }
        setDialogue(player, "guide_unavailable")
                .thenRun(() -> LoneAdventurerProgress.setStage(player, LoneAdventurerStage.GUIDE_READY));
    }

    private static List<String> wornArmor(SkyBlockPlayer player) {
        return Arrays.stream(player.getArmor()).filter(Objects::nonNull).map(NPCLoneAdventurerNest::typeName)
                .filter(Objects::nonNull).toList();
    }

    private static List<String> ownedAccessories(SkyBlockPlayer player) {
        return Stream.concat(
                player.getAccessoryBag().getAllAccessories().stream().map(NPCLoneAdventurerNest::typeName),
                EnderArmorSet.ACCESSORIES.stream().filter(id -> carrying(player, id))
        ).filter(Objects::nonNull).toList();
    }

    private static boolean carrying(SkyBlockPlayer player, String id) {
        ItemType type = ItemType.get(id);
        return type != null && player.countItem(type) > 0;
    }

    private static String typeName(SkyBlockItem item) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        return type == null ? null : type.name();
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("armor_hints").lines(
                        "The dragons fear one thing: those who dress like Endermen.",
                        "Bring me the full <5>Ender Armor<f>, worn: <5>Helmet<f>, <5>Chestplate<f>, <5>Leggings<f> and <5>Boots<f>, plus the <5>Ender Cloak<f>, <5>Belt<f>, <5>Necklace <f>and <5>Gauntlet<f>.",
                        "<5>Seers <f>drop the Cloak, <5>Obsidian Defenders <f>the Belt, and <5>Endermen <f>the Necklace and armor pieces.",
                        "The Gauntlet comes from <5>Ender Nodes<f>."
                ).build(),
                DialogueSet.builder().key("dragon_story").lines(
                        "Now that is a proper Ender set!",
                        "Long ago a dragon nested here, and all who tried to slay it fell to its arrows.",
                        "The <5>Seers <f>guarding the Nest carry a <5>Dragon Shortbow<f>. Kill Seers until one drops and bring it to me."
                ).build(),
                DialogueSet.builder().key("seers").lines(
                        "Kill <5>Seers <f>in the <5>Dragon's Nest <f>until a <5>Dragon Shortbow <f>drops.",
                        "<c>The Dragon Shortbow drop isn't in this server yet."
                ).build(),
                DialogueSet.builder().key("shortbow_handed_in").lines(
                        "A <5>Dragon Shortbow<f>! Marvelous.",
                        "Go and see the <9>Pearl Dealer<f>. He will give you one of your own."
                ).build(),
                DialogueSet.builder().key("visit_pearl_dealer").lines(
                        "Visit the <9>Pearl Dealer<f>. He has a <5>Dragon Shortbow <f>waiting for you."
                ).build(),
                DialogueSet.builder().key("guide_wait").lines(
                        "Reach <b>Combat 18 <f>and give me a <a>SkyBlock day <f>to collect my notes, then come back.",
                        "I will tell you everything about the <5>Ender Dragons<f>."
                ).build(),
                DialogueSet.builder().key("guide_unavailable").lines(
                        "My notes on the <5>Ender Dragons <f>are ready.",
                        "<c>The Dragon Guide isn't in this server yet."
                ).build()
        };
    }
}
