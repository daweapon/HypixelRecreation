package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.skyblockgeneric.gui.inventories.abiphone.AbiphoneView.SortType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class AbiphoneItemSettings {
    private AbiphoneItemSettings() {
    }

    public static void recordCall(SkyBlockItem abiphone, AbiphoneNPC npc) {
        String stats = abiphone.getAttributeHandler().getAbiphoneCallStats();
        abiphone.getAttributeHandler().setAbiphoneCallStats(AbiphoneContactData.recordCall(stats, npc.getId(), System.currentTimeMillis()));
    }

    public static boolean supportsDoNotDisturb(SkyBlockItem abiphone) {
        AbiphoneComponent component = abiphone.getComponent(AbiphoneComponent.class);
        return component.getFeatures().contains(AbiphoneComponent.AbiphoneFeature.DO_NOT_DISTURB_MODE);
    }

    public static boolean isDoNotDisturb(SkyBlockItem abiphone, AbiphoneNPC npc) {
        return AbiphoneContactData.doNotDisturb(abiphone.getAttributeHandler().getAbiphoneDoNotDisturb()).contains(npc.getId());
    }

    public static void toggleDoNotDisturb(SkyBlockItem abiphone, AbiphoneNPC npc) {
        abiphone.getAttributeHandler().setAbiphoneDoNotDisturb(
                AbiphoneContactData.toggleDoNotDisturb(abiphone.getAttributeHandler().getAbiphoneDoNotDisturb(), npc.getId()));
    }

    public static List<AbiphoneNPC> sorted(List<AbiphoneNPC> contacts, SortType type, SkyBlockItem abiphone) {
        Map<String, AbiphoneContactData.CallStat> stats = AbiphoneContactData.callStats(abiphone.getAttributeHandler().getAbiphoneCallStats());
        Set<String> silenced = AbiphoneContactData.doNotDisturb(abiphone.getAttributeHandler().getAbiphoneDoNotDisturb());
        Comparator<AbiphoneNPC> alphabetical = Comparator.comparing(npc -> npc.getName().plain().toLowerCase());
        Comparator<AbiphoneNPC> order = switch (type) {
            case FIRST_ADDED -> null;
            case ALPHABETICAL -> alphabetical;
            case LAST_CALLED -> Comparator.comparingLong((AbiphoneNPC npc) -> -stats.getOrDefault(npc.getId(), new AbiphoneContactData.CallStat(0, 0)).lastCalled()).thenComparing(alphabetical);
            case MOST_CALLED -> Comparator.comparingInt((AbiphoneNPC npc) -> -stats.getOrDefault(npc.getId(), new AbiphoneContactData.CallStat(0, 0)).count()).thenComparing(alphabetical);
            case DO_NOT_DISTURB_FIRST -> Comparator.comparing((AbiphoneNPC npc) -> !silenced.contains(npc.getId())).thenComparing(alphabetical);
        };
        return order == null ? contacts : contacts.stream().sorted(order).toList();
    }
}
