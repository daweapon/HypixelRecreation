package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.generic.data.datapoints.DatapointStringList;
import dev.daweapon.type.skyblockgeneric.data.SkyBlockDataHandler;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.levels.SkyBlockXPSource;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public final class AbiphoneContactXP {
    public static final int XP_PER_CONTACT = 10;

    private AbiphoneContactXP() {
    }

    public static final int MAX_CONTACTS = SkyBlockXPSource.ABIPHONE_CONTACTS.getMaxXP() / XP_PER_CONTACT;
    private static final String UNBUILT_CONTACT_PREFIX = "UNBUILT_CONTACT_";

    public static void maxOut(SkyBlockPlayer player) {
        List<String> ids = new ArrayList<>(unlocked(player));
        AbiphoneRegistry.getRegisteredContactNPCs().stream().map(AbiphoneNPC::getId)
                .filter(id -> !ids.contains(id)).forEach(ids::add);
        for (int number = 1; ids.size() < MAX_CONTACTS; number++) {
            String id = UNBUILT_CONTACT_PREFIX + number;
            if (!ids.contains(id)) ids.add(id);
        }
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.ABIPHONE_CONTACTS_UNLOCKED, DatapointStringList.class)
                .setValue(ids);
        syncSkyBlockXP(player);
    }

    public static int total(int unlockedContacts) {
        return Math.min(unlockedContacts * XP_PER_CONTACT, SkyBlockXPSource.ABIPHONE_CONTACTS.getMaxXP());
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
