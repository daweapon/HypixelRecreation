package dev.daweapon.type.crimsonisle.faction;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.StringUtility;
import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonScene;
import dev.daweapon.type.skyblockgeneric.crimson.questline.UniversalStep;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * The "talk to 4 villagers from each faction" step of the arrival quest. Every town villager that
 * takes part registers here; while the step is active the ones the player still needs show green
 * particles, and talking to one plays the shared 1st/2nd/3rd/4th villager lines for its faction.
 */
public final class FactionVillagers {
    private static final double PARTICLE_RANGE = 48;
    private static final Map<String, Villager> VILLAGERS = new ConcurrentHashMap<>();
    private static final String[] ALREADY_SPOKEN = {
            "You seem suspicious.", "Sorry, I can't talk to you right now.", "Please stop bothering me."
    };

    private FactionVillagers() {
    }

    public record Villager(String name, String speaker, Pos position, CrimsonFaction faction) {
    }

    public static void register(CrimsonNPC npc, CrimsonFaction faction) {
        register(npc.getName(), npc.speaker(), npc.home(), faction);
    }

    /** For faction NPCs that are not {@link CrimsonNPC}s (Alwin and Igor, owned by the blacksmith menus). */
    public static void register(String name, String speaker, Pos position, CrimsonFaction faction) {
        VILLAGERS.put(name, new Villager(name, speaker, position, faction));
    }

    public static boolean intercept(CrimsonNPC npc, SkyBlockPlayer player) {
        return intercept(npc.getName(), player);
    }

    /**
     * Handles a click on a faction villager during the villager step. Returns true when the click
     * was a villager conversation, so the NPC should not also run its own behaviour.
     */
    public static boolean intercept(String name, SkyBlockPlayer player) {
        Villager villager = VILLAGERS.get(name);
        if (villager == null || CrimsonQuestlines.universal(player) != UniversalStep.TALK_TO_VILLAGERS) return false;
        CrimsonFaction faction = villager.faction();
        if (CrimsonQuestlines.hasSpokenTo(player, faction, name)) return false;
        if (CrimsonQuestlines.villagersSpokenTo(player, faction) >= CrimsonQuestlines.VILLAGERS_PER_FACTION) {
            String line = ALREADY_SPOKEN[ThreadLocalRandom.current().nextInt(ALREADY_SPOKEN.length)];
            CrimsonScene.create().say(villager.speaker(), line).play(player);
            return true;
        }
        int count = CrimsonQuestlines.markSpokenTo(player, faction, name);
        CrimsonScene.create().say(villager.speaker(), lines(player, faction, count)).play(player).thenRun(() -> {
            if (CrimsonQuestlines.hasMetVillagers(player)) {
                CrimsonQuestlines.advance(player, UniversalStep.TALK_TO_VILLAGERS, UniversalStep.CHOOSE_FACTION);
            } else if (count == CrimsonQuestlines.VILLAGERS_PER_FACTION) {
                player.sendMessage("<a>You talked to 4 villagers of the " + faction.coloured()
                        + " Faction<a>. Now talk to villagers of the other faction.");
            }
        });
        return true;
    }

    private static String[] lines(SkyBlockPlayer player, CrimsonFaction faction, int count) {
        if (faction == CrimsonFaction.BARBARIAN) {
            return switch (count) {
                case 1 -> new String[]{"Are you a Wizard?",
                        "Well you don't look like one, and you don't smell like one either.",
                        "In that case, I have nothing against you. I wish you a sulphuric day sir."};
                case 2 -> new String[]{"Hello stranger. Are you looking to join our faction?",
                        "What a fantastic choice!",
                        "We have a range of motives far deeper and more interesting than those terrible fire mages.",
                        "Joining our faction is quite hard but I'm sure an incredible individual like you will manage!"};
                case 3 -> new String[]{"Sul-phur", "Sulphur?", "Me Sulphur."};
                default -> new String[]{"Are you a Hero?",
                        "Did you come to free us from the everlasting threat of the wizards?",
                        "The mighty Volcano is protecting us, but for how long?"};
            };
        }
        return switch (count) {
            case 1 -> new String[]{"Oh, a new face! Welcome to the <5>City of Magic<f>!",
                    "We used to be Pig Farmers centuries ago, but thanks to <7>Sulphur <f>we are now superior beings capable of mastering the magical elements!",
                    "If you are interested in tourist attractions I suggest you visit our <5>Cathedral<f>, which is also our Academy now."};
            case 2 -> new String[]{intelligenceLine(player)};
            case 3 -> new String[]{"Have you tried the new ability phone?",
                    "The new one is barely better than the one from last year, people still buy them like sheep...",
                    "Wait, you don't even have an <3>Abiphone<f>?", "Yikes..."};
            default -> new String[]{"Ah, the Piglins yes, quite troublesome they are.", "I wouldn't go there if I were you."};
        };
    }

    private static String intelligenceLine(SkyBlockPlayer player) {
        double intelligence = Math.max(0, player.getStatistics().allStatistics().getOverall(ItemStatistic.INTELLIGENCE));
        String amount = "<b>" + StringUtility.commaify((long) intelligence) + " <f>Intelligence! ";
        if (intelligence < 1000) return amount + "That's...ok I guess!";
        if (intelligence <= 2000) return amount + "Wow that's not bad at all!";
        return amount + "What, how? Who are you even?";
    }

    public static void startParticles() {
        MinecraftServer.getSchedulerManager().buildTask(FactionVillagers::sparkle)
                .repeat(TaskSchedule.seconds(1)).schedule();
    }

    private static void sparkle() {
        for (var online : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            if (!(online instanceof SkyBlockPlayer player) || !player.isOnline() || player.getSkyblockDataHandler() == null) continue;
            if (CrimsonQuestlines.universal(player) != UniversalStep.TALK_TO_VILLAGERS) continue;
            for (Villager villager : VILLAGERS.values()) {
                if (CrimsonQuestlines.villagersSpokenTo(player, villager.faction()) >= CrimsonQuestlines.VILLAGERS_PER_FACTION) continue;
                if (CrimsonQuestlines.hasSpokenTo(player, villager.faction(), villager.name())) continue;
                Pos pos = villager.position();
                if (pos.distance(player.getPosition()) > PARTICLE_RANGE) continue;
                player.sendPacket(new ParticlePacket(Particle.HAPPY_VILLAGER, false, false,
                        (float) pos.x(), (float) pos.y() + 2.2f, (float) pos.z(),
                        0.3f, 0.3f, 0.3f, 0, 4 + ThreadLocalRandom.current().nextInt(3)));
            }
        }
    }
}
