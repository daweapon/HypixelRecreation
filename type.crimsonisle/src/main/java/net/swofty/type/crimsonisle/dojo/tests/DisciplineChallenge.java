package net.swofty.type.crimsonisle.dojo.tests;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.crimsonisle.dojo.DojoArena;
import net.swofty.type.crimsonisle.dojo.DojoChallenge;
import net.swofty.type.crimsonisle.dojo.DojoMobs;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.crimson.dojo.DojoTest;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Test of Discipline: zombies wear leather, iron, gold or diamond helmets and must be hit with the
 * matching wooden, iron, gold or diamond sword (+8); the wrong sword costs 16. A new sword arrives
 * every 30 seconds. A zombie left alone vanishes after 5 to 7 seconds.
 */
public class DisciplineChallenge extends DojoChallenge {
    private static final double PLATFORM_RADIUS = 10.5;
    private static final int MAX_ZOMBIES = 6;
    private static final int SPAWN_INTERVAL = 10;
    private static final int SWORD_INTERVAL = 30 * TICKS_PER_SECOND;
    private static final int HIT_COOLDOWN = 10;

    private final Map<EntityCreature, Tier> zombies = new HashMap<>();
    private final Map<EntityCreature, Long> cooldownUntil = new HashMap<>();
    private final Map<EntityCreature, Long> despawnAt = new HashMap<>();
    private int unlockedTiers = 1;

    public DisciplineChallenge(SkyBlockPlayer player) {
        super(player, DojoTest.DISCIPLINE);
    }

    @Override
    protected void build() {
        arena.disc(PLATFORM_RADIUS, Block.SMOOTH_STONE);
    }

    @Override
    protected Pos spawn() {
        return DojoArena.CENTER;
    }

    @Override
    protected List<ItemStack> kit() {
        return List.of(Tier.WOOD.sword());
    }

    @Override
    protected void tick() {
        if (ticks % SWORD_INTERVAL == 0 && ticks > 0 && unlockedTiers < Tier.values().length) {
            Tier tier = Tier.values()[unlockedTiers++];
            player.getInventory().setItemStack(tier.ordinal(), tier.sword());
            player.sendMessage("<e>You received a " + tier.swordName + "<e>!");
            sound("entity.item.pickup", 1f);
        }
        if (zombies.size() < MAX_ZOMBIES && ticks % SPAWN_INTERVAL == 0) spawnZombie();
        zombies.keySet().removeIf(zombie -> {
            if (zombie.isRemoved()) return forget(zombie);
            if (DojoArena.isInLava(zombie.getPosition()) || ticks >= despawnAt.getOrDefault(zombie, Long.MAX_VALUE)) {
                zombie.remove();
                return forget(zombie);
            }
            if (zombie.isOnGround()) DojoMobs.walkToward(zombie, player.getPosition(), 1.6, 1.5);
            return false;
        });
    }

    @Override
    public void onAttack(Entity target) {
        if (!(target instanceof EntityCreature zombie) || !zombies.containsKey(zombie)) return;
        if (cooldownUntil.getOrDefault(zombie, 0L) > ticks) return;
        cooldownUntil.put(zombie, ticks + HIT_COOLDOWN);
        Tier needed = zombies.get(zombie);
        if (player.getItemInMainHand().material() == needed.sword) {
            addScore(8);
            zombies.remove(zombie);
            forget(zombie);
            zombie.remove();
            sound("entity.experience_orb.pickup", 1.2f);
        } else {
            addScore(-16);
            DojoMobs.knockAway(zombie, player.getPosition(), 5, 3);
            sound("entity.villager.no", 1f);
        }
    }

    private void spawnZombie() {
        Tier tier = Tier.values()[ThreadLocalRandom.current().nextInt(unlockedTiers)];
        EntityCreature zombie = DojoMobs.zombie(tier.helmet, false, null);
        zombie.setInstance(DojoArena.instance(), DojoMobs.randomOnDisc(3, PLATFORM_RADIUS - 1.5));
        zombies.put(track(zombie), tier);
        despawnAt.put(zombie, ticks + mobLifetimeTicks());
    }

    private boolean forget(EntityCreature zombie) {
        cooldownUntil.remove(zombie);
        despawnAt.remove(zombie);
        return true;
    }

    private enum Tier {
        WOOD(Material.LEATHER_HELMET, Material.WOODEN_SWORD, "<f>Wooden Sword"),
        IRON(Material.IRON_HELMET, Material.IRON_SWORD, "<f>Iron Sword"),
        GOLD(Material.GOLDEN_HELMET, Material.GOLDEN_SWORD, "<f>Golden Sword"),
        DIAMOND(Material.DIAMOND_HELMET, Material.DIAMOND_SWORD, "<f>Diamond Sword");

        private final Material helmet;
        private final Material sword;
        private final String swordName;

        Tier(Material helmet, Material sword, String swordName) {
            this.helmet = helmet;
            this.sword = sword;
            this.swordName = swordName;
        }

        private ItemStack sword() {
            return ItemStacks.item(sword, swordName + "\n\n<7>Hit the matching zombies!").build();
        }
    }
}
