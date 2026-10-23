package dev.daweapon.type.skyblockgeneric.user.pets.autopet;

import dev.daweapon.commons.ServerType;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.generic.HypixelConst;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointAutopets;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointPetData;
import dev.daweapon.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEventType;
import dev.daweapon.type.skyblockgeneric.miningevent.MiningEvents;
import dev.daweapon.type.skyblockgeneric.slayer.SlayerQuest;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.pets.PetLevels;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class AutopetService {
    public static final long SWAP_COOLDOWN_MILLIS = 100;
    public static final int MAX_SIMULTANEOUS_RULES = 2;
    private static final Map<UUID, Long> LAST_SWAP = new ConcurrentHashMap<>();

    private AutopetService() {
    }

    public static DatapointAutopets.AutopetData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.AUTOPETS, DatapointAutopets.class).getValue();
    }

    public static void save(SkyBlockPlayer player, DatapointAutopets.AutopetData data) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.AUTOPETS, DatapointAutopets.class).setValue(data);
    }

    public static boolean consumePack(SkyBlockPlayer player) {
        DatapointAutopets.AutopetData data = data(player);
        if (!data.addPack()) {
            player.sendMessage("<c>You already own the maximum of {} Autopet rules!", DatapointAutopets.MAX_RULES);
            return false;
        }
        save(player, data);
        player.sendMessage("<a>You unlocked <e>{} <a>additional Autopet rule slots! <7>({}/{})", DatapointAutopets.SLOTS_PER_PACK, data.unlockedSlots(), DatapointAutopets.MAX_RULES);
        return true;
    }

    public static void maxOut(SkyBlockPlayer player) {
        DatapointAutopets.AutopetData data = data(player);
        data.setPacks(DatapointAutopets.MAX_PACKS);
        save(player, data);
    }

    public static void forget(SkyBlockPlayer player) {
        LAST_SWAP.remove(player.getUuid());
    }

    public static void fire(SkyBlockPlayer player, AutopetTrigger trigger, String... params) {
        if (player.getSkyblockDataHandler() == null) return;
        DatapointAutopets.AutopetData data = data(player);
        if (data.unlockedSlots() == 0) return;
        long now = System.currentTimeMillis();
        Long last = LAST_SWAP.get(player.getUuid());
        if (last != null && now - last < SWAP_COOLDOWN_MILLIS) return;

        int applied = 0;
        for (int slot = 0; slot < data.unlockedSlots() && applied < MAX_SIMULTANEOUS_RULES; slot++) {
            AutopetRule rule = data.rule(slot);
            if (rule == null || rule.trigger() != trigger || !matches(rule, params) || blocked(player, rule)) continue;
            SkyBlockItem pet = resolve(player, rule);
            if (pet == null) continue;
            equip(player, pet);
            applied++;
        }
        if (applied > 0) LAST_SWAP.put(player.getUuid(), now);
    }

    public static @Nullable SkyBlockItem resolve(SkyBlockPlayer player, AutopetRule rule) {
        return player.getPetData().getPetsMap().keySet().stream()
                .filter(pet -> rule.petType().equals(typeName(pet)) && rule.petRarity().equals(pet.getAttributeHandler().getRarity().name()))
                .max(Comparator.comparingInt(PetLevels::level))
                .orElse(null);
    }

    public static AutopetRule describePet(SkyBlockItem pet, AutopetTrigger trigger, @Nullable String param, List<AutopetRule.Condition> exceptions) {
        return new AutopetRule(trigger, param, typeName(pet), pet.getAttributeHandler().getRarity().name(), List.copyOf(exceptions));
    }

    private static boolean matches(AutopetRule rule, String[] params) {
        if (rule.trigger().param() == AutopetParam.NONE || rule.param() == null) return true;
        for (String param : params) {
            if (rule.param().equals(param)) return true;
        }
        return false;
    }

    private static boolean blocked(SkyBlockPlayer player, AutopetRule rule) {
        return rule.exceptions().stream().anyMatch(condition -> triggered(player, condition));
    }

    private static boolean triggered(SkyBlockPlayer player, AutopetRule.Condition condition) {
        ServerType here = HypixelConst.getTypeLoader().getType();
        return switch (condition.exception()) {
            case IN_ISLAND -> here.name().equals(condition.param());
            case NOT_IN_ISLAND -> !here.name().equals(condition.param());
            case PET_EQUIPPED -> {
                SkyBlockItem enabled = player.getPetData().getEnabledPet();
                yield enabled != null && typeName(enabled).equals(condition.param());
            }
            case DURING_EVENT -> condition.param() != null && MiningEvents.isActive(MiningEventType.valueOf(condition.param()));
            case SLAYER_ACTIVE -> quest(player) != null;
            case SLAYER_BOSS_SPAWNED -> quest(player) != null && quest(player).bossSpawned();
            case DAY -> false;
        };
    }

    private static @Nullable SlayerQuest quest(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SLAYER, DatapointSlayer.class).getValue().getActiveQuest();
    }

    private static void equip(SkyBlockPlayer player, SkyBlockItem pet) {
        DatapointPetData.UserPetData petData = player.getPetData();
        if (petData.getEnabledPet() == pet) return;
        petData.setEnabled(pet, true);
        petData.updatePetEntityImpl(player);
        player.sendMessage("<c>Autopet <e>equipped your {}<e>! <hover:'<e>Click to open your Pets menu!'><click:run:'/pets'><a><l>VIEW RULE</l></click></hover>",
                pet.getDisplayName());
    }

    private static @Nullable String typeName(SkyBlockItem pet) {
        ItemType type = pet.getAttributeHandler().getPotentialType();
        return type == null ? null : type.name();
    }
}
