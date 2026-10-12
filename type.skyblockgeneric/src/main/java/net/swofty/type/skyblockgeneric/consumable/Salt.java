package net.swofty.type.skyblockgeneric.consumable;

import net.swofty.type.skyblockgeneric.accessory.DungeonAccessoryHook;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.seacreature.SeaCreatureMob;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableItemConfig;
import net.swofty.type.skyblockgeneric.item.handlers.interactable.InteractableRegistry;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

public enum Salt {
    CANDYCOMB("Candycomb", 1.25, 12, (player, mob) -> DungeonAccessoryHook.isInDungeon(player)),
    OCEANDY("Oceandy", 1.25, 12, (player, mob) -> mob instanceof SeaCreatureMob),
    BRINE_TONIC("Brine Tonic", 2.75, 24, (player, mob) -> {
        SkyBlockRegion region = player.getRegion();
        return mob instanceof SeaCreatureMob && region != null && region.getType() == RegionType.MOONGLADE_MARSH;
    });

    private final String displayName;
    private final double charmChance;
    private final long durationMillis;
    private final BiPredicate<SkyBlockPlayer, SkyBlockMob> applies;

    Salt(String displayName, double charmChance, int hours, BiPredicate<SkyBlockPlayer, SkyBlockMob> applies) {
        this.displayName = displayName;
        this.charmChance = charmChance;
        this.durationMillis = hours * 60L * 60 * 1000;
        this.applies = applies;
    }

    public double charmChance() {
        return charmChance;
    }

    public static double charmChanceBonus(Map<String, Long> expirations, long now, Predicate<Salt> applying) {
        double total = 0;
        for (Salt salt : values()) {
            if (TimedConsumables.isActive(expirations, salt.name(), now) && applying.test(salt)) total += salt.charmChance;
        }
        return total;
    }

    public static double charmChanceBonus(SkyBlockPlayer player, SkyBlockMob mob) {
        return charmChanceBonus(TimedConsumables.expirations(player), System.currentTimeMillis(), salt -> salt.applies.test(player, mob));
    }

    public static void registerInteractables() {
        for (Salt salt : values()) {
            InteractableRegistry.register(salt.name(), InteractableItemConfig.builder()
                    .leftClickHandler((player, item) -> {
                        if (!TimedConsumables.start(player, salt.name(), salt.durationMillis)) {
                            player.sendMessage("<c>You already have an active {}!", salt.displayName);
                            return;
                        }
                        TimedConsumables.consumeHeld(player, item);
                        player.sendMessage("<a>You consumed a <9>{}<a>! <7>(+{} Charm Chance)", salt.displayName, salt.charmChance);
                    }).build());
        }
    }
}
