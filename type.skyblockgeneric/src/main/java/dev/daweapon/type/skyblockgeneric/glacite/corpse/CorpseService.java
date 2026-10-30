package dev.daweapon.type.skyblockgeneric.glacite.corpse;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.commons.text.Text;
import dev.daweapon.type.generic.entity.InteractionEntity;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointFrozenCorpses;
import dev.daweapon.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import dev.daweapon.type.skyblockgeneric.essence.EssencePerks;
import dev.daweapon.type.skyblockgeneric.essence.FossilEssencePerk;
import dev.daweapon.type.skyblockgeneric.glacite.corpse.mob.ShaftMobSpawns;
import dev.daweapon.type.skyblockgeneric.glacite.mineshaft.MineshaftService;
import dev.daweapon.type.skyblockgeneric.hotm.HotmPowder;
import dev.daweapon.type.skyblockgeneric.hotm.HotmService;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class CorpseService {
    private static final String BORDER = "<9><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";
    private static final String DEAD_MANS_CHEST = "dead_mans_chest";
    private static final String GIFTS_FROM_THE_DEPARTED = "gifts_from_the_departed";

    private CorpseService() {
    }

    public static void populate(Instance instance, String layoutCode, SkyBlockPlayer owner) {
        OrganDonor.start();
        List<Pos> spots = CorpseAnchors.shuffled(layoutCode);
        double bonusChance = CorpseCount.bonusChancePercent(HotmService.perkValue(owner, DEAD_MANS_CHEST), CorpseMilestones.claimed(owner));
        boolean bonus = LootRoll.roll(owner.getUuid(), null, bonusChance);
        int count = CorpseCount.total(CorpseCount.base(layoutCode, spots.size()), spots.size(), bonus);
        ShaftCorpses shaft = new ShaftCorpses(layoutCode, owner.getUuid());
        for (int i = 0; i < count; i++) {
            shaft.add(spawnCorpse(instance, shaft, i, pickType(layoutCode), spots.get(i)));
        }
        ShaftCorpses.register(instance, shaft);
        ShaftMobSpawns.populate(instance, layoutCode, spots);
    }

    private static CorpseType pickType(String layoutCode) {
        if (layoutCode.startsWith("FAIR")) return CorpseType.VANGUARD;
        CorpseLootTable table = CorpseLootTable.get();
        double total = 0;
        for (CorpseType type : CorpseType.values()) total += table.rules(type).spawnShare();
        double pick = ThreadLocalRandom.current().nextDouble() * total;
        for (CorpseType type : CorpseType.values()) {
            pick -= table.rules(type).spawnShare();
            if (pick < 0) return type;
        }
        return CorpseType.LAPIS;
    }

    private static ShaftCorpses.Corpse spawnCorpse(Instance instance, ShaftCorpses shaft, int index, CorpseType type, Pos position) {
        LivingEntity stand = new LivingEntity(EntityType.ARMOR_STAND);
        stand.editEntityMeta(ArmorStandMeta.class, meta -> {
            meta.setInvisible(true);
            meta.setHasNoGravity(true);
            meta.setSmall(true);
            meta.setNotifyAboutChanges(false);
        });
        stand.setHelmet(type.helmetStack());
        stand.setInstance(instance, position);
        InteractionEntity[] click = new InteractionEntity[1];
        click[0] = new InteractionEntity(1.4f, 1.6f, (player, ignored) -> {
            ShaftCorpses.Corpse corpse = shaft.corpses().stream().filter(candidate -> candidate.click() == click[0]).findFirst().orElse(null);
            if (corpse != null) open((SkyBlockPlayer) player, shaft, corpse);
        });
        click[0].setInstance(instance, position.add(0, 0.2, 0));
        return new ShaftCorpses.Corpse(index, type, position, stand, click[0]);
    }

    public static void open(SkyBlockPlayer player, ShaftCorpses shaft, ShaftCorpses.Corpse corpse) {
        CorpseType type = corpse.type();
        CorpseLootTable table = CorpseLootTable.get();
        CorpseLootTable.Rules rules = table.rules(type);
        if (shaft.isLooted(player.getUuid(), corpse)) {
            player.sendMessage("<c>You have already looted this corpse!");
            return;
        }
        ItemType key = rules.key();
        if (key != null) {
            if (player.getAmountInInventory(key) < 1) {
                String name = new SkyBlockItem(key).getDisplayNameText().plain();
                player.sendMessage("<c>You need " + article(name) + " " + name + " to unlock this corpse!");
                return;
            }
            if (LootRoll.roll(player.getUuid(), null, EssencePerks.value(player, FossilEssencePerk.RESOURCEFUL))) {
                player.sendMessage("<6><l>LUCKY! <f>Your <r><e>Resourceful <r><f>perk saved your key from being consumed!");
            } else {
                player.removeItemFromPlayer(key, 1);
            }
        }
        shaft.markLooted(player.getUuid(), corpse);
        MineshaftService.recordCorpseLooted(player);

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int rolls = random.nextInt(rules.minRolls(), rules.maxRolls() + 1);
        double bonusRollChance = CorpseCount.bonusRollPercent(HotmService.perkValue(player, GIFTS_FROM_THE_DEPARTED), CorpseMilestones.claimed(player));
        boolean bonusRoll = LootRoll.roll(player.getUuid(), null, bonusRollChance);
        if (bonusRoll) rolls++;

        Map<String, Reward> rewards = new LinkedHashMap<>();
        boolean guaranteedPending = true;
        for (int i = 0; i < rolls; i++) {
            CorpseRngMeter.Boost boost = CorpseRngMeter.boost(player);
            boolean guaranteed = guaranteedPending && boost.guaranteed();
            if (rollDye(player, rules, boost, guaranteed)) {
                guaranteedPending = guaranteedPending && !guaranteed;
                CorpseRngMeter.onObtained(player, CorpseRngMeter.DYE_KEY);
                addReward(rewards, new CorpseLootTable.Entry(ItemType.DYE_FROSTBITTEN, null, 0, 1, 0, CorpseRngMeter.DYE_KEY));
                continue;
            }
            CorpseLootTable.Entry entry;
            if (guaranteed && table.poolHas(type, boost.selectedKey())) {
                guaranteedPending = false;
                entry = table.rollVariant(player.getUuid(), type, boost.selectedKey(), random);
            } else {
                entry = table.roll(player.getUuid(), type, boost, random);
            }
            CorpseRngMeter.onObtained(player, entry.meterKey());
            addReward(rewards, entry);
        }

        long powder = HotmService.gainPowder(player, HotmPowder.GLACITE, rules.powder());
        HotmService.addExperience(player, rules.hotmXp());
        List<Text> block = new ArrayList<>();
        block.add(Text.of(BORDER));
        block.add(Text.of("  " + type.getColour() + "<l>" + type.getDisplayName().toUpperCase() + " CORPSE LOOT! "));
        block.add(Text.of(" "));
        block.add(Text.of("  <f><l>REWARDS"));
        block.add(Text.of("    <b>Glacite Powder <f>x{}", String.format("%,d", powder)));
        block.add(Text.of("    <5>+{} Heart of the Mountain XP", String.format("%,d", rules.hotmXp())));
        for (Reward reward : rewards.values()) {
            SkyBlockItem item = reward.item();
            player.addAndUpdateItem(item);
            block.add(Text.of("    {}" + (reward.amount > 1 ? " <f>x{}" : ""), reward.amount > 1
                    ? new Object[]{item.getDisplayNameText(), reward.amount} : new Object[]{item.getDisplayNameText()}));
        }
        if (bonusRoll) block.add(Text.of("    <a>1 bonus drop!"));

        CorpseRngMeter.addXp(player, rules.meterXp());
        DatapointFrozenCorpses.FrozenCorpseData data = CorpseProgress.data(player);
        data.addLooted(type.name());
        CorpseProgress.save(player, data);
        CorpseMilestones.claimReached(player).forEach(block::add);
        block.add(Text.of(BORDER));
        block.forEach(line -> player.sendMessage(line));
    }

    private static boolean rollDye(SkyBlockPlayer player, CorpseLootTable.Rules rules, CorpseRngMeter.Boost boost, boolean guaranteed) {
        boolean selected = boost.applies(CorpseRngMeter.DYE_KEY);
        if (selected && guaranteed) return true;
        double chance = rules.dyeChance() * (selected ? boost.multiplier() : 1);
        return LootRoll.roll(player.getUuid(), ItemType.DYE_FROSTBITTEN, chance);
    }

    private static void addReward(Map<String, Reward> rewards, CorpseLootTable.Entry entry) {
        String key = entry.item().name() + ":" + entry.book() + ":" + entry.bookLevel();
        rewards.merge(key, new Reward(entry, entry.amount()), (a, b) -> new Reward(a.entry, a.amount + b.amount));
    }

    private record Reward(CorpseLootTable.Entry entry, int amount) {
        SkyBlockItem item() {
            SkyBlockItem item = new SkyBlockItem(entry.item(), amount);
            if (entry.book() != null) item.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(entry.book(), entry.bookLevel()));
            return item;
        }
    }

    private static String article(String name) {
        return "AEIOUaeiou".indexOf(name.charAt(0)) >= 0 ? "an" : "a";
    }

    public static List<Text> widgetLines(SkyBlockPlayer player) {
        return ShaftCorpses.of(player.getInstance()).map(shaft -> shaft.widgetLines(player)).orElse(List.of());
    }
}
