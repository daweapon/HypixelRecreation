package net.swofty.type.skyblockgeneric.mythological;

import net.swofty.commons.party.FullParty;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointBestiary;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.loottable.LootAffector;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Minos Inquisitor loot sharing: party members of the player who dug it up who also hit it get their
 * own roll of its drop table and Bestiary credit.
 */
public final class MythologicalLootShare {
    private static final Map<MythologicalCreatureMob, Set<UUID>> PARTIES = new ConcurrentHashMap<>();

    private MythologicalLootShare() {
    }

    public static void track(MythologicalCreatureMob mob, SkyBlockPlayer digger) {
        CompletableFuture.runAsync(() -> {
            FullParty party = PartyManager.getPartyFromPlayer(digger);
            if (party != null) PARTIES.put(mob, Set.copyOf(party.getParticipants()));
        });
    }

    public static void forget(MythologicalCreatureMob mob) {
        PARTIES.remove(mob);
    }

    public static void share(MythologicalCreatureMob mob, SkyBlockPlayer digger) {
        Set<UUID> party = PARTIES.remove(mob);
        if (party == null) return;
        for (UUID attacker : mob.getAttackers()) {
            if (attacker.equals(digger.getUuid()) || !party.contains(attacker)) continue;
            SkyBlockPlayer member = SkyBlockGenericLoader.getFromUUID(attacker);
            if (member == null || member.getInstance() != mob.getInstance()) continue;
            reward(member, mob, digger);
        }
    }

    private static void reward(SkyBlockPlayer member, MythologicalCreatureMob mob, SkyBlockPlayer digger) {
        Map<ItemType, SkyBlockLootTable.LootRecord> loot = mob.getLootTable().runChances(member, LootAffector.MAGIC_FIND);
        loot.forEach((type, record) -> member.addAndUpdateItem(new SkyBlockItem(type, record.getAmount())));
        member.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.BESTIARY, DatapointBestiary.class)
                .getValue().increase(mob, 1);
        MythologicalBestiary.updateKillsXP(member);
        member.sendMessage("<d><l>LOOT SHARE!</l> <e>You received loot for assisting <b>" + digger.getUsername() + "<e>!");
    }
}
