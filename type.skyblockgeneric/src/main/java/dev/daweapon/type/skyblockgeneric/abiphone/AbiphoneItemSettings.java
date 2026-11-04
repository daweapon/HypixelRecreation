package dev.daweapon.type.skyblockgeneric.abiphone;

import dev.daweapon.type.skyblockgeneric.gui.inventories.abiphone.AbiphoneView.SortType;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.components.AbiphoneComponent;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class AbiphoneItemSettings {
    private AbiphoneItemSettings() {
    }

    public static void mutate(SkyBlockPlayer player, SkyBlockItem viewCopy, Consumer<SkyBlockItem> change) {
        String id = viewCopy.getAttributeHandler().getUniqueTrackedID();
        change.accept(viewCopy);
        for (int slot = 0; slot < PlayerInventory.INNER_INVENTORY_SIZE; slot++) {
            ItemStack stack = player.getInventory().getItemStack(slot);
            if (stack.isAir() || !SkyBlockItem.isSkyBlockItem(stack)) continue;
            SkyBlockItem live = new SkyBlockItem(stack);
            if (!live.hasComponent(AbiphoneComponent.class)) continue;
            if (id == null ? slot == player.getHeldSlot() : id.equals(live.getAttributeHandler().getUniqueTrackedID())) {
                player.updateItemInSlot(slot, change);
                return;
            }
        }
    }

    public static void recordCall(SkyBlockPlayer player, SkyBlockItem abiphone, AbiphoneNPC npc) {
        long now = System.currentTimeMillis();
        mutate(player, abiphone, item -> item.getAttributeHandler().setAbiphoneCallStats(
                AbiphoneContactData.recordCall(item.getAttributeHandler().getAbiphoneCallStats(), npc.getId(), now)));
    }

    public static boolean supports(SkyBlockItem abiphone, AbiphoneComponent.AbiphoneFeature feature) {
        return abiphone.getComponent(AbiphoneComponent.class).getFeatures().contains(feature);
    }

    public static boolean supportsDoNotDisturb(SkyBlockItem abiphone) {
        return supports(abiphone, AbiphoneComponent.AbiphoneFeature.DO_NOT_DISTURB_MODE);
    }

    public static boolean isDoNotDisturb(SkyBlockItem abiphone, AbiphoneNPC npc) {
        return AbiphoneContactData.doNotDisturb(abiphone.getAttributeHandler().getAbiphoneDoNotDisturb()).contains(npc.getId());
    }

    public static void toggleDoNotDisturb(SkyBlockPlayer player, SkyBlockItem abiphone, AbiphoneNPC npc) {
        mutate(player, abiphone, item -> item.getAttributeHandler().setAbiphoneDoNotDisturb(
                AbiphoneContactData.toggleDoNotDisturb(item.getAttributeHandler().getAbiphoneDoNotDisturb(), npc.getId())));
    }

    public static void removeContact(SkyBlockPlayer player, SkyBlockItem abiphone, AbiphoneNPC npc) {
        mutate(player, abiphone, item -> {
            item.getAttributeHandler().removeAbiphoneNPC(npc);
            if (npc.getId().equals(item.getAttributeHandler().getAbiphoneSpeedDial())) item.getAttributeHandler().setAbiphoneSpeedDial("");
        });
        AbiphoneContactListeners.removed(player, npc.getId());
    }

    public static String speedDial(SkyBlockItem abiphone) {
        return abiphone.getAttributeHandler().getAbiphoneSpeedDial();
    }

    public static void setSpeedDial(SkyBlockPlayer player, SkyBlockItem abiphone, String contactId) {
        mutate(player, abiphone, item -> item.getAttributeHandler().setAbiphoneSpeedDial(contactId));
    }

    public static Ringtone ringtone(SkyBlockItem abiphone) {
        return Ringtone.byName(abiphone.getAttributeHandler().getAbiphoneRingtone());
    }

    public static void setRingtone(SkyBlockPlayer player, SkyBlockItem abiphone, Ringtone ringtone) {
        mutate(player, abiphone, item -> item.getAttributeHandler().setAbiphoneRingtone(ringtone.name()));
    }

    public static AbiphoneGameStats gameStats(SkyBlockItem abiphone) {
        return AbiphoneGameStats.parse(abiphone.getAttributeHandler().getAbiphoneGameStats());
    }

    public static void updateGameStats(SkyBlockPlayer player, SkyBlockItem abiphone, java.util.function.UnaryOperator<AbiphoneGameStats> change) {
        mutate(player, abiphone, item -> item.getAttributeHandler().setAbiphoneGameStats(
                change.apply(AbiphoneGameStats.parse(item.getAttributeHandler().getAbiphoneGameStats())).serialize()));
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
