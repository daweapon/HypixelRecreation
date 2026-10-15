package dev.daweapon.type.skyblockgeneric.crimson.kuudra.npc;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.configuration.HumanConfiguration;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.generic.event.custom.NPCInteractEvent;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.KuudraProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.FollowerStep;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerProgress;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.KuudraFollowerRules;
import dev.daweapon.type.skyblockgeneric.crimson.kuudra.follower.TeethPlaqueStage;
import dev.daweapon.type.skyblockgeneric.gui.inventories.kuudra.GUICragSelectTier;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Crag (the Kuudra Gatekeeper until 2025-10) stands in the Forgotten Skull and the Dungeon Hub. He
 * opens Select Tier once the faction questline is done, and finishes the Teeth Plaque chain.
 */
public abstract class CragNPC extends HypixelNPC implements NPCAbiphoneTrait {
    private static final String TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmY5NGI3ZTM4ZDhhMmQ0MDQ5ZTdiMWViNDNmY2E3OGFiNDI0YzU2OTgwZWM0YjhkZWJjYmExMDM0N2E3MjZlNyIsIm1ldGFkYXRhIjp7fX19LCJwcm9maWxlSWQiOiJiMDdjMzQxNzUwZmYyODliYWI5ZmYxM2M0Y2RmMmU4NSIsInByb2ZpbGVOYW1lIjoibmVhODkiLCJpc1B1YmxpYyI6dHJ1ZSwidGltZXN0YW1wIjoxNzUyMTU4MzEzNjU4fQ==";

    protected CragNPC(Pos position) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<c>Crag", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public Text chatNameText(HypixelPlayer player) {
                return Text.of("<c>Crag");
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return position;
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
        if (canFinishPlaque(player)) {
            setDialogue(player, "plaque").thenRun(() -> finishPlaque(player));
            return;
        }
        if (!KuudraProgress.isQuestlineReady(player)) {
            setDialogue(player, "not_ready");
            return;
        }
        player.openView(new GUICragSelectTier());
    }

    private static boolean canFinishPlaque(SkyBlockPlayer player) {
        TeethPlaqueStage stage = KuudraFollowerProgress.plaqueStage(player);
        return (stage == TeethPlaqueStage.DECAYING || stage == TeethPlaqueStage.FINAL)
                && KuudraFollowerRules.holding(player, "KUUDRA_CAVITY")
                && KuudraFollowerRules.wearingFollowerArmor(player)
                && KuudraFollowerRules.ownsFollowerArtifact(player);
    }

    private static void finishPlaque(SkyBlockPlayer player) {
        if (!canFinishPlaque(player)) return;
        player.removeItemFromPlayer(ItemType.KUUDRA_FOLLOWER_ARTIFACT, 1);
        player.addAndUpdateItem(ItemType.KUUDRA_FOLLOWER_RELIC);
        KuudraFollowerProgress.complete(player, FollowerStep.RELIC_UPGRADED);
        player.sendMessage("<a>Your <6>Kuudra Follower Artifact <a>became a <6>Kuudra Follower Relic<a>!");
        player.sendMessage("<7>Your plaque is now a <c>Kuudra Teeth Plaque<7>, and Kuudra may now drop its <6>Kuudra Mandible <7>for you.");
    }

    @Override
    public String getAbiphoneKey() {
        return "crag";
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder().key("not_ready").lines(
                        "You are not ready for this fight.",
                        "Finish the <c>Crimson Isle <f>Factions questline before talking to me again."
                ).build(),
                DialogueSet.builder().key("plaque").lines(
                        "That plaque... it has rotted down to nothing.",
                        "Only a true follower of Kuudra could have brought it this far.",
                        "Hold still, adventurer. Let me show you what it really is."
                ).build()
        };
    }
}
