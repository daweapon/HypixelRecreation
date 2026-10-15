package dev.daweapon.type.crimsonisle.faction;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.timer.TaskSchedule;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.generic.entity.npc.HypixelNPC;
import dev.daweapon.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.questline.CrimsonSideQuests;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;
import dev.daweapon.type.skyblockgeneric.entity.mob.mobs.crimsonquest.MobFactionDuelist;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rollim (Mage) and Kaus (Barbarian): 7 daily training duels, +25 reputation per win and a Bag of
 * Gold after the last (wiki: Rollim, Kaus). The duel ends unfinished after {@link #DUEL_TIME}.
 */
public abstract class DuelistNPC extends FactionVillagerNPC implements NPCAbiphoneTrait {
    public static final int REPUTATION = 25;
    public static final Duration COOLDOWN = Duration.ofHours(24);
    public static final Duration DUEL_TIME = Duration.ofMinutes(3);
    private static final Map<UUID, MobFactionDuelist> DUELS = new ConcurrentHashMap<>();

    private final String plainName;
    private final String skin;

    protected DuelistNPC(String name, String plainName, String skin, Pos home, CrimsonFaction faction) {
        super(name, skin, home, faction);
        this.plainName = plainName;
        this.skin = skin;
    }

    /** Day 1..7: the line asking whether the player is ready. */
    protected abstract String[] invitation(int day);

    /** Day 1..7: what they say after losing. */
    protected abstract String[] afterFight(int day);

    protected abstract String[] firstYes();

    protected abstract String countdownEnd();

    protected abstract String declined();

    protected abstract String[] afterTraining();

    protected abstract String notMember();

    protected abstract String otherFaction();

    @Override
    public String getAbiphoneKey() {
        return plainName.toLowerCase();
    }

    @Override
    protected void talk(SkyBlockPlayer player) {
        CrimsonFaction faction = faction();
        int done = CrimsonSideQuests.duelDays(player, faction);
        if (!isMember(player)) {
            if (done >= CrimsonSideQuests.DUEL_DAYS - 1 && done > 0) say(player, "Wait you're not on my my side? Even after all that training? I am so dead...");
            else if (done > 0) say(player, "Why aren't you on my side anymore? I don't want to embarrass myself; come back and help me.");
            else if (CrimsonReputationService.isMember(player, faction.opposite())) say(player, otherFaction());
            else say(player, notMember());
            return;
        }
        if (done >= CrimsonSideQuests.DUEL_DAYS) {
            say(player, afterTraining());
            return;
        }
        MobFactionDuelist active = DUELS.get(player.getUuid());
        if (active != null && !active.isRemoved() && !active.isDead()) return;
        if (!CrimsonSideQuests.isReady(player, CrimsonSideQuests.duelLastKey(faction), COOLDOWN)) {
            say(player, "Please come back tomorrow.");
            return;
        }
        int day = done + 1;
        say(player, invitation(day)).thenRun(() -> CrimsonChoices.yesNo(player, "duel_" + plainName.toLowerCase(),
                () -> startDuel(player, day), () -> say(player, declined())));
    }

    private void startDuel(SkyBlockPlayer player, int day) {
        String[] intro = day == 1 ? firstYes() : new String[0];
        String[] lines = new String[intro.length + 4];
        System.arraycopy(intro, 0, lines, 0, intro.length);
        lines[intro.length] = "3";
        lines[intro.length + 1] = "2";
        lines[intro.length + 2] = "1";
        lines[intro.length + 3] = countdownEnd();
        say(player, lines).thenRun(() -> {
            MobFactionDuelist duelist = new MobFactionDuelist(player.getUuid(), faction(), plainName, skin, day);
            duelist.setInstance(HypixelConst.getInstanceContainer(), home().add(1.5, 0, 1.5));
            DUELS.put(player.getUuid(), duelist);
            MinecraftServer.getSchedulerManager().buildTask(() -> endUnfinished(player.getUuid(), duelist))
                    .delay(TaskSchedule.millis(DUEL_TIME.toMillis())).schedule();
        });
    }

    private static void endUnfinished(UUID player, MobFactionDuelist duelist) {
        if (DUELS.get(player) != duelist || duelist.isRemoved() || duelist.isDead()) return;
        DUELS.remove(player);
        duelist.remove();
        var online = MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(player);
        if (online != null) online.sendMessage("<c>The training duel took too long and was called off.");
    }

    /** Called for every Crimson Isle kill: a won duel pays out and the trainer thanks the player. */
    public static void onKill(SkyBlockPlayer player, SkyBlockMob mob) {
        if (!(mob instanceof MobFactionDuelist duelist) || !player.getUuid().equals(duelist.getOpponent())) return;
        DUELS.remove(player.getUuid());
        CrimsonFaction faction = duelist.getFaction();
        int day = CrimsonSideQuests.duelDays(player, faction) + 1;
        CrimsonSideQuests.set(player, CrimsonSideQuests.duelDaysKey(faction), day);
        CrimsonSideQuests.stamp(player, CrimsonSideQuests.duelLastKey(faction));
        CrimsonReputationService.add(player, faction, REPUTATION, "Training Duel");
        for (var npc : HypixelNPC.getRegisteredNPCs()) {
            if (npc instanceof DuelistNPC trainer && trainer.faction() == faction) {
                trainer.say(player, trainer.afterFight(day)).thenRun(() -> {
                    if (day >= CrimsonSideQuests.DUEL_DAYS) player.addAndUpdateItem(ItemType.BAG_OF_GOLD);
                });
                return;
            }
        }
    }
}
