package dev.daweapon.type.catacombs.classes;

import dev.daweapon.commons.skyblock.statistics.ItemStatistic;
import dev.daweapon.commons.skyblock.statistics.ItemStatistics;
import dev.daweapon.dungeons.catacombs.CatacombsFloor;
import dev.daweapon.dungeons.catacombs.classes.AbilityCooldowns;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassParty;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassScaling;
import dev.daweapon.dungeons.catacombs.classes.DungeonClassType;
import dev.daweapon.type.skyblockgeneric.dungeons.DungeonClassProgress;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.skyblockgeneric.user.statistics.TemporaryConditionalStatistic;
import net.minestom.server.item.ItemStack;
import org.tinylog.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One run's classes: what each member picked in the waiting room, the party frozen when the run starts (with every
 * member's class level), cooldowns, timed effects and the ghost toolbar. Abilities live in {@link ClassAbilities}.
 */
public final class RunClasses {
    private final ClassHost host;
    private final Map<UUID, DungeonClassType> selections = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> levels = new ConcurrentHashMap<>();
    private final Map<UUID, Long> castleUntil = new ConcurrentHashMap<>();
    private final Map<UUID, Long> ragnarokUntil = new ConcurrentHashMap<>();
    private final AbilityCooldowns cooldowns = new AbilityCooldowns();
    private final HitHistory meleeHits = new HitHistory();
    private final HitHistory bowHits = new HitHistory();
    private final GhostToolbar toolbar = new GhostToolbar();
    private final ClassAbilities abilities;
    private volatile DungeonClassParty party = new DungeonClassParty(Map.of());

    public RunClasses(ClassHost host) {
        this.host = host;
        this.abilities = new ClassAbilities(this);
    }

    public void admit(SkyBlockPlayer player, boolean waiting) {
        selections.putIfAbsent(player.getUuid(), DungeonClassProgress.selected(player));
        if (waiting) showSelector(player);
    }

    public void select(SkyBlockPlayer player, DungeonClassType type) {
        if (party.members().containsKey(player.getUuid())) return;
        selections.put(player.getUuid(), type);
        showSelector(player);
        host.broadcast("<a>" + player.getUsername() + " <7>selected the <e>" + type.displayName() + " <7>class!");
    }

    private void showSelector(SkyBlockPlayer player) {
        ItemStack selector = ClassItems.selector(selectedOf(player.getUuid()));
        int slot = slotOf(player, ClassItems.SELECTOR);
        if (slot < 0 && player.getInventory().getItemStack(ClassItems.SELECTOR_SLOT).isAir()) slot = ClassItems.SELECTOR_SLOT;
        if (slot >= 0) player.getInventory().setItemStack(slot, selector);
        else player.getInventory().addItemStack(selector);
    }

    private static int slotOf(SkyBlockPlayer player, String kind) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            if (kind.equals(ClassItems.kind(player.getInventory().getItemStack(slot)))) return slot;
        }
        return -1;
    }

    public DungeonClassType selectedOf(UUID player) {
        return selections.getOrDefault(player, DungeonClassProgress.DEFAULT_CLASS);
    }

    public void begin(List<SkyBlockPlayer> members) {
        Map<UUID, DungeonClassType> chosen = new HashMap<>();
        for (SkyBlockPlayer member : members) chosen.put(member.getUuid(), selectedOf(member.getUuid()));
        party = new DungeonClassParty(chosen);
        for (SkyBlockPlayer member : members) {
            DungeonClassType type = chosen.get(member.getUuid());
            levels.put(member.getUuid(), DungeonClassProgress.level(member, type));
            removeKind(member, ClassItems.SELECTOR);
            giveOrb(member);
            applyStats(member);
            member.sendMessage("<7>Your active Dungeon Class is <e>" + type.displayName() + "<7>!");
            if (party.isLone(type) && members.size() > 1) {
                member.sendMessage("<a>Your " + type.displayName() + " stats are doubled because you are the only player using this class!");
            }
        }
    }

    /** A member who rejoins mid-run gets their orb and class stats back. */
    public void rejoin(SkyBlockPlayer player) {
        if (!party.members().containsKey(player.getUuid())) return;
        giveOrb(player);
        applyStats(player);
    }

    private void giveOrb(SkyBlockPlayer player) {
        removeKind(player, ClassItems.ORB);
        ItemStack orb = ClassItems.orb(classOf(player.getUuid()), level(player.getUuid()));
        if (!player.getInventory().addItemStack(orb)) {
            player.sendMessage("<c>Your inventory is full, so your Dungeon Orb couldn't be given. Your class abilities still work with your drop keys.");
        }
    }

    private void applyStats(SkyBlockPlayer player) {
        UUID uuid = player.getUuid();
        DungeonClassType type = classOf(uuid);
        player.getStatistics().boostStatistic(TemporaryConditionalStatistic.builder()
                .withStatistics(_ -> statistics(type, level(uuid), strength(uuid)))
                .withExpiry(p -> host.classesActive() && party.classOf(p.getUuid()) == type
                        && p.getInstance() == host.instance())
                .build());
    }

    static ItemStatistics statistics(DungeonClassType type, int level, int strength) {
        ItemStatistics.Builder builder = ItemStatistics.builder();
        switch (type) {
            case HEALER -> builder.withBase(ItemStatistic.VITALITY, DungeonClassScaling.healerVitality(level, strength))
                    .withBase(ItemStatistic.MENDING, DungeonClassScaling.healerMending(level, strength));
            case MAGE -> builder.withBase(ItemStatistic.INTELLIGENCE, DungeonClassScaling.mageIntelligence(level, strength))
                    .withBase(ItemStatistic.ABILITY_DAMAGE, DungeonClassScaling.mageAbilityDamage(level, strength));
            case BERSERK -> builder.withBase(ItemStatistic.SPEED, DungeonClassScaling.berserkSpeed(level, strength));
            case ARCHER -> {
            }
            case TANK -> builder.withBase(ItemStatistic.HEALTH, DungeonClassScaling.tankHealth(strength))
                    .withBase(ItemStatistic.DEFENSE, DungeonClassScaling.tankDefense(level, strength))
                    .withBase(ItemStatistic.VITALITY, DungeonClassScaling.tankVitality(level, strength))
                    .withMultiplicativePercentage(ItemStatistic.DEFENSE, DungeonClassScaling.tankDefensePercent(level, strength));
        }
        return builder.build();
    }

    public void onGhost(SkyBlockPlayer player) {
        castleUntil.remove(player.getUuid());
        ragnarokUntil.remove(player.getUuid());
        if (party.members().containsKey(player.getUuid())) toolbar.show(player, classOf(player.getUuid()));
    }

    public void onRevive(SkyBlockPlayer player) {
        toolbar.hide(player);
    }

    public void onDisconnect(SkyBlockPlayer player) {
        toolbar.hide(player);
    }

    public void conclude(List<SkyBlockPlayer> members) {
        abilities.stop();
        for (SkyBlockPlayer member : members) {
            toolbar.hide(member);
            removeKind(member, ClassItems.ORB);
            removeKind(member, ClassItems.SELECTOR);
        }
    }

    public void removeWaitingItems(SkyBlockPlayer player) {
        removeKind(player, ClassItems.SELECTOR);
    }

    private static void removeKind(SkyBlockPlayer player, String kind) {
        for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
            if (kind.equals(ClassItems.kind(player.getInventory().getItemStack(slot)))) {
                player.getInventory().setItemStack(slot, ItemStack.AIR);
            }
        }
    }

    public void tickSecond() {
        try {
            abilities.tickSecond();
        } catch (Exception exception) {
            Logger.error(exception, "Dungeon class tick failed");
        }
    }

    public Map<DungeonClassType, Long> reward(SkyBlockPlayer player, CatacombsFloor floor) {
        UUID uuid = player.getUuid();
        if (!party.members().containsKey(uuid)) return Map.of();
        return DungeonClassProgress.completeRun(player, classOf(uuid), party.teammatesOf(uuid), floor);
    }

    public boolean inParty(UUID player) {
        return party.members().containsKey(player);
    }

    public DungeonClassType classOf(UUID player) {
        DungeonClassType type = party.classOf(player);
        return type == null ? selectedOf(player) : type;
    }

    public int level(UUID player) {
        return levels.getOrDefault(player, 0);
    }

    public int strength(UUID player) {
        return party.strength(player);
    }

    public DungeonClassParty party() {
        return party;
    }

    public ClassHost host() {
        return host;
    }

    public ClassAbilities abilities() {
        return abilities;
    }

    public AbilityCooldowns cooldowns() {
        return cooldowns;
    }

    public HitHistory meleeHits() {
        return meleeHits;
    }

    public HitHistory bowHits() {
        return bowHits;
    }

    public void castle(UUID player, long until) {
        castleUntil.put(player, until);
    }

    public boolean inCastle(UUID player, long now) {
        return castleUntil.getOrDefault(player, 0L) > now;
    }

    public void ragnarok(UUID player, long until) {
        ragnarokUntil.put(player, until);
    }

    public boolean inRagnarok(UUID player, long now) {
        return ragnarokUntil.getOrDefault(player, 0L) > now;
    }
}
