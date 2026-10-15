package dev.daweapon.type.skyblockgeneric.item.armor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import dev.daweapon.type.skyblockgeneric.item.SkyBlockItem;
import dev.daweapon.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import dev.daweapon.type.generic.entity.drop.ItemDrops;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class SneakAbilities {
    public static final long GOLEM_HAT_COOLDOWN_MILLIS = 5_000;
    public static final long SPLASH_COOLDOWN_MILLIS = 1_000;
    public static final int FISH_HAT_MANA = 10;
    public static final int MAGMA_FISH_HAT_MANA = 20;
    public static final int WATER_BURST_MANA = 20;
    public static final double WATER_BURST_VELOCITY = 20;
    public static final Set<String> SALMON_NEW = Set.of("SALMON_HELMET_NEW", "SALMON_CHESTPLATE_NEW",
            "SALMON_LEGGINGS_NEW", "SALMON_BOOTS_NEW");
    private static final Map<String, Long> LAST_USE = new ConcurrentHashMap<>();

    private SneakAbilities() {
    }

    public static boolean offCooldown(long lastUse, long now, long cooldownMillis) {
        return now - lastUse >= cooldownMillis;
    }

    public static void onSneak(SkyBlockPlayer player) {
        List<String> worn = WornArmor.ids(player);
        String helmet = worn.getFirst();
        switch (helmet) {
            case "GOLEM_HAT" -> golemHat(player);
            case "FISH_HAT" -> splash(player, "FISH_HAT", FISH_HAT_MANA, Particle.SPLASH);
            case "MAGMA_FISH_HAT" -> splash(player, "MAGMA_FISH_HAT", MAGMA_FISH_HAT_MANA, Particle.LAVA);
            default -> {
            }
        }
        if (WornArmor.wearsAll(worn, SALMON_NEW)) waterBurst(player);
    }

    private static boolean ready(SkyBlockPlayer player, String ability, long cooldownMillis) {
        long now = System.currentTimeMillis();
        String key = player.getUuid() + ability;
        if (!offCooldown(LAST_USE.getOrDefault(key, 0L), now, cooldownMillis)) return false;
        LAST_USE.put(key, now);
        return true;
    }

    private static boolean spendMana(SkyBlockPlayer player, int cost) {
        RegisteredAbility.AbilityManaCost manaCost = new RegisteredAbility.AbilityManaCost(cost);
        if (!manaCost.canUse(player)) {
            manaCost.onFail(player);
            return false;
        }
        player.setMana(player.getMana() - RegisteredAbility.effectiveManaCost(player, cost));
        return true;
    }

    private static void golemHat(SkyBlockPlayer player) {
        if (!ready(player, "GOLEM_HAT", GOLEM_HAT_COOLDOWN_MILLIS)) return;
        ItemDrops.throwFromPlayer(player, new DroppedItemEntityImpl(new SkyBlockItem(ItemType.IRON_INGOT), player));
    }

    private static void splash(SkyBlockPlayer player, String ability, int manaCost, Particle particle) {
        if (player.getInstance() == null || !ready(player, ability, SPLASH_COOLDOWN_MILLIS)) return;
        if (!spendMana(player, manaCost)) return;
        Pos pos = player.getPosition().add(0, 1, 0);
        ParticlePacket packet = new ParticlePacket(particle, true, true, pos.x(), pos.y(), pos.z(),
                1.5f, 0.5f, 1.5f, 0.1f, 60);
        player.getInstance().getPlayers().forEach(viewer -> viewer.sendPacket(packet));
    }

    private static void waterBurst(SkyBlockPlayer player) {
        if (player.getInstance() == null) return;
        if (!player.getInstance().getBlock(player.getPosition()).compare(Block.WATER)) return;
        if (!spendMana(player, WATER_BURST_MANA)) return;
        player.setVelocity(player.getPosition().direction().mul(WATER_BURST_VELOCITY));
    }
}
