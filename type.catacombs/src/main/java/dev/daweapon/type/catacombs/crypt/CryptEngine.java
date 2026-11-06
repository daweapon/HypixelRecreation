package dev.daweapon.type.catacombs.crypt;

import dev.daweapon.commons.skyblock.item.ItemType;
import dev.daweapon.type.catacombs.engine.DungeonHost;
import dev.daweapon.type.catacombs.secret.SecretMessages;
import dev.daweapon.type.skyblockgeneric.loottable.LootRoll;
import dev.daweapon.type.skyblockgeneric.user.SkyBlockPlayer;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Crypts: cracked-brick tombs that Superboom TNT blows open, releasing a Crypt Undead. Each Crypt Undead a player kills
 * counts one crypt toward the bonus score (the score caps the bonus at 5) and may drop a Revive Stone.
 */
public final class CryptEngine {
    public static final double REVIVE_STONE_PERCENT = 22;
    private static final long HINT_COOLDOWN_MILLIS = 4_000;
    private static final Block TOMB = Block.CRACKED_STONE_BRICKS;

    private final DungeonHost host;
    private final List<CryptPlan.Crypt> crypts;
    private final Set<Integer> opened = ConcurrentHashMap.newKeySet();
    private final Set<CryptUndead> undead = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastHint = new ConcurrentHashMap<>();

    public CryptEngine(DungeonHost host, List<CryptPlan.Crypt> crypts) {
        this.host = host;
        this.crypts = crypts;
    }

    public void start() {
        for (CryptPlan.Crypt crypt : crypts) {
            host.instance().loadChunk(crypt.x(), crypt.z()).thenRun(() -> {
                for (int dx = 0; dx < CryptPlan.TOMB_LENGTH; dx++) host.instance().setBlock(crypt.x() + dx, crypt.y(), crypt.z(), TOMB);
            });
        }
    }

    /** A right-click on a sealed crypt without a Superboom: the hint. True when the block was a sealed crypt. */
    public boolean interact(SkyBlockPlayer player, int x, int y, int z) {
        CryptPlan.Crypt crypt = sealedAt(x, y, z);
        if (crypt == null) return false;
        hint(player);
        return true;
    }

    /** A Superboom used on a sealed crypt opens it; false when the block is no sealed crypt. */
    public boolean blast(int x, int y, int z) {
        CryptPlan.Crypt crypt = sealedAt(x, y, z);
        if (crypt == null) return false;
        blowUp(crypt);
        return true;
    }

    private CryptPlan.Crypt sealedAt(int x, int y, int z) {
        return crypts.stream().filter(each -> each.covers(x, y, z) && !opened.contains(each.id())).findFirst().orElse(null);
    }

    private void hint(SkyBlockPlayer player) {
        long now = System.currentTimeMillis();
        if (now - lastHint.getOrDefault(player.getUuid(), 0L) < HINT_COOLDOWN_MILLIS) return;
        lastHint.put(player.getUuid(), now);
        player.sendMessage("<7>This crypt is sealed. Blow it open with <9>Superboom TNT<7>.");
    }

    /** Blows the crypt nearest to {@code at} that is still sealed; false when none is left. */
    public boolean blowNearest(Pos at) {
        return crypts.stream()
                .filter(crypt -> !opened.contains(crypt.id()))
                .min(Comparator.comparingDouble(crypt -> at.distanceSquared(new Pos(crypt.x(), crypt.y(), crypt.z()))))
                .map(crypt -> {
                    blowUp(crypt);
                    return true;
                }).orElse(false);
    }

    private void blowUp(CryptPlan.Crypt crypt) {
        if (!opened.add(crypt.id())) return;
        for (int dx = 0; dx < CryptPlan.TOMB_LENGTH; dx++) host.instance().setBlock(crypt.x() + dx, crypt.y(), crypt.z(), Block.AIR);
        Pos centre = new Pos(crypt.x() + 1, crypt.y(), crypt.z() + 0.5);
        host.instance().playSound(Sound.sound(Key.key("entity.generic.explode"), Sound.Source.BLOCK, 1f, 1f),
                centre.x(), centre.y(), centre.z());
        host.instance().sendGroupedPacket(new ParticlePacket(Particle.EXPLOSION, centre.x(), centre.y() + 0.5, centre.z(),
                0.5f, 0.5f, 0.5f, 0f, 3));
        CryptUndead mob = new CryptUndead(this::killed);
        undead.add(mob);
        mob.setInstance(host.instance(), centre);
    }

    private void killed(CryptUndead mob, SkyBlockPlayer killer) {
        undead.remove(mob);
        host.recordCrypt();
        if (LootRoll.roll(killer.getUuid(), ItemType.REVIVE_STONE, REVIVE_STONE_PERCENT)) {
            killer.addAndUpdateItem(ItemType.REVIVE_STONE);
            host.broadcast(SecretMessages.obtained(killer, ItemType.REVIVE_STONE));
        }
    }

    public int total() {
        return crypts.size();
    }

    public int remaining() {
        return crypts.size() - opened.size();
    }

    public List<CryptPlan.Crypt> crypts() {
        return crypts;
    }

    public void dispose() {
        undead.forEach(CryptUndead::remove);
        undead.clear();
    }
}
