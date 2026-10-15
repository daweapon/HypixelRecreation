package dev.daweapon.type.crimsonisle.npcs.town;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.crimsonisle.faction.CrimsonNPC;
import dev.daweapon.type.crimsonisle.gui.faction.GUIQuestClaim;
import dev.daweapon.type.crimsonisle.town.TownSkins;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static dev.daweapon.type.skyblockgeneric.crimson.town.CrimsonTownProgress.ARANYA_COOLDOWN;
import static dev.daweapon.type.skyblockgeneric.crimson.town.CrimsonTownProgress.ARANYA_FED;
import static dev.daweapon.type.skyblockgeneric.crimson.town.CrimsonTownProgress.ARANYA_LAST_MEAL;
import static dev.daweapon.type.skyblockgeneric.crimson.town.CrimsonTownProgress.ARANYA_REWARD;

/**
 * Aranya, the spider in her Wasteland den (wiki Aranya). An armorless player who talks to her is
 * eaten; if an armored player stood by her ready for this week's meal, that player gets a Chyme to
 * claim (once a week). Eating yourself works but pays nothing.
 */
public class NPCAranya extends CrimsonNPC implements NPCAbiphoneTrait {
    private static final String NAME = "<c>Aranya";
    private static final double ACCOMPLICE_RANGE = 8;
    private static final Pos SPIT_OUT = new Pos(-360.5, 80, -430.5, -180, 0);

    public NPCAranya() {
        super(NAME, TownSkins.ARANYA, new Pos(-322.5, 152, -1007.5));
    }

    @Override
    public String getAbiphoneKey() {
        return "aranya";
    }

    private static boolean armorless(SkyBlockPlayer player) {
        return List.of(EquipmentSlot.HELMET, EquipmentSlot.CHESTPLATE, EquipmentSlot.LEGGINGS, EquipmentSlot.BOOTS)
                .stream().allMatch(slot -> player.getEquipment(slot).isAir());
    }

    private static boolean hungry(SkyBlockPlayer player) {
        return CrimsonSideQuests.isReady(player, ARANYA_LAST_MEAL, ARANYA_COOLDOWN);
    }

    @Override
    protected void interact(SkyBlockPlayer player) {
        if (armorless(player)) {
            eat(player);
            return;
        }
        if (CrimsonSideQuests.is(player, ARANYA_REWARD)) {
            player.openView(new GUIQuestClaim("Claim Reward", new SkyBlockItem(ItemType.CHYME), () -> {
                if (!CrimsonSideQuests.is(player, ARANYA_REWARD)) return;
                CrimsonSideQuests.set(player, ARANYA_REWARD, 0);
                player.addAndUpdateItem(ItemType.CHYME);
            }));
            return;
        }
        if (CrimsonSideQuests.firstMeeting(player, "aranya")) {
            say(player, "Welcome to my Den!", "I'm a spider, I eat humans, I'm evil, etc... Just bear with me.",
                    "I can't eat people wearing armor like you are.", "So why don't we make a deal?",
                    "You bring me a delicious <c>meal<f>, one without any armor. I'll take them out and you get a reward!",
                    "What say you? Are you in?");
            return;
        }
        if (!hungry(player)) {
            sayRandom(player, "Maybe you can lure another unsuspecting meal when I'm hungry again?", "I'm full!",
                    "I was famished! Thanks!");
            return;
        }
        sayRandom(player, "I'll give you something pretty useful. You'll see.",
                "It has to be a human, and they cannot be wearing any armor. That's all I'm asking!");
    }

    private void eat(SkyBlockPlayer meal) {
        SkyBlockPlayer accomplice = accompliceFor(meal);
        if (accomplice == null) {
            say(meal, "I mean that works too!").thenRun(() -> spitOut(meal));
            return;
        }
        CrimsonSideQuests.stamp(accomplice, ARANYA_LAST_MEAL);
        CrimsonSideQuests.flag(accomplice, ARANYA_FED);
        CrimsonSideQuests.flag(accomplice, ARANYA_REWARD);
        say(meal, "Nom nom nom.").thenRun(() -> spitOut(meal));
        say(accomplice, "Yummy yummy in my tummy!", "What a feast!", "You are quite unscrupulous! Maybe we can be friends!")
                .thenRun(() -> accomplice.sendMessage("<a>Talk to <c>Aranya <a>to claim your reward!"));
    }

    private SkyBlockPlayer accompliceFor(SkyBlockPlayer meal) {
        for (var online : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (!(online instanceof SkyBlockPlayer other) || other == meal || other.getInstance() != meal.getInstance()) continue;
            if (other.getPosition().distance(meal.getPosition()) > ACCOMPLICE_RANGE || armorless(other)) continue;
            if (hungry(other) && !CrimsonSideQuests.is(other, ARANYA_REWARD)) return other;
        }
        return null;
    }

    private static void spitOut(SkyBlockPlayer meal) {
        meal.sendMessage("<c>You were eaten by Aranya!");
        meal.teleport(SPIT_OUT);
    }
}
