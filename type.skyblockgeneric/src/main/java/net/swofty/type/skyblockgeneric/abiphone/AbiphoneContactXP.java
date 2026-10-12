package net.swofty.type.skyblockgeneric.abiphone;

import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.AbiphoneComponent;
import net.swofty.type.skyblockgeneric.levels.SkyBlockXPSource;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public final class AbiphoneContactXP {
    public static final int XP_PER_CONTACT = 10;

    private AbiphoneContactXP() {
    }

    public static int total(int unlockedContacts) {
        return unlockedContacts * XP_PER_CONTACT;
    }

    public static List<String> unlocked(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.ABIPHONE_CONTACTS_UNLOCKED, DatapointStringList.class).getValue()
                .stream().filter(id -> !id.isBlank()).toList();
    }

    public static void unlock(SkyBlockPlayer player, String contactId) {
        DatapointStringList datapoint = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.ABIPHONE_CONTACTS_UNLOCKED, DatapointStringList.class);
        List<String> ids = new ArrayList<>(unlocked(player));
        if (!ids.contains(contactId)) {
            ids.add(contactId);
            datapoint.setValue(ids);
        }
        syncSkyBlockXP(player);
    }

    public static void syncSkyBlockXP(SkyBlockPlayer player) {
        DatapointStringList datapoint = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.ABIPHONE_CONTACTS_UNLOCKED, DatapointStringList.class);
        List<String> ids = new ArrayList<>(unlocked(player));
        boolean changed = false;
        for (SkyBlockItem item : player.getAllInventoryItems()) {
            if (!item.hasComponent(AbiphoneComponent.class)) continue;
            for (AbiphoneNPC npc : item.getAttributeHandler().getAbiphoneNPCs()) {
                if (!ids.contains(npc.getId())) {
                    ids.add(npc.getId());
                    changed = true;
                }
            }
        }
        if (changed) datapoint.setValue(ids);
        player.getSkyBlockExperience().setSourceXP(SkyBlockXPSource.ABIPHONE_CONTACTS, total(ids.size()));
    }
}
