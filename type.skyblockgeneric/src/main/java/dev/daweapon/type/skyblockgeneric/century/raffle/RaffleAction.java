package dev.daweapon.type.skyblockgeneric.century.raffle;

import dev.daweapon.commons.text.Text;
import dev.daweapon.type.skyblockgeneric.entity.mob.MobType;
import dev.daweapon.type.skyblockgeneric.entity.mob.SkyBlockMob;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public record RaffleAction(Trigger trigger, String subject, int value, Set<MobType> mobTypes) {
    public enum Trigger {
        MOB_KILL, SLAYER_BOSS, SEA_CREATURE, TREASURE, TROPHY_FISH, ITEM_MINED, BLOCK_MINED,
        COMMISSION, KUUDRA, TREE_GIFT, TICKET, EAT_CAKE, FEED_SLICE
    }

    public static RaffleAction of(Trigger trigger) {
        return new RaffleAction(trigger, "", 0, Set.of());
    }

    public static RaffleAction of(Trigger trigger, String subject) {
        return new RaffleAction(trigger, subject == null ? "" : subject, 0, Set.of());
    }

    public static RaffleAction of(Trigger trigger, String subject, int value) {
        return new RaffleAction(trigger, subject == null ? "" : subject, value, Set.of());
    }

    public static RaffleAction kill(SkyBlockMob mob) {
        String name = Text.read(mob.getDisplayName()).plain();
        List<MobType> types = mob.getMobTypes() == null ? List.of() : mob.getMobTypes();
        return new RaffleAction(Trigger.MOB_KILL, name, 0, Set.copyOf(types.stream().filter(Objects::nonNull).toList()));
    }

    public boolean is(Trigger trigger, String subject) {
        return this.trigger == trigger && this.subject.equalsIgnoreCase(subject);
    }

    public boolean killed(MobType type) {
        return trigger == Trigger.MOB_KILL && mobTypes.contains(type);
    }

    public boolean killedNamed(String name) {
        return trigger == Trigger.MOB_KILL && subject.contains(name);
    }
}
