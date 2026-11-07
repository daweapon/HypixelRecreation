package dev.daweapon.type.skyblockgeneric.levels;

import dev.daweapon.type.skyblockgeneric.abiphone.AbiphoneContactXP;
import dev.daweapon.type.skyblockgeneric.abiphone.ninef.NineFNetwork;
import dev.daweapon.type.skyblockgeneric.accessory.AccessoryBagXP;
import dev.daweapon.type.skyblockgeneric.bank.BankUpgradeAwards;
import dev.daweapon.type.skyblockgeneric.bestiary.BestiaryExperience;
import dev.daweapon.type.skyblockgeneric.chapters.ChapterService;
import dev.daweapon.type.skyblockgeneric.crimson.dojo.DojoProgress;
import dev.daweapon.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import dev.daweapon.type.skyblockgeneric.crimson.town.CarrolynExports;
import dev.daweapon.type.skyblockgeneric.crystalhollows.CrystalNucleus;
import dev.daweapon.type.skyblockgeneric.dungeons.CatacombsProgress;
import dev.daweapon.type.skyblockgeneric.entity.mob.CollectedMobTypes;
import dev.daweapon.type.skyblockgeneric.essence.EssenceShops;
import dev.daweapon.type.skyblockgeneric.experiments.MetaphysicalSerum;
import dev.daweapon.type.skyblockgeneric.fishing.festival.SharkBestiary;
import dev.daweapon.type.skyblockgeneric.fishing.ship.ShipPartAwards;
import dev.daweapon.type.skyblockgeneric.fishing.trophy.TrophyFishAwards;
import dev.daweapon.type.skyblockgeneric.foraging.TreeGiftMilestones;
import dev.daweapon.type.skyblockgeneric.fossil.FossilService;
import dev.daweapon.type.skyblockgeneric.garden.GardenService;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.CorpseMilestones;
import dev.daweapon.type.skyblockgeneric.hotf.HotfService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.hunting.AttributeLevelAwards;
import dev.daweapon.type.skyblockgeneric.minion.MinionAwards;
import dev.daweapon.type.skyblockgeneric.moonglade.beacon.MoongladeBeacon;
import dev.daweapon.type.skyblockgeneric.mythological.MythologicalBestiary;
import dev.daweapon.type.skyblockgeneric.mythological.gear.MythologicalItemHandlers;
import dev.daweapon.type.skyblockgeneric.rabbits.DarkCacaoTruffle;
import dev.daweapon.type.skyblockgeneric.rabbits.HoppitysHuntXP;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerProgression;
import dev.daweapon.type.skyblockgeneric.spidersden.ArachneFight;
import dev.daweapon.type.skyblockgeneric.spookyfestival.SpookyFestivalXP;
import dev.daweapon.type.skyblockgeneric.starlyn.StarlynMilestones;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetScore;
import dev.daweapon.type.skyblockgeneric.warps.TravelScrollAwards;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/** Every SkyBlock XP task that is recomputed from the player's own data rather than accumulated. */
public final class SkyBlockXPSyncs {
    /** Run on every server join to bring stored XP back in line with the player's data. */
    public static final List<Consumer<SkyBlockPlayer>> JOIN = List.of(
            BestiaryExperience::syncSkyBlockXP,
            AttributeLevelAwards::syncSkyBlockXP,
            MinionAwards::syncSkyBlockXP,
            SlayerProgression::sync,
            AbiphoneContactXP::syncSkyBlockXP,
            TravelScrollAwards::syncSkyBlockXP,
            HoppitysHuntXP::syncSkyBlockXP,
            SpookyFestivalXP::syncSkyBlockXP,
            BankUpgradeAwards::syncSkyBlockXP,
            TreeGiftMilestones::syncSkyBlockXP,
            ShipPartAwards::syncSkyBlockXP,
            MythologicalItemHandlers::syncSkyBlockXP,
            CollectedMobTypes::syncSkyBlockXP,
            HotmService::syncSkyBlockXp,
            HotfService::syncSkyBlockXp,
            CrystalNucleus::syncSkyBlockXp);

    /** Recomputed by their own systems when that data changes, never on join. */
    public static final List<Consumer<SkyBlockPlayer>> ON_CHANGE = List.of(
            ChapterService::syncSkyBlockXp,
            DojoProgress::syncSkyBlockXP,
            CrimsonReputationService::syncSkyBlockXP,
            CarrolynExports::syncSkyBlockXP,
            EssenceShops::syncSkyBlockXP,
            MetaphysicalSerum::syncXp,
            SharkBestiary::updateKillsXP,
            TrophyFishAwards::syncSkyBlockXP,
            FossilService::syncSkyBlockXp,
            GardenService::syncExperience,
            CorpseMilestones::syncXp,
            MoongladeBeacon::syncSkyBlockXp,
            MythologicalBestiary::updateKillsXP,
            DarkCacaoTruffle::syncSkyBlockXP,
            ArachneFight::syncSkyBlockXP,
            StarlynMilestones::sync,
            PetScore::syncSkyBlockXP,
            AccessoryBagXP::sync,
            player -> NineFNetwork.syncSkyBlockXP(player, NineFNetwork.state(player)),
            CatacombsProgress::syncSkyBlockXp);

    private SkyBlockXPSyncs() {
    }

    public static List<Consumer<SkyBlockPlayer>> all() {
        List<Consumer<SkyBlockPlayer>> all = new ArrayList<>(JOIN);
        all.addAll(ON_CHANGE);
        return all;
    }

    public static void syncAll(SkyBlockPlayer player) {
        all().forEach(sync -> sync.accept(player));
    }
}
