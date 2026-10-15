package dev.daweapon.type.thegarden.tab;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.tab.TablistModule;
import dev.daweapon.type.generic.tab.TablistSkinRegistry;
import dev.daweapon.type.generic.user.HypixelPlayer;
import dev.daweapon.type.skyblockgeneric.garden.GardenProfile;
import dev.daweapon.type.skyblockgeneric.garden.Gardens;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseData;
import dev.daweapon.type.skyblockgeneric.garden.greenhouse.GreenhouseService;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestData;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestItems;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestService;
import dev.daweapon.type.skyblockgeneric.garden.pest.PestTraps;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.PetComponent;
import dev.daweapon.type.skyblockgeneric.skill.SkillCategories;
import dev.daweapon.type.skyblockgeneric.tabmodules.AccountInformationModule;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class GardenPestsModule extends TablistModule {

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        GardenProfile profile = Gardens.profile(player);
        List<TablistEntry> entries = new ArrayList<>();
        entries.add(centered("<b><l>Info", p.getLocale(), TablistSkinRegistry.CYAN));
        if (PestService.unlocked(profile)) {
            PestData data = profile.pests();
            long now = System.currentTimeMillis();
            entries.add(new TablistEntry(Text.of("<a><l>Pests:"), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Alive: <a>{}", data.aliveCount()), TablistSkinRegistry.GRAY));
            if (data.aliveCount() > 0) {
                TreeSet<Integer> plots = new TreeSet<>();
                data.getAlive().forEach(alive -> plots.add(alive.getPlot()));
                entries.add(new TablistEntry(Text.of(" <7>Plots: <a>{}", Text.parse(String.join("<7>, <a>", plots.stream().map(String::valueOf).toList()))), TablistSkinRegistry.GRAY));
            }
            entries.add(new TablistEntry(Text.of(" <7>Spray: <a>{}", Text.parse(spray(data, now))), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Repellent: <a>{}", data.getRepellentUntil() > now ? minutes(data.getRepellentUntil() - now) : "None"), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Bonus: {}", Text.parse(data.getBoostUntil() > now ? "<a>+" + data.getBoostFortune() + "☘ (" + minutes(data.getBoostUntil() - now) + ")" : "<c>INACTIVE")), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Cooldown: {}", Text.parse(data.getCooldownUntil() > now ? "<c>" + clock(data.getCooldownUntil() - now) : "<a>READY")), TablistSkinRegistry.GRAY));
            int loss = PestService.fortuneLoss(player, profile);
            if (loss > 0) entries.add(new TablistEntry(Text.of(" <c>Fortune reduced by {}%", loss), TablistSkinRegistry.GRAY));
        }
        GreenhouseData greenhouse = profile.greenhouse();
        if (!greenhouse.getBeds().isEmpty()) {
            entries.add(new TablistEntry(Text.of("<a><l>Greenhouse:"), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Next Stage: <a>{}", clock(GreenhouseService.nextStageIn(greenhouse))), TablistSkinRegistry.GRAY));
        }
        entries.add(getGrayEntry());
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        entries.add(new TablistEntry(Text.of("<a><l>Pet:"), TablistSkinRegistry.GRAY));
        if (pet == null) {
            entries.add(new TablistEntry(Text.of(" <7>None"), TablistSkinRegistry.GRAY));
        } else {
            entries.add(new TablistEntry(Text.of(" <7>[Lvl {}] <color:{}>{}", PetLevels.level(pet), pet.getAttributeHandler().getRarity().getColor().asHexString(), pet.getComponent(PetComponent.class).getPetName()), TablistSkinRegistry.GRAY));
            if (PetLevels.isMaxLevel(pet)) entries.add(new TablistEntry(Text.of(" <b><l>MAX LEVEL"), TablistSkinRegistry.GRAY));
        }
        entries.add(getGrayEntry());
        SkillCategories skill = SkillCategories.FARMING;
        entries.add(new TablistEntry(Text.of("<a><l>Skills:"), TablistSkinRegistry.GRAY));
        int skillLevel = player.getSkills().getCurrentLevel(skill);
        entries.add(new TablistEntry(Text.of(" <7>Farming {}: {}", skillLevel, Text.parse(skillLevel >= 60 ? "<c><l>MAX" : "<a>" + player.getSkills().getPercentage(skill) + "%")), TablistSkinRegistry.GRAY));
        entries.add(getGrayEntry());
        ItemStatistics stats = player.getStatistics().allStatistics();
        entries.add(new TablistEntry(Text.of("<a><l>Stats:"), TablistSkinRegistry.GRAY));
        for (ItemStatistic statistic : List.of(ItemStatistic.SPEED, ItemStatistic.STRENGTH, ItemStatistic.BONUS_PEST_CHANCE)) {
            entries.add(new TablistEntry(Text.of(" ").append(Text.component(AccountInformationModule.stat(statistic, stats))), TablistSkinRegistry.GRAY));
        }
        fillRestWithGray(entries);
        return entries;
    }

    static List<TablistEntry> trapEntries(GardenProfile profile) {
        List<TablistEntry> entries = new ArrayList<>();
        if (!PestService.unlocked(profile)) return entries;
        PestData data = profile.pests();
        entries.add(getGrayEntry());
            entries.add(new TablistEntry(Text.of("<a><l>Pest Traps:"), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Placed: <a>{}/{}", data.getTraps().size(), PestTraps.MAX_TRAPS), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>Full Traps: <a>{}", Text.parse(trapList(data, true))), TablistSkinRegistry.GRAY));
            entries.add(new TablistEntry(Text.of(" <7>No Bait: <a>{}", Text.parse(trapList(data, false))), TablistSkinRegistry.GRAY));
        return entries;
    }

    private static String trapList(PestData data, boolean full) {
        List<String> numbers = new ArrayList<>();
        for (int i = 0; i < data.getTraps().size(); i++) {
            PestData.Trap trap = data.getTraps().get(i);
            PestTraps.Kind kind = PestTraps.KINDS.get(trap.getKind());
            boolean matches = full ? kind != null && trap.getCaught().size() >= kind.capacity() : trap.getBait() == 0;
            if (matches) numbers.add(String.valueOf(i + 1));
        }
        return numbers.isEmpty() ? "None" : String.join("<7>, <a>", numbers);
    }

    private static String spray(PestData data, long now) {
        return data.getSprays().entrySet().stream()
                .filter(entry -> entry.getValue().until() > now)
                .map(entry -> PestItems.materialName(entry.getValue().material()) + " <7>(" + minutes(entry.getValue().until() - now) + " in " + entry.getKey() + ")")
                .findFirst().orElse("None");
    }

    private static String minutes(long millis) {
        return (millis / 60_000 + 1) + "m";
    }

    private static String clock(long millis) {
        long seconds = millis / 1000;
        if (seconds >= 3600) return seconds / 3600 + "h " + seconds % 3600 / 60 + "m";
        return seconds >= 60 ? seconds / 60 + "m " + seconds % 60 + "s" : seconds + "s";
    }
}
