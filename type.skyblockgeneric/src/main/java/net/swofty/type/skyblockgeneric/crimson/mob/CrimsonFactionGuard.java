package net.swofty.type.skyblockgeneric.crimson.mob;

import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonFaction;
import net.swofty.type.skyblockgeneric.crimson.faction.CrimsonReputationService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Town guards (wiki: Mage_Guard, Barbarian_Guard). They patrol their town, answer every hit with a line, and turn
 * on a player after 3 hits. Since reputation can no longer go below zero, the wiki's "-1,000 reputation" rule is
 * replaced by hostility to members of the rival faction (estimate).
 */
public abstract class CrimsonFactionGuard extends CrimsonIsleMob {
    static final int HITS_BEFORE_HOSTILE = 3;

    private final Map<UUID, Integer> hits = new ConcurrentHashMap<>();

    protected CrimsonFactionGuard(CrimsonMobProfile profile) {
        super(profile);
    }

    protected abstract CrimsonFaction faction();

    protected abstract List<String> hitLines();

    protected abstract List<String> hostileLines();

    @Override
    protected int strollRadius() {
        return 10;
    }

    @Override
    protected float aggroRange() {
        return 24;
    }

    @Override
    public boolean countsTowardVanquisher() {
        return false;
    }

    @Override
    protected boolean canTarget(SkyBlockPlayer player) {
        return hits != null && (hits.getOrDefault(player.getUuid(), 0) >= HITS_BEFORE_HOSTILE
                || CrimsonReputationService.isMember(player, faction().opposite()));
    }

    @Override
    public List<TargetSelector> getTargetSelectors() {
        return List.of(new ClosestEntityTarget(this, aggroRange(),
                entity -> entity instanceof SkyBlockPlayer player && canTarget(player)));
    }

    @Override
    public void onSpawn() {
        super.onSpawn();
        setItemInMainHand(ItemStack.of(faction() == CrimsonFaction.MAGE ? Material.BLAZE_ROD : Material.GOLDEN_AXE));
    }

    @Override
    protected void onDamagedBy(SkyBlockPlayer player, Damage damage) {
        int count = hits.merge(player.getUuid(), 1, Integer::sum);
        List<String> lines = count == HITS_BEFORE_HOSTILE ? hostileLines() : hitLines();
        String line = lines.get(ThreadLocalRandom.current().nextInt(lines.size()));
        player.sendMessage("<e>[NPC] " + faction().getColour() + faction().getDisplayName() + " Guard<f>: " + line);
    }
}
