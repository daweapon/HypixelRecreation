package dev.daweapon.type.crimsonisle.npcs.faction;

import net.minestom.server.coordinate.Pos;
import dev.daweapon.type.crimsonisle.faction.CrimsonSkins;
import dev.daweapon.type.crimsonisle.faction.FactionVillagerNPC;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.skyblockgeneric.abiphone.impl.AbiphoneIgrupan;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.crimson.questline.MageStep;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonisle.MobChickzilla;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// Igrupan and his chicken coop: the Mage questline's runaway chickens, then Chickzilla at Honored (+100, once).
public class NPCIgrupan extends FactionVillagerNPC implements NPCAbiphoneTrait {
    private static final Pos COOP = new Pos(-33.5, 93, -819.5);
    private static final Map<UUID, SkyBlockMob> CHICKZILLAS = new ConcurrentHashMap<>();

    public NPCIgrupan() {
        super("<5>Igrupan", CrimsonSkins.IGRUPAN, new Pos(-31, 93, -824), CrimsonFaction.MAGE);
    }

    @Override
    public String getAbiphoneKey() {
        return "igrupan";
    }

    @Override
    protected void talk(SkyBlockPlayer player) {
        if (!isMember(player)) {
            sayRandom(player, "Sorry, I only work with Mages...", "I don't recognise you stranger, clear off.",
                    "Guards! Stranger danger! Keep them away from my chickens!");
            return;
        }
        MageStep step = CrimsonQuestlines.mage(player);
        if (step == MageStep.HELP_IGRUPAN) {
            if (CrimsonQuestlines.counter(player, "igrupan_started") == 0) {
                CrimsonQuestlines.setCounter(player, "igrupan_started", 1);
                say(player, "Every. Single. One.", "I wake up this morning and all my chickens are GONE.",
                        "I finally found animals immune to Sulphur mutations but breeding them is a misery.",
                        "Could you help me getting them back inside?");
                return;
            }
            sayRandom(player, "I quit my previous job for this. It HAS to work.", "My chickens can't be that far, I have 5 of them.");
            return;
        }
        if (!AbiphoneIgrupan.chickensReturned(player)) {
            say(player, "I'm busy working on my coop.");
            return;
        }
        if (CrimsonSideQuests.is(player, CrimsonSideQuests.CHICKZILLA_DONE)) {
            sayRandom(player, "Maybe I should try to get my old job back...", "I will never forget Chickzilla.");
            return;
        }
        SkyBlockMob alive = CHICKZILLAS.get(player.getUuid());
        if (alive != null && !alive.isRemoved() && !alive.isDead()) {
            say(player, "What am I going to do? My business is finished!", "Are you strong enough to defeat it?");
            return;
        }
        if (!AbiphoneIgrupan.canSpawnChickzilla(player)) {
            say(player, "Maybe come back when you are more experienced for more.",
                    "Build yourself some reputation first, then we can talk again.",
                    "Come back later and I will have more for you to see.");
            return;
        }
        MobChickzilla chickzilla = new MobChickzilla();
        chickzilla.setInstance(HypixelConst.getInstanceContainer(), COOP);
        CHICKZILLAS.put(player.getUuid(), chickzilla);
        say(player, "A SUPER CHICKEN!", "I was wrong!! My chickens are not immune to Sulphur!",
                "One of my chickens mutated into an abomination.", "I don't know what to do, it's way to strong for me!");
    }
}
