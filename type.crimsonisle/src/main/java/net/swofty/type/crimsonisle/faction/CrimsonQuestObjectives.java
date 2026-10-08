package net.swofty.type.crimsonisle.faction;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.crimsonisle.npcs.NPCMasterTao;
import net.swofty.type.crimsonisle.npcs.faction.NPCApprentice;
import net.swofty.type.crimsonisle.npcs.faction.NPCCaptainAhone;
import net.swofty.type.crimsonisle.npcs.faction.NPCChiefScorn;
import net.swofty.type.crimsonisle.npcs.faction.NPCDean;
import net.swofty.type.crimsonisle.npcs.faction.NPCElle;
import net.swofty.type.crimsonisle.npcs.faction.NPCGris;
import net.swofty.type.crimsonisle.npcs.faction.NPCIgrupan;
import net.swofty.type.crimsonisle.npcs.faction.NPCLasea;
import net.swofty.type.crimsonisle.npcs.faction.NPCOdexar;
import net.swofty.type.crimsonisle.npcs.faction.NPCQueenNyx;
import net.swofty.type.crimsonisle.npcs.faction.NPCUgo;
import net.swofty.type.crimsonisle.npcs.faction.NPCUgoPig;
import net.swofty.type.crimsonisle.npcs.town.NPCAbiphoneShops;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.mob.miniboss.CrimsonMiniboss;
import net.swofty.type.skyblockgeneric.crimson.questline.BarbarianStep;
import net.swofty.type.skyblockgeneric.crimson.questline.CrimsonQuestlines;
import net.swofty.type.skyblockgeneric.crimson.questline.MageStep;
import net.swofty.type.skyblockgeneric.crimson.questline.UniversalStep;
import net.swofty.type.skyblockgeneric.user.QuestObjective;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The sidebar objective for the Crimson Isle Main Questline: the arrival steps first, then the
 * step of the faction line the player is on. Targets follow the quest NPCs as they move.
 */
public final class CrimsonQuestObjectives {
    private static final Map<Class<? extends HypixelNPC>, HypixelNPC> NPCS = new ConcurrentHashMap<>();

    private CrimsonQuestObjectives() {
    }

    public static @Nullable QuestObjective of(SkyBlockPlayer player) {
        if (player.getSkyblockDataHandler() == null) return null;
        UniversalStep universal = CrimsonQuestlines.universal(player);
        if (universal != UniversalStep.COMPLETE) return universal(player, universal);
        CrimsonFaction line = ActiveLine.of(player);
        if (line == CrimsonFaction.MAGE) return mage(player, CrimsonQuestlines.mage(player));
        if (line == CrimsonFaction.BARBARIAN) return barbarian(player, CrimsonQuestlines.barbarian(player));
        return null;
    }

    private static @Nullable QuestObjective universal(SkyBlockPlayer player, UniversalStep step) {
        return switch (step) {
            case KILL_BLAZES -> QuestObjective.at("Kill Blazes (" + CrimsonQuestlines.counter(player, "intro_blazes")
                    + "/" + CrimsonQuestlines.INTRO_BLAZES + ")", ElleQuest.STRONGHOLD);
            case HELP_ELLE -> talkTo("Elle", player, NPCElle.class);
            case BRING_ITEMS -> QuestObjective.at("Bring Elle the items", npc(player, NPCElle.class));
            case EXPLORE -> QuestObjective.anywhere("Explore the Crimson Isle");
            case TALK_TO_VILLAGERS -> QuestObjective.anywhere("Talk to villagers ("
                    + villagersMet(player) + "/" + CrimsonQuestlines.VILLAGERS_PER_FACTION * 2 + ")");
            case CHOOSE_FACTION -> QuestObjective.at("Talk to Elle", nearest(player, ElleQuest.SCARLETON_CC, ElleQuest.DRAGONTAIL));
            case COMPLETE -> null;
        };
    }

    private static @Nullable QuestObjective mage(SkyBlockPlayer player, MageStep step) {
        return switch (step) {
            case NOT_STARTED -> talkTo("Elle", player, NPCElle.class);
            case SHOW_BATPHONE, RETURN_TO_ACADEMY, REPORT_TO_DEAN, TALK_TO_DEAN_FINAL -> talkTo("the Dean", player, NPCDean.class);
            case DELIVER_LETTER -> talkTo("Queen Nyx", player, NPCQueenNyx.class);
            case BUY_ABIPHONE -> QuestObjective.at("Buy an Abiphone", npc(player, NPCAbiphoneShops.Udel.class));
            case TALK_TO_ELLE -> talkTo("Elle", player, NPCElle.class);
            case MEET_ELLE_AT_BASTION -> QuestObjective.at("Meet Elle on the cliff", npc(player, NPCElle.class));
            case RECOVER_RESEARCH -> QuestObjective.at("Recover the research", MageScenes.BASTION_RESEARCH);
            case CALL_DEAN -> QuestObjective.anywhere("Call the Dean");
            case MAGE_COUNCIL -> QuestObjective.at("Go to the Mage Council", npc(player, NPCDean.class));
            case FIND_AHONE -> QuestObjective.at("Find the Apprentice", npc(player, NPCCaptainAhone.class));
            case FIND_APPRENTICE -> QuestObjective.at("Find the Apprentice", npc(player, NPCApprentice.class));
            case CALL_AHONE -> QuestObjective.anywhere("Call Captain Ahone");
            case BRING_MATERIALS -> QuestObjective.at("Bring the Dean materials", npc(player, NPCDean.class));
            case HELP_IGRUPAN -> QuestObjective.at("Help Igrupan", npc(player, NPCIgrupan.class));
            case HELP_ODEXAR -> QuestObjective.at("Help Odexar", npc(player, NPCOdexar.class));
            case HALLOWED_SKULL -> QuestObjective.at("Kill Bladesoul", CrimsonMiniboss.BLADESOUL.getSpawn());
            case LUMINO_FIBER -> QuestObjective.at("Kill Ashfang", CrimsonMiniboss.ASHFANG.getSpawn());
            case LEATHER_CLOTH -> QuestObjective.at("Kill Barbarian Duke X", CrimsonMiniboss.BARBARIAN_DUKE_X.getSpawn());
            case TALK_TO_AHONE -> talkTo("Captain Ahone", player, NPCCaptainAhone.class);
            case MEET_AT_FORGOTTEN_SKULL -> QuestObjective.at("Go to the Forgotten Skull", npc(player, NPCCaptainAhone.class));
            case DEFEAT_KUUDRA -> QuestObjective.at("Defeat Kuudra", npc(player, NPCElle.class));
            case COMPLETE -> null;
        };
    }

    private static @Nullable QuestObjective barbarian(SkyBlockPlayer player, BarbarianStep step) {
        return switch (step) {
            case NOT_STARTED, FIND_THE_NOISE -> talkTo("Elle", player, NPCElle.class);
            case FOLLOW_ELLE -> QuestObjective.at("Meet Elle in the desert", npc(player, NPCElle.class));
            case KILL_CORRUPTED_MONSTER -> QuestObjective.at("Kill the Corrupted Spider", ElleQuest.CORRUPTED_SPIDER_SPAWN);
            case FEED_THE_PIG -> QuestObjective.at("Give the Piglin Sulphur", npc(player, NPCUgoPig.class));
            case ESCORT_UGO -> QuestObjective.at("Escort Ugo to the Hut", ElleQuest.CHIEFS_HUT);
            case TALK_TO_UGO, RETURN_TO_UGO, UGO_TOWN_BOARD, REPORT_DOJO, REPORT_MAGE_OUTLAW, REPORT_MAGMA_BOSS ->
                    talkTo("Ugo", player, NPCUgo.class);
            case MEET_ELLE_AT_CATHEDRAL -> QuestObjective.at("Meet Elle at the Cathedral", npc(player, NPCElle.class));
            case STEAL_RESEARCH -> QuestObjective.at("Steal the research", BarbarianScenes.DEAN_OFFICE_RESEARCH);
            case PRESENT_RESEARCH, TALK_TO_CHIEF, TALK_TO_CHIEF_FINAL -> talkTo("Chief Scorn", player, NPCChiefScorn.class);
            case DOJO_TRAINING -> QuestObjective.at("Train at the Dojo (" + dojoTestsPassed(player) + "/"
                    + CrimsonQuestlines.UGO_TRAINING.size() + ")", npc(player, NPCMasterTao.class));
            case KILL_MAGE_OUTLAW -> QuestObjective.at("Kill the Mage Outlaw", CrimsonMiniboss.MAGE_OUTLAW.getSpawn());
            case KILL_MAGMA_BOSS -> QuestObjective.at("Kill the Magma Boss", CrimsonMiniboss.MAGMA_BOSS.getSpawn());
            case HELP_LASEA -> QuestObjective.at("Help Lasea", npc(player, NPCLasea.class));
            case HELP_GRIS -> QuestObjective.at("Help Gris", npc(player, NPCGris.class));
            case MEET_AT_FORGOTTEN_SKULL -> QuestObjective.at("Go to the Forgotten Skull", npc(player, NPCUgo.class));
            case DEFEAT_KUUDRA -> QuestObjective.at("Defeat Kuudra", npc(player, NPCElle.class));
            case COMPLETE -> null;
        };
    }

    private static QuestObjective talkTo(String name, SkyBlockPlayer player, Class<? extends HypixelNPC> type) {
        return QuestObjective.at("Talk to " + name, npc(player, type));
    }

    private static @Nullable Pos npc(SkyBlockPlayer player, Class<? extends HypixelNPC> type) {
        HypixelNPC npc = NPCS.get(type);
        if (npc == null) {
            npc = HypixelNPC.getRegisteredNPCs().stream().filter(type::isInstance).findFirst().orElse(null);
            if (npc == null) return null;
            NPCS.put(type, npc);
        }
        return npc.getParameters().position(player);
    }

    private static Pos nearest(SkyBlockPlayer player, Pos first, Pos second) {
        Pos at = player.getPosition();
        return at.distanceSquared(first) <= at.distanceSquared(second) ? first : second;
    }

    private static int villagersMet(SkyBlockPlayer player) {
        int met = 0;
        for (CrimsonFaction faction : CrimsonFaction.values()) {
            met += Math.min(CrimsonQuestlines.VILLAGERS_PER_FACTION, CrimsonQuestlines.villagersSpokenTo(player, faction));
        }
        return met;
    }

    private static long dojoTestsPassed(SkyBlockPlayer player) {
        return CrimsonQuestlines.UGO_TRAINING.stream()
                .filter(test -> CrimsonQuestlines.counter(player, "ugo_training:" + test.name()) > 0)
                .count();
    }
}
